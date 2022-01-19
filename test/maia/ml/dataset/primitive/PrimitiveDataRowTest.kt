package maia.ml.dataset.primitive

import maia.ml.dataset.error.DoesntSupportMissingValues
import maia.ml.dataset.error.InvalidValue
import maia.ml.dataset.error.MissingValue
import maia.ml.dataset.error.UnownedRepresentationError
import maia.ml.dataset.primitive.type.PrimitiveNumeric
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.type.standard.Numeric
import maia.ml.dataset.type.standard.UntypedData
import maia.ml.dataset.util.appendColumn
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests [PrimitiveDataRow].
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
internal class PrimitiveDataRowTest {

    private val testRow = PrimitiveDataRow()

    init {
        testRow.appendColumn(
            "f1",
            PrimitiveNumeric(false).canonicalRepresentation,
            false,
            1.5
        )
        testRow.appendColumn(
            "f2",
            PrimitiveNumeric(true),
            false
        )
        testRow.appendColumn(
            "class",
            Nominal.PlaceHolder(false, "apple", "banana").indexRepresentation,
            true,
            0
        )
    }

    @Test
    fun appendColumn() {
        testRow.appendColumn(
            "test",
            UntypedData.PlaceHolder(true),
            false
        )

        assertEquals(4, testRow.headers.size)
    }

    @Test
    fun getHeaders() {
    }

    @Test
    fun getValue() {
        assertThrows<UnownedRepresentationError> {
            testRow.getValue(Numeric.PlaceHolder(true).canonicalRepresentation)
        }
        val type = testRow.headers[0].type
        assertIs<Numeric<*, *>>(type)
        val value = testRow.getValue(type.canonicalRepresentation)
        assertEquals(1.5, value)
    }

    @Test
    fun setValue() {
        val type = testRow.headers[1].type
        assertIs<Numeric<*, *>>(type)
        assertDoesNotThrow { testRow.setValue(type.canonicalRepresentation, 1.1) }
        assertThrows<InvalidValue> { testRow.setValue(type.canonicalRepresentation, Double.NaN) }
    }

    @Test
    fun clearValue() {
        assertDoesNotThrow { testRow.clearValue(1) }
        assertThrows<DoesntSupportMissingValues> { testRow.clearValue(0) }
        assertThrows<MissingValue> { testRow.getValue(testRow.headers[1].type.canonicalRepresentation) }
    }

    @Test
    fun deleteColumn() {
        val type = testRow.headers[2].type
        assertIs<Nominal<*, *, *, *>>(type)
        assertDoesNotThrow { testRow.deleteColumn(2, "class") }
        assertEquals(2, testRow.headers.size)
        assertThrows<UnownedRepresentationError> { testRow.getValue(type.canonicalRepresentation) }
    }

    @Test
    fun insertColumn() {
    }

    @Test
    fun changeColumn() {
    }

    @Test
    fun clearColumns() {
        testRow.clearColumns()
        assertEquals(0, testRow.headers.size)
    }
}
