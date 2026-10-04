package com.example.ohmmate.calculator

enum class ElectricalParameter(
    val symbol: String,
    val unit: String,
    val displayName: String,
    val spokenUnit: String
) {
    VOLTAGE("V", "V", "Voltage", "volts"),
    CURRENT("I", "A", "Current", "amperes"),
    RESISTANCE("R", "Ω", "Resistance", "ohms"),
    POWER("P", "W", "Power", "watts");

    companion object {
        val DEFAULT_SELECTION: Set<ElectricalParameter> = linkedSetOf(VOLTAGE, RESISTANCE)
    }
}
