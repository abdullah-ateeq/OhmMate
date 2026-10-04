package com.example.ohmmate.ui.screens.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ohmmate.ui.components.SafetyBanner
import com.example.ohmmate.ui.viewmodel.OhmMateViewModel

@Composable
fun CalculatorScreen(
    viewModel: OhmMateViewModel,
    modifier: Modifier = Modifier
) {
    val selectedParameters by viewModel.selectedParameters.collectAsState()
    val inputValues by viewModel.inputValues.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    val calculationResult by viewModel.calculationResult.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSnackbarMessage()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        Column(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(hostState = snackbarHostState)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header
                Column {
                    Text(
                        text = "OHMMATE",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Quick voltage, current, resistance & power calculations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isWide) {
                    // Two-column layout for tablets / landscape
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Column: Parameters, Inputs, Actions
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ParameterChips(
                                selectedParameters = selectedParameters,
                                onSelectParameter = { viewModel.selectParameter(it) },
                                hapticFeedback = hapticFeedback,
                                hapticEnabled = settings.hapticFeedback
                            )

                            InputFieldSection(
                                selectedParameters = selectedParameters,
                                inputValues = inputValues,
                                validationErrors = validationErrors,
                                onInputChange = { p, v -> viewModel.onInputChange(p, v) },
                                onApplyQuickValue = { p, v -> viewModel.applyQuickValue(p, v) },
                                hapticFeedback = hapticFeedback,
                                hapticEnabled = settings.hapticFeedback
                            )

                            CalculatorActionButtons(
                                onCalculate = {
                                    if (settings.hapticFeedback) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    }
                                    viewModel.calculate()
                                },
                                onReset = {
                                    if (settings.hapticFeedback) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    }
                                    viewModel.reset()
                                },
                                onSave = {
                                    if (settings.hapticFeedback) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    }
                                    viewModel.saveCalculation()
                                },
                                isSaveEnabled = calculationResult?.isSuccess == true
                            )
                        }

                        // Right Column: Formula, Results, Safety Note
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (settings.showFormula) {
                                FormulaCard(
                                    result = calculationResult,
                                    decimalPlaces = settings.decimalPlaces
                                )
                            }

                            ResultCards(
                                result = calculationResult,
                                decimalPlaces = settings.decimalPlaces,
                                autoScale = settings.autoScaleUnits
                            )

                            if (settings.showSafetyNote) {
                                SafetyBanner()
                            }
                        }
                    }
                } else {
                    // Single column layout for phones
                    ParameterChips(
                        selectedParameters = selectedParameters,
                        onSelectParameter = { viewModel.selectParameter(it) },
                        hapticFeedback = hapticFeedback,
                        hapticEnabled = settings.hapticFeedback
                    )

                    InputFieldSection(
                        selectedParameters = selectedParameters,
                        inputValues = inputValues,
                        validationErrors = validationErrors,
                        onInputChange = { p, v -> viewModel.onInputChange(p, v) },
                        onApplyQuickValue = { p, v -> viewModel.applyQuickValue(p, v) },
                        hapticFeedback = hapticFeedback,
                        hapticEnabled = settings.hapticFeedback
                    )

                    CalculatorActionButtons(
                        onCalculate = {
                            if (settings.hapticFeedback) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            }
                            viewModel.calculate()
                        },
                        onReset = {
                            if (settings.hapticFeedback) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            }
                            viewModel.reset()
                        },
                        onSave = {
                            if (settings.hapticFeedback) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            }
                            viewModel.saveCalculation()
                        },
                        isSaveEnabled = calculationResult?.isSuccess == true
                    )

                    if (settings.showFormula) {
                        FormulaCard(
                            result = calculationResult,
                            decimalPlaces = settings.decimalPlaces
                        )
                    }

                    ResultCards(
                        result = calculationResult,
                        decimalPlaces = settings.decimalPlaces,
                        autoScale = settings.autoScaleUnits
                    )

                    if (settings.showSafetyNote) {
                        SafetyBanner()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CalculatorActionButtons(
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCalculate,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Calculate electrical values" },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CALCULATE",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Reset calculator" },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RESET",
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        OutlinedButton(
            onClick = onSave,
            enabled = isSaveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Save calculation" },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (isSaveEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SAVE CALCULATION",
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
        }
    }
}
