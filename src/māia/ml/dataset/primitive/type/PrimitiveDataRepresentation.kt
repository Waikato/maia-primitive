package māia.ml.dataset.primitive.type

import māia.ml.dataset.type.DataRepresentation
import māia.ml.dataset.type.DataType
import māia.util.assertType

/**
 * Internal marker interface for identifying data-representations
 * as our own. Provides support for getting/setting values
 */
sealed interface PrimitiveDataRepresentation<Self, D: DataType<D, *>, T, I>
where Self: DataRepresentation<Self, D, T>,
      Self: PrimitiveDataRepresentation<Self, D, T, I>
{
    val self: Self get() = this as Self
    fun convertIn(value: T): I
    fun convertOut(value: I): T
    fun clearSentinel(): I
}
