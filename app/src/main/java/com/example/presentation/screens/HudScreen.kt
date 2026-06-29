package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.JarvisViewModel
import com.example.presentation.OrbState
import com.example.presentation.components.JarvisOrb
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HudScreen(
    viewModel: JarvisViewModel,
    onNavigateToChat: () -> Unit
) {
    val orbState by viewModel.orbState.collectAsState()
    val partialSpeechText by viewModel.partialSpeechText.collectAsState()
    val consoleLogs by viewModel.consoleLogs.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isBatteryCharging.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    // Keep track of time
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    val terminalScrollState = rememberLazyListState()
    // Auto scroll console terminal when new logs are added
    LaunchedEffect(consoleLogs.size) {
        if (consoleLogs.isNotEmpty()) {
            terminalScrollState.animateScrollToItem(consoleLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberDark, CyberGray.copy(alpha = 0.8f))
                )
            )
            .padding(16.dp)
            .testTag("hud_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. GLASSMORPHIC TOP TELEMETRY BAR ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "MAINFRAME CLOCK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberMuted,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Battery Status
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "POWER STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberMuted,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                            contentDescription = "Battery",
                            tint = if (batteryLevel > 30) CyberCyan else CyberOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%$batteryLevel",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CyberWhite,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // Network Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "COMMS PROTOCOL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberMuted,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Network",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (networkStatus.length > 10) networkStatus.substring(0, 10) + ".." else networkStatus,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyberWhite,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }

        // --- 2. THE MAIN SHINING JARVIS ORB CORE ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive clickable Jarvis Orb
                JarvisOrb(
                    orbState = orbState,
                    modifier = Modifier
                        .clickable {
                            if (orbState == OrbState.Listening) {
                                viewModel.stopVoiceInteraction()
                            } else {
                                viewModel.startVoiceInteraction()
                            }
                        }
                        .padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action status text
                Text(
                    text = when (orbState) {
                        OrbState.Idle -> "STANDBY"
                        OrbState.Listening -> "LISTENING PROTOCOLS"
                        OrbState.Thinking -> "PROCESSING MAINFRAME"
                        OrbState.Speaking -> "JARVIS SYNTHESIS"
                        OrbState.Error -> "SYSTEM ANOMALY"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = when (orbState) {
                            OrbState.Error -> CyberOrange
                            OrbState.Thinking -> CyberPurple
                            else -> CyberCyan
                        },
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )

                // Partial Speech text bubble overlay
                AnimatedVisibility(
                    visible = partialSpeechText.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCyan.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = partialSpeechText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CyberWhite,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- 3. THE SCROLLING CYBER MAINFRAME TERMINAL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.15f))
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                LazyColumn(
                    state = terminalScrollState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(consoleLogs) { log ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "> ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (log.contains("Hata") || log.contains("SYSTEM ANOMALY")) CyberOrange else CyberWhite.copy(alpha = 0.9f),
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- 4. HUD INTERACTIVE PROTOCOL SHORTCUTS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val shortCuts = listOf(
                Triple("Hava Durumu", "Bugün hava nasıl?", Icons.Default.Cloud),
                Triple("Cihaz Analizi", "Sistem özelliklerini ve hafızayı test et.", Icons.Default.Settings),
                Triple("Konumum", "Konum koordinatlarımı çıkar.", Icons.Default.LocationOn),
                Triple("Taramayı Başlat", "Tüm sistemleri ve bataryayı analiz et.", Icons.Default.Refresh)
            )

            shortCuts.forEach { (label, actionPrompt, icon) ->
                OutlinedButton(
                    onClick = { viewModel.sendMessage(actionPrompt) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(16.dp),
                        tint = CyberCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- 5. THE ULTIMATE ARC REACTOR GLOW MICROPHONE CONTROL ---
        Box(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 4.dp)
                .size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple glow effect
            val pulseTransition = rememberInfiniteTransition(label = "reactor_pulse")
            val pulseSize by pulseTransition.animateFloat(
                initialValue = 60f,
                targetValue = 78f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutQuad),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "size"
            )

            Box(
                modifier = Modifier
                    .size(pulseSize.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        if (orbState == OrbState.Listening) {
                            CyberCyan.copy(alpha = 0.25f)
                        } else {
                            CyberCyan.copy(alpha = 0.08f)
                        }
                    )
            )

            IconButton(
                onClick = {
                    if (orbState == OrbState.Listening) {
                        viewModel.stopVoiceInteraction()
                    } else {
                        viewModel.startVoiceInteraction()
                    }
                },
                modifier = Modifier
                    .size(60.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CyberGray, Color.Black)
                        )
                    )
                    .border(
                        2.dp,
                        if (orbState == OrbState.Listening) CyberOrange else CyberCyan,
                        androidx.compose.foundation.shape.CircleShape
                    )
                    .testTag("voice_trigger_button")
            ) {
                Icon(
                    imageVector = if (orbState == OrbState.Listening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Trigger Jarvis Listening",
                    tint = if (orbState == OrbState.Listening) CyberOrange else CyberCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
