package maia.ml.dataset.primitive

/**
 * TODO
 */

import maia.ml.dataset.WithIndexableRows
import maia.ml.dataset.error.MissingValue
import maia.ml.dataset.primitive.error.UnsupportedDataTypeError
import maia.ml.dataset.primitive.error.UnsupportedRepresentationError
import maia.ml.dataset.primitive.type.PrimitiveDataRepresentation
import maia.ml.dataset.primitive.type.PrimitiveNominal
import maia.ml.dataset.primitive.type.PrimitiveNumeric
import maia.ml.dataset.primitive.type.PrimitiveUntyped
import maia.ml.dataset.type.DataRepresentation
import maia.ml.dataset.type.DataType
import maia.ml.dataset.type.EntropicRepresentation
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.type.standard.NominalCanonicalRepresentation
import maia.ml.dataset.type.standard.NominalIndexRepresentation
import maia.ml.dataset.type.standard.NominalLabelRepresentation
import maia.ml.dataset.type.standard.Numeric
import maia.ml.dataset.type.standard.NumericCanonicalRepresentation
import maia.ml.dataset.type.standard.UntypedData
import maia.ml.dataset.type.standard.UntypedRepresentation
import maia.util.assertType
import maia.util.ensureIndexInRange

/**
 * TODO
 */
internal fun <T> newPrimitiveRepresentation(
    representation: DataRepresentation<*, *, T>,
    supportsMissingValues: Boolean
): PrimitiveDataRepresentation<*, *, T, *> {
    val type = representation.dataType
    return when(type) {
        is Nominal<*, *, *, *, *> -> {
            val newType = PrimitiveNominal(supportsMissingValues, *type.toTypedArray())
            when (representation) {
                is NominalCanonicalRepresentation<*, *> -> newType.canonicalRepresentation
                is NominalLabelRepresentation<*, *> -> newType.labelRepresentation
                is NominalIndexRepresentation<*, *> -> newType.indexRepresentation
                is EntropicRepresentation<*, *> -> newType.entropicRepresentation
                else -> throw UnsupportedRepresentationError(representation)
            }
        }
        is Numeric<*, *> -> {
            val newType = PrimitiveNumeric(supportsMissingValues)
            when (representation) {
                is NumericCanonicalRepresentation -> newType.canonicalRepresentation
                else -> throw UnsupportedRepresentationError(representation)
            }
        }
        is UntypedData<*, *> -> {
            val newType = PrimitiveUntyped(supportsMissingValues)
            when (representation) {
                is UntypedRepresentation<*, *> -> newType.canonicalRepresentation
                else -> throw UnsupportedRepresentationError(representation)
            }
        }
        else -> throw UnsupportedDataTypeError(type)
    } as PrimitiveDataRepresentation<*, *, T, *>
}

/**
 * TODO
 */
inline fun <R> WithIndexableRows<*>.ensureRowIndex(
    rowIndex : Int,
    includeSize: Boolean = false,
    crossinline block: () -> R
): R = ensureIndexInRange(rowIndex, numRows, includeSize) { block() }

/**
 * TODO
 */
inline fun <R> WithIndexableRows<*>.ensureRowsIndices(
    startIndex: Int,
    count: Int,
    crossinline block: () -> R
): R = ensureRowIndex(startIndex) {
    ensureIndexInRange(
        startIndex + count,
        numRows,
        true,
        { "'count' must not extend beyond the end of the column. " }
    ) {
        block()
    }
}

/**
 * TODO
 */
internal inline fun <I> newDataStore(
    representation: PrimitiveDataRepresentation<*, *, *, I>,
    clearSentinel : I,
    initialCapacity: Int = 16,
    initialSize: Int = 0,
    noinline initial: (Int) -> I = { clearSentinel }
): PrimitiveDataStore<I> {
    return when (representation.self.dataType) {
        is PrimitiveNominal -> PrimitiveDataStore(PrimitiveIntArray(IntArray(initialCapacity, initial as ((Int) -> Int))), clearSentinel as Int, initialSize)
        is PrimitiveNumeric -> PrimitiveDataStore(PrimitiveDoubleArray(DoubleArray(initialCapacity, initial as ((Int) -> Double))), clearSentinel as Double, initialSize)
        else -> PrimitiveDataStore(PrimitiveObjectArray(Array(initialCapacity, initial)), clearSentinel, initialSize)
    } as PrimitiveDataStore<I>
}

/**
 * TODO
 */
internal inline fun <I, X> handleMissingOnSet(
    convert: (X) -> I,
    clearSentinel: I,
    data: () -> X
): I {
    return try {
        convert(data())
    } catch (e: MissingValue) {
        clearSentinel
    }
}

/**
 * TODO
 */
internal inline fun <I, X> PrimitiveDataRepresentation<*, *, out X, in I>.handleMissingOnGet(
    value: I,
    test: (I) -> Boolean,
    block: (I) -> X
): X {
    if (test(value)) throw MissingValue(this.self)
    return block(value)
}

/**
 * TODO
 */
internal inline fun <Self, D: DataType<D, *>, T, I, O> PrimitiveDataRepresentation<Self, D, T, I>.convertValue(
    value: O,
    fromRepresentation : DataRepresentation<*, D, O>
): T where Self: DataRepresentation<Self, D, T>, Self: PrimitiveDataRepresentation<Self, D, T, I> {
    val primRepr = assertType<PrimitiveDataRepresentation<*, D, O, I>>(fromRepresentation)
    return convertOut(primRepr.convertIn(value))
}
