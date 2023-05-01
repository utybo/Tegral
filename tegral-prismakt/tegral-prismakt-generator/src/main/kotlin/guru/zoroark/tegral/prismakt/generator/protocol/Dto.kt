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

package guru.zoroark.tegral.prismakt.generator.protocol

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.node.JsonNodeType

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "method")
@JsonSubTypes(
    JsonSubTypes.Type(value = GeneratorRequest.GetManifestRequest::class, name = "getManifest"),
    JsonSubTypes.Type(value = GeneratorRequest.GenerateRequest::class, name = "generate")
)
sealed class GeneratorRequest<TParams> {
    abstract val jsonrpc: String
    abstract val id: Long
    abstract val params: TParams

    data class GetManifestRequest(
        override val jsonrpc: String,
        override val id: Long,
        override val params: GeneratorConfig
    ) : GeneratorRequest<GeneratorConfig>()

    data class GenerateRequest(
        override val jsonrpc: String,
        override val id: Long,
        override val params: GeneratorOptions
    ) : GeneratorRequest<GeneratorOptions>()
}

sealed class GeneratorResponse<TResult> {
    abstract val jsonrpc: String
    abstract val id: Long
    abstract val result: TResult

    data class GetManifestResponse(
        override val jsonrpc: String,
        override val result: GeneratorManifestContainer,
        override val id: Long
    ) : GeneratorResponse<GetManifestResponse.GeneratorManifestContainer>() {
        data class GeneratorManifestContainer(
            val manifest: GeneratorManifest
        )
    }
}

data class GeneratorConfig(
    val name: String,
    val output: EnvValue?,
    val isCustomOutput: Boolean,
    val provider: EnvValue,
    val config: Map<String, String>,
    val binaryTargets: List<BinaryTargetsEnvValue>,
    val previewFeatures: List<String>
)

data class GeneratorManifest(
    val prettyName: String?,
    val defaultOutput: String? = null,
    val denylists: DenyLists? = null,
    val requiresGenerators: List<String>? = null,
    val requiresEngines: List<String>? = null,
    val version: String? = null,
    val requiresEngineVersion: String? = null
) {
    data class DenyLists(
        val models: List<String>? = null,
        val fields: List<String>? = null
    )
}

data class GeneratorOptions(
    val generator: GeneratorConfig,
    val otherGenerators: List<GeneratorConfig>,
    val schemaPath: String,
    val dmmf: DMMFDocument,
    val datasources: List<DataSource>,
    val datamodel: String,
    val version: String,
    val dataProxy: Boolean
)

data class DataSource(
    val name: String,
    val provider: String,
    val activeProvider: String,
    val url: EnvValue,
    val directUrl: EnvValue?,
    val schemas: List<String>
)

data class EnvValue(
    val fromEnvVar: String?,
    val value: String?
)

data class BinaryTargetsEnvValue(
    val fromEnvVar: String?,
    val value: String
)

data class DMMFDocument(
    val datamodel: Datamodel,
    val schema: Schema,
    val mappings: Mappings
)

data class Mappings(
    val modelOperations: List<ModelMapping>,
    val otherOperations: OtherOperationMappings
)

data class OtherOperationMappings(
    val read: List<String>,
    val write: List<String>,
)

data class DatamodelEnum(
    val name: String,
    val values: List<EnumValue>,
    val dbName: String? = null,
    val documentation: String? = null
)

data class SchemaEnum(
    val name: String,
    val values: List<String>
)

data class EnumValue(
    val name: String,
    val dbName: String?
)

data class Datamodel(
    val models: List<Model>,
    val enums: List<DatamodelEnum>,
    val types: List<Model>
)

data class UniqueIndex(
    val name: String,
    val fields: List<String>
)

data class PrimaryKey(
    val name: String?,
    val fields: List<String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Model(
    val name: String,
    val dbName: String?,
    val fields: List<Field>,
    val uniqueFields: List<List<String>>,
    val uniqueIndexes: List<UniqueIndex>,
    val documentation: String? = null,
    val primaryKey: PrimaryKey?
)

enum class FieldKind(@JsonValue val value: String) {
    Scalar("scalar"),
    Object("object"),
    Enum("enum"),
    Unsupported("unsupported")
}

data class Field(
    val kind: FieldKind,
    val name: String,
    val isRequired: Boolean,
    val isList: Boolean,
    val isUnique: Boolean,
    val isId: Boolean,
    val isReadOnly: Boolean,
    val isGenerated: Boolean? = null,
    val isUpdatedAt: Boolean? = null,
    val type: String,
    val dbNames: List<String>? = null,
    val hasDefaultValue: Boolean,
    val default: Any? = null,
    val relationFromFields: List<String>? = null,
    val relationToFields: List<Any>? = null,
    val relationOnDelete: String? = null,
    val relationName: String? = null
)

data class Schema(
    val rootQueryType: String? = null,
    val rootMutationType: String? = null,
    val inputObjectTypes: InputTypes,
    val outputObjectTypes: OutputTypes,
    val enumTypes: EnumTypes,
    val fieldRefTypes: FieldRefTypes
) {
    data class InputTypes(
        val model: List<InputType>? = null,
        val prisma: List<InputType>
    )

    data class OutputTypes(
        val model: List<OutputType>,
        val prisma: List<OutputType>
    )

    data class EnumTypes(
        val model: List<SchemaEnum>? = null,
        val prisma: List<SchemaEnum>
    )

    data class FieldRefTypes(
        val prisma: List<FieldRefType>? = null
    )
}

data class Query(
    val name: String,
    val args: List<SchemaArg>,
    val output: QueryOutput
)

data class QueryOutput(
    val name: String,
    val isRequired: Boolean,
    val isList: Boolean
)

@JsonDeserialize(using = ArgTypeDeserializer::class)
sealed class ArgType {
    data class ArgStringValue(val value: String) : ArgType()
    data class ArgInputType(val inputType: InputType) : ArgType()
    data class ArgSchemaEnum(val value: SchemaEnum) : ArgType()
}

class ArgTypeDeserializer : StdDeserializer<ArgType>(ArgType::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ArgType {
        val node = p.codec.readTree<JsonNode>(p)
        if (node.nodeType == JsonNodeType.STRING) return ArgType.ArgStringValue(node.textValue())
        if (node.isObject) {
            return if (node["fields"] != null) {
                p.readValueAs(ArgType.ArgInputType::class.java)
            } else {
                p.readValueAs(ArgType.ArgSchemaEnum::class.java)
            }
        } else {
            throw InvalidDefinitionException.from(p, "Not a valid ArgType", ctxt.contextualType)
        }
    }
}

enum class FieldLocation(@JsonValue val value: String) {
    Scalar("scalar"),
    InputObjectTypes("inputObjectTypes"),
    OutputObjectTypes("outputObjectTypes"),
    EnumTypes("enumTypes"),
    FieldRefTypes("fieldRefTypes")
}

enum class FieldNamespace(@JsonValue val value: String) {
    Model("model"),
    Prisma("prisma")
}

data class SchemaArgInputType(
    val isList: Boolean,
    val type: ArgType,
    val location: FieldLocation,
    val namespace: FieldNamespace? = null
)

data class SchemaArg(
    val name: String,
    val comment: String? = null,
    val isNullable: Boolean,
    val isRequired: Boolean,
    val inputTypes: List<SchemaArgInputType>,
    val deprecation: Deprecation? = null
)

data class OutputType(
    val name: String,
    val fields: List<SchemaField>,
    val fieldMap: Map<String, SchemaField>? = null
)

data class SchemaField(
    val name: String,
    val isNullable: Boolean? = null,
    val outputType: OutputTypeRef,
    val args: List<SchemaArg>,
    val deprecation: Deprecation? = null,
    val documentation: String? = null
)

class ObjectOrString<T : Any> {
    private constructor(stringValue: String?, objectValue: T?) {
        if (stringValue == null && objectValue == null)
            error("Both string and objects are null")

        string = stringValue
        obj = objectValue
    }

    constructor(stringValue: String) : this(stringValue, null)
    constructor(objectValue: T) : this(null, objectValue)

    val string: String?
    val obj: T?
    override fun toString(): String =
        string ?: obj?.toString() ?: "ERROR"
}

typealias OutputTypeRef = TypeRef

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "location")
@JsonSubTypes(
    JsonSubTypes.Type(value = TypeRef.TypeRefScalar::class, name = "scalar"),
    JsonSubTypes.Type(value = TypeRef.TypeRefScalar::class, name = "outputObjectTypes"),
    JsonSubTypes.Type(value = TypeRef.TypeRefScalar::class, name = "enumTypes")
)
sealed class TypeRef(
    val isList: Boolean,
    val namespace: FieldNamespace? = null
) {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "location")
    @JsonSubTypes(
        JsonSubTypes.Type(value = TypeRefScalar::class, name = "scalar"),
        JsonSubTypes.Type(value = TypeRefScalar::class, name = "enumTypes")
    )
    sealed interface FieldRefAllowType<out TThis : TypeRef> {
        val self: TThis
    }

    class TypeRefScalar(
        isList: Boolean,
        namespace: FieldNamespace?,
        val type: String
    ) : TypeRef(isList, namespace), FieldRefAllowType<TypeRefScalar> {
        override val self = this
    }

    class TypeRefOutputObject(
        isList: Boolean,
        namespace: FieldNamespace?,
        val type: ObjectOrString<OutputType>
    ) : TypeRef(isList, namespace)

    class TypeRefEnum(
        isList: Boolean,
        namespace: FieldNamespace?,
        val type: ObjectOrString<SchemaEnum>
    ) : TypeRef(isList, namespace), FieldRefAllowType<TypeRefEnum> {
        override val self = this
    }
}

data class Deprecation(
    val sinceVersion: String,
    val reason: String,
    val plannedRemovalVersion: String?
)

data class InputType(
    val name: String,
    val constraints: Constraints,
    val meta: Meta? = null,
    val fields: List<SchemaArg>,
    val fieldMap: Map<String, SchemaArg>? = null
) {
    data class Constraints(
        val maxNumFields: Int?,
        val minNumFields: Int?,
        val fields: List<String>?
    )

    data class Meta(
        val source: String?
    )
}

data class FieldRefType(
    val name: String,
    val allowTypes: List<TypeRef.FieldRefAllowType<TypeRef>>,
    val fields: List<SchemaArg>
)

data class ModelMapping(
    val model: String,
    val plural: String? = null,
    val findUnique: String? = null,
    val findUniqueOrThrow: String? = null,
    val findFirst: String? = null,
    val findFirstOrThrow: String? = null,
    val findMany: String? = null,
    val create: String? = null,
    val createOne: String? = null,
    val createMany: String? = null,
    val update: String? = null,
    val updateOne: String? = null,
    val updateMany: String? = null,
    val upsert: String? = null,
    val upsertOne: String? = null,
    val delete: String? = null,
    val deleteOne: String? = null,
    val deleteMany: String? = null,
    val aggregate: String? = null,
    val groupBy: String? = null,
    val count: String? = null,
    val findRaw: String? = null,
    val aggregateRaw: String? = null
)
