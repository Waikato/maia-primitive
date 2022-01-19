package maia.ml.dataset.primitive.type

import maia.ml.dataset.primitive.handleMissingOnGet
import maia.ml.dataset.type.DataRepresentation
import maia.ml.dataset.type.standard.Numeric
import maia.ml.dataset.type.standard.NumericCanonicalRepresentation

/**
 * TODO
 */
class PrimitiveNumericCanonicalRepresentation internal constructor():
    NumericCanonicalRepresentation<PrimitiveNumericCanonicalRepresentation, PrimitiveNumeric>(),
    PrimitiveDataRepresentation<PrimitiveNumericCanonicalRepresentation, PrimitiveNumeric, Double, Double>
{
    override fun convertIn(value : Double) : Double = value
    override fun convertOut(value : Double) : Double = handleMissingOnGet(value, Double::isNaN) { it }
    override fun clearSentinel() : Double = Double.NaN
    override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PrimitiveNumeric, I>) : Double = convertValue(value, fromRepresentation)
}

/**
 * TODO
 */
class PrimitiveNumeric(
    supportsMissingValues: Boolean
) : Numeric<
        PrimitiveNumeric,
        PrimitiveNumericCanonicalRepresentation
>(
    PrimitiveNumericCanonicalRepresentation(),
    supportsMissingValues
) {
    override fun copy() : PrimitiveNumeric = PrimitiveNumeric(supportsMissingValues)
}
