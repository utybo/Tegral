@file:Suppress("TooManyFunctions")

package guru.zoroark.tegral.prismakt.generator.generators

import guru.zoroark.tegral.prismakt.generator.parser.PArgBare
import guru.zoroark.tegral.prismakt.generator.parser.PArgNamed
import guru.zoroark.tegral.prismakt.generator.parser.PArgument
import guru.zoroark.tegral.prismakt.generator.parser.PAttribute
import guru.zoroark.tegral.prismakt.generator.parser.PExpValueInt

private fun argAsInt(attributeContext: String, arg: PArgument): Int {
    val expr = when (arg) {
        is PArgNamed -> arg.expr
        is PArgBare -> arg.expr
    }
    return when (expr) {
        is PExpValueInt -> expr.value
        else -> {
            error(
                "Could not find or parse an argument '$arg' of attribute $attributeContext argument for type " +
                    "resolution."
            )
        }
    }
}

private fun List<PAttribute>.resolveDbAttribute(): Pair<String, PAttribute>? {
    val dbDot = "db."
    return lastOrNull { it.name.startsWith(dbDot) }
        ?.let {
            it.name.substring(dbDot.length) to it
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
            argAsInt(contextInfo, attribute.params[0])
        ).accurate()

        "NChar" -> ScalarType.TChar(
            argAsInt(contextInfo, attribute.params[0])
        ).accurate()

        "VarChar" -> ScalarType.TVarChar(
            argAsInt(contextInfo, attribute.params[0])
        ).accurate()

        "NVarChar" -> ScalarType.TVarChar(
            argAsInt(contextInfo, attribute.params[0])
        ).accurate()

        "Bit" -> ScalarType.TString.inaccurate("Bit and VarBit are not supported, using String instead.")
        "VarBit" -> ScalarType.TString.inaccurate("Bit and VarBit are not supported, using String instead.")
        "Uuid" -> ScalarType.TUuid.accurate()
        else -> ScalarType.TString.inaccurate("'@db.$name' is unknown")
    }
}

@Suppress("CyclomaticComplexMethod") // This report is stupid, this is literally just a `when`
private fun resolveAccurateIntType(attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, _) = attributes.resolveDbAttribute() ?: return ScalarType.TInt.accurate()
    return when (name) {
        "SmallInt" -> ScalarType.TShort.accurate()
        "UnsignedSmallInt" -> ScalarType.TUShort.accurate()
        "Integer" -> ScalarType.TInt.accurate()
        "Int" -> ScalarType.TInt.accurate()
        "UnsignedInt" -> ScalarType.TUInt.accurate()
        "Oid" -> ScalarType.TInt.inaccurate("No accurate 'Oid' representation available, using Int") // TODO
        "TinyInt" -> ScalarType.TByte.accurate()
        "UnsignedTinyInt" -> ScalarType.TUByte.accurate()
        "MediumInt" -> ScalarType.TInt.inaccurate(
            "There are no exact representations of 3-byte signed numbers in Kotlin"
        )

        "UnsignedMediumInt" -> ScalarType.TUInt.inaccurate(
            "There are no exact representations of 3-byte unsigned numbers in Kotlin"
        )

        "Year" -> ScalarType.TInt.inaccurate("No accurate 'Year' representation available, using Int") // TODO
        "Bit" -> ScalarType.TInt.inaccurate("No accurate 'Bit' representation available, using Int") // TODO
        else -> ScalarType.TInt.inaccurate("'@db.$name' is unknown")
    }
}

// The following values are taken from:
// https://dev.mysql.com/doc/refman/8.0/en/storage-requirements.html#data-types-storage-reqs-strings

private const val MAX_TINY_BLOB_SIZE = 255 // == 2 ^ 8 - 1
private const val MAX_BLOB_SIZE = 65535 // == 2 ^ 16 - 1
private const val MAX_MEDIUM_BLOB_SIZE = 16777215 // == 2 ^ 24 - 1

// TODO Exposed does not support (2 ^ 32) - 1 sizes here, which should be used instead.
private val MAX_LONG_BLOB_SIZE: Int? = null // Indicates unlimited size
// private const val MAX_LONG_BLOB_SIZE = 4294967295 // == 2 ^ 32 - 1

private fun resolveAccurateBinaryType(fieldName: String, attributes: List<PAttribute>): ScalarTypeWithAccuracy {
    val (name, attribute) = attributes.resolveDbAttribute() ?: return ScalarType.TBinary(null).accurate()
    val contextInfo = "$fieldName @${attribute.name}"
    return when (name) {
        "TinyBlob" -> ScalarType.TBinary(MAX_TINY_BLOB_SIZE).accurate()
        "Blob" -> ScalarType.TBinary(MAX_BLOB_SIZE).accurate()
        "MediumBlob" -> ScalarType.TBinary(MAX_MEDIUM_BLOB_SIZE).accurate()
        "LongBlob" -> ScalarType.TBinary(MAX_LONG_BLOB_SIZE)
            .inaccurate("The Exposed binary type does not support sizes greater than ~2GB, while LongBlob is ~4GB.")

        "ByteA" -> ScalarType.TBinary(null).accurate()
        "Binary" -> ScalarType.TBinary(
            argAsInt(contextInfo, attribute.params[0])
        ).accurate()

        "VarBinary" -> ScalarType.TBinary(
            argAsInt(contextInfo, attribute.params[0])
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
        "Double" -> ScalarType.TDouble.accurate()
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
            argAsInt(contextInfo, attribute.params[0]) to argAsInt(contextInfo, attribute.params[1]), // TODO
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

/**
 * Resolves Prisma type ([typeName]) with additional database [attributes] into a [ScalarType] with additional precision
 * information.
 *
 * @param fieldName Name of the field, as it is provided in the Prisma schema. Only used for logging.
 * @param typeName Simple type name. Corresponds to the "basic type" in the Prisma schema.
 * @param attributes List of attributes associated to the field. May be empty
 */
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

/**
 * Create an accurate [ScalarTypeWithAccuracy] with `this` being the scalar type.
 */
fun ScalarType.accurate(): ScalarTypeWithAccuracy {
    return ScalarTypeWithAccuracy.Accurate(this)
}

/**
 * Create an inaccurate [ScalarTypeWithAccuracy] with `this` being the scalar type and the given [reason].
 */
fun ScalarType.inaccurate(reason: String): ScalarTypeWithAccuracy {
    return ScalarTypeWithAccuracy.Inaccurate(this, reason)
}

/**
 * A [ScalarType] with additional accuracy information. Used as the rreturn type of [nativeTypeToScalarType].
 */
sealed class ScalarTypeWithAccuracy {
    /**
     * The actual [ScalarType]
     */
    abstract val type: ScalarType

    /**
     * The [ScalarType] is, to the best of Tegral PrismaKT's ability, accurate.
     */
    data class Accurate(override val type: ScalarType) : ScalarTypeWithAccuracy()

    /**
     * The [ScalarType] is an approximation of the actual type.
     *
     * @property inaccuracyReason Human-formatted reason for the inaccuracy. Usually provides additional context as to
     * why finding the accurate type failed.
     */
    data class Inaccurate(override val type: ScalarType, val inaccuracyReason: String) : ScalarTypeWithAccuracy()
}

/**
 * A scalar type for a field in a Prisma schema.
 *
 * Types defined here are more precise than Prisma's own types. Because Kotlin has more precise typing, we can use
 * Prisma's "basic" type (e.g. String, Int...) *and* the `@db.` attributes (e.g. `@db.Text`) to retrieve the fully
 * accurate type.
 */
@Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
sealed class ScalarType {
    // String-like types
    object TString : ScalarType()
    object TText : ScalarType()
    object TMediumText : ScalarType()
    object TLongText : ScalarType()
    data class TChar(val n: Int) : ScalarType()
    data class TVarChar(val n: Int) : ScalarType()
    object TUuid : ScalarType()

    // Int-like types
    object TInt : ScalarType()
    object TUInt : ScalarType()
    object TByte : ScalarType()
    object TUByte : ScalarType()
    object TShort : ScalarType()
    object TUShort : ScalarType()
    object TLong : ScalarType()
    object TULong : ScalarType()

    // Misc. types
    object TBoolean : ScalarType()
    object TDouble : ScalarType()
    object TFloat : ScalarType()
    data class TDecimal(private val precisionAndScale: Pair<Int, Int>?) : ScalarType() {
        /**
         * Returns the precision and scale of this decimal number, using defaults as needed.
         *
         * Precision and scale are to be interpreted as [java.math.BigDecimal] defines them.
         *
         * @param isMsSql True if the database is Microsoft SQL Server. This is needed because Prisma defines different
         * defaults on MSSQL compared to the rest of the supported databases.
         */
        fun getPrecisionAndScale(isMsSql: Boolean): Pair<Int, Int> {
            return precisionAndScale ?: if (isMsSql) {
                DEFAULT_MSSQL_PRECISION to DEFAULT_MSSQL_SCALE
            } else {
                DEFAULT_PRECISION to DEFAULT_SCALE
            }
        }

        companion object {
            private const val DEFAULT_MSSQL_PRECISION = 32
            private const val DEFAULT_MSSQL_SCALE = 16
            private const val DEFAULT_PRECISION = 65
            private const val DEFAULT_SCALE = 30
        }
    }

    object TLocalDateTime : ScalarType()
    object TLocalDate : ScalarType()
    object TLocalTime : ScalarType()
    object TInstant : ScalarType()

    data class TBinary(val maxSize: Int?) : ScalarType()
}
