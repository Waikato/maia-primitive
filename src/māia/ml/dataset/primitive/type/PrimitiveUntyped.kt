package māia.ml.dataset.primitive.type

import māia.ml.dataset.primitive.convertValue
import māia.ml.dataset.primitive.handleMissingOnGet
import māia.ml.dataset.type.DataRepresentation
import māia.ml.dataset.type.standard.UntypedData
import māia.ml.dataset.type.standard.UntypedRepresentation

/**
 * TODO
 */
class PrimitiveUntypedRepresentation internal constructor():
    UntypedRepresentation<PrimitiveUntypedRepresentation, PrimitiveUntyped>(),
    PrimitiveDataRepresentation<PrimitiveUntypedRepresentation, PrimitiveUntyped, Any?, Any?>
{
    override fun convertIn(value : Any?) : Any? = value
    override fun convertOut(value : Any?) : Any? = handleMissingOnGet(value, {it is Missing}) { value }
    override fun clearSentinel() : Any = Missing
    override fun <I> convertValue(value : I, fromRepresentation : DataRepresentation<*, PrimitiveUntyped, I>) : Any? = convertValue(value, fromRepresentation)
}

/**
 * TODO
 */
class PrimitiveUntyped(
    supportsMissingValues: Boolean
): UntypedData<PrimitiveUntyped, PrimitiveUntypedRepresentation>(
    PrimitiveUntypedRepresentation(),
    supportsMissingValues
) {
    override fun copy() : PrimitiveUntyped = PrimitiveUntyped(supportsMissingValues)
}
