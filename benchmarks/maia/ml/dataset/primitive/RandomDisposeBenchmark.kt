package maia.ml.dataset.primitive

import kotlinx.benchmark.*
import maia.ml.dataset.primitive.type.PrimitiveNumeric
import maia.ml.dataset.type.standard.Numeric
import maia.ml.dataset.util.EmptyRow
import maia.ml.dataset.util.appendColumn
import maia.ml.dataset.util.appendRow
import maia.util.assertType
import maia.util.eval
import maia.util.nextDoubleArray
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
@Warmup(iterations = 0)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
open class RandomDisposeBenchmark {

    enum class ContainerType {
        ArrayListOfArrayLists,
        PrimitiveDataBatch
    }

    enum class RemoveBy {
        Rows,
        Columns
    }

    @State(Scope.Benchmark)
    open class Params {

        @Param("100", "1000")
        var numRows: Int = 0

        @Param("100", "1000")
        var numCols: Int = 0

        @Param
        var containerType: ContainerType = ContainerType.ArrayListOfArrayLists

        @Param
        var removeBy: RemoveBy = RemoveBy.Rows

        val rand get() = Random(42)

        lateinit var array: Array<DoubleArray>

        var sum = 0.0
            private set

        @Setup
        fun createArray() {
            array = Array(numCols) { rand.nextDoubleArray(numRows) }
            sum = arrayListBenchmark()
        }

        inline fun <T> calcSum(
            container: T,
            crossinline get: T.(Int, Int) -> Double,
            crossinline popRow: T.(Int) -> Unit,
            crossinline popCol: T.(Int) -> Unit,
            crossinline numRows: T.() -> Int,
            crossinline numCols: T.() -> Int
        ): Double {
            return when (removeBy) {
                RemoveBy.Rows -> removeByRows(container, get, numRows, popRow)
                RemoveBy.Columns -> removeByCols(container, get, numCols, popCol)
            }
        }

        inline fun <T> removeByRows(
            container: T,
            get: T.(Int, Int) -> Double,
            numRows: T.() -> Int,
            popRow: T.(Int) -> Unit
        ): Double {
            var sum = 0.0
            val rand = this.rand
            while (container.numRows() > 0) {
                val rowIndex = rand.nextInt(container.numRows())
                val colOrder = IntArray(numCols) { it }
                colOrder.shuffle(rand)
                colOrder.forEach { colIndex ->
                    sum += container.get(colIndex, rowIndex)
                }
                container.popRow(rowIndex)
            }
            return sum
        }

        inline fun <T> removeByCols(
            container: T,
            get: T.(Int, Int) -> Double,
            numCols: T.() -> Int,
            popCol: T.(Int) -> Unit
        ): Double {
            var sum = 0.0
            val rand = this.rand
            while (container.numCols() > 0) {
                val colIndex = rand.nextInt(container.numCols())
                val rowOrder = IntArray(numRows) { it }
                rowOrder.shuffle(rand)
                rowOrder.forEach { rowIndex ->
                    sum += container.get(colIndex, rowIndex)
                }
                container.popCol(colIndex)
            }
            return sum
        }

        fun arrayListBenchmark(): Double = runBenchmark(
            arrayListOf(*Array(numCols) { arrayListOf(*Array(numRows) { 0.0 }) } ),
            { colIndex, rowIndex, value -> this[colIndex][rowIndex] = value },
            { colIndex, rowIndex -> this[colIndex][rowIndex] },
            { rowIndex -> forEach { column -> column.removeAt(rowIndex) } },
            { colIndex -> removeAt(colIndex) },
            { this[0].size },
            { this.size }
        )

        fun dataBatchBenchmark() = runBenchmark(
            eval {
                val db = PrimitiveDataBatch("", numCols, numRows)
                repeat(numRows) {
                    db.appendRow(EmptyRow)
                }
                val rep = PrimitiveNumeric(false).canonicalRepresentation
                repeat(numCols) {
                    db.appendColumn("$it", rep, false) { 0.0 }
                }
                db
            },
            { colIndex, rowIndex, value ->
                val type = assertType<Numeric<*, *>>(headers[colIndex].type)
                val rep = type.canonicalRepresentation
                setValue(rep, rowIndex, value)
            },
            { colIndex, rowIndex ->
                val type = assertType<Numeric<*, *>>(headers[colIndex].type)
                val rep = type.canonicalRepresentation
                getValue(rep, rowIndex)
            },
            { rowIndex -> deleteRow(rowIndex) },
            { colIndex -> deleteColumn(colIndex) },
            { numRows },
            { numColumns }
        )

        inline fun <T> runBenchmark(
            container: T,
            crossinline set: T.(Int, Int, Double) -> Unit,
            crossinline get: T.(Int, Int) -> Double,
            crossinline popRow: T.(Int) -> Unit,
            crossinline popCol: T.(Int) -> Unit,
            crossinline numRows: T.() -> Int,
            crossinline numCols: T.() -> Int
        ): Double {
            // Copy source values into container
            array.forEachIndexed { columnIndex, column ->
                column.forEachIndexed { rowIndex, value ->
                    container.set(columnIndex, rowIndex, value)
                }
            }

            val sum = calcSum(container, get, popRow, popCol, numRows, numCols)

            return sum
        }

    }

    @Benchmark
    fun benchmark(params: Params) {
        val sum = when (params.containerType) {
            ContainerType.ArrayListOfArrayLists -> params.arrayListBenchmark()
            ContainerType.PrimitiveDataBatch -> params.dataBatchBenchmark()
        }

        if (sum != params.sum)
            throw Exception("Expected sum ${params.sum}, got $sum")
    }
}
