package maia.ml.dataset.primitive

import maia.ml.dataset.headers.header.DataColumnHeader
import maia.ml.dataset.mutable.MutableDataColumn
import maia.ml.dataset.mutable.WithMutableRowStructure
import maia.ml.dataset.primitive.type.PrimitiveDataRepresentation
import maia.ml.dataset.type.DataRepresentation

/**
 * TODO
 */
class PrimitiveDataColumn<I, X> internal constructor(
    private val primitiveDataRepresentation : PrimitiveDataRepresentation<*, *, X, I>,
    private val storage: PrimitiveDataStore<I>
): MutableDataColumn<X>, WithMutableRowStructure<X, X> {

    constructor(
        representation : PrimitiveDataRepresentation<*, *, X, I>,
        supportMissingValues: Boolean = representation.self.dataType.supportsMissingValues,
        initialCapacity: Int = 16
    ): this(
        newPrimitiveRepresentation(representation.self, supportMissingValues) as PrimitiveDataRepresentation<*, *, X, I>,
        initialCapacity
    )

    internal constructor(
        representation : PrimitiveDataRepresentation<*, *, X, I>,
        initialCapacity: Int
    ): this(
        representation,
        newDataStore(representation, representation.clearSentinel(), initialCapacity)
    )

    private val dataRepresentation = primitiveDataRepresentation.self as DataRepresentation<*, *, X>

    override val numRows : Int
        get() = storage.size

    override val header : DataColumnHeader
        get() = primitiveDataRepresentation.self.dataType.header

    override fun getRow(rowIndex : Int) : X = ensureRowIndex(rowIndex) {
        storage.getRow(rowIndex, primitiveDataRepresentation::convertOut)
    }

    override fun setRow(rowIndex : Int, value : X) = ensureRowIndex(rowIndex) {
        dataRepresentation.validate(value)
        storage.setRow(rowIndex, primitiveDataRepresentation::convertIn, value)
    }

    override fun setRows(rowIndex : Int, values : Collection<X>) = ensureRowsIndices(rowIndex, values.size) {
        values.forEach { dataRepresentation.validate(it) }
        storage.setRows(rowIndex, values.count(), primitiveDataRepresentation::convertIn, values.iterator()::next)
    }

    override fun clearRow(rowIndex : Int) = ensureRowIndex(rowIndex) {
        storage.clearRow(rowIndex)
    }

    override fun clearRows(rowIndex : Int, count : Int) = ensureRowsIndices(rowIndex, count) {
        storage.clearRows(rowIndex, count)
    }

    override fun insertRow(rowIndex : Int, value : X) = ensureRowIndex(rowIndex, true) {
        dataRepresentation.validate(value)
        storage.insertRow(rowIndex, primitiveDataRepresentation::convertIn, value)
    }

    override fun insertRows(rowIndex : Int, values : Collection<X>) = ensureRowIndex(rowIndex, true) {
        values.forEach { dataRepresentation.validate(it) }
        storage.insertRows(rowIndex, values.size, primitiveDataRepresentation::convertIn, values.iterator()::next)
    }

    override fun deleteRow(rowIndex : Int) = ensureRowIndex(rowIndex) {
        storage.deleteRow(rowIndex)
    }

    override fun deleteRows(rowIndex : Int, count: Int) = ensureRowsIndices(rowIndex, count) {
        storage.deleteRows(rowIndex, count)
    }
}

