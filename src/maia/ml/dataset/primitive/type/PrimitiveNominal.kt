package maia.ml.dataset.primitive.type

import maia.ml.dataset.primitive.convertValue
import maia.ml.dataset.primitive.handleMissingOnGet
import maia.ml.dataset.type.DataRepresentation
import maia.ml.dataset.type.EntropicRepresentation
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.type.standard.NominalCanonicalRepresentation
import maia.ml.dataset.type.standard.NominalIndexRepresentation
import java.math.BigInteger

/**
 * TODO
 */
internal inline fun <X> PrimitiveDataRepresentation<*, *, out X, in Int>.handleMissingNominal(
    value: Int,
    block: (Int) -> X
): X = handleMissingOnGet(value, {it < 0}, block)

/**
 * TODO
 */
class PrimitiveNominalCanonicalRepresentation internal constructor():
    NominalCanonicalRepresentation<PrimitiveNominalCanonicalRepresentation, PrimitiveNominal>(),
    PrimitiveDataRepresentation<PrimitiveNominalCanonicalRepresentation, PrimitiveNominal, String, Int>
{
    override fun convertIn(value: String) : Int = dataType.indexOf(value)
    override fun convertOut(value: Int) : String = handleMissingNominal(value, dataType::get)
    override fun clearSentinel() : Int = -1
    override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PrimitiveNominal, I>) : String = convertValue(value, fromRepresentation)
}

/**
 * TODO
 */
class PrimitiveNominalEntropicRepresentation internal constructor():
    EntropicRepresentation<PrimitiveNominalEntropicRepresentation, PrimitiveNominal>(),
    PrimitiveDataRepresentation<PrimitiveNominalEntropicRepresentation, PrimitiveNominal, BigInteger, Int>
{
    override fun convertIn(value : BigInteger) : Int = value.toInt()
    override fun convertOut(value : Int) : BigInteger = handleMissingNominal(value) { it.toBigInteger() }
    override fun clearSentinel() : Int = -1
    override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PrimitiveNominal, I>) : BigInteger = convertValue(value, fromRepresentation)
}

/**
 * TODO
 */
class PrimitiveNominalIndexRepresentation internal constructor():
    NominalIndexRepresentation<PrimitiveNominalIndexRepresentation, PrimitiveNominal>(),
    PrimitiveDataRepresentation<PrimitiveNominalIndexRepresentation, PrimitiveNominal, Int, Int>
{
    override fun convertIn(value : Int) : Int = value
    override fun convertOut(value : Int) : Int = handleMissingNominal(value) { it }
    override fun clearSentinel() : Int = -1
    override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PrimitiveNominal, I>) : Int = convertValue(value, fromRepresentation)
}

/**
 * TODO
 */
class PrimitiveNominal(
    supportsMissingValues: Boolean,
    vararg categories : String
): Nominal<PrimitiveNominal, PrimitiveNominalCanonicalRepresentation, PrimitiveNominalEntropicRepresentation, PrimitiveNominalIndexRepresentation>(
    PrimitiveNominalCanonicalRepresentation(),
    PrimitiveNominalEntropicRepresentation(),
    PrimitiveNominalIndexRepresentation(),
    supportsMissingValues,
    *categories
) {
    override fun copy() : PrimitiveNominal =
        PrimitiveNominal(supportsMissingValues, *toTypedArray())
}
