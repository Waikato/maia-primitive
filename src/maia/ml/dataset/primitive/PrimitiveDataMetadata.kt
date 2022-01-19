package maia.ml.dataset.primitive

import maia.ml.dataset.mutable.MutableDataMetadata

/**
 * Primitive implementation of the data-set meta-data interface.
 *
 * @param name  The name to give the data-set.
 */
class PrimitiveDataMetadata(override var name: String) : MutableDataMetadata
