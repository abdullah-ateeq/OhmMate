package com.example.ohmmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ohmmate.calculator.ElectricalInput
import com.example.ohmmate.calculator.ElectricalParameter
import com.example.ohmmate.calculator.ElectricalResult
import com.example.ohmmate.calculator.OhmsLawCalculator
import com.example.ohmmate.data.AppSettings
import com.example.ohmmate.data.DefaultParameterPair
import com.example.ohmmate.data.HistoryItem
import com.example.ohmmate.data.PreferencesManager
import com.example.ohmmate.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class OhmMateViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = preferencesManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val historyList: StateFlow<List<HistoryItem>> = preferencesManager.historyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selection order tracked as a list for FIFO replacement
    private val _selectedParameters = MutableStateFlow<List<ElectricalParameter>>(
        listOf(ElectricalParameter.VOLTAGE, ElectricalParameter.RESISTANCE)
    )
    val selectedParameters: StateFlow<List<ElectricalParameter>> = _selectedParameters.asStateFlow()

    private val _inputValues = MutableStateFlow<Map<ElectricalParameter, String>>(
        mapOf(
            ElectricalParameter.VOLTAGE to "12.0",
            ElectricalParameter.CURRENT to "2.0",
            ElectricalParameter.RESISTANCE to "6.0",
            ElectricalParameter.POWER to "24.0"
        )
    )
    val inputValues: StateFlow<Map<ElectricalParameter, String>> = _inputValues.asStateFlow()

    private val _validationErrors = MutableStateFlow<Map<ElectricalParameter, String?>>(emptyMap())
    val validationErrors: StateFlow<Map<ElectricalParameter, String?>> = _validationErrors.asStateFlow()

    private val _calculationResult = MutableStateFlow<ElectricalResult?>(null)
    val calculationResult: StateFlow<ElectricalResult?> = _calculationResult.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var userHasInteracted = false

    init {
        calculate()
        // Apply the user's preferred default parameter pair once settings load
        viewModelScope.launch {
            val defPair = preferencesManager.settingsFlow.first().defaultParameters
            if (!userHasInteracted) {
                _selectedParameters.value = listOf(defPair.param1, defPair.param2)
                calculate()
            }
        }
    }

    fun selectParameter(param: ElectricalParameter) {
        userHasInteracted = true
        val current = _selectedParameters.value.toMutableList()
        if (current.contains(param)) {
            // Already selected, do nothing because exactly two must be selected
            return
        }

        if (current.size >= 2) {
            // Replace oldest selection (FIFO)
            current.removeAt(0)
        }
        current.add(param)
        _selectedParameters.value = current

        // Recalculate with the active two parameters
        calculate()
    }

    fun onInputChange(param: ElectricalParameter, text: String) {
        userHasInteracted = true
        val currentInputs = _inputValues.value.toMutableMap()
        currentInputs[param] = text
        _inputValues.value = currentInputs

        validateAndCalculate()
    }

    fun applyQuickValue(param: ElectricalParameter, valueStr: String) {
        onInputChange(param, valueStr)
    }

    private fun validateAndCalculate() {
        val activeParams = _selectedParameters.value
        val inputs = _inputValues.value
        val errors = mutableMapOf<ElectricalParameter, String?>()
        var hasError = false
        val parsedInputs = mutableListOf<ElectricalInput>()

        for (param in activeParams) {
            val text = inputs[param]?.trim() ?: ""
            if (text.isEmpty()) {
                errors[param] = "Enter a valid value."
                hasError = true
                continue
            }

            val bd = text.toBigDecimalOrNull()
            if (bd == null) {
                errors[param] = "Enter a valid value."
                hasError = true
            } else if (bd < BigDecimal.ZERO) {
                errors[param] = "Value cannot be negative."
                hasError = true
            } else {
                errors[param] = null
                parsedInputs.add(ElectricalInput(param, bd))
            }
        }

        _validationErrors.value = errors

        if (!hasError && parsedInputs.size == 2) {
            _calculationResult.value = OhmsLawCalculator.calculate(parsedInputs)
        } else {
            _calculationResult.value = null
        }
    }

    fun calculate() {
        validateAndCalculate()
    }

    fun reset() {
        val defPair = settings.value.defaultParameters
        _selectedParameters.value = listOf(defPair.param1, defPair.param2)
        _inputValues.value = mapOf(
            ElectricalParameter.VOLTAGE to "12.0",
            ElectricalParameter.CURRENT to "2.0",
            ElectricalParameter.RESISTANCE to "6.0",
            ElectricalParameter.POWER to "24.0"
        )
        _validationErrors.value = emptyMap()
        calculate()
    }

    fun saveCalculation() {
        val res = _calculationResult.value
        val activeParams = _selectedParameters.value
        if (res == null || activeParams.size < 2) return

        val p1 = activeParams[0]
        val p2 = activeParams[1]
        val val1 = _inputValues.value[p1] ?: ""
        val val2 = _inputValues.value[p2] ?: ""

        val decimals = settings.value.decimalPlaces
        val vStr = res.voltage?.let { OhmsLawCalculator.formatDisplay(it, decimals) }
        val iStr = res.current?.let { OhmsLawCalculator.formatDisplay(it, decimals) }
        val rStr = res.resistance?.let { OhmsLawCalculator.formatDisplay(it, decimals) }
        val pStr = res.power?.let { OhmsLawCalculator.formatDisplay(it, decimals) }

        val historyItem = HistoryItem(
            param1 = p1,
            value1 = val1,
            param2 = p2,
            value2 = val2,
            voltage = vStr,
            current = iStr,
            resistance = rStr,
            power = pStr,
            formula = res.formula
        )

        viewModelScope.launch {
            preferencesManager.saveHistory(historyItem)
            _snackbarMessage.value = "Calculation saved to history."
        }
    }

    fun restoreCalculation(item: HistoryItem) {
        userHasInteracted = true
        _selectedParameters.value = listOf(item.param1, item.param2)
        val currentInputs = _inputValues.value.toMutableMap()
        currentInputs[item.param1] = item.value1
        currentInputs[item.param2] = item.value2
        _inputValues.value = currentInputs
        _validationErrors.value = emptyMap()
        calculate()
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            preferencesManager.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            preferencesManager.clearHistory()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDynamicColor(enabled)
        }
    }

    fun setDefaultParameters(pair: DefaultParameterPair) {
        viewModelScope.launch {
            preferencesManager.setDefaultParameters(pair)
        }
    }

    fun setDecimalPlaces(places: Int) {
        viewModelScope.launch {
            preferencesManager.setDecimalPlaces(places)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHapticFeedback(enabled)
        }
    }

    fun setShowFormula(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowFormula(enabled)
        }
    }

    fun setShowSafetyNote(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowSafetyNote(enabled)
        }
    }

    fun setAutoScaleUnits(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoScaleUnits(enabled)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            preferencesManager.resetSettings()
        }
    }
}
