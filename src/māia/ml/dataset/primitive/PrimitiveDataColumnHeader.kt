package māia.ml.dataset.primitive

import māia.ml.dataset.DataColumnHeader
import māia.ml.dataset.type.DataType

/**
 * Primitive implementation of the data-column header interface.
 *
 * @param name      The name of the column.
 * @param type      The type of data in the column.
 * @param isTarget  Whether the data in the column is meant as the learning
 *                  target for the data-set's context.
 */
class PrimitiveDataColumnHeader(
        override val name: String,
        override val type: DataType<*, *>,
        override val isTarget: Boolean
) : DataColumnHeader
