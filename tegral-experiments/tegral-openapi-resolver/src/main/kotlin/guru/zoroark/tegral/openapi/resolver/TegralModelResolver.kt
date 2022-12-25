package guru.zoroark.tegral.openapi.resolver

import com.fasterxml.jackson.core.type.ResolvedType
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.core.jackson.AbstractModelConverter
import io.swagger.v3.core.jackson.TypeNameResolver
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.core.util.ReflectionUtils
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import io.swagger.v3.oas.annotations.media.ArraySchema as ArraySchemaAnn
import io.swagger.v3.oas.annotations.media.Schema as SchemaAnn

val DEFAULT_IGNORED_CLASSES = setOf("javax.ws.rs.Response")

private val logger = LoggerFactory.getLogger(TegralModelResolver::class.java)

class TegralModelResolver(
    mapper: ObjectMapper,
    typeNameResolver: TypeNameResolver,
    private val ignoredClasses: Set<String> = DEFAULT_IGNORED_CLASSES
) :
    AbstractModelConverter(mapper, typeNameResolver) {
    override fun resolve(
        annotatedType: AnnotatedType?,
        context: ModelConverterContext?,
        chain: MutableIterator<ModelConverter>?
    ): Schema<*>? {
        if (annotatedType == null || shouldIgnoreClass(annotatedType.type)) return null

        val type: JavaType =
            if (annotatedType.type is JavaType) annotatedType.type as JavaType
            else _mapper.constructType(annotatedType.type)

        val annotations = resolveAnnotation(annotatedType, type)
        val beanDescription = resolveBeanDescription(type)

        val name = annotatedType.name.ifBlank {
            if (annotatedType.isSkipSchemaName && !annotations.schema?.name.isNullOrBlank()) {
                annotations.schema!!.name
            } else if (!ReflectionUtils.isSystemType(type)) {
                _typeName(type, beanDescription)
            } else ""
        }

        // TODO decorateModelName

        if (!annotations.schema?.ref.isNullOrBlank()) {
            val refSchema =Schema<Any>().`$ref`(annotations.schema!!.ref).name(name)
            return if (annotations.arraySchema == null) {
                refSchema
            } else {
                createArraySchema(annotatedType, annotations.arraySchema)
                    .items(refSchema)
            }
        }

        return super.resolve(annotatedType, context, chain)
    }

    private fun shouldIgnoreClass(type: Type): Boolean {
        if (type is Class<*>) {
            return type.name in ignoredClasses
        } else if (type is ResolvedType) {
            // TODO is this checked by any test and if not, shouldn't it include ignoredClasses?
            return type.rawClass == Class::class.java
        }
    }

    data class SchemaAnnotations(val schema: SchemaAnn?, val arraySchema: ArraySchemaAnn?)

    private fun resolveAnnotation(annotatedType: AnnotatedType, javaType: JavaType): SchemaAnnotations {
        val resolvedSchemaOrArrayAnnotation: Annotation? =
            AnnotationsUtils.mergeSchemaAnnotations(annotatedType.ctxAnnotations, javaType)
        val resolvedSchemaAnnotation: SchemaAnn? =
            resolvedSchemaOrArrayAnnotation?.let { if (it is ArraySchemaAnn) it.schema else it as SchemaAnn }
        val resolvedArrayAnnotation = resolvedSchemaOrArrayAnnotation as? ArraySchemaAnn
        return SchemaAnnotations(resolvedSchemaAnnotation, resolvedArrayAnnotation)
    }

    // Resolve bean description, recursing into @JsonSerialize annotations
    private fun resolveBeanDescription(javaType: JavaType): BeanDescription {
        val beanDesc = _mapper.serializationConfig.introspect(javaType)
        val visited = mutableSetOf<String>()
        tailrec fun recurseJsonSerialize(beanDesc: BeanDescription): BeanDescription {
            val jsonSerialize = beanDesc.classAnnotations.get(JsonSerialize::class.java)
            if (jsonSerialize == null || jsonSerialize.`as` == Void::class.java) return beanDesc

            val asName = jsonSerialize.`as`.java.name
            if (asName in visited) return beanDesc
            visited.add(asName)

            return recurseJsonSerialize(_mapper.serializationConfig.introspect(_mapper.constructType(jsonSerialize.`as`.java)))
        }
        return recurseJsonSerialize(beanDesc)
    }

    private fun createArraySchema(annotatedType: AnnotatedType, schemaAnnotation: ArraySchemaAnn): ArraySchema {
        TODO()
    }
}