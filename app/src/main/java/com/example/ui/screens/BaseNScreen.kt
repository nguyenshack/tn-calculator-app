package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.BaseMode
import com.example.math.BaseNEngine
import com.example.ui.components.KeyDomeButton
import com.example.ui.components.KeyDomeType

@Composable
fun BaseNScreen() {
    var activeMode by remember { mutableStateOf(BaseMode.DEC) }
    var currentVal by remember { mutableLongStateOf(255L) }
    var inputBuffer by remember { mutableStateOf("255") }
    var lastOperator by remember { mutableStateOf<String?>(null) }
    var operandA by remember { mutableStateOf<Long?>(null) }

    fun updateValue(newVal: Long) {
        currentVal = newVal
        inputBuffer = BaseNEngine.format(newVal, activeMode).replace(" ", "")
    }

    fun applyLogic(op: String) {
        operandA = currentVal
        lastOperator = op
        inputBuffer = ""
    }

    fun executeEquals() {
        if (operandA != null && lastOperator != null) {
            val a = operandA!!
            val b = currentVal
            val res = when (lastOperator) {
                "AND" -> BaseNEngine.and(a, b)
                "OR" -> BaseNEngine.or(a, b)
                "XOR" -> BaseNEngine.xor(a, b)
                "XNOR" -> BaseNEngine.xnor(a, b)
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> if (b != 0L) a / b else 0L
                else -> b
            }
            operandA = null
            lastOperator = null
            updateValue(res)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Multi-Base Dashboard (DEC, HEX, BIN, OCT)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                BaseMode.entries.forEach { mode ->
                    val isSel = activeMode == mode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) Color(0xFF0C4A6E) else Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) Color(0xFF38BDF8) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                activeMode = mode
                                inputBuffer = BaseNEngine.format(currentVal, mode).replace(" ", "")
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${mode.prefix}:",
                                color = if (isSel) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                text = BaseNEngine.format(currentVal, mode),
                                color = if (isSel) Color(0xFFFBBF24) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Logic Operations Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyDomeButton(label = "AND", onClick = { applyLogic("AND") }, keyType = KeyDomeType.FUNCTION, modifier = Modifier.weight(1f), height = 40.dp)
            KeyDomeButton(label = "OR", onClick = { applyLogic("OR") }, keyType = KeyDomeType.FUNCTION, modifier = Modifier.weight(1f), height = 40.dp)
            KeyDomeButton(label = "XOR", onClick = { applyLogic("XOR") }, keyType = KeyDomeType.FUNCTION, modifier = Modifier.weight(1f), height = 40.dp)
            KeyDomeButton(label = "NOT", onClick = { updateValue(BaseNEngine.not(currentVal)) }, keyType = KeyDomeType.FUNCTION, modifier = Modifier.weight(1f), height = 40.dp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyDomeButton(label = "XNOR", onClick = { applyLogic("XNOR") }, keyType = KeyDomeType.FUNCTION, modifier = Modifier.weight(1f), height = 40.dp)
            KeyDomeButton(label = "Lsh <<", onClick = { updateValue(BaseNEngine.lsh(currentVal, 1)) }, keyType = KeyDomeType.FUNCTION, modifier = Modifier.weight(1f), height = 40.dp)
            KeyDomeButton(label = "Rsh >>", onClick = { updateValue(BaseNEngine.rsh(currentVal, 1)) }, keyType = KeyDomeType.FUNCTION, modifier = Modifier.weight(1f), height = 40.dp)
            KeyDomeButton(label = "AC", onClick = { updateValue(0L); operandA = null; lastOperator = null }, keyType = KeyDomeType.ACTION_CLEAR, modifier = Modifier.weight(1f), height = 40.dp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hex & Digits Quick Keypad
        if (activeMode == BaseMode.HEX) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("A", "B", "C", "D", "E", "F").forEach { hKey ->
                    KeyDomeButton(
                        label = hKey,
                        onClick = {
                            val newBuf = inputBuffer + hKey
                            try {
                                val v = BaseNEngine.parse(newBuf, BaseMode.HEX)
                                updateValue(v)
                            } catch (_: Exception) {}
                        },
                        keyType = KeyDomeType.FUNCTION,
                        modifier = Modifier.weight(1f),
                        height = 38.dp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Numeric Digits (0..9, DEL, =)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("7", "8", "9").forEach { d ->
                KeyDomeButton(
                    label = d,
                    onClick = {
                        val newBuf = inputBuffer + d
                        try {
                            val v = BaseNEngine.parse(newBuf, activeMode)
                            updateValue(v)
                        } catch (_: Exception) {}
                    },
                    keyType = KeyDomeType.NUMBER,
                    modifier = Modifier.weight(1f),
                    height = 42.dp
                )
            }
            KeyDomeButton(
                label = "DEL",
                onClick = {
                    if (inputBuffer.isNotEmpty()) {
                        val newBuf = inputBuffer.dropLast(1)
                        val v = if (newBuf.isEmpty()) 0L else BaseNEngine.parse(newBuf, activeMode)
                        updateValue(v)
                    }
                },
                keyType = KeyDomeType.ACTION_CLEAR,
                modifier = Modifier.weight(1f),
                height = 42.dp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("4", "5", "6").forEach { d ->
                KeyDomeButton(
                    label = d,
                    onClick = {
                        val newBuf = inputBuffer + d
                        try {
                            val v = BaseNEngine.parse(newBuf, activeMode)
                            updateValue(v)
                        } catch (_: Exception) {}
                    },
                    keyType = KeyDomeType.NUMBER,
                    modifier = Modifier.weight(1f),
                    height = 42.dp
                )
            }
            KeyDomeButton(label = "+", onClick = { applyLogic("+") }, keyType = KeyDomeType.OPERATION, modifier = Modifier.weight(1f), height = 42.dp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("1", "2", "3").forEach { d ->
                KeyDomeButton(
                    label = d,
                    onClick = {
                        val newBuf = inputBuffer + d
                        try {
                            val v = BaseNEngine.parse(newBuf, activeMode)
                            updateValue(v)
                        } catch (_: Exception) {}
                    },
                    keyType = KeyDomeType.NUMBER,
                    modifier = Modifier.weight(1f),
                    height = 42.dp
                )
            }
            KeyDomeButton(label = "-", onClick = { applyLogic("-") }, keyType = KeyDomeType.OPERATION, modifier = Modifier.weight(1f), height = 42.dp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            KeyDomeButton(
                label = "0",
                onClick = {
                    val newBuf = inputBuffer + "0"
                    try {
                        val v = BaseNEngine.parse(newBuf, activeMode)
                        updateValue(v)
                    } catch (_: Exception) {}
                },
                keyType = KeyDomeType.NUMBER,
                modifier = Modifier.weight(1f),
                height = 42.dp
            )
            KeyDomeButton(label = "*", onClick = { applyLogic("*") }, keyType = KeyDomeType.OPERATION, modifier = Modifier.weight(1f), height = 42.dp)
            KeyDomeButton(label = "/", onClick = { applyLogic("/") }, keyType = KeyDomeType.OPERATION, modifier = Modifier.weight(1f), height = 42.dp)
            KeyDomeButton(label = "=", onClick = { executeEquals() }, keyType = KeyDomeType.EQUALS, modifier = Modifier.weight(1f), height = 42.dp)
        }
    }
}
