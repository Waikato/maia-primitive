package maia.ml.dataset.primitive

import maia.ml.dataset.primitive.type.PrimitiveNumeric
import maia.ml.dataset.util.appendRow
import maia.ml.dataset.util.appendRows
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests [PrimitiveDataColumn].
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
internal class PrimitiveDataColumnTest {
    val testColumn = PrimitiveDataColumn(
        PrimitiveNumeric(false).canonicalRepresentation
    )

    @Test
    fun appendRows() {
        assertEquals(0, testColumn.numRows)
        assertDoesNotThrow {
            testColumn.appendRow(1.1)
        }
        assertEquals(1, testColumn.numRows)
        assertDoesNotThrow {
            testColumn.appendRows(arrayListOf(1.2, 1.3, 1.4))
        }
        assertEquals(4, testColumn.numRows)
    }

}
