package māia.ml.dataset.primitive

import māia.ml.dataset.DataColumnHeader
import māia.ml.dataset.error.DifferentColumnStructure
import māia.ml.dataset.mutable.MutableDataRow
import māia.ml.dataset.mutable.WithMutableColumnStructure
import māia.ml.dataset.util.convertToInternalUnchecked
import māia.ml.dataset.util.isValidInternalUnchecked
import māia.util.collect
import māia.util.map
import māia.util.zip

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class PrimitiveDataRow internal constructor(
    internal val headers: ArrayList<PrimitiveDataColumnHeader>,
    internal val values : ArrayList<Any?>
) : MutableDataRow, WithMutableColumnStructure<Any?, Any?> {

    init {
        // Make sure the number of headers matches the number of columns
        if (headers.size != values.size) throw DifferentColumnStructure()
    }

    constructor(headers : List<DataColumnHeader>, values : List<Any?>) : this(
            headers
                    .iterator()
                    .map { it.toPrimitive() }
                    .collect(ArrayList()),
            values
                    .iterator()
                    .collect(ArrayList())
    ) {
        // Make sure the values are valid for the data-types of the columns
        zip(headers, values)
                .forEach { (header, value) ->
                    if (!header.type.isValidInternalUnchecked(value))
                        throw IllegalArgumentException()
                }
    }

    companion object {

        fun fromExternal(headers : List<DataColumnHeader>, values : List<Any?>) : PrimitiveDataRow {
            return PrimitiveDataRow(
                    headers.iterator().map { it.toPrimitive() }.collect(ArrayList()),
                    zip(headers, values).map { (header, value) ->
                        header.type.convertToInternalUnchecked(value)
                    }.collect(ArrayList())
            )
        }

    }

    override val numColumns : Int
        get() = values.size

    override fun getColumnHeader(columnIndex : Int) : DataColumnHeader {
        return headers[columnIndex]
    }

    override fun getColumn(columnIndex : Int) : Any? {
        return values[columnIndex]
    }

    override fun setColumn(columnIndex : Int, column : Any?) {
        if (!headers[columnIndex].type.isValidInternalUnchecked(column))
            throw IllegalArgumentException()
        values[columnIndex] = column
    }

    override fun deleteColumn(columnIndex : Int) {
        headers.removeAt(columnIndex)
        values.removeAt(columnIndex)
    }

    override fun insertColumn(columnIndex : Int, header : DataColumnHeader, column : Any?) {
        if (!header.type.isValidInternalUnchecked(column))
            throw IllegalArgumentException()
        values.add(columnIndex, column)
        headers.add(columnIndex, header.toPrimitive())
    }

    override fun changeColumn(columnIndex : Int, header : DataColumnHeader, column : Any?) {
        if (!header.type.isValidInternalUnchecked(column))
            throw IllegalArgumentException()
        values[columnIndex] = column
        headers[columnIndex] = header.toPrimitive()
    }

}
