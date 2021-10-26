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
open class FillBenchmark {

    @State(Scope.Benchmark)
    open class Params {

        @Param("1000", "10000")
        var NUM_COLS: Int = 1_000_000

        @Param
        var containerType: Container.Type = Container.Type.ArrayList

    }


    @Benchmark
    fun benchmark(state: Params): Container {
        val container = createContainer(state.containerType, 1)
        val NUM_COLS = state.NUM_COLS
        val rand = Random(42)
        inlineRangeForLoop(NUM_COLS) {
            container.append(it, rand.nextDouble())
        }
        if (container.size != NUM_COLS)
            throw Exception("Expected $NUM_COLS items but got ${container.size}")
        return container
    }

    @Benchmark
    fun bench2(state: Params) {
        val container = benchmark(state)
        val NUM_COLS = state.NUM_COLS
        val rand = Random(34)
        inlineRangeForLoop(NUM_COLS) {
            container.set(it, rand.nextDouble())
        }
    }

}
