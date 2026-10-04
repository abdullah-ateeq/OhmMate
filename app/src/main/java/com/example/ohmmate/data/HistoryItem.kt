package com.example.ohmmate.data

import com.example.ohmmate.calculator.ElectricalParameter
import java.util.UUID

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val param1: ElectricalParameter,
    val value1: String,
    val param2: ElectricalParameter,
    val value2: String,
    val voltage: String?,
    val current: String?,
    val resistance: String?,
    val power: String?,
    val formula: String
)
