package maia.ml.dataset.primitive

import maia.ml.dataset.primitive.type.PrimitiveNumeric
import maia.ml.dataset.type.standard.Numeric
import maia.ml.dataset.util.appendColumn
import maia.ml.dataset.util.appendRow

/**
 * TODO
 */
sealed interface Container {
    fun append(index: Int, value: Double)
    fun get(index: Int): Double
    fun set(index: Int, value: Double)
    val size: Int

    enum class Type {
        ArrayList,
        DataColumn
    }
}

/**
 * TODO
 */
fun createContainer(
    type: Container.Type,
    size: Int
): Container = when (type) {
    Container.Type.ArrayList -> ArrayList(ArrayList(size))
    Container.Type.DataColumn -> DataColumn(PrimitiveDataColumn(PrimitiveNumeric(false).canonicalRepresentation, initialCapacity = size))
}

/**
 * TODO
 */
@JvmInline
value class ArrayList(val arr: kotlin.collections.ArrayList<Double>): Container {
    override val size : Int
        get() = arr.size

    override fun append(index : Int, value : Double) {
        arr.add(value)
    }

    override fun get(index : Int) : Double {
        return arr[index]
    }

    override fun set(index : Int, value : Double) {
        arr[index] = value
    }
}

/**
 * TODO
 */
@JvmInline
value class DataColumn(val arr: PrimitiveDataColumn<*, Double>): Container {
    override val size : Int
        get() = arr.numRows

    override fun append(index : Int, value : Double) {
        arr.appendRow(value)
    }

    override fun get(index : Int) : Double {
        return arr.getRow(index)
    }

    override fun set(index : Int, value : Double) {
        arr.setRow(index, value)
    }
}
