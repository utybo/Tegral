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
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.prismakt.generator.protocol.Model
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.slf4j.LoggerFactory
import java.nio.file.Path

private val logger = LoggerFactory.getLogger("tegral.prismakt.exposed-dao")

class ExposedDaoGenerator(scope: InjectionScope) : ModelGenerator {
    private val exposedSqlGenerator: ExposedSqlGenerator by scope()

    private fun unwrapTableIdType(tableSpec: TypeSpec): TypeName? {
        val superclass = tableSpec.superclass as? ParameterizedTypeName ?: return null
        return superclass
            .takeIf { it.rawType == IdTable::class.asClassName() }
            ?.typeArguments
            ?.singleOrNull()
    }

    private fun unwrapColumnType(type: TypeName): TypeName {
        if (type !is ParameterizedTypeName || type.rawType != Column::class.asClassName() || type.typeArguments.size != 1) {
            error("Table contains a type that is not Column<T>: $type")
        }
        return type.typeArguments.single()
    }

    private fun generateDaoClass(model: Model, tableSpec: TypeSpec, idType: TypeName): TypeSpec {
        val entityClassName = "${model.name}Entity"
        val entityIdType = EntityID::class.asClassName().parameterizedBy(idType)
        return TypeSpec.classBuilder(entityClassName).apply {
            primaryConstructor(FunSpec.constructorBuilder().apply {
                addParameter(ParameterSpec.builder("id", entityIdType).build())
            }.build())

            superclass(Entity::class.asClassName().parameterizedBy(listOf(idType)))
            addSuperclassConstructorParameter("id")

            val nonIdFields = model.fields.filterNot { it.isId }
            for (field in nonIdFields) {
                val tableProperty = tableSpec.propertySpecs.find { it.name == field.name }
                    ?: error("Expected property was not generated?")
                val unwrappedColumnType = unwrapColumnType(tableProperty.type)
                addProperty(
                    PropertySpec.builder(field.name, unwrappedColumnType)
                        .mutable()
                        .delegate("${tableSpec.name}.${tableProperty.name}")
                        .build()
                )
            }

            addType(TypeSpec.companionObjectBuilder().apply {
                superclass(EntityClass::class.asClassName()
                    .parameterizedBy(listOf(idType, ClassName("prismakt.generated", entityClassName))))
                addSuperclassConstructorParameter(tableSpec.name!!)
            }.build())
        }.build()
    }


    override fun generateModels(context: GeneratorContext) {
        for (model in context.datamodel.models) {
            val fileName = model.name + "Table"
            logger.trace("Generating $fileName")
            val fileContent = FileSpec.builder("prismakt.generated", fileName).apply {
                val tableSpec = exposedSqlGenerator.generateTypeSpec(model, context)
                addType(tableSpec)
                val idType = unwrapTableIdType(tableSpec)
                if (idType != null) {
                    addType(generateDaoClass(model, tableSpec, idType))
                } else {
                    logger.warn("Not generating a DAO for model ${model.name} because no ID type could be found")
                }
            }.build()
            fileContent.writeTo(context.outputDir)
            logger.debug("Generated $fileName")
            logger.trace(fileContent.toString())
        }
    }
}
