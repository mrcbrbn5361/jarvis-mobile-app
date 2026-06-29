package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.JarvisViewModel
import com.example.presentation.OrbState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: JarvisViewModel) {
    val chatLogs by viewModel.chatLogs.collectAsState()
    val orbState by viewModel.orbState.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom of the chat list on new messages
    LaunchedEffect(chatLogs.size) {
        if (chatLogs.isNotEmpty()) {
            listState.animateScrollToItem(chatLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .testTag("chat_screen")
    ) {
        // --- 1. SCREEN HEADER ---
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "MAINFRAME SOZEL SOHBET",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = CyberDark,
                titleContentColor = CyberCyan
            ),
            actions = {
                IconButton(
                    onClick = { viewModel.clearAllHistory() },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Session",
                        tint = CyberOrange
                    )
                }
            }
        )

        // --- 2. CONVERSATION LOG LIST ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (chatLogs.isEmpty()) {
                // Futuristic empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Core Star",
                        tint = CyberCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sistem protokolleri hazır, efendim.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CyberWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bana günlük asistanlık işlerinizi, batarya, konum, hatırlatıcı ve rehber komutlarını söyleyin. J.A.R.V.I.S olarak her an yardıma hazırım.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyberMuted,
                            lineHeight = 22.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatLogs) { log ->
                        val isUser = log.role == "user"
                        
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                // Message Sender Identifier Label
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
                                ) {
                                    if (!isUser) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Jarvis Logo",
                                            tint = CyberCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (isUser) "EFENDİM" else "J.A.R.V.I.S",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isUser) CyberMuted else CyberCyan,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                // Message bubble box
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) CyberGray else CyberGray.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isUser) CyberMuted.copy(alpha = 0.3f) else CyberCyan.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text(
                                        text = log.message,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = CyberWhite,
                                            lineHeight = 20.sp
                                        ),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. INPUT TEXT BAR FIELD PANEL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberGray),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Bir komut veya soru yazın...",
                            color = CyberMuted,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text"),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = CyberWhite,
                        unfocusedTextColor = CyberWhite
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendMessage(textInput)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank() && orbState != OrbState.Thinking,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (textInput.isNotBlank()) CyberCyan else CyberGray.copy(alpha = 0.5f)
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Command",
                        tint = if (textInput.isNotBlank()) Color.Black else CyberMuted
                    )
                }
            }
        }
    }
}
