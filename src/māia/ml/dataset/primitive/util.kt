package māia.ml.dataset.primitive

import māia.ml.dataset.DataColumnHeader
import māia.ml.dataset.DataRow
import māia.util.collect
import māia.util.map


/*
 * Utility functions for working with primitive data-sets.
 */

/**
 * Captures a data-column header as a primitive instance.
 *
 * @receiver    The data-column header to capture.
 * @return      The receiving header if it already is primitive, or
 *              a primitive copy if it's not.
 */
fun DataColumnHeader.toPrimitive() : PrimitiveDataColumnHeader {
    if (this is PrimitiveDataColumnHeader) return this
    return PrimitiveDataColumnHeader(name, type, isTarget)
}

/**
 * Creates a primitive data-row that is a copy of the receiver.
 *
 * @receiver    The data-row to copy.
 * @return      The primitive data-row copy.
 */
fun DataRow.primitiveCopy() : PrimitiveDataRow {
    return PrimitiveDataRow(
            iterateColumnHeaders().map { it.toPrimitive() }.collect(ArrayList()),
            iterateColumns().collect(ArrayList())
    )
}
