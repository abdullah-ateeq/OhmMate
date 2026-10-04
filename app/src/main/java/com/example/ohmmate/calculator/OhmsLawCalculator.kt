package com.example.ohmmate.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.sqrt

object OhmsLawCalculator {

    private val MC = MathContext.DECIMAL128

    fun calculate(inputs: List<ElectricalInput>): ElectricalResult {
        if (inputs.size != 2) {
            return ElectricalResult(
                voltage = null,
                current = null,
                resistance = null,
                power = null,
                knownParameters = inputs.map { it.parameter }.toSet(),
                formula = "",
                errorMessage = "Select exactly two known values."
            )
        }

        val input1 = inputs[0]
        val input2 = inputs[1]

        if (input1.parameter == input2.parameter) {
            return ElectricalResult(
                voltage = null,
                current = null,
                resistance = null,
                power = null,
                knownParameters = setOf(input1.parameter),
                formula = "",
                errorMessage = "Select two different parameters."
            )
        }

        // Validate non-negative
        if (input1.value < BigDecimal.ZERO || input2.value < BigDecimal.ZERO) {
            return ElectricalResult(
                voltage = null,
                current = null,
                resistance = null,
                power = null,
                knownParameters = setOf(input1.parameter, input2.parameter),
                formula = "",
                errorMessage = "Value cannot be negative."
            )
        }

        val map = mapOf(input1.parameter to input1.value, input2.parameter to input2.value)

        return when {
            map.containsKey(ElectricalParameter.VOLTAGE) && map.containsKey(ElectricalParameter.CURRENT) ->
                calculateFromVoltageCurrent(map.getValue(ElectricalParameter.VOLTAGE), map.getValue(ElectricalParameter.CURRENT))

            map.containsKey(ElectricalParameter.VOLTAGE) && map.containsKey(ElectricalParameter.RESISTANCE) ->
                calculateFromVoltageResistance(map.getValue(ElectricalParameter.VOLTAGE), map.getValue(ElectricalParameter.RESISTANCE))

            map.containsKey(ElectricalParameter.VOLTAGE) && map.containsKey(ElectricalParameter.POWER) ->
                calculateFromVoltagePower(map.getValue(ElectricalParameter.VOLTAGE), map.getValue(ElectricalParameter.POWER))

            map.containsKey(ElectricalParameter.CURRENT) && map.containsKey(ElectricalParameter.RESISTANCE) ->
                calculateFromCurrentResistance(map.getValue(ElectricalParameter.CURRENT), map.getValue(ElectricalParameter.RESISTANCE))

            map.containsKey(ElectricalParameter.CURRENT) && map.containsKey(ElectricalParameter.POWER) ->
                calculateFromCurrentPower(map.getValue(ElectricalParameter.CURRENT), map.getValue(ElectricalParameter.POWER))

            map.containsKey(ElectricalParameter.RESISTANCE) && map.containsKey(ElectricalParameter.POWER) ->
                calculateFromResistancePower(map.getValue(ElectricalParameter.RESISTANCE), map.getValue(ElectricalParameter.POWER))

            else -> ElectricalResult(
                voltage = null,
                current = null,
                resistance = null,
                power = null,
                knownParameters = emptySet(),
                formula = "",
                errorMessage = "Unknown parameter combination."
            )
        }
    }

    /**
     * 1. Voltage + Current
     * Calculate: R = V / I, P = V × I
     */
    fun calculateFromVoltageCurrent(voltage: BigDecimal, current: BigDecimal): ElectricalResult {
        val known = setOf(ElectricalParameter.VOLTAGE, ElectricalParameter.CURRENT)
        if (voltage < BigDecimal.ZERO || current < BigDecimal.ZERO) {
            return ElectricalResult(null, null, null, null, known, "", errorMessage = "Value cannot be negative.")
        }

        if (current.compareTo(BigDecimal.ZERO) == 0) {
            val errorMsg = if (voltage.compareTo(BigDecimal.ZERO) == 0) {
                "Resistance is indeterminate when voltage and current are both zero."
            } else {
                "Resistance cannot be determined because current is zero."
            }
            return ElectricalResult(
                voltage = voltage,
                current = current,
                resistance = null,
                power = BigDecimal.ZERO,
                knownParameters = known,
                formula = "R = V / I\nP = V × I",
                formulaDetails = "Power: P = V × I = 0 W\nOhm's Law: R = V / I cannot be computed because current is zero.",
                errorMessage = errorMsg
            )
        }

        val r = voltage.divide(current, MC)
        val p = voltage.multiply(current, MC)

        return ElectricalResult(
            voltage = voltage,
            current = current,
            resistance = r,
            power = p,
            knownParameters = known,
            formula = "R = V / I\nP = V × I",
            formulaDetails = "Ohm's Law:\nR = V ÷ I\n\nPower:\nP = V × I"
        )
    }

    /**
     * 2. Voltage + Resistance
     * Calculate: I = V / R, P = V² / R
     */
    fun calculateFromVoltageResistance(voltage: BigDecimal, resistance: BigDecimal): ElectricalResult {
        val known = setOf(ElectricalParameter.VOLTAGE, ElectricalParameter.RESISTANCE)
        if (voltage < BigDecimal.ZERO || resistance < BigDecimal.ZERO) {
            return ElectricalResult(null, null, null, null, known, "", errorMessage = "Value cannot be negative.")
        }

        if (resistance.compareTo(BigDecimal.ZERO) == 0) {
            val errorMsg = if (voltage.compareTo(BigDecimal.ZERO) == 0) {
                "Current and power are indeterminate for zero voltage across zero resistance."
            } else {
                "Current cannot be represented for zero resistance with non-zero voltage."
            }
            return ElectricalResult(
                voltage = voltage,
                current = null,
                resistance = resistance,
                power = null,
                knownParameters = known,
                formula = "I = V / R\nP = V² / R",
                formulaDetails = "Ohm's Law: I = V / R cannot be calculated with zero resistance.",
                errorMessage = errorMsg
            )
        }

        val i = voltage.divide(resistance, MC)
        val p = voltage.multiply(voltage, MC).divide(resistance, MC)

        return ElectricalResult(
            voltage = voltage,
            current = i,
            resistance = resistance,
            power = p,
            knownParameters = known,
            formula = "I = V / R\nP = V² / R",
            formulaDetails = "Ohm's Law:\nI = V ÷ R\n\nPower:\nP = V² ÷ R"
        )
    }

    /**
     * 3. Voltage + Power
     * Calculate: I = P / V, R = V² / P
     */
    fun calculateFromVoltagePower(voltage: BigDecimal, power: BigDecimal): ElectricalResult {
        val known = setOf(ElectricalParameter.VOLTAGE, ElectricalParameter.POWER)
        if (voltage < BigDecimal.ZERO || power < BigDecimal.ZERO) {
            return ElectricalResult(null, null, null, null, known, "", errorMessage = "Value cannot be negative.")
        }

        if (voltage.compareTo(BigDecimal.ZERO) == 0) {
            val errorMsg = if (power.compareTo(BigDecimal.ZERO) == 0) {
                "Resistance is indeterminate when voltage and power are both zero."
            } else {
                "Current cannot be calculated with zero voltage and non-zero power."
            }
            return ElectricalResult(
                voltage = voltage,
                current = if (power.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else null,
                resistance = null,
                power = power,
                knownParameters = known,
                formula = "I = P / V\nR = V² / P",
                formulaDetails = "Ohm's Law: I = P / V cannot be calculated with zero voltage.",
                errorMessage = errorMsg
            )
        }

        if (power.compareTo(BigDecimal.ZERO) == 0) {
            return ElectricalResult(
                voltage = voltage,
                current = BigDecimal.ZERO,
                resistance = null,
                power = power,
                knownParameters = known,
                formula = "I = P / V\nR = V² / P",
                formulaDetails = "Current: I = P / V = 0 A\nResistance: R is open circuit / undefined when power is zero with non-zero voltage.",
                errorMessage = "Resistance is open circuit / undefined when power is zero."
            )
        }

        val i = power.divide(voltage, MC)
        val r = voltage.multiply(voltage, MC).divide(power, MC)

        return ElectricalResult(
            voltage = voltage,
            current = i,
            resistance = r,
            power = power,
            knownParameters = known,
            formula = "I = P / V\nR = V² / P",
            formulaDetails = "Ohm's Law:\nI = P ÷ V\n\nPower:\nR = V² ÷ P"
        )
    }

    /**
     * 4. Current + Resistance
     * Calculate: V = I × R, P = I² × R
     */
    fun calculateFromCurrentResistance(current: BigDecimal, resistance: BigDecimal): ElectricalResult {
        val known = setOf(ElectricalParameter.CURRENT, ElectricalParameter.RESISTANCE)
        if (current < BigDecimal.ZERO || resistance < BigDecimal.ZERO) {
            return ElectricalResult(null, null, null, null, known, "", errorMessage = "Value cannot be negative.")
        }

        val v = current.multiply(resistance, MC)
        val p = current.multiply(current, MC).multiply(resistance, MC)

        return ElectricalResult(
            voltage = v,
            current = current,
            resistance = resistance,
            power = p,
            knownParameters = known,
            formula = "V = I × R\nP = I² × R",
            formulaDetails = "Ohm's Law:\nV = I × R\n\nPower:\nP = I² × R"
        )
    }

    /**
     * 5. Current + Power
     * Calculate: V = P / I, R = P / I²
     */
    fun calculateFromCurrentPower(current: BigDecimal, power: BigDecimal): ElectricalResult {
        val known = setOf(ElectricalParameter.CURRENT, ElectricalParameter.POWER)
        if (current < BigDecimal.ZERO || power < BigDecimal.ZERO) {
            return ElectricalResult(null, null, null, null, known, "", errorMessage = "Value cannot be negative.")
        }

        if (current.compareTo(BigDecimal.ZERO) == 0) {
            val errorMsg = if (power.compareTo(BigDecimal.ZERO) == 0) {
                "Resistance is indeterminate when current and power are zero."
            } else {
                "Voltage cannot be calculated with zero current and non-zero power."
            }
            return ElectricalResult(
                voltage = if (power.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else null,
                current = current,
                resistance = null,
                power = power,
                knownParameters = known,
                formula = "V = P / I\nR = P / I²",
                formulaDetails = "V = P / I cannot be computed with zero current.",
                errorMessage = errorMsg
            )
        }

        val v = power.divide(current, MC)
        val iSquared = current.multiply(current, MC)
        val r = power.divide(iSquared, MC)

        return ElectricalResult(
            voltage = v,
            current = current,
            resistance = r,
            power = power,
            knownParameters = known,
            formula = "V = P / I\nR = P / I²",
            formulaDetails = "Ohm's Law:\nV = P ÷ I\n\nPower:\nR = P ÷ I²"
        )
    }

    /**
     * 6. Resistance + Power
     * Calculate: V = √(P × R), I = √(P / R)
     */
    fun calculateFromResistancePower(resistance: BigDecimal, power: BigDecimal): ElectricalResult {
        val known = setOf(ElectricalParameter.RESISTANCE, ElectricalParameter.POWER)
        if (resistance < BigDecimal.ZERO || power < BigDecimal.ZERO) {
            return ElectricalResult(null, null, null, null, known, "", errorMessage = "Value cannot be negative.")
        }

        if (resistance.compareTo(BigDecimal.ZERO) == 0) {
            return if (power.compareTo(BigDecimal.ZERO) == 0) {
                ElectricalResult(
                    voltage = BigDecimal.ZERO,
                    current = BigDecimal.ZERO,
                    resistance = resistance,
                    power = power,
                    knownParameters = known,
                    formula = "V = √(P × R)\nI = √(P / R)",
                    formulaDetails = "With zero resistance and zero power, voltage is 0 V and current is 0 A."
                )
            } else {
                ElectricalResult(
                    voltage = null,
                    current = null,
                    resistance = resistance,
                    power = power,
                    knownParameters = known,
                    formula = "V = √(P × R)\nI = √(P / R)",
                    formulaDetails = "Current cannot be represented for zero resistance with non-zero power.",
                    errorMessage = "Current cannot be represented for zero resistance with non-zero power."
                )
            }
        }

        if (power.compareTo(BigDecimal.ZERO) == 0) {
            return ElectricalResult(
                voltage = BigDecimal.ZERO,
                current = BigDecimal.ZERO,
                resistance = resistance,
                power = power,
                knownParameters = known,
                formula = "V = √(P × R)\nI = √(P / R)",
                formulaDetails = "Ohm's Law:\nV = √(P × R) = 0 V\n\nCurrent:\nI = √(P ÷ R) = 0 A"
            )
        }

        val pTimesR = power.multiply(resistance, MC).toDouble()
        val pDivR = power.divide(resistance, MC).toDouble()

        val vDouble = sqrt(pTimesR)
        val iDouble = sqrt(pDivR)

        val v = BigDecimal.valueOf(vDouble)
        val i = BigDecimal.valueOf(iDouble)

        return ElectricalResult(
            voltage = v,
            current = i,
            resistance = resistance,
            power = power,
            knownParameters = known,
            formula = "V = √(P × R)\nI = √(P / R)",
            formulaDetails = "Ohm's Law:\nV = √(P × R)\n\nCurrent:\nI = √(P ÷ R)"
        )
    }

    /**
     * Formats a value with its unit symbol, applying SI prefixes when [autoScale] is enabled.
     * Returns "—" when the value could not be determined.
     */
    fun formatWithUnit(
        value: BigDecimal?,
        decimalPlaces: Int,
        autoScale: Boolean,
        parameter: ElectricalParameter
    ): String {
        if (value == null) return "—"
        val formatted = formatDisplay(value, decimalPlaces, autoScale, parameter)
        // Scaled output already carries a prefixed unit such as "mA" or "kΩ"
        return if (formatted.endsWith(parameter.unit)) formatted else "$formatted ${parameter.unit}"
    }

    /**
     * Formats a BigDecimal value to the specified number of decimal places (default 2).
     */
    fun formatDisplay(value: BigDecimal?, decimalPlaces: Int = 2, autoScale: Boolean = false, parameter: ElectricalParameter? = null): String {
        if (value == null) return "—"

        if (autoScale && parameter != null) {
            val d = value.toDouble()
            when (parameter) {
                ElectricalParameter.VOLTAGE -> {
                    if (d in 0.000001..0.999999) {
                        return String.format(Locale.US, "%.${decimalPlaces}f mV", d * 1000.0)
                    } else if (d >= 1000.0) {
                        return String.format(Locale.US, "%.${decimalPlaces}f kV", d / 1000.0)
                    }
                }
                ElectricalParameter.CURRENT -> {
                    if (d in 0.000001..0.999999) {
                        return String.format(Locale.US, "%.${decimalPlaces}f mA", d * 1000.0)
                    } else if (d >= 1000.0) {
                        return String.format(Locale.US, "%.${decimalPlaces}f kA", d / 1000.0)
                    }
                }
                ElectricalParameter.RESISTANCE -> {
                    if (d in 0.000001..0.999999) {
                        return String.format(Locale.US, "%.${decimalPlaces}f mΩ", d * 1000.0)
                    } else if (d in 1000.0..999999.999) {
                        return String.format(Locale.US, "%.${decimalPlaces}f kΩ", d / 1000.0)
                    } else if (d >= 1000000.0) {
                        return String.format(Locale.US, "%.${decimalPlaces}f MΩ", d / 1000000.0)
                    }
                }
                ElectricalParameter.POWER -> {
                    if (d in 0.000001..0.999999) {
                        return String.format(Locale.US, "%.${decimalPlaces}f mW", d * 1000.0)
                    } else if (d >= 1000.0) {
                        return String.format(Locale.US, "%.${decimalPlaces}f kW", d / 1000.0)
                    }
                }
            }
        }

        return value.setScale(decimalPlaces, RoundingMode.HALF_UP).toPlainString()
    }
}
