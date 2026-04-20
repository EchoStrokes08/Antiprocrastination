package com.example.antiprocrastination.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.antiprocrastination.ui.theme.*
import com.example.antiprocrastination.viewmodel.AppViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(viewModel: AppViewModel, navController: NavController) {
    var taskName    by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var nameError   by remember { mutableStateOf(false) }
    var dateError   by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("d-MMMM-yyyy")

    // Material3 DatePicker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("New Task", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Name field ────────────────────────────────────────────────────
            FormLabel("Task name *")
            OutlinedTextField(
                value         = taskName,
                onValueChange = { taskName = it; nameError = false },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("e.g. Lectura 4 PPC", color = Muted) },
                isError       = nameError,
                supportingText = if (nameError) ({ Text("Name is required", color = MaterialTheme.colorScheme.error) }) else null,
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Next
                ),
                shape  = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Primary,
                    unfocusedBorderColor = SurfaceVar
                )
            )

            // ── Date field ────────────────────────────────────────────────────
            FormLabel("Due date *")
            OutlinedTextField(
                value         = selectedDate?.format(dateFormatter) ?: "",
                onValueChange = {},
                modifier      = Modifier.fillMaxWidth(),
                readOnly      = true,
                placeholder   = { Text("Pick a date", color = Muted) },
                trailingIcon  = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick date",
                            tint = Primary)
                    }
                },
                isError       = dateError,
                supportingText = if (dateError) ({ Text("Date is required", color = MaterialTheme.colorScheme.error) }) else null,
                shape  = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Primary,
                    unfocusedBorderColor = SurfaceVar
                )
            )

            // ── Description field ─────────────────────────────────────────────
            FormLabel("Description (optional)")
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder   = { Text("Add more context…", color = Muted) },
                maxLines      = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                shape  = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Primary,
                    unfocusedBorderColor = SurfaceVar
                )
            )

            Spacer(Modifier.height(8.dp))

            // ── Action buttons ────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Muted)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        nameError = taskName.isBlank()
                        dateError = selectedDate == null
                        if (!nameError && !dateError) {
                            viewModel.addTask(taskName.trim(), selectedDate!!, description.trim())
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // ── DatePicker dialog ─────────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        dateError = false
                    }
                    showDatePicker = false
                }) { Text("OK", color = Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Muted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color      = OnSurface
    )
}
