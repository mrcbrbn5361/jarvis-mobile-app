package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Reminder
import com.example.presentation.JarvisViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: JarvisViewModel) {
    val reminders by viewModel.reminders.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDueDate by remember { mutableStateOf("Bugün saat 18:00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .testTag("reminders_screen")
    ) {
        // --- SCREEN HEADER ---
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "MAINFRAME GÖREV DEFTERİ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CyberDark),
            actions = {
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.testTag("add_reminder_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Task",
                        tint = CyberCyan
                    )
                }
            }
        )

        // --- REMINDERS MAIN BOARD ---
        if (reminders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Planlanmış Görev Yok",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CyberWhite,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "J.A.R.V.I.S sesli olarak 'Bugün saat beşte beni aramayı hatırlat' dediğinizde buraya otomatik olarak ekler.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CyberMuted),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reminders) { reminder ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Toggle status
                                IconButton(
                                    onClick = { viewModel.toggleReminder(reminder) },
                                    modifier = Modifier.testTag("reminder_checkbox_${reminder.id}")
                                ) {
                                    Icon(
                                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Toggle Complete",
                                        tint = if (reminder.isCompleted) CyberCyan else CyberMuted
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Task Title / Due Date
                                Column {
                                    Text(
                                        text = reminder.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = if (reminder.isCompleted) CyberMuted else CyberWhite,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Termin: ${reminder.dueDate}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CyberCyan.copy(alpha = 0.8f),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                            }

                            // Delete Action
                            IconButton(
                                onClick = { viewModel.deleteReminder(reminder) },
                                modifier = Modifier.testTag("reminder_delete_${reminder.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Reminder",
                                    tint = CyberOrange.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- MANUALLY ADD REMINDER DIALOG ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        text = "YENİ GÖREV EKLE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Görev/Açıklama", color = CyberMuted) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberMuted
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("add_reminder_input_title")
                        )

                        OutlinedTextField(
                            value = newDueDate,
                            onValueChange = { newDueDate = it },
                            label = { Text("Zamanlama / Termin", color = CyberMuted) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberMuted
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("add_reminder_input_due")
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                viewModel.sendMessage("create_reminder(title = '$newTitle', due_date = '$newDueDate')")
                                showDialog = false
                                newTitle = ""
                            }
                        }
                    ) {
                        Text("EKLE", color = CyberCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("İPTAL", color = CyberMuted, fontFamily = FontFamily.Monospace)
                    }
                },
                containerColor = CyberGray,
                textContentColor = CyberWhite
            )
        }
    }
}
