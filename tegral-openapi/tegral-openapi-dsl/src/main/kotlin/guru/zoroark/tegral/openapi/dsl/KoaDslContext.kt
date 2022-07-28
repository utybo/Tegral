package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType

interface KoaDslContext {
    fun computeAndRegisterSchema(type: KType): Schema<*>
}
