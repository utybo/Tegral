package guru.zoroark.tegral.prismakt.generator.generators

import guru.zoroark.tegral.prismakt.generator.parser.PArgBare
import guru.zoroark.tegral.prismakt.generator.parser.PArgNamed
import guru.zoroark.tegral.prismakt.generator.parser.PArgument
import guru.zoroark.tegral.prismakt.generator.parser.PAttribute
import guru.zoroark.tegral.prismakt.generator.parser.PExpValueInt
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("prismakt.typing")

private fun argAsInt(attributeContext: String, arg: PArgument): Int? {
    val expr = when (arg) {
        is PArgNamed -> arg.expr
        is PArgBare -> arg.expr
    }
    return when (expr) {
        is PExpValueInt -> expr.value
        else -> {
            logger.warn(
                "Ignoring '$arg' of attribute $attributeContext argument for type resolution because its expression " +
                    "('expr') is not a simple integer value."
            )
            null
        }
    }
}

private fun List<PAttribute>.resolveDbAttribute(): Pair<String, PAttribute>? {
    return lastOrNull { it.name.startsWith("db.") }
        ?.let {
            it.name.substring(3) to it
        }
}

private fun resolveAccurateStringType(fieldName: String, attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, attribute) = attributes.resolveDbAttribute() ?: return ScalarType.TString.accurate()
    val contextInfo = "$fieldName @${attribute.name}"
    return when (name) {
        "Text" -> ScalarType.TText.accurate()
        "TinyText" ->
            ScalarType.TText.inaccurate(
                "Using Text instead of TinyText because Exposed does not have support for TinyText"
            )
        "MediumText" -> ScalarType.TMediumText.accurate()
        "LongText" -> ScalarType.TLongText.accurate()
        "Char" -> ScalarType.TChar(
            argAsInt(contextInfo, attribute.params[0])!! /* TODO */
        ).accurate()

        "NChar" -> ScalarType.TChar(
            argAsInt(contextInfo, attribute.params[0])!! /* TODO */
        ).accurate()

        "VarChar" -> ScalarType.TVarChar(
            argAsInt(contextInfo, attribute.params[0])!! /* TODO */
        ).accurate()

        "NVarChar" -> ScalarType.TVarChar(
            argAsInt(contextInfo, attribute.params[0])!! /* TODO */
        ).accurate()

        "Bit" -> ScalarType.TString.inaccurate("Bit and VarBit are not supported, using String instead.")
        "VarBit" -> ScalarType.TString.inaccurate("Bit and VarBit are not supported, using String instead.")
        "Uuid" -> ScalarType.TUuid.accurate()
        else -> ScalarType.TString.inaccurate("'@db.$name' is unknown")
    }
}

private fun resolveAccurateIntType(attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, _) = attributes.resolveDbAttribute() ?: return ScalarType.TInt.accurate()
    return when (name) {
        "SmallInt" -> ScalarType.TShort.accurate()
        "UnsignedSmallInt" -> ScalarType.TUInt.accurate()
        "Integer" -> ScalarType.TInt.accurate()
        "UnsignedInteger" -> ScalarType.TUInt.accurate()
        "Int" -> ScalarType.TInt.accurate()
        "Oid" -> ScalarType.TInt.inaccurate("No accurate 'Oid' representation available, using Int") // TODO
        "TinyInt" -> ScalarType.TByte.accurate()
        "UnsignedTinyInt" -> ScalarType.TUByte.accurate()
        "Year" -> ScalarType.TInt.inaccurate("No accurate 'Year' representation available, using Int") // TODO
        "Bit" -> ScalarType.TInt.inaccurate("No accurate 'Bit' representation available, using Int") // TODO
        else -> ScalarType.TInt.inaccurate("'@db.$name' is unknown")
    }
}

private fun resolveAccurateBinaryType(fieldName: String, attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, attribute) = attributes.resolveDbAttribute() ?: return ScalarType.TBinary(null).accurate()
    val contextInfo = "$fieldName @${attribute.name}"
    return when (name) {
        "TinyBlob" -> ScalarType.TBinary((2 shl 8) - 1).accurate()
        "Blob" -> ScalarType.TBinary((2 shl 16) - 1).accurate()
        "MediumBlob" -> ScalarType.TBinary((2 shl 24) - 1).accurate()
        "LongBlob" -> ScalarType.TBinary(null)
            .inaccurate("The Exposed binary type does not support sizes greater than ~2GB, while LongBlob is ~4GB.")

        "ByteA" -> ScalarType.TBinary(null).accurate()
        "Binary" -> ScalarType.TBinary(
            argAsInt(contextInfo, attribute.params[0])!! /* TODO */
        ).accurate()

        "VarBinary" -> ScalarType.TBinary(
            argAsInt(contextInfo, attribute.params[0])!! /* TODO */
        ).accurate()

        "Bit" -> ScalarType.TBinary(1).accurate()
        else -> ScalarType.TBinary(null).inaccurate("'@db.$name' is unknown")
    }
}

private fun resolveAccurateBigIntType(attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, _) = attributes.resolveDbAttribute() ?: return ScalarType.TLong.accurate()
    return when (name) {
        "UnsignedBigInt" -> ScalarType.TULong.accurate()
        "BigInt" -> ScalarType.TLong.accurate()

        else -> ScalarType.TLong.inaccurate("'@db.$name' is unknown")
    }
}

private fun resolveAccurateFloatType(attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, _) = attributes.resolveDbAttribute() ?: return ScalarType.TDouble.accurate()
    return when (name) {
        "DoublePrecision" -> ScalarType.TDouble.accurate()
        "Real" -> ScalarType.TFloat.accurate()
        "Float" -> ScalarType.TFloat.accurate()
        else -> ScalarType.TDouble.inaccurate("'@db.$name' is unknown")
    }
}

private fun resolveAccurateDecimalType(fieldName: String, attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, attribute) = attributes.resolveDbAttribute() ?: return ScalarType.TDecimal(null).accurate()
    val contextInfo = "$fieldName @${attribute.name}"
    return when (name) {
        "Decimal" -> ScalarType.TDecimal(
            argAsInt(contextInfo, attribute.params[0])!! to argAsInt(contextInfo, attribute.params[1])!!, // TODO
        ).accurate()
        // TODO money
        else -> ScalarType.TDecimal(null).inaccurate("'@db.$name' is unknown")
    }
}

private fun resolveAccurateDateTimeType(attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, _) = attributes.resolveDbAttribute() ?: return ScalarType.TLocalDateTime.accurate()
    return when (name) {
        "Timestamp" -> ScalarType.TInstant.accurate()
        "Timestamptz" -> ScalarType.TInstant.accurate()
        "Date" -> ScalarType.TLocalDate.accurate()
        "Time" -> ScalarType.TLocalTime.accurate()
        "Timetz" -> ScalarType.TLocalTime.accurate()
        "DateTime" -> ScalarType.TLocalDateTime.accurate()
        "DateTime2" -> ScalarType.TLocalDateTime.accurate()
        "SmallDateTime" -> ScalarType.TLocalDateTime.accurate()
        "DateTimeOffset" -> ScalarType.TLocalDateTime.inaccurate("Does not have proper equivalents in Exposed")
        else -> ScalarType.TLocalDateTime.inaccurate("'@db.$name' is unknown")
    }
}

fun nativeTypeToScalarType(fieldName: String, typeName: String, attributes: List<PAttribute>): ScalarTypeWithAccuracy? {
    return when (typeName) {
        "String" -> resolveAccurateStringType(fieldName, attributes)
        "Int" -> resolveAccurateIntType(attributes)
        "Boolean" -> ScalarType.TBoolean.accurate()
        "BigInt" -> resolveAccurateBigIntType(attributes)
        "Float" -> resolveAccurateFloatType(attributes)
        "Decimal" -> resolveAccurateDecimalType(fieldName, attributes)
        "DateTime" -> resolveAccurateDateTimeType(attributes)
        "Bytes" -> resolveAccurateBinaryType(fieldName, attributes)
        else -> null
    }
}

fun ScalarType.accurate(): ScalarTypeWithAccuracy {
    return ScalarTypeWithAccuracy.Accurate(this)
}

fun ScalarType.inaccurate(reason: String): ScalarTypeWithAccuracy {
    return ScalarTypeWithAccuracy.Inaccurate(this, reason)
}

sealed class ScalarTypeWithAccuracy(val type: ScalarType) {
    class Accurate(type: ScalarType) : ScalarTypeWithAccuracy(type)
    class Inaccurate(type: ScalarType, val inaccuracyReason: String) : ScalarTypeWithAccuracy(type)
}

/**
 * A scalar type for a field in a Prisma schema.
 *
 * Types defined here are more precise than Prisma's own types. Because Kotlin has more precise typing, we can use
 * Prisma's "basic" type (e.g. String, Int...) *and* the `@db.` attributes (e.g. `@db.Text`) to retrieve the fully
 * accurate type.
 */
sealed class ScalarType {
    // String-like types
    object TString : ScalarType()
    object TText : ScalarType()
    object TMediumText : ScalarType()
    object TLongText : ScalarType()
    class TChar(val n: Int) : ScalarType()
    class TVarChar(val n: Int) : ScalarType()
    object TUuid : ScalarType()

    // Int-like types
    object TInt : ScalarType()
    object TUInt : ScalarType()
    object TByte : ScalarType()
    object TUByte : ScalarType()
    object TShort : ScalarType()
    object TUShort : ScalarType()

    object TBoolean : ScalarType()
    object TLong : ScalarType()
    object TULong : ScalarType()
    object TDouble : ScalarType()
    object TFloat : ScalarType()
    class TDecimal(private val precisionAndScale: Pair<Int, Int>?) : ScalarType() {
        fun getPrecisionAndScale(isMsSql: Boolean): Pair<Int, Int> {
            return precisionAndScale ?: if (isMsSql) 32 to 16 else 65 to 30
        }
    }

    object TLocalDateTime : ScalarType()
    object TLocalDate : ScalarType()
    object TLocalTime : ScalarType()
    object TInstant : ScalarType()

    class TBinary(val maxSize: Int?) : ScalarType()
}
