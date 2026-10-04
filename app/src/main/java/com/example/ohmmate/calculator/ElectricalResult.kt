package com.example.ohmmate.calculator

import java.math.BigDecimal

data class ElectricalResult(
    val voltage: BigDecimal?,
    val current: BigDecimal?,
    val resistance: BigDecimal?,
    val power: BigDecimal?,
    val knownParameters: Set<ElectricalParameter>,
    val formula: String,
    val formulaDetails: String = "",
    val errorMessage: String? = null
) {
    val isSuccess: Boolean
        get() = errorMessage == null && voltage != null && current != null && resistance != null && power != null

    fun getValue(parameter: ElectricalParameter): BigDecimal? = when (parameter) {
        ElectricalParameter.VOLTAGE -> voltage
        ElectricalParameter.CURRENT -> current
        ElectricalParameter.RESISTANCE -> resistance
        ElectricalParameter.POWER -> power
    }

    fun isKnown(parameter: ElectricalParameter): Boolean = parameter in knownParameters
}
