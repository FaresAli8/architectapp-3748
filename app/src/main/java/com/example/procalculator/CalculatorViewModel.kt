package com.example.procalculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import net.objecthunter.exp4j.ExpressionBuilder
import java.text.DecimalFormat

class CalculatorViewModel : ViewModel() {

    var state by mutableStateOf(CalculatorState())
        private set

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> state = CalculatorState()
            is CalculatorAction.Delete -> performDeletion()
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.Parenthesis -> enterParenthesis()
        }
    }

    private fun enterParenthesis() {
        // Simple logic to toggle or guess open/close based on counts
        // For professional use, explicit ( ) buttons are better, but if single button:
        val openCount = state.expression.count { it == '(' }
        val closeCount = state.expression.count { it == ')' }
        
        val lastChar = state.expression.lastOrNull()

        if (state.expression.isEmpty() || lastChar in listOf('+', '-', '*', '/', '(')) {
             state = state.copy(expression = state.expression + "(")
        } else if (openCount > closeCount && lastChar !in listOf('(', '+', '-', '*', '/')) {
             state = state.copy(expression = state.expression + ")")
        } else {
             state = state.copy(expression = state.expression + "(")
        }
    }

    private fun performDeletion() {
        if (state.expression.isNotEmpty()) {
            state = state.copy(expression = state.expression.dropLast(1))
        }
    }

    private fun enterDecimal() {
        // Prevent multiple decimals in one number segment
        // This is a simplified check. A robust lexer is better, but this suffices for UI.
        val lastNumberSegment = state.expression.takeLastWhile { it.isDigit() || it == '.' }
        if (!lastNumberSegment.contains(".")) {
            state = state.copy(expression = state.expression + ".")
        } else if (state.expression.isEmpty()) {
            state = state.copy(expression = "0.")
        }
    }

    private fun enterOperation(operation: String) {
        if (state.expression.isNotEmpty()) {
            val lastChar = state.expression.last()
            if (lastChar.toString() in listOf("+", "-", "*", "/", "%")) {
                // Replace operator
                state = state.copy(expression = state.expression.dropLast(1) + operation)
            } else {
                state = state.copy(expression = state.expression + operation)
            }
        } else if (operation == "-") {
            // Negative number start
            state = state.copy(expression = "-")
        }
    }

    private fun enterNumber(number: Int) {
        state = state.copy(expression = state.expression + number)
    }

    private fun performCalculation() {
        if (state.expression.isEmpty()) return

        try {
            // Use exp4j for reliable BODMAS/PEMDAS parsing
            val expression = ExpressionBuilder(state.expression)
                .build()
            val result = expression.evaluate()
            
            // Format result (remove .0 if integer)
            val df = DecimalFormat("#.##########")
            val formattedResult = df.format(result)

            // Add to history
            val newHistory = state.history.toMutableList()
            newHistory.add(0, "${state.expression} = $formattedResult")
            if (newHistory.size > 5) newHistory.removeLast() // Keep last 5

            state = state.copy(
                expression = formattedResult,
                history = newHistory
            )
        } catch (e: Exception) {
            // Handle division by zero or syntax errors
            state = state.copy(expression = "Error")
        }
    }
}

data class CalculatorState(
    val expression: String = "",
    val history: List<String> = emptyList()
)

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operation: String) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Calculate : CalculatorAction()
    object Decimal : CalculatorAction()
    object Parenthesis : CalculatorAction()
}