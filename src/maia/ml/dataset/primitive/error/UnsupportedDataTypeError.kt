package maia.ml.dataset.primitive.error

import maia.ml.dataset.type.DataType

/**
 * Error for when primitive is asked to handle a data-type
 * that is not yet supported.
 *
 * @param dataType
 *          The data-type that isn't supported.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class UnsupportedDataTypeError(
    dataType: DataType<*, *>
): Exception(
    "Primitive datasets don't support the $dataType data-type"
)
