package māia.ml.dataset.primitive

import kotlinx.benchmark.*
import māia.util.inlineRangeForLoop
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * TODO
 */
@Warmup(iterations = 0)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
open class SumBenchmark {

    @State(Scope.Benchmark)
    open class Params {
        @Param(100_000.toString(), 1_000_000.toString())
        var NUM_COLS : Int = 1_000_000

        @Param
        var containerType : Container.Type = Container.Type.ArrayList

        lateinit var values: DoubleArray
        var expectedSum = 0.0

        @Setup
        fun setup() {
            val rand = Random(42)
            expectedSum = 0.0
            values = DoubleArray(NUM_COLS) {
                val nextValue = rand.nextDouble()
                expectedSum += nextValue
                nextValue
            }
        }

    }

    @Benchmark
    fun benchmark(state: Params) {
        val container = createContainer(state.containerType, state.NUM_COLS)
        state.values.forEachIndexed(container::append)
        var sum = 0.0
        inlineRangeForLoop(state.NUM_COLS) {
            sum += container.get(it)
        }
        if (sum != state.expectedSum)
            throw Exception("Expected sum: ${state.expectedSum}, got: $sum")
    }

}
