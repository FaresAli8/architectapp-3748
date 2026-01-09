package com.example.procalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.procalculator.ui.theme.ProCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: CalculatorViewModel by viewModels()
        setContent {
            ProCalculatorTheme {
                CalculatorScreen(viewModel)
            }
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val state = viewModel.state
    val buttonSpacing = 8.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        
        // History Display
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.Bottom
        ) {
            items(state.history) { item ->
                Text(
                    text = item,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                )
            }
        }

        // Current Expression
        Text(
            text = state.expression.ifEmpty { "0" },
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 50.sp,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            lineHeight = 60.sp
        )

        // Buttons Grid
        val buttonRows = listOf(
            listOf("AC", "( )", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )

        buttonRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                row.forEach { symbol ->
                    CalculatorButton(
                        symbol = symbol,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                    ) {
                        handleButtonClick(symbol, viewModel)
                    }
                }
            }
        }
    }
}

fun handleButtonClick(symbol: String, viewModel: CalculatorViewModel) {
    when (symbol) {
        "AC" -> viewModel.onAction(CalculatorAction.Clear)
        "⌫" -> viewModel.onAction(CalculatorAction.Delete)
        "=" -> viewModel.onAction(CalculatorAction.Calculate)
        "." -> viewModel.onAction(CalculatorAction.Decimal)
        "+", "-", "*", "/", "%" -> viewModel.onAction(CalculatorAction.Operation(symbol))
        "( )" -> viewModel.onAction(CalculatorAction.Parenthesis)
        else -> {
            if (symbol.toIntOrNull() != null) {
                viewModel.onAction(CalculatorAction.Number(symbol.toInt()))
            }
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when(symbol) {
        "AC", "⌫" -> MaterialTheme.colorScheme.secondary
        "=", "+", "-", "*", "/" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when(symbol) {
        "=", "+", "-", "*", "/" -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() }
    ) {
        Text(
            text = symbol,
            fontSize = 28.sp,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}