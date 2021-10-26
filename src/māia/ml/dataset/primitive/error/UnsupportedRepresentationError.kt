package māia.ml.dataset.primitive.error

import māia.ml.dataset.type.DataRepresentation

/**
 * TODO
 */
class UnsupportedRepresentationError(
    representation: DataRepresentation<*, *, *>
): Exception(
    "Primitive datasets don't support the $representation representational type"
)
