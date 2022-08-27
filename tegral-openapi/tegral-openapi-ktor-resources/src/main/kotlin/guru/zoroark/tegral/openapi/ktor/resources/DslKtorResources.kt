package guru.zoroark.tegral.openapi.ktor.resources

import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.dsl.ParameterDsl
import guru.zoroark.tegral.openapi.dsl.ResponseDsl
import io.ktor.http.HttpStatusCode
import kotlin.reflect.KProperty

fun <A> OperationDsl.pathParameter(
    field: KProperty<A>,
    builder: ParameterDsl.() -> Unit
) = field.name pathParameter builder

fun <A> OperationDsl.headerParameter(
    field: KProperty<A>,
    builder: ParameterDsl.() -> Unit
) = field.name headerParameter builder

fun <A> OperationDsl.cookieParameter(
    field: KProperty<A>,
    builder: ParameterDsl.() -> Unit
) = field.name cookieParameter builder

fun <A> OperationDsl.queryParameter(
    field: KProperty<A>,
    builder: ParameterDsl.() -> Unit
) = field.name queryParameter builder

fun OperationDsl.response(
    code: HttpStatusCode,
    builder: ResponseDsl.() -> Unit
) = code.value response builder
