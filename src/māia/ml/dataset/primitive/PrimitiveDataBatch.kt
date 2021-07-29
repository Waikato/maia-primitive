package māia.ml.dataset.primitive

import māia.ml.dataset.DataColumn
import māia.ml.dataset.DataColumnHeader
import māia.ml.dataset.DataRow
import māia.ml.dataset.mutable.MutableDataBatch
import māia.ml.dataset.mutable.MutableDataMetadata
import māia.ml.dataset.mutable.WithMutableColumnStructure
import māia.ml.dataset.mutable.WithMutableMetadata
import māia.ml.dataset.mutable.WithMutableRowStructure
import māia.ml.dataset.util.clone
import māia.ml.dataset.util.mustBeEquivalentTo
import māia.ml.dataset.util.mustHaveEquivalentColumnStructureTo
import māia.ml.dataset.util.mustHaveEquivalentRowStructureTo
import māia.ml.dataset.view.MutableDataBatchColumnView
import māia.ml.dataset.view.MutableDataBatchRowView
import māia.ml.dataset.view.MutableDataRowView
import māia.util.collect
import māia.util.zip


class PrimitiveDataBatch(
        name : String = ""
) : MutableDataBatch<MutableDataRowView, MutableDataBatchColumnView>,
        WithMutableMetadata,
        WithMutableRowStructure<DataRow, MutableDataRowView>,
        WithMutableColumnStructure<DataColumn, MutableDataBatchColumnView> {

    private val data = ArrayList<PrimitiveDataRow>()

    private val headers = ArrayList<PrimitiveDataColumnHeader>()

    override val metadata : MutableDataMetadata = PrimitiveDataMetadata(name)

    override val numColumns : Int
        get() = headers.size

    override val numRows : Int
        get() = data.size

    override fun setValue(rowIndex : Int, columnIndex : Int, value : Any?) {
        data[rowIndex].setColumn(columnIndex, value)
    }

    override fun getValue(rowIndex : Int, columnIndex : Int) : Any? {
        return data[rowIndex].getColumn(columnIndex)
    }

    override fun getColumnHeader(columnIndex : Int) : DataColumnHeader {
        return headers[columnIndex]
    }

    override fun getColumn(columnIndex : Int) : MutableDataBatchColumnView {
        return MutableDataBatchColumnView(this, columnIndex)
    }

    override fun getRow(rowIndex : Int) : MutableDataRowView {
        return MutableDataRowView(data[rowIndex])
    }

    override fun setRow(rowIndex : Int, value : DataRow) {
        val view = MutableDataBatchRowView(this, rowIndex)
        view.clone(value)
    }

    override fun setColumn(columnIndex : Int, column : DataColumn) {
        val view = MutableDataBatchColumnView(this, columnIndex)
        view.clone(column)
    }

    override fun insertRow(rowIndex : Int, value : DataRow) {
        this mustHaveEquivalentColumnStructureTo value
        data.add(rowIndex, PrimitiveDataRow(headers, value.iterateColumns().collect(ArrayList())))
    }

    override fun deleteRow(rowIndex : Int) {
        data.removeAt(rowIndex)
    }

    override fun deleteColumn(columnIndex : Int) {
        headers.removeAt(columnIndex)
        for (row in data) row.values.removeAt(columnIndex)
    }

    override fun insertColumn(columnIndex : Int, header : DataColumnHeader, column : DataColumn) {
        header mustBeEquivalentTo column.header
        column mustHaveEquivalentRowStructureTo this
        headers.add(columnIndex, header.toPrimitive())
        for ((row, value) in zip(data.iterator(), column.rowIterator())) row.values.add(columnIndex, value)
    }

    override fun changeColumn(columnIndex : Int, header : DataColumnHeader, column : DataColumn) {
        header mustBeEquivalentTo column.header
        column mustHaveEquivalentRowStructureTo this
        headers[columnIndex] = header.toPrimitive()
        for ((row, value) in zip(data.iterator(), column.rowIterator())) row.values[columnIndex] = value
    }

}
