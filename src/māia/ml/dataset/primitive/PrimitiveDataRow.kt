package māia.ml.dataset.primitive

import māia.ml.dataset.error.MissingValue
import māia.ml.dataset.error.checkMissingValueSupport
import māia.ml.dataset.headers.MutableDataColumnHeaders
import māia.ml.dataset.headers.ensureOwnership
import māia.ml.dataset.mutable.MutableColumnStructureDataRow
import māia.ml.dataset.primitive.type.Missing
import māia.ml.dataset.primitive.type.PrimitiveDataRepresentation
import māia.ml.dataset.type.DataRepresentation
import māia.ml.dataset.type.DataType

/**
 * TODO
 */
class PrimitiveDataRow(
    initialCapacity: Int? = null
) : MutableColumnStructureDataRow {

    private val headersInternal: MutableDataColumnHeaders = MutableDataColumnHeaders(initialCapacity)
    private val values : ArrayList<Any?> = initialCapacity?.let { ArrayList(it) } ?: ArrayList()

    override val headers = headersInternal.readOnlyView

    private inline fun <T, R> ensureOwnership(
        representation : DataRepresentation<*, *, T>,
        block: PrimitiveDataRepresentation<*, *, T, Any?>.() -> R
    ): R = headersInternal.ensureOwnership(representation) {
        return (representation as PrimitiveDataRepresentation<*, *, T, Any?>).block()
    }

    override fun <T> getValue(
        representation : DataRepresentation<*, *, out T>
    ) : T  = ensureOwnership(representation) {
        val value = values[representation.columnIndex]
        if (value is Missing) throw MissingValue(representation)
        return convertOut(value)
    }

    override fun <T> setValue(
        representation : DataRepresentation<*, *, in T>,
        value : T
    ) = ensureOwnership(representation) {
        representation.validate(value)
        values[representation.columnIndex] = convertIn(value)
    }

    override fun clearValue(columnIndex : Int) {
        headersInternal[columnIndex].checkMissingValueSupport()
        values[columnIndex] = Missing
    }

    override fun deleteColumn(columnIndex : Int, columnName : String?): String {
        return headersInternal.delete(columnIndex, columnName).also {
            values.removeAt(columnIndex)
        }

    }

    override fun deleteColumn(columnName : String, columnIndex : Int?): Int {
        return headersInternal.delete(columnName, columnIndex).also {
            values.removeAt(it)
        }
    }

    override fun <T> insertColumn(
        index : Int,
        name : String,
        representation : DataRepresentation<*, *, in T>,
        isTarget : Boolean,
        data : T,
        supportsMissingValues : Boolean
    ) {
        val newRepresentation = newPrimitiveRepresentation(representation, supportsMissingValues) as DataRepresentation<*, *, in T>
        newRepresentation.validate(data)
        headersInternal.insert(index, name, newRepresentation.dataType, isTarget)
        values.add(index, convertValueForNewRepresentation(newRepresentation, data))
    }

    override fun insertColumn(
        index : Int,
        name : String,
        type : DataType<*, *>,
        isTarget : Boolean
    ) {
        val newRepresentation = newPrimitiveRepresentation(type.canonicalRepresentation, true) as DataRepresentation<*, *, *>
        headersInternal.insert(index, name, newRepresentation.dataType, isTarget)
        values.add(index, Missing)
    }

    override fun <T> changeColumn(
        index : Int,
        name : String,
        representation: DataRepresentation<*, *, in T>,
        isTarget : Boolean,
        data : T,
        supportsMissingValues: Boolean
    ) {
        val newRepresentation = newPrimitiveRepresentation(representation, supportsMissingValues) as DataRepresentation<*, *, in T>
        newRepresentation.validate(data)
        headersInternal.set(index, name, newRepresentation.dataType, isTarget)
        values[index] = convertValueForNewRepresentation(newRepresentation, data)
    }

    override fun changeColumn(
        index : Int,
        name : String,
        type : DataType<*, *>,
        isTarget : Boolean
    ) {
        val newRepresentation = newPrimitiveRepresentation(type.canonicalRepresentation, true) as DataRepresentation<*, *, *>
        headersInternal.set(index, name, newRepresentation.dataType, isTarget)
        values[index] = Missing
    }

    override fun clearColumns() {
        headersInternal.clear()
        values.clear()
    }

    private inline fun <T> convertValueForNewRepresentation(
        newRepresentation: DataRepresentation<*, *, in T>,
        data: T
    ): Any? {
        return (newRepresentation as PrimitiveDataRepresentation<*, *, in T, out Any?>).convertIn(data)
    }
}
