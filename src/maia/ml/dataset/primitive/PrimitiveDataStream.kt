package maia.ml.dataset.primitive

import maia.util.map
import maia.ml.dataset.DataStream
import maia.ml.dataset.mutable.WithMutableMetadata

/**
 * TODO: What class does.
 *
 * TODO: Reinstate.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 *
class PrimitiveDataStream(
        headers : List<DataColumnHeader>,
        private val source : Iterator<List<Any?>>
) : DataStream<PrimitiveDataRow>, WithMutableMetadata {

    private val headers = headers.map { it.toPrimitive() }

    override val metadata = PrimitiveDataMetadata("")

    override val numColumns : Int = headers.size

    override fun getColumnHeader(columnIndex : Int) : DataColumnHeader {
        return headers[columnIndex]
    }

    override fun rowIterator() : Iterator<PrimitiveDataRow> {
        return source.map {
            PrimitiveDataRow(headers, it)
        }
    }
}
*/
