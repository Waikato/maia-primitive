package māia.ml.dataset.primitive

import māia.ml.dataset.DataColumnHeader
import māia.ml.dataset.mutable.MutableDataColumn
import māia.ml.dataset.mutable.WithMutableRowStructure
import māia.ml.dataset.util.convertToInternalUnchecked
import māia.ml.dataset.util.isValidInternalUnchecked
import māia.util.collect
import māia.util.map

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class PrimitiveDataColumn internal constructor(
        override val header : DataColumnHeader,
        private val values : ArrayList<Any?>
) : MutableDataColumn, WithMutableRowStructure<Any?, Any?> {

    constructor(header : DataColumnHeader, values : Iterator<Any?>) : this(
            header.toPrimitive(),
            values.map { header.type.convertToInternalUnchecked(it) }.collect(ArrayList())
    )

    override val numRows : Int
        get() = values.size

    override fun getRow(rowIndex : Int) : Any? {
        return values[rowIndex]
    }

    override fun rowIterator() : Iterator<Any?> {
        return values.iterator()
    }

    override fun setRow(rowIndex : Int, value : Any?) {
        if (!header.type.isValidInternalUnchecked(value))
            throw IllegalArgumentException()
        values[rowIndex] = value
    }

    override fun insertRow(rowIndex : Int, value : Any?) {
        if (!header.type.isValidInternalUnchecked(value))
            throw IllegalArgumentException()
        values.add(rowIndex, value)
    }

    override fun deleteRow(rowIndex : Int) {
        values.removeAt(rowIndex)
    }

}
