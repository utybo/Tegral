package guru.zoroark.tegral.prismakt.generator

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.prismakt.generator.UserTable.entityId
import guru.zoroark.tegral.utils.logtools.applyMinimalistLoggingOverrides
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("tegral.prismakt")
private const val DEFAULT_OUTPUT_DIR = "generatedSrc"

interface GeneratorProtocolHandler {
    fun getManifest(request: GeneratorRequest.GetManifestRequest): GeneratorResponse.GetManifestResponse

    fun handle(request: GeneratorRequest<*>): GeneratorResponse<*>? {
        return when (request) {
            is GeneratorRequest.GetManifestRequest -> getManifest(request)
            is GeneratorRequest.GenerateRequest -> {
                generate(request)
                null
            }
        }
    }

    fun generate(request: GeneratorRequest.GenerateRequest)
}

class ModelGenerator {
    private lateinit var directory: Path

    fun useDirectory(dirPath: String) {
        val path = Path.of(dirPath);
        Files.createDirectories(path);
        directory = path;
    }

    private fun columnTypeOf(field: Field): Pair<TypeName, CodeBlock> {
        val initializer = CodeBlock.builder()
        var typeName: ParameterizedTypeName = when (field.type) {
            "Int" -> {
                initializer.add("%M(name = %S)", Table::class.member("integer"), field.name)
                Column::class.parameterizedBy(Int::class)
            }

            "String" -> {
                initializer.add("%M(name = %S)", Table::class.member("text"), field.name)
                Column::class.parameterizedBy(String::class)
            }

            else -> error("Unrecognized field type ${field.type}, stopping")
        }
        if (!field.isRequired) {
            initializer.add(".%M()", Column::class.member("nullable"))
            // Make all type parameters nullable
            typeName = typeName.copy(typeArguments = typeName.typeArguments.map { it.copy(nullable = true) })
        }
        return typeName to initializer.build()
    }

    private fun createProperty(field: Field): PropertySpec {
        val (typeName, initializer) = columnTypeOf(field)
        return PropertySpec.builder(field.name, typeName)
            .initializer(initializer)
            .build()
    }

    private fun processId(tableSpec: TypeSpec.Builder, idField: Field) {
        val propertySpecs = tableSpec.propertySpecs

        val pregeneratedColumn = propertySpecs.find { it.name == idField.name }
        if (pregeneratedColumn == null) {
            logger.warn("Found a Prisma ID field but no matching generated ID field! Please report this.")
            return
        }
        val originalTypeParam = (pregeneratedColumn.type as? ParameterizedTypeName)?.typeArguments?.get(0)
        require(originalTypeParam != null) {
            "Failed to retrieve type of ID column from already computed table properties. Please report this"
        }
        tableSpec.superclass(IdTable::class.asClassName().parameterizedBy(listOf(originalTypeParam)))

        val columnEntityIdType =
            Column::class.asTypeName().parameterizedBy(
                EntityID::class.asTypeName().parameterizedBy(
                    originalTypeParam
                )
            )

        // Replace the existing column with one that has an appropriate type
        val newColumn = pregeneratedColumn
            .toBuilder(type = columnEntityIdType)
            .apply {
                if (pregeneratedColumn.name == "id") addModifiers(KModifier.OVERRIDE)

                val prevInitializer = pregeneratedColumn.initializer?.toBuilder()
                    ?: error("Pregenerated column ($pregeneratedColumn) did not contain an initializer!")
                initializer(prevInitializer.add(".%M()", Table::class.member("entityId")).build())
            }
            .build()

        propertySpecs.remove(pregeneratedColumn)
        propertySpecs.add(newColumn)

        // If the existing column did not already have the name "id", create a new column that just aliases the
        // existing one.
        if (pregeneratedColumn.name != "id") {
            tableSpec.addProperty(PropertySpec.builder("id", columnEntityIdType, KModifier.OVERRIDE)
                .getter(FunSpec.getterBuilder().addStatement("return %N", pregeneratedColumn).build())
                .build())
        }
    }

    fun generateModel(model: Model) {
        val file = FileSpec.builder("tegralkt.generated", "${model.name}Table").apply {
            addType(
                TypeSpec.objectBuilder("${model.name}Table").apply {
                    for (field in model.fields) {
                        addProperty(createProperty(field))
                    }

                    val idField = model.fields.find { it.isId }
                    if (idField != null) {
                        processId(this, idField)
                    }
                }.build()
            )
        }.build()
        logger.info(file.toString())
    }
}

class GeneratorProtocolController(scope: InjectionScope) : GeneratorProtocolHandler {
    private val modelGenerator: ModelGenerator by scope()

    override fun getManifest(request: GeneratorRequest.GetManifestRequest): GeneratorResponse.GetManifestResponse {
        return GeneratorResponse.GetManifestResponse(
            id = request.id,
            jsonrpc = "2.0",
            result = GeneratorResponse.GetManifestResponse.GeneratorManifestContainer(
                GeneratorManifest(
                    prettyName = "Tegral PrismaKt Generator",
                    defaultOutput = DEFAULT_OUTPUT_DIR
                )
            )
        )
    }

    override fun generate(request: GeneratorRequest.GenerateRequest) {
        logger.info(
            "Will generate bindings for the following table(s): " +
                request.params.dmmf.datamodel.models.joinToString { it.name })
        logger.debug(request.params.dmmf.datamodel.toString())
        modelGenerator.useDirectory(request.params.generator.output?.value ?: DEFAULT_OUTPUT_DIR)
        for (model in request.params.dmmf.datamodel.models) {
            modelGenerator.generateModel(model)
        }
    }
}

class JsonRpcProtocol(scope: InjectionScope) {
    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger("tegral.prismakt.rpc")

    private val handler: GeneratorProtocolHandler by scope()

    private fun blockUntilRequest(): GeneratorRequest<*>? {
        logger.trace("Waiting for request")
        val input = readlnOrNull() ?: return null.also { logger.debug("Input EOF reached") }
        logger.trace("Received input: $input")
        val req = runCatching { objectMapper.readValue<GeneratorRequest<*>>(input) }
            .onFailure { logger.error("Failed to parse request", it) }
            .getOrThrow()
        logger.trace("Parsed request: $req")
        return req
    }

    fun exchange() {
        repeat(2) {
            val request = blockUntilRequest() ?: return@repeat
            logger.trace("Calling handler with request ${request.id}")
            val response = handler.handle(request)
            if (response != null) {
                logger.trace("Will send response object: $response")
                val responseStr = objectMapper.writeValueAsString(response)
                logger.trace("Sending response JSON: $responseStr")
                System.err.println(responseStr)
            }
        }
    }
}

fun main() {
    applyMinimalistLoggingOverrides(Level.DEBUG)
    try {
        logger.warn("Tegral PrismaKt Generator is EXPERIMENTAL! Use at your own risk!")
        val env = tegralDi {
            put(::JsonRpcProtocol)
            put<GeneratorProtocolHandler>(::GeneratorProtocolController)
            put(::ModelGenerator)
        }
        val protocol = env.get<JsonRpcProtocol>()
        protocol.exchange()
    } catch (ex: Exception) {
        logger.error("Fatal error", ex)
        exitProcess(1);
    }
}

object UserTable : UUIDTable("users") {
    val name = text("name").nullable()

}