package com.example.ohmmate.ui.screens.calculator

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ohmmate.calculator.ElectricalParameter

@Composable
fun InputFieldSection(
    selectedParameters: List<ElectricalParameter>,
    inputValues: Map<ElectricalParameter, String>,
    validationErrors: Map<ElectricalParameter, String?>,
    onInputChange: (ElectricalParameter, String) -> Unit,
    onApplyQuickValue: (ElectricalParameter, String) -> Unit,
    hapticFeedback: HapticFeedback?,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        selectedParameters.forEachIndexed { index, param ->
            val value = inputValues[param] ?: ""
            val error = validationErrors[param]
            val isLast = index == selectedParameters.lastIndex

            SingleParameterInput(
                parameter = param,
                value = value,
                errorMessage = error,
                onValueChange = { onInputChange(param, it) },
                onQuickValueClick = {
                    if (hapticEnabled && hapticFeedback != null) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    }
                    onApplyQuickValue(param, it)
                },
                imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
                onDone = {
                    if (isLast) focusManager.clearFocus()
                }
            )
        }
    }
}

@Composable
private fun SingleParameterInput(
    parameter: ElectricalParameter,
    value: String,
    errorMessage: String?,
    onValueChange: (String) -> Unit,
    onQuickValueClick: (String) -> Unit,
    imeAction: ImeAction,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickValues = when (parameter) {
        ElectricalParameter.VOLTAGE -> listOf("5", "12", "24", "48")
        ElectricalParameter.CURRENT -> listOf("0.5", "1", "2", "5")
        ElectricalParameter.RESISTANCE -> listOf("1", "10", "100", "1000")
        ElectricalParameter.POWER -> listOf("5", "10", "50", "100")
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = parameter.displayName.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.sp
            )
            Text(
                text = "Unit: ${parameter.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "${parameter.displayName} input"
                },
            shape = RoundedCornerShape(12.dp),
            isError = errorMessage != null,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            trailingIcon = {
                Text(
                    text = parameter.unit,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() },
                onNext = { onDone() }
            ),
            supportingText = if (errorMessage != null) {
                {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Quick selection chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickValues.forEach { qv ->
                AssistChip(
                    onClick = { onQuickValueClick(qv) },
                    label = {
                        Text(
                            text = "$qv ${parameter.unit}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}
