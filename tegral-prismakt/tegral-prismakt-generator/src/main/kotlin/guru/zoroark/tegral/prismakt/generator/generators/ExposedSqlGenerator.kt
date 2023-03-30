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

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import guru.zoroark.tegral.di.extensions.fundef.configureFundef
import guru.zoroark.tegral.prismakt.generator.protocol.Field
import guru.zoroark.tegral.prismakt.generator.protocol.Model
import guru.zoroark.tegral.prismakt.generator.protocol.TypeRef
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.LoggerFactory
import java.lang.reflect.Member
import java.math.BigDecimal
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger("tegral.prismakt.exposed-sql")


class ExposedSqlGenerator {

    private fun columnTypeOf(field: Field): Pair<TypeName, CodeBlock> {
        val initializer = CodeBlock.builder()
        var typeName: ParameterizedTypeName = when (parseScalarType(field.type)) {
            ScalarTypes.Int -> {
                initializer.add("integer(name = %S)", field.name)
                Column::class.parameterizedBy(Int::class)
            }

            ScalarTypes.String -> {
                initializer.add("text(name = %S)", field.name)
                Column::class.parameterizedBy(String::class)
            }

            ScalarTypes.Boolean -> {
                initializer.add("bool(name = %S)", field.name)
                Column::class.parameterizedBy(Boolean::class)
            }

            ScalarTypes.BigInt -> {
                initializer.add("long(name = %S)", field.name)
                Column::class.parameterizedBy(Long::class)
            }

            ScalarTypes.Bytes -> {
                initializer.add("binary(name = %S)", field.name)
                Column::class.parameterizedBy(ByteArray::class)
            }

            ScalarTypes.Float -> {
                initializer.add("float(name = %S)", field.name)
                Column::class.parameterizedBy(Float::class)
            }

            ScalarTypes.Decimal -> {
                initializer.add("decimal(name = %S, precision = %L, scale = %L)", field.name, 65, 30) // TODO this is 32, 16 for mssql
                Column::class.parameterizedBy(BigDecimal::class)
            }

            ScalarTypes.DateTime -> {
                val timestampFun = MemberName("org.jetbrains.exposed.sql.javatime", "timestamp", true)
                initializer.add("%M(name = %S)", timestampFun, field.name)
                Column::class.parameterizedBy(Instant::class)
            }

            else -> error("Unrecognized field type ${field.type}, stopping")
        }
        if (!field.isRequired) {
            initializer.add(".nullable()")
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
                initializer(prevInitializer.add(".entityId()").build())
            }
            .build()

        propertySpecs.remove(pregeneratedColumn)
        propertySpecs.add(newColumn)

        // If the existing column did not already have the name "id", create a new column that just aliases the
        // existing one.
        if (pregeneratedColumn.name != "id") {
            tableSpec.addProperty(PropertySpec.Companion.builder("id", columnEntityIdType, KModifier.OVERRIDE)
                .getter(FunSpec.getterBuilder().addStatement("return %N", pregeneratedColumn).build())
                .build())
        }
    }

    fun generateTypeSpec(model: Model): TypeSpec {
        return TypeSpec.objectBuilder("${model.name}Table").apply {
            for (field in model.fields) {
                addProperty(createProperty(field))
            }

            val idField = model.fields.find { it.isId }
            if (idField != null) {
                processId(this, idField)
            }
        }.build()
    }

    fun generateModels(outputDir: Path, models: List<Model>) {
        for (model in models) {
            val fileName = "${model.name}Table"
            logger.trace("Generating $fileName")
            val fileContent = FileSpec.builder("prismakt.generated", fileName).apply {
                addType(generateTypeSpec(model))
            }.build()
            fileContent.writeTo(outputDir)
            logger.debug("Generated $fileName")
            logger.trace(fileContent.toString())
        }
    }
}