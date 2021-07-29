package māia.ml.dataset.primitive

import māia.util.map
import māia.ml.dataset.DataColumnHeader
import māia.ml.dataset.DataStream
import māia.ml.dataset.mutable.WithMutableMetadata

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
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
