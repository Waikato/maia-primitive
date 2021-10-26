/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * PrimitiveDataBatchTest.kt
 * Copyright (C) 2021 University of Waikato, Hamilton, NZ
 */

package māia.ml.dataset.primitive;

import māia.ml.dataset.primitive.type.PrimitiveNumeric
import māia.ml.dataset.type.standard.Numeric
import māia.ml.dataset.util.EmptyRow
import māia.ml.dataset.util.appendColumn
import māia.ml.dataset.util.appendRow
import māia.util.error.UNREACHABLE_CODE
import māia.util.inlineRangeForLoop
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests [PrimitiveDataBatch].
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
internal class PrimitiveDataBatchTest {

    @Test
    fun testColumnRemoval() {
        val batch = PrimitiveDataBatch("test", 2)
        batch.appendColumn("c1", Numeric.PlaceHolder(false).canonicalRepresentation, false) {
            UNREACHABLE_CODE("No rows yet")
        }
        batch.appendColumn("c2", Numeric.PlaceHolder(false).canonicalRepresentation, false) {
            UNREACHABLE_CODE("No rows yet")
        }

        val row = PrimitiveDataRow(2)
        row.appendColumn("c1", Numeric.PlaceHolder(false).canonicalRepresentation, false, 1.0)
        row.appendColumn("c2", Numeric.PlaceHolder(false).canonicalRepresentation, false, 1.1)

        batch.appendRow(
            row
        )

        val col = batch.getColumn(batch.headers[0].type.canonicalRepresentation)

        batch.deleteColumn(0)

        val value = col.getRow(0)

        assertEquals(1.0, value)
    }

    @Test
    fun testCreateNumeric(): PrimitiveDataBatch {
        val db = PrimitiveDataBatch("", 123, 456)
        inlineRangeForLoop(456) {
            db.appendRow(EmptyRow)
        }
        val rep = Numeric.PlaceHolder(false).canonicalRepresentation
        inlineRangeForLoop(123) { colIndex ->
            db.appendColumn("$colIndex", rep, false) { rowIndex ->
                (456 * colIndex + rowIndex).toDouble()
            }
        }
        assertEquals(123, db.numColumns)
        assertEquals(456, db.numRows)
        inlineRangeForLoop(123) { colIndex ->
            val type = db.headers[colIndex].type
            assertIs<PrimitiveNumeric>(type)
            inlineRangeForLoop(456) { rowIndex ->
                assertEquals((456 * colIndex + rowIndex).toDouble(), db.getValue(type.canonicalRepresentation, rowIndex))
            }
        }
        return db
    }

    @Test
    fun testRemoveByRows() {
        val db = testCreateNumeric()
        var sum = 0.0
        val rand = Random(42)
        while (db.numRows > 0) {
            val rowIndex = rand.nextInt(db.numRows)
            val colOrder = IntArray(db.numColumns) { it }
            colOrder.shuffle(rand)
            colOrder.forEach { colIndex ->
                val type = db.headers[colIndex].type
                assertIs<PrimitiveNumeric>(type)
                sum += db.getValue(type.canonicalRepresentation, rowIndex)
            }
            db.deleteRow(rowIndex)
        }
        assertEquals(1572903828.0, sum)
    }
}
