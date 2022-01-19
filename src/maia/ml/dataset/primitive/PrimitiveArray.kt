package maia.ml.dataset.primitive

/**
 * TODO
 */
sealed interface PrimitiveArray<T> {
    val base: Any
    val size: Int
    operator fun get(index: Int): T
    operator fun set(index: Int, value: T)
    operator fun iterator(): Iterator<T>
    fun copy(newSize: Int): PrimitiveArray<T>
}

/**
 * TODO
 */
@JvmInline
value class PrimitiveIntArray(
    override val base: IntArray
): PrimitiveArray<Int> {
    override val size : Int get() = base.size
    override fun get(index : Int) : Int = base[index]
    override fun set(index : Int, value : Int) { base[index] = value }
    override fun iterator() : IntIterator = base.iterator()
    override fun copy(newSize: Int): PrimitiveArray<Int> = PrimitiveIntArray(base.copyOf(newSize))
}

/**
 * TODO
 */
@JvmInline
value class PrimitiveLongArray(
    override val base: LongArray
): PrimitiveArray<Long> {
    override val size : Int get() = base.size
    override fun get(index : Int) : Long = base[index]
    override fun set(index : Int, value : Long) { base[index] = value }
    override fun iterator() : LongIterator = base.iterator()
    override fun copy(newSize: Int): PrimitiveArray<Long> = PrimitiveLongArray(base.copyOf(newSize))
}

/**
 * TODO
 */
@JvmInline
value class PrimitiveDoubleArray(
    override val base: DoubleArray
): PrimitiveArray<Double> {
    override val size : Int get() = base.size
    override fun get(index : Int) : Double = base[index]
    override fun set(index : Int, value : Double) { base[index] = value }
    override fun iterator() : DoubleIterator = base.iterator()
    override fun copy(newSize: Int): PrimitiveArray<Double> = PrimitiveDoubleArray(base.copyOf(newSize))
}

/**
 * TODO
 */
@JvmInline
value class PrimitiveObjectArray(
    override val base: Array<Any?>
): PrimitiveArray<Any?> {
    override val size : Int get() = base.size
    override fun get(index : Int) : Any? = base[index]
    override fun set(index : Int, value : Any?) { base[index] = value }
    override fun iterator() : Iterator<Any?> = base.iterator()
    override fun copy(newSize: Int): PrimitiveArray<Any?> = PrimitiveObjectArray(base.copyOf(newSize))
}
