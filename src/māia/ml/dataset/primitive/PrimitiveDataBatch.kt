package māia.ml.dataset.primitive

import māia.ml.dataset.DataColumn
import māia.ml.dataset.DataRow
import māia.ml.dataset.error.MissingValue
import māia.ml.dataset.error.checkMissingValueSupport
import māia.ml.dataset.headers.header.DataColumnHeader
import māia.ml.dataset.headers.MutableDataColumnHeaders
import māia.ml.dataset.headers.ensureOwnership
import māia.ml.dataset.mutable.MutableColumnStructureDataBatch
import māia.ml.dataset.mutable.MutableDataColumn
import māia.ml.dataset.mutable.MutableDataMetadata
import māia.ml.dataset.mutable.MutableDataRow
import māia.ml.dataset.mutable.WithMutableMetadata
import māia.ml.dataset.mutable.WithMutableRowStructure
import māia.ml.dataset.primitive.error.UnsupportedDataTypeError
import māia.ml.dataset.primitive.type.PrimitiveDataRepresentation
import māia.ml.dataset.type.DataRepresentation
import māia.ml.dataset.type.DataType
import māia.ml.dataset.type.standard.Nominal
import māia.ml.dataset.type.standard.Numeric
import māia.ml.dataset.type.standard.UntypedData
import māia.ml.dataset.util.mustHaveEquivalentColumnStructureTo
import māia.util.discard
import māia.util.identity
import kotlin.math.max

/**
 * TODO
 */
class PrimitiveDataBatch(
        name : String = "",
        initialColumnCapacity: Int? = null,
        private val initialRowCapacity: Int = 16
) : MutableColumnStructureDataBatch<PrimitiveDataBatch.RowView>,
        WithMutableMetadata,
        WithMutableRowStructure<DataRow, PrimitiveDataBatch.RowView> {

    inner class RowView(private var rowIndex: Int): MutableDataRow {

        override val headers
            get() = this@PrimitiveDataBatch.headers

        override fun <T> getValue(representation : DataRepresentation<*, *, out T>) : T =
            getValue(representation, rowIndex)

        override fun <T> setValue(representation : DataRepresentation<*, *, in T>, value : T) =
            setValue(representation, rowIndex, value)

        override fun clearValue(columnIndex : Int) =
            clearValue(columnIndex, rowIndex)
    }


    private val headersInternal: MutableDataColumnHeaders = MutableDataColumnHeaders(initialColumnCapacity)
    private val data: ArrayList<PrimitiveDataStore<*>> = initialColumnCapacity?.let { ArrayList() } ?: ArrayList()

    override val headers = headersInternal.readOnlyView

    override val metadata : MutableDataMetadata = PrimitiveDataMetadata(name)

    override var numRows : Int = 0
        private set

    override fun <T> getValue(
        representation : DataRepresentation<*, *, out T>,
        rowIndex : Int
    ) : T = ensureRowIndex(rowIndex) {
        ensureOwnership(representation) {
            it.getRow(rowIndex, this::convertOut)
        }
    }

    override fun <T> setValue(
        representation : DataRepresentation<*, *, in T>,
        rowIndex : Int,
        value : T
    ) = ensureRowIndex(rowIndex) {
        ensureOwnership(representation) {
            representation.validate(value)
            it.setRow(rowIndex, this::convertIn, value)
        }
    }

    override fun <T> setValues(
        representation : DataRepresentation<*, *, in T>,
        rowIndex : Int,
        values : Collection<T>
    ) = ensureRowsIndices(rowIndex, values.size) {
        ensureOwnership(representation) {
            values.forEach { representation.validate(it) }
            it.setRows(rowIndex, values.size, ::convertIn, values.iterator()::next)
        }
    }

    override fun clearValue(
        columnIndex : Int,
        rowIndex : Int
    ) = ensureRowIndex(rowIndex) {
        val type = headersInternal[columnIndex].type
        type.checkMissingValueSupport()
        data[columnIndex].clearRow(rowIndex)
    }

    override fun clearValues(
        columnIndex : Int,
        rowIndex : Int,
        count : Int
    ) = ensureRowsIndices(rowIndex, count) {
        val type = headersInternal[columnIndex].type
        type.checkMissingValueSupport()
        data[columnIndex].clearRows(rowIndex, count)
    }

    override fun getRow(rowIndex : Int) : RowView {
        return RowView(rowIndex)
    }

    override fun setRow(rowIndex : Int, value : DataRow) = ensureRowIndex(rowIndex) {
        value mustHaveEquivalentColumnStructureTo headersInternal
        setRowInternal(rowIndex, value)
    }

    override fun setRows(rowIndex : Int, values : Collection<DataRow>) {
        values.forEach { it mustHaveEquivalentColumnStructureTo headersInternal }

        val valueIterator = values.iterator()
        repeat(values.size) { valueIndex ->
            setRowInternal(rowIndex + valueIndex, valueIterator.next())
        }
    }

    private inline fun setRowInternal(rowIndex : Int, value : DataRow) {
        headersInternal.forEachIndexed { columnIndex, header ->
            val type = header.type
            when (type) {
                is Nominal<*, *, *, *> -> (data[columnIndex] as PrimitiveDataStore<Int>).setRow(
                    rowIndex,
                    ::identity,
                    value.getValue(type.indexRepresentation)
                )
                is Numeric<*, *> -> (data[columnIndex] as PrimitiveDataStore<Double>).setRow(
                    rowIndex,
                    ::identity,
                    value.getValue(type.canonicalRepresentation)
                )
                is UntypedData<*, *> -> (data[columnIndex] as PrimitiveDataStore<Any?>).setRow(
                    rowIndex,
                    ::identity,
                    value.getValue(type.canonicalRepresentation)
                )
                else -> throw UnsupportedDataTypeError(type)
            }
        }
    }

    override fun clearRow(rowIndex : Int) = ensureRowIndex(rowIndex) {
        headersInternal.forEach { header: DataColumnHeader -> header.checkMissingValueSupport() }
        data.forEach { it.clearRow(rowIndex) }
    }

    override fun clearRows(rowIndex : Int, count : Int) = ensureRowsIndices(rowIndex, count) {
        headersInternal.forEach { header: DataColumnHeader -> header.checkMissingValueSupport() }
        data.forEach { it.clearRows(rowIndex, count) }
    }

    override fun insertRow(rowIndex : Int, value : DataRow) = ensureRowIndex(rowIndex, true) {
        value mustHaveEquivalentColumnStructureTo headersInternal
        insertRowInternal(rowIndex, value)
        discard { numRows++ }
    }

    override fun insertRows(rowIndex : Int, values : Collection<DataRow>) = ensureRowIndex(rowIndex, true) {
        values.forEach { it mustHaveEquivalentColumnStructureTo headersInternal }
        // Insert empty values into the backing columns so we only need to iterate
        // through the given collection once
        data.forEach {
            it.insertEmptyRows(rowIndex, values.size)
        }

        // Because storage has already been allocated above, use setRowsInternal instead
        // of insertRowsInternal
        values.forEachIndexed { valueIndex, row ->
            setRowInternal(rowIndex + valueIndex, row)
        }

        numRows += values.size
    }

    private inline fun insertRowInternal(rowIndex : Int, value : DataRow) {
        headersInternal.forEachIndexed { columnIndex, header ->
            val type = header.type
            when (type) {
                is Nominal<*, *, *, *> -> (data[columnIndex] as PrimitiveDataStore<Int>).insertRow(
                    rowIndex,
                    ::identity,
                    value.getValue(type.indexRepresentation)
                )
                is Numeric<*, *> -> (data[columnIndex] as PrimitiveDataStore<Double>).insertRow(
                    rowIndex,
                    ::identity,
                    value.getValue(type.canonicalRepresentation)
                )
                is UntypedData<*, *> -> (data[columnIndex] as PrimitiveDataStore<Any?>).insertRow(
                    rowIndex,
                    ::identity,
                    value.getValue(type.canonicalRepresentation)
                )
                else -> throw UnsupportedDataTypeError(type)
            }
        }
    }

    override fun deleteRow(rowIndex : Int) = ensureRowIndex(rowIndex) {
        data.forEach { it.deleteRow(rowIndex) }
        discard { numRows-- }
    }

    override fun deleteRows(rowIndex : Int, count : Int) = ensureRowsIndices(rowIndex, count) {
        data.forEach { it.deleteRows(rowIndex, count) }
        numRows -= count
    }

    override fun <T> getColumn(
        representation : DataRepresentation<*, *, T>
    ) : DataColumn<T> = ensureOwnership(representation) {
        val column = PrimitiveDataColumn(this, it)
        return object : MutableDataColumn<T> by column {}
    }

    override fun <T> setColumn(
        representation : DataRepresentation<*, *, in T>,
        column : Collection<T>
    ) = ensureOwnership(representation) { storage ->
        if (column.size != numRows)
            throw IllegalArgumentException("Inserted column does not contained the required number of values ($numRows)")
        column.forEach { representation.validate(it) }
        storage.setRows(0, numRows, ::convertIn, column.iterator()::next)
    }

    override fun clearColumn(columnIndex : Int) {
        headersInternal[columnIndex].checkMissingValueSupport()
        data[columnIndex].clearRows(0, numRows)
    }

    override fun clearColumns() {
        headersInternal.forEach { header: DataColumnHeader -> header.checkMissingValueSupport() }
        data.forEach { it.clearRows(0, numRows) }
    }

    override fun deleteColumn(
        columnIndex : Int,
        columnName : String?
    ) : String {
        return headersInternal.delete(columnIndex, columnName).also {
            data.removeAt(columnIndex)
        }
    }

    override fun deleteColumn(columnName : String, columnIndex : Int?) : Int {
        return headersInternal.delete(columnName, columnIndex).also {
            data.removeAt(it)
        }
    }

    override fun <T> insertColumn(
        index : Int,
        name : String,
        representation : DataRepresentation<*, *, in T>,
        isTarget : Boolean,
        supportsMissingValues : Boolean,
        data : (Int) -> T
    ) {
        headersInternal.checkInsert(index, name)
        withNewColumn(representation, supportsMissingValues, data) { newRepresentation, storage ->
            headersInternal.insert(index, name, newRepresentation.self.dataType, isTarget)
            this.data.add(index, storage)
        }
    }

    override fun insertColumn(
        index : Int,
        name : String,
        type : DataType<*, *>,
        isTarget : Boolean
    ) {
        insertColumn(
            index,
            name,
            type.canonicalRepresentation,
            isTarget,
            true
        ) { throw MissingValue() }
    }

    override fun <T> changeColumn(
        index : Int,
        name : String,
        representation : DataRepresentation<*, *, in T>,
        isTarget : Boolean,
        supportsMissingValues : Boolean,
        data : (Int) -> T
    ) {
        headersInternal.checkSet(index, name)
        withNewColumn(representation, supportsMissingValues, data) { newRepresentation, storage ->
            headersInternal.set(index, name, newRepresentation.self.dataType, isTarget)
            this.data.add(index, storage)
        }
    }

    override fun changeColumn(
        index : Int,
        name : String,
        type : DataType<*, *>,
        isTarget : Boolean
    ) {
        changeColumn(
            index,
            name,
            type.canonicalRepresentation,
            isTarget,
            true
        ) { throw MissingValue() }
    }

    private inline fun <T, R> ensureOwnership(
        representation : DataRepresentation<*, *, T>,
        block: PrimitiveDataRepresentation<*, *, T, Any?>.(PrimitiveDataStore<Any?>) -> R
    ): R = headersInternal.ensureOwnership(representation) {
        return (representation as PrimitiveDataRepresentation<*, *, T, Any?>).block(
            data[representation.columnIndex] as PrimitiveDataStore<Any?>
        )
    }

    private inline fun <T, R> withNewColumn(
        representation : DataRepresentation<*, *, in T>,
        supportsMissingValues : Boolean,
        crossinline data : (Int) -> T,
        block: (PrimitiveDataRepresentation<*, *, in T, Any?>, PrimitiveDataStore<Any?>) -> R
    ): R {
        val newPrimitiveRepresentation = newPrimitiveRepresentation(
            representation,
            supportsMissingValues
        ) as PrimitiveDataRepresentation<*, *, in T, Any?>

        val clearSentinel = newPrimitiveRepresentation.clearSentinel()

        val storage = newDataStore(newPrimitiveRepresentation, clearSentinel, max(initialRowCapacity, numRows), numRows) { rowIndex ->
            if (rowIndex < numRows)
                handleMissingOnSet(newPrimitiveRepresentation::convertIn, clearSentinel) {
                    data(rowIndex)
                }
            else
                clearSentinel
        }

        return block(newPrimitiveRepresentation, storage)
    }
}
