package com.example.ui.screens.jamb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlin.math.sqrt

private val CalcGreen = Color(0xFF00C853)
private val CalcOpColor = Color(0xFF0288D1)
private val CalcSpecialColor = Color(0xFFFF9100)
private val CalcKeyColor = Color(0xFF242731)

@Composable
fun JambCalculatorDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf("0") }
    var expressionHistory by remember { mutableStateOf("") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var activeOperator by remember { mutableStateOf<String?>(null) }
    var isNewEntry by remember { mutableStateOf(true) }

    fun onNumberClick(digit: String) {
        if (isNewEntry || displayValue == "0" || displayValue == "Error") {
            displayValue = digit
            isNewEntry = false
        } else {
            if (displayValue.length < 12) {
                displayValue += digit
            }
        }
    }

    fun onDecimalClick() {
        if (isNewEntry || displayValue == "Error") {
            displayValue = "0."
            isNewEntry = false
        } else if (!displayValue.contains(".")) {
            displayValue += "."
        }
    }

    fun onOperatorClick(operator: String) {
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        if (operand1 == null) {
            operand1 = currentValue
            expressionHistory = "$displayValue $operator"
        } else if (activeOperator != null && !isNewEntry) {
            // calculate intermediate result
            val result = when (activeOperator) {
                "+" -> operand1!! + currentValue
                "−" -> operand1!! - currentValue
                "×" -> operand1!! * currentValue
                "÷" -> if (currentValue != 0.0) operand1!! / currentValue else Double.NaN
                else -> currentValue
            }
            operand1 = if (result.isNaN() || result.isInfinite()) null else result
            displayValue = if (result.isNaN() || result.isInfinite()) "Error" else formatResult(result)
            expressionHistory = if (operand1 != null) "$displayValue $operator" else ""
        } else {
            expressionHistory = "$displayValue $operator"
        }
        activeOperator = operator
        isNewEntry = true
    }

    fun onEqualsClick() {
        if (operand1 != null && activeOperator != null) {
            val currentValue = displayValue.toDoubleOrNull() ?: 0.0
            val result = when (activeOperator) {
                "+" -> operand1!! + currentValue
                "−" -> operand1!! - currentValue
                "×" -> operand1!! * currentValue
                "÷" -> if (currentValue != 0.0) operand1!! / currentValue else Double.NaN
                else -> currentValue
            }
            expressionHistory = "$operand1 $activeOperator $currentValue ="
            displayValue = if (result.isNaN() || result.isInfinite()) "Error" else formatResult(result)
            operand1 = null
            activeOperator = null
            isNewEntry = true
        }
    }

    fun onSquareRootClick() {
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        if (currentValue >= 0) {
            val res = sqrt(currentValue)
            expressionHistory = "√($displayValue)"
            displayValue = formatResult(res)
            isNewEntry = true
        } else {
            displayValue = "Error"
            isNewEntry = true
        }
    }

    fun onPercentageClick() {
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        val res = currentValue / 100.0
        expressionHistory = "$displayValue% ="
        displayValue = formatResult(res)
        isNewEntry = true
    }

    fun onClearClick() {
        displayValue = "0"
        expressionHistory = ""
        operand1 = null
        activeOperator = null
        isNewEntry = true
    }

    fun onBackspaceClick() {
        if (!isNewEntry && displayValue.length > 1 && displayValue != "Error") {
            displayValue = displayValue.dropLast(1)
        } else {
            displayValue = "0"
            isNewEntry = true
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardSurface,
            border = BorderStroke(1.5.dp, CalcGreen.copy(alpha = 0.8f)),
            modifier = modifier
                .width(320.dp)
                .testTag("jamb_calculator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CalcGreen.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, CalcGreen)
                        ) {
                            Text(
                                text = "JAMB",
                                style = MaterialTheme.typography.labelSmall,
                                color = CalcGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "UTME Calculator",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("jamb_calc_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Calculator",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Calculator LCD Screen
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F141A),
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = expressionHistory.ifBlank { " " },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayValue,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = CalcGreen,
                            maxLines = 1,
                            textAlign = TextAlign.End,
                            modifier = Modifier.testTag("jamb_calc_display")
                        )
                    }
                }

                // Keypad Layout (Standard 5x4 Grid)
                val rows = listOf(
                    listOf("C" to CalcSpecialColor, "⌫" to CalcSpecialColor, "%" to CalcOpColor, "÷" to CalcOpColor),
                    listOf("7" to TextPrimary, "8" to TextPrimary, "9" to TextPrimary, "×" to CalcOpColor),
                    listOf("4" to TextPrimary, "5" to TextPrimary, "6" to TextPrimary, "−" to CalcOpColor),
                    listOf("1" to TextPrimary, "2" to TextPrimary, "3" to TextPrimary, "+" to CalcOpColor),
                    listOf("√" to CalcOpColor, "0" to TextPrimary, "." to TextPrimary, "=" to CalcGreen)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rows.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowKeys.forEach { (key, textColor) ->
                                val isEquals = key == "="
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isEquals) CalcGreen else CalcKeyColor,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isEquals) CalcGreen else CardSurfaceBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clickable {
                                            when (key) {
                                                "C" -> onClearClick()
                                                "⌫" -> onBackspaceClick()
                                                "%" -> onPercentageClick()
                                                "√" -> onSquareRootClick()
                                                "÷", "×", "−", "+" -> onOperatorClick(key)
                                                "=" -> onEqualsClick()
                                                "." -> onDecimalClick()
                                                else -> onNumberClick(key)
                                            }
                                        }
                                        .testTag("jamb_calc_key_$key")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEquals) Color.Black else textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatResult(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        val s = String.format("%.6f", value).trimEnd('0')
        if (s.endsWith(".")) s.dropLast(1) else s
    }
}
