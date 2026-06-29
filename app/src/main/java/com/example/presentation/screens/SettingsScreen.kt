package com.example.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.JarvisViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val customApiKey by viewModel.customApiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val speechPitch by viewModel.speechPitch.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var inputKey by remember { mutableStateOf(customApiKey) }
    var modelSelector by remember { mutableStateOf(selectedModel) }
    var pitchSlider by remember { mutableStateOf(speechPitch) }
    var rateSlider by remember { mutableStateOf(speechRate) }
    var inputName by remember { mutableStateOf(userName) }

    // Check actual platform permissions
    val micGranted = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    val cameraGranted = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val locationGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val contactsGranted = context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SCREEN TITLE ---
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "KONTROL PANELİ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CyberDark)
        )

        // --- 1. SECURE API CREDENTIALS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = "API Keys", tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MAINFRAME ANALİTİK ANAHTARI (GEMINI)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberWhite,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = { Text("AI Studio Gemini API Key", color = CyberMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("api_key_field"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberMuted
                    )
                )

                // --- USER NAME HITAP AYARI ---
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "User Name", tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KULLANICI İSMİ (HİTAP)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberWhite,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    placeholder = { Text("Örn: Bay Stark, Efendim, Mirac", color = CyberMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("user_name_field"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberMuted
                    )
                )

                // Model selection Dropdown options (simplified with manual segment buttons)
                Text(
                    text = "YAPAY ZEKA MODELİ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberMuted,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val availableModels = listOf(
                        "gemini-3.5-flash",
                        "gemini-3.1-pro-preview",
                        "gemini-3.1-flash-lite-preview",
                        "gemini-2.5-flash",
                        "gemini-2.5-pro"
                    )
                    availableModels.chunked(2).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { model ->
                                val isSel = modelSelector == model
                                Button(
                                    onClick = { modelSelector = model },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) CyberCyan else CyberGray
                                    ),
                                    border = if (!isSel) BorderStroke(1.dp, CyberMuted.copy(alpha = 0.4f)) else null,
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = model.replace("gemini-", ""),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.Black else CyberWhite,
                                        maxLines = 1,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            if (chunk.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // --- 2. VOCAL INTERFACE SYNTH SOUND SPEECH CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SES VE SES SENTEZLEYİCİ AYARLARI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )

                // Speech rate
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Konuşma Hızı (Rate)", color = CyberWhite, fontSize = 12.sp)
                        Text(text = String.format("%.2f", rateSlider), color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = rateSlider,
                        onValueChange = { rateSlider = it },
                        valueRange = 0.6f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )
                }

                // Speech pitch
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Konuşma Tonu (Pitch)", color = CyberWhite, fontSize = 12.sp)
                        Text(text = String.format("%.2f", pitchSlider), color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = pitchSlider,
                        onValueChange = { pitchSlider = it },
                        valueRange = 0.5f..1.8f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )
                }

                // Voice sound synthesizer test action
                Button(
                    onClick = {
                        viewModel.saveConfig(inputKey, modelSelector, rateSlider, pitchSlider, inputName)
                        val greetName = if (inputName.isBlank()) "efendim" else inputName
                        viewModel.sendMessage("Sistem ayarları güncellendi. Yeni protokoller devrede, $greetName.")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_settings_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("KONFİGÜRASYONU DEPOLA & TEST ET", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 3. HARDWARE PERMISSIONS STATUS BOARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SİSTEM İZİNLERİ VE BÜTÜNLÜK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )

                val permList = listOf(
                    "Mikrofon İzni (RECORD_AUDIO)" to micGranted,
                    "Kamera İzni (CAMERA)" to cameraGranted,
                    "Konum İzni (LOCATION)" to locationGranted,
                    "Rehber İzni (READ_CONTACTS)" to contactsGranted
                )

                permList.forEach { (name, granted) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name, color = CyberWhite, fontSize = 12.sp)
                        Text(
                            text = if (granted) "AKTİF" else "KAPALI",
                            color = if (granted) CyberCyan else CyberOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- 4. CLEAR CACHE & REBOOT SYSTEM ACTION ---
        Button(
            onClick = { viewModel.clearAllHistory() },
            modifier = Modifier.fillMaxWidth().testTag("clear_all_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear Cache")
            Spacer(modifier = Modifier.width(8.dp))
            Text("MAINFRAME RESETLE (TÜM BELLEĞİ VE GEÇMİŞİ TEMİZLE)", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
