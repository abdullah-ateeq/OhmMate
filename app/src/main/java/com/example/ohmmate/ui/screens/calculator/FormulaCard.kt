package com.example.ohmmate.ui.screens.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ohmmate.calculator.ElectricalParameter
import com.example.ohmmate.calculator.ElectricalResult
import com.example.ohmmate.calculator.OhmsLawCalculator

@Composable
fun FormulaCard(
    result: ElectricalResult?,
    decimalPlaces: Int,
    modifier: Modifier = Modifier
) {
    if (result == null || result.formula.isBlank()) return

    val known = ElectricalParameter.entries.filter { result.isKnown(it) }
    val calculated = ElectricalParameter.entries.filterNot { result.isKnown(it) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionLabel("FORMULAS", MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            result.formula.lines().forEach { line ->
                MonoLine(
                    text = line.replace(" / ", " ÷ ").replace("(P / R)", "(P ÷ R)"),
                    color = MaterialTheme.colorScheme.onSurface,
                    weight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            SectionLabel("USING", MaterialTheme.colorScheme.onSurfaceVariant)
            known.forEach { param ->
                MonoLine(
                    text = "${param.symbol} = ${valueText(result, param, decimalPlaces)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SectionLabel("THEREFORE", MaterialTheme.colorScheme.onSurfaceVariant)
            calculated.forEach { param ->
                MonoLine(
                    text = "${param.symbol} = ${valueText(result, param, decimalPlaces)}",
                    color = MaterialTheme.colorScheme.primary,
                    weight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun valueText(result: ElectricalResult, param: ElectricalParameter, decimalPlaces: Int): String {
    val value = result.getValue(param) ?: return "not determinable"
    return "${OhmsLawCalculator.formatDisplay(value, decimalPlaces)} ${param.unit}"
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = color
    )
}

@Composable
private fun MonoLine(text: String, color: Color, weight: FontWeight = FontWeight.Normal) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        fontWeight = weight,
        color = color
    )
}
