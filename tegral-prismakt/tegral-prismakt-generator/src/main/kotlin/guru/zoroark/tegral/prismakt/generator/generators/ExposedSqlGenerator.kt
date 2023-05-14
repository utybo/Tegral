/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guru.zoroark.tegral.prismakt.generator.generators

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import guru.zoroark.tegral.prismakt.generator.parser.PModel
import guru.zoroark.tegral.prismakt.generator.protocol.Field
import guru.zoroark.tegral.prismakt.generator.protocol.Model
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlin.reflect.KClass

private val logger = LoggerFactory.getLogger("tegral.prismakt.exposed-sql")

@Suppress("CyclomaticComplexMethod")
private fun initializeColumnWith(
    initializer: CodeBlock.Builder,
    fieldName: String,
    type: ScalarType
): ParameterizedTypeName {
    fun builtInColumn(funName: String, columnTypeParam: KClass<*>, size: Int? = null): ParameterizedTypeName {
        if (size == null) {
            initializer.add("$funName(name = %S)", fieldName)
        } else {
            initializer.add("$funName(name = %S, length = %L)", fieldName, size)
        }
        return Column::class.parameterizedBy(columnTypeParam)
    }

    fun javaDateColumn(funName: String, columnTypeParam: KClass<*>): ParameterizedTypeName {
        val memberName = MemberName("org.jetbrains.exposed.sql.javatime", funName, true)
        initializer.add("%M(name = %S)", memberName, fieldName)
        return Column::class.parameterizedBy(columnTypeParam)
    }

    return when (type) {
        is ScalarType.TBinary -> builtInColumn("binary", ByteArray::class, type.maxSize)
        ScalarType.TBoolean -> builtInColumn("bool", Boolean::class)
        ScalarType.TByte -> builtInColumn("byte", Byte::class)
        is ScalarType.TChar -> builtInColumn("char", String::class, type.n)

        is ScalarType.TDecimal -> type.getPrecisionAndScale(false /* TODO */).let { (precision, scale) ->
            initializer.add("decimal(name = %S, precision = %L, scale = %L)", fieldName, precision, scale)
            Column::class.parameterizedBy(BigDecimal::class)
        }

        ScalarType.TDouble -> builtInColumn("double", Double::class)
        ScalarType.TFloat -> builtInColumn("float", Float::class)
        ScalarType.TInstant -> javaDateColumn("timestamp", Instant::class)
        ScalarType.TInt -> builtInColumn("integer", Int::class)
        ScalarType.TLocalDate -> javaDateColumn("date", LocalDate::class)
        ScalarType.TLocalDateTime -> javaDateColumn("datetime", LocalDateTime::class)
        ScalarType.TLocalTime -> javaDateColumn("time", LocalTime::class)
        ScalarType.TLong -> builtInColumn("long", Long::class)
        ScalarType.TLongText -> builtInColumn("largeText", String::class)
        ScalarType.TMediumText -> builtInColumn("mediumText", String::class)
        ScalarType.TShort -> builtInColumn("short", Short::class)
        ScalarType.TString -> builtInColumn("text", String::class)
        ScalarType.TText -> builtInColumn("text", String::class)
        ScalarType.TUByte -> builtInColumn("ubyte", UByte::class)
        ScalarType.TUInt -> builtInColumn("uinteger", UInt::class)
        ScalarType.TULong -> builtInColumn("ulong", ULong::class)
        ScalarType.TUShort -> builtInColumn("ushort", UShort::class)
        ScalarType.TUuid -> builtInColumn("uuid", UUID::class)
        is ScalarType.TVarChar -> builtInColumn("varchar", String::class, type.n)
    }
}

/**
 * Generator for JetBrains Exposed's SQL API.
 */
class ExposedSqlGenerator : ModelGenerator {

    private fun handleNullability(
        field: Field,
        originalType: ParameterizedTypeName,
        initializer: CodeBlock.Builder
    ): ParameterizedTypeName {
        if (!field.isRequired) {
            initializer.add(".nullable()")
            // Make all type parameters nullable
            return originalType.copy(typeArguments = originalType.typeArguments.map { it.copy(nullable = true) })
        }
        return originalType
    }

    private fun handleDefaultValue(
        field: Field,
        initializer: CodeBlock.Builder
    ) {
        if (field.hasDefaultValue) {
            if (field.default is Map<*, *>) {
                when (val funName = field.default["name"]) {
                    "autoincrement" -> initializer.add(".autoIncrement()")
                    "uuid" -> initializer.add(".autoGenerate()")
                    "now" -> initializer.add(".defaultExpression(%T())", CurrentTimestamp::class.asClassName())
                    else -> logger.warn("Unrecognized default value function: $funName. Will be ignored.")
                }
            } else {
                if (field.default is String) {
                    initializer.add(".default(%S)", field.default)
                } else {
                    initializer.add(".default(%L)", field.default ?: "null")
                }
            }
        }
    }

    private fun columnTypeOf(modelName: String, pmodel: PModel?, field: Field): Pair<TypeName, CodeBlock>? {
        val pfield = pmodel?.fields?.firstOrNull { it.name == field.name }
        if (pfield == null && pmodel != null) {
            logger.warn("Field $modelName.${field.name} was not found in re-parsed model. Please report this.")
        }
        val foundType = nativeTypeToScalarType(field.name, field.type, pfield?.attributes ?: emptyList())
        if (foundType == null) {
            logger.warn(
                "Unrecognized field type ${field.type} for field $modelName.${field.name}, field will be skipped " +
                    "during generation"
            )
            return null
        }
        if (foundType is ScalarTypeWithAccuracy.Inaccurate) {
            logger.warn("Typing for $modelName.${field.name} will be inaccurate: ${foundType.inaccuracyReason}")
        }
        val initializer = CodeBlock.builder()
        var typeName: ParameterizedTypeName = initializeColumnWith(initializer, field.name, foundType.type)

        typeName = handleNullability(field, typeName, initializer)
        handleDefaultValue(field, initializer)

        return typeName to initializer.build()
    }

    private fun createProperty(modelName: String, pmodel: PModel?, field: Field): PropertySpec? {
        val (typeName, initializer) = columnTypeOf(modelName, pmodel, field) ?: return null
        return PropertySpec.builder(field.name, typeName)
            .initializer(initializer)
            .build()
    }

    private fun processId(tableSpec: TypeSpec.Builder, idField: Field): TypeName? {
        val propertySpecs = tableSpec.propertySpecs

        val pregeneratedColumn = propertySpecs.find { it.name == idField.name }
        if (pregeneratedColumn == null) {
            logger.warn("Found a Prisma ID field but no matching generated ID field! Please report this.")
            return null
        }
        val originalTypeParam = (pregeneratedColumn.type as? ParameterizedTypeName)?.typeArguments?.get(0)
        require(originalTypeParam != null) {
            "Failed to retrieve type of ID column from already computed table properties. Please report this"
        }

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

                initializer(prevInitializer.add(".entityId()").build())
            }
            .build()

        propertySpecs.remove(pregeneratedColumn)
        propertySpecs.add(newColumn)

        // If the existing column did not already have the name "id", create a new column that just aliases the
        // existing one.
        if (pregeneratedColumn.name != "id") {
            tableSpec.addProperty(
                PropertySpec.Companion.builder("id", columnEntityIdType, KModifier.OVERRIDE)
                    .getter(FunSpec.getterBuilder().addStatement("return %N", pregeneratedColumn).build())
                    .build()
            )
        }
        return IdTable::class.asClassName().parameterizedBy(listOf(originalTypeParam))
    }

    /**
     * Generate an Exposed SQL Table for the provided [model] and [context].
     */
    fun generateTypeSpec(model: Model, context: GeneratorContext): TypeSpec {
        val pmodel = context.rawParsingResult?.elements?.asSequence()
            ?.filterIsInstance<PModel>()
            ?.firstOrNull { it.name == model.name }
        return TypeSpec.objectBuilder("${model.name}Table").apply {
            for (field in model.fields) {
                createProperty(model.name, pmodel, field)?.let { addProperty(it) }
            }

            val tableType = model.fields.find { it.isId }?.let { processId(this, it) }
            superclass(tableType ?: Table::class.asTypeName())
            // Quoting is added around the model name because of https://github.com/JetBrains/Exposed/issues/683
            addSuperclassConstructorParameter("name = %S", "\"${model.name}\"")
        }.build()
    }

    override fun generateModels(context: GeneratorContext) {
        for (model in context.datamodel.models) {
            val fileName = "${model.name}Table"
            logger.trace("Generating $fileName")
            val fileContent = FileSpec.builder("prismakt.generated", fileName).apply {
                addType(generateTypeSpec(model, context))
            }.build()
            fileContent.writeTo(context.outputDir)
            logger.debug("Generated $fileName")
            logger.trace(fileContent.toString())
        }
    }
}
