package com.example.ohmmate

import com.example.ohmmate.calculator.ElectricalInput
import com.example.ohmmate.calculator.ElectricalParameter
import com.example.ohmmate.calculator.OhmsLawCalculator
import com.example.ohmmate.data.HistoryItem
import com.example.ohmmate.data.HistoryJsonSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class OhmsLawCalculatorTest {

    private val tolerance = 1e-4

    // 1. Voltage + Current
    @Test
    fun testVoltageAndCurrent() {
        // V = 24 V, I = 5 A -> Expected: R = 4.8 Ω, P = 120 W
        val v = BigDecimal("24")
        val i = BigDecimal("5")
        val result = OhmsLawCalculator.calculateFromVoltageCurrent(v, i)

        assertTrue(result.isSuccess)
        assertNull(result.errorMessage)
        assertEquals(4.8, result.resistance!!.toDouble(), tolerance)
        assertEquals(120.0, result.power!!.toDouble(), tolerance)
        assertTrue(result.formula.contains("R = V / I"))
        assertTrue(result.formula.contains("P = V × I"))
    }

    // 2. Voltage + Resistance
    @Test
    fun testVoltageAndResistance() {
        // V = 12 V, R = 6 Ω -> Expected: I = 2 A, P = 24 W
        val v = BigDecimal("12")
        val r = BigDecimal("6")
        val result = OhmsLawCalculator.calculateFromVoltageResistance(v, r)

        assertTrue(result.isSuccess)
        assertNull(result.errorMessage)
        assertEquals(2.0, result.current!!.toDouble(), tolerance)
        assertEquals(24.0, result.power!!.toDouble(), tolerance)
        assertTrue(result.formula.contains("I = V / R"))
        assertTrue(result.formula.contains("P = V² / R"))
    }

    // 3. Voltage + Power
    @Test
    fun testVoltageAndPower() {
        // V = 20 V, P = 100 W -> Expected: I = 5 A, R = 4 Ω
        val v = BigDecimal("20")
        val p = BigDecimal("100")
        val result = OhmsLawCalculator.calculateFromVoltagePower(v, p)

        assertTrue(result.isSuccess)
        assertNull(result.errorMessage)
        assertEquals(5.0, result.current!!.toDouble(), tolerance)
        assertEquals(4.0, result.resistance!!.toDouble(), tolerance)
        assertTrue(result.formula.contains("I = P / V"))
        assertTrue(result.formula.contains("R = V² / P"))
    }

    // 4. Current + Resistance
    @Test
    fun testCurrentAndResistance() {
        // I = 2 A, R = 10 Ω -> Expected: V = 20 V, P = 40 W
        val i = BigDecimal("2")
        val r = BigDecimal("10")
        val result = OhmsLawCalculator.calculateFromCurrentResistance(i, r)

        assertTrue(result.isSuccess)
        assertNull(result.errorMessage)
        assertEquals(20.0, result.voltage!!.toDouble(), tolerance)
        assertEquals(40.0, result.power!!.toDouble(), tolerance)
        assertTrue(result.formula.contains("V = I × R"))
        assertTrue(result.formula.contains("P = I² × R"))
    }

    // 5. Current + Power
    @Test
    fun testCurrentAndPower() {
        // I = 5 A, P = 100 W -> Expected: V = 20 V, R = 4 Ω
        val i = BigDecimal("5")
        val p = BigDecimal("100")
        val result = OhmsLawCalculator.calculateFromCurrentPower(i, p)

        assertTrue(result.isSuccess)
        assertNull(result.errorMessage)
        assertEquals(20.0, result.voltage!!.toDouble(), tolerance)
        assertEquals(4.0, result.resistance!!.toDouble(), tolerance)
        assertTrue(result.formula.contains("V = P / I"))
        assertTrue(result.formula.contains("R = P / I²"))
    }

    // 6. Resistance + Power
    @Test
    fun testResistanceAndPower() {
        // R = 4 Ω, P = 100 W -> Expected: V = 20 V, I = 5 A
        val r = BigDecimal("4")
        val p = BigDecimal("100")
        val result = OhmsLawCalculator.calculateFromResistancePower(r, p)

        assertTrue(result.isSuccess)
        assertNull(result.errorMessage)
        assertEquals(20.0, result.voltage!!.toDouble(), tolerance)
        assertEquals(5.0, result.current!!.toDouble(), tolerance)
        assertTrue(result.formula.contains("V = √(P × R)"))
        assertTrue(result.formula.contains("I = √(P / R)"))
    }

    // 7. Zero voltage
    @Test
    fun testZeroVoltage() {
        // V = 0, R = 6 -> I = 0, P = 0
        val resVR = OhmsLawCalculator.calculateFromVoltageResistance(BigDecimal.ZERO, BigDecimal("6"))
        assertTrue(resVR.isSuccess)
        assertEquals(0.0, resVR.current!!.toDouble(), tolerance)
        assertEquals(0.0, resVR.power!!.toDouble(), tolerance)

        // V = 0, I = 2 -> R = 0, P = 0
        val resVI = OhmsLawCalculator.calculateFromVoltageCurrent(BigDecimal.ZERO, BigDecimal("2"))
        assertTrue(resVI.isSuccess)
        assertEquals(0.0, resVI.resistance!!.toDouble(), tolerance)
        assertEquals(0.0, resVI.power!!.toDouble(), tolerance)
    }

    // 8. Zero current
    @Test
    fun testZeroCurrent() {
        // I = 0, R = 6 -> V = 0, P = 0
        val resIR = OhmsLawCalculator.calculateFromCurrentResistance(BigDecimal.ZERO, BigDecimal("6"))
        assertTrue(resIR.isSuccess)
        assertEquals(0.0, resIR.voltage!!.toDouble(), tolerance)
        assertEquals(0.0, resIR.power!!.toDouble(), tolerance)

        // V = 12, I = 0 -> Division by zero, message displayed, no Infinity/NaN
        val resVI = OhmsLawCalculator.calculateFromVoltageCurrent(BigDecimal("12"), BigDecimal.ZERO)
        assertFalse(resVI.isSuccess)
        assertNotNull(resVI.errorMessage)
        assertNull(resVI.resistance)
        assertTrue(resVI.errorMessage!!.contains("Resistance cannot be determined because current is zero"))
    }

    // 9. Zero resistance
    @Test
    fun testZeroResistance() {
        // V = 12, R = 0 -> Undefined infinite current protected, message shown, no Infinity
        val resVR = OhmsLawCalculator.calculateFromVoltageResistance(BigDecimal("12"), BigDecimal.ZERO)
        assertFalse(resVR.isSuccess)
        assertNotNull(resVR.errorMessage)
        assertNull(resVR.current)
        assertTrue(resVR.errorMessage!!.contains("Current cannot be represented for zero resistance"))

        // I = 2, R = 0 -> V = 0, P = 0
        val resIR = OhmsLawCalculator.calculateFromCurrentResistance(BigDecimal("2"), BigDecimal.ZERO)
        assertTrue(resIR.isSuccess)
        assertEquals(0.0, resIR.voltage!!.toDouble(), tolerance)
        assertEquals(0.0, resIR.power!!.toDouble(), tolerance)
    }

    // 10. Zero power
    @Test
    fun testZeroPower() {
        // R = 4, P = 0 -> V = 0, I = 0
        val resRP = OhmsLawCalculator.calculateFromResistancePower(BigDecimal("4"), BigDecimal.ZERO)
        assertTrue(resRP.isSuccess)
        assertEquals(0.0, resRP.voltage!!.toDouble(), tolerance)
        assertEquals(0.0, resRP.current!!.toDouble(), tolerance)

        // I = 5, P = 0 -> V = 0, R = 0
        val resIP = OhmsLawCalculator.calculateFromCurrentPower(BigDecimal("5"), BigDecimal.ZERO)
        assertTrue(resIP.isSuccess)
        assertEquals(0.0, resIP.voltage!!.toDouble(), tolerance)
        assertEquals(0.0, resIP.resistance!!.toDouble(), tolerance)
    }

    // 11. Negative values
    @Test
    fun testNegativeValues() {
        val res1 = OhmsLawCalculator.calculateFromVoltageResistance(BigDecimal("-12"), BigDecimal("6"))
        assertFalse(res1.isSuccess)
        assertEquals("Value cannot be negative.", res1.errorMessage)

        val res2 = OhmsLawCalculator.calculate(
            listOf(
                ElectricalInput(ElectricalParameter.CURRENT, BigDecimal("-2")),
                ElectricalInput(ElectricalParameter.POWER, BigDecimal("10"))
            )
        )
        assertFalse(res2.isSuccess)
        assertEquals("Value cannot be negative.", res2.errorMessage)
    }

    // 12. Division-by-zero protection
    @Test
    fun testDivisionByZeroProtection() {
        val res = OhmsLawCalculator.calculateFromVoltagePower(BigDecimal.ZERO, BigDecimal("50"))
        assertFalse(res.isSuccess)
        assertNotNull(res.errorMessage)
        assertFalse(res.errorMessage!!.contains("Infinity"))
        assertFalse(res.errorMessage!!.contains("NaN"))
    }

    // 13. Square-root calculations
    @Test
    fun testSquareRootCalculations() {
        // R = 3 Ω, P = 48 W -> V = √(48 * 3) = √144 = 12 V, I = √(48 / 3) = √16 = 4 A
        val res = OhmsLawCalculator.calculateFromResistancePower(BigDecimal("3"), BigDecimal("48"))
        assertTrue(res.isSuccess)
        assertEquals(12.0, res.voltage!!.toDouble(), tolerance)
        assertEquals(4.0, res.current!!.toDouble(), tolerance)

        // Non-integer square root: R = 5 Ω, P = 20 W -> V = √(100) = 10 V, I = √(4) = 2 A
        val res2 = OhmsLawCalculator.calculateFromResistancePower(BigDecimal("5"), BigDecimal("20"))
        assertTrue(res2.isSuccess)
        assertEquals(10.0, res2.voltage!!.toDouble(), tolerance)
        assertEquals(2.0, res2.current!!.toDouble(), tolerance)
    }

    // 14. Decimal calculations
    @Test
    fun testDecimalCalculations() {
        // V = 3.3 V, R = 220 Ω -> I = 3.3 / 220 = 0.015 A, P = 3.3 * 0.015 = 0.0495 W
        val res = OhmsLawCalculator.calculateFromVoltageResistance(BigDecimal("3.3"), BigDecimal("220"))
        assertTrue(res.isSuccess)
        assertEquals(0.015, res.current!!.toDouble(), tolerance)
        assertEquals(0.0495, res.power!!.toDouble(), tolerance)
    }

    // 15. Result formatting
    @Test
    fun testResultFormatting() {
        val value = BigDecimal("12.3456")
        assertEquals("12.3", OhmsLawCalculator.formatDisplay(value, decimalPlaces = 1))
        assertEquals("12.35", OhmsLawCalculator.formatDisplay(value, decimalPlaces = 2))
        assertEquals("12.346", OhmsLawCalculator.formatDisplay(value, decimalPlaces = 3))

        // Scaling formatting
        val resistance1k = BigDecimal("1500")
        assertEquals("1.50 kΩ", OhmsLawCalculator.formatDisplay(resistance1k, decimalPlaces = 2, autoScale = true, parameter = ElectricalParameter.RESISTANCE))

        val currentMilli = BigDecimal("0.0025")
        assertEquals("2.50 mA", OhmsLawCalculator.formatDisplay(currentMilli, decimalPlaces = 2, autoScale = true, parameter = ElectricalParameter.CURRENT))
    }

    // 16. Formula selection
    @Test
    fun testFormulaSelection() {
        val resVR = OhmsLawCalculator.calculate(
            listOf(
                ElectricalInput(ElectricalParameter.VOLTAGE, BigDecimal("12")),
                ElectricalInput(ElectricalParameter.RESISTANCE, BigDecimal("6"))
            )
        )
        assertTrue(resVR.formula.contains("I = V / R"))
        assertTrue(resVR.formula.contains("P = V² / R"))

        val resRP = OhmsLawCalculator.calculate(
            listOf(
                ElectricalInput(ElectricalParameter.RESISTANCE, BigDecimal("6")),
                ElectricalInput(ElectricalParameter.POWER, BigDecimal("24"))
            )
        )
        assertTrue(resRP.formula.contains("V = √(P × R)"))
        assertTrue(resRP.formula.contains("I = √(P / R)"))
    }

    // 17. History serialization
    @Test
    fun testHistorySerialization() {
        val items = listOf(
            HistoryItem(
                id = "item-1",
                timestamp = 1600000000000L,
                param1 = ElectricalParameter.VOLTAGE,
                value1 = "12.0",
                param2 = ElectricalParameter.RESISTANCE,
                value2 = "6.0",
                voltage = "12.00",
                current = "2.00",
                resistance = "6.00",
                power = "24.00",
                formula = "I = V / R\nP = V² / R"
            ),
            HistoryItem(
                id = "item-2",
                timestamp = 1600000001000L,
                param1 = ElectricalParameter.CURRENT,
                value1 = "5.0",
                param2 = ElectricalParameter.POWER,
                value2 = "100.0",
                voltage = "20.00",
                current = "5.00",
                resistance = "4.00",
                power = "100.00",
                formula = "V = P / I\nR = P / I²"
            )
        )

        val json = HistoryJsonSerializer.serialize(items)
        assertNotNull(json)
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))

        val deserialized = HistoryJsonSerializer.deserialize(json)
        assertEquals(2, deserialized.size)
        assertEquals("item-1", deserialized[0].id)
        assertEquals(ElectricalParameter.VOLTAGE, deserialized[0].param1)
        assertEquals("12.0", deserialized[0].value1)
        assertEquals("2.00", deserialized[0].current)
        assertEquals("24.00", deserialized[0].power)

        assertEquals("item-2", deserialized[1].id)
        assertEquals(ElectricalParameter.CURRENT, deserialized[1].param1)
        assertEquals("5.0", deserialized[1].value1)

        // Empty list serialization
        val emptyJson = HistoryJsonSerializer.serialize(emptyList())
        assertEquals("[]", emptyJson)
        val emptyList = HistoryJsonSerializer.deserialize(emptyJson)
        assertTrue(emptyList.isEmpty())
    }
}
