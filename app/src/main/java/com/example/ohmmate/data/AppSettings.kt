package com.example.ohmmate.data

import com.example.ohmmate.calculator.ElectricalParameter

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class DefaultParameterPair(
    val param1: ElectricalParameter,
    val param2: ElectricalParameter,
    val label: String
) {
    VOLTAGE_RESISTANCE(ElectricalParameter.VOLTAGE, ElectricalParameter.RESISTANCE, "Voltage + Resistance"),
    VOLTAGE_CURRENT(ElectricalParameter.VOLTAGE, ElectricalParameter.CURRENT, "Voltage + Current"),
    VOLTAGE_POWER(ElectricalParameter.VOLTAGE, ElectricalParameter.POWER, "Voltage + Power"),
    CURRENT_RESISTANCE(ElectricalParameter.CURRENT, ElectricalParameter.RESISTANCE, "Current + Resistance"),
    CURRENT_POWER(ElectricalParameter.CURRENT, ElectricalParameter.POWER, "Current + Power"),
    RESISTANCE_POWER(ElectricalParameter.RESISTANCE, ElectricalParameter.POWER, "Resistance + Power");

    companion object {
        fun fromParameters(p1: ElectricalParameter, p2: ElectricalParameter): DefaultParameterPair {
            val set = setOf(p1, p2)
            return values().firstOrNull { setOf(it.param1, it.param2) == set } ?: VOLTAGE_RESISTANCE
        }
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultParameters: DefaultParameterPair = DefaultParameterPair.VOLTAGE_RESISTANCE,
    val decimalPlaces: Int = 2,
    val hapticFeedback: Boolean = true,
    val showFormula: Boolean = true,
    val showSafetyNote: Boolean = true,
    val autoScaleUnits: Boolean = false
)
