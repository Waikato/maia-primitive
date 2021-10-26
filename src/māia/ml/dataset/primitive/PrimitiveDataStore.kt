package māia.ml.dataset.primitive

import māia.util.identity
import māia.util.inlineRangeForLoop
import kotlin.math.max

/**
 * TODO
 */
open class PrimitiveDataStore<I> internal constructor(
    private var storage: PrimitiveArray<I>,
    private val clearSentinel: I,
    initialSize: Int = 0
) {

    internal var size : Int = initialSize
        private set

    internal inline fun <X> getRow(
        rowIndex : Int,
        convert: (I) -> X
    ) : X {
        return convert(storage[rowIndex])
    }

    internal inline fun <X> setRow(
        rowIndex : Int,
        convert: (X) -> I,
        value : X
    ) {
        storage[rowIndex] = convert(value)
    }

    internal inline fun <X> setRows(
        rowIndex : Int,
        count: Int,
        convert: (X) -> I,
        data: () -> X
    ) {
        inlineRangeForLoop(rowIndex, rowIndex + count) {
            storage[it] = convert(data())
        }
    }

    internal inline fun clearRow(rowIndex : Int) {
        storage[rowIndex] = clearSentinel
    }

    internal inline fun clearRows(rowIndex : Int, count : Int) {
        inlineRangeForLoop(rowIndex, count) {
            storage[it] = clearSentinel
        }
    }

    internal inline fun <X> insertRow(
        rowIndex : Int,
        convert: (X) -> I,
        value : X
    ) {
        insertRows(rowIndex, 1, convert) { value }
    }

    internal inline fun <X> insertRows(
        rowIndex: Int,
        count: Int,
        convert: (X) -> I,
        data: () -> X
    ) {
        val currentSize = storage.size
        val requiredSize = size + count
        val target = if (currentSize >= requiredSize)
            storage
        else
            storage.copy(max(requiredSize, currentSize + currentSize / 2))
        val newIndex = rowIndex + count
        System.arraycopy(target.base, rowIndex, target.base, newIndex, size - rowIndex)
        inlineRangeForLoop(rowIndex, newIndex) {
            target[it] = convert(data())
        }
        storage = target
        size += count
    }

    internal inline fun insertEmptyRows(
        rowIndex: Int,
        count: Int
    ) {
        insertRows(rowIndex, count, ::identity) { clearSentinel }
    }

    internal inline fun deleteRow(rowIndex : Int) {
        deleteRows(rowIndex, 1)
    }

    internal inline fun deleteRows(rowIndex: Int, count: Int) {
        val end = rowIndex + count
        System.arraycopy(storage.base, end, storage.base, rowIndex, size - end)
        size -= count
    }

}

