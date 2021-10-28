package māia.ml.dataset.primitive

import kotlinx.benchmark.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * TODO
 */
@Warmup(iterations = 0)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
open class SortBenchmark {

    @State(Scope.Benchmark)
    open class Params {
        @Param(1_000.toString(), 10_000.toString())
        var NUM_COLS : Int = 1_000_000

        @Param
        var containerType : Container.Type = Container.Type.ArrayList

        lateinit var toSort: DoubleArray

        @Setup
        fun setup() {
            toSort = Random(42).let {
                val arr = DoubleArray(NUM_COLS) { it.toDouble() }
                arr.shuffle(it)
                arr
            }
        }
    }

    @Benchmark
    fun benchmark(params: Params) {
        val container = createContainer(params.containerType, params.NUM_COLS)
        params.toSort.forEachIndexed(container::append)
        val NUM_COLS = params.NUM_COLS
        repeat(NUM_COLS - 1) { headIndex ->
            var minVal = container.get(headIndex)
            val headVal = minVal
            var minIndex = headIndex
            for (nextIndex in headIndex + 1 until NUM_COLS) {
                val nextVal = container.get(nextIndex)
                if (nextVal < minVal) {
                    minVal = nextVal
                    minIndex = nextIndex
                }
            }
            if (headIndex != minIndex) {
                container.set(headIndex, minVal)
                container.set(minIndex, headVal)
            }
        }
        repeat(NUM_COLS) {
            val expectedValue = it.toDouble()
            val actualValue = container.get(it)
            if (actualValue != expectedValue)
                throw Exception("Container not sorted (item $it = $actualValue)")
        }
    }

}
