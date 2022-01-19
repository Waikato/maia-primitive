package maia.ml.dataset.primitive.type

import maia.ml.dataset.type.DataRepresentation
import maia.ml.dataset.type.DataType
import maia.util.assertType

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
