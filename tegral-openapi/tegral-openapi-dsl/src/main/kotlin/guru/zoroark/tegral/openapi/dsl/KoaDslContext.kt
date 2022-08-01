package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType

/**
 * This interface is passed to most of the builder classes and provides utilities that need to be present through the
 * "DSL tree".
 */
interface KoaDslContext {
    /**
     * Computes (or gets) the schema associated with the given KType. You can get a KType using the `typeOf<>()`
     * function.
     *
     * The schema will be returned as a '$ref', while the actual components are stored inside the context.
     */
    fun computeAndRegisterSchema(type: KType): Schema<*>
}
