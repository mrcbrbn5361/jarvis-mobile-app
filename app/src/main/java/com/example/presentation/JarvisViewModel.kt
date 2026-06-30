package com.example.presentation

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChatLog
import com.example.data.database.Reminder
import com.example.data.database.UserMemory
import com.example.data.repository.JarvisRepository
import com.example.core.tools.JarvisToolManager
import com.example.core.voice.JarvisVoiceManager
import com.example.network.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

sealed class OrbState {
    object Idle : OrbState()
    object Listening : OrbState()
    object Thinking : OrbState()
    object Speaking : OrbState()
    object Error : OrbState()
}

class JarvisViewModel(
    application: Application,
    private val repository: JarvisRepository
) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext

    // Shared preferences for secure API key and configurations
    private val prefs: SharedPreferences = context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    // Live state bindings
    val chatLogs: StateFlow<List<ChatLog>> = repository.allChatLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<UserMemory>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Status Fields
    private val _orbState = MutableStateFlow<OrbState>(OrbState.Idle)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _partialSpeechText = MutableStateFlow("")
    val partialSpeechText: StateFlow<String> = _partialSpeechText.asStateFlow()

    private val _consoleLogs = MutableStateFlow<List<String>>(listOf("Jarvis Mainframe v1.3.0 Başlatıldı.", "Sistem taraması: Kararlı.", "API ve Protokoller hazır."))
    val consoleLogs: StateFlow<List<String>> = _consoleLogs.asStateFlow()

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isBatteryCharging = MutableStateFlow(false)
    val isBatteryCharging: StateFlow<Boolean> = _isBatteryCharging.asStateFlow()

    private val _networkStatus = MutableStateFlow("Çevrimiçi (Wi-Fi)")
    val networkStatus: StateFlow<String> = _networkStatus.asStateFlow()

    // Configuration Settings (backed by prefs)
    val customApiKey = MutableStateFlow(prefs.getString("api_key", "") ?: "")
    val selectedModel = MutableStateFlow(prefs.getString("model_name", "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val speechPitch = MutableStateFlow(prefs.getFloat("speech_pitch", 1.0f))
    val speechRate = MutableStateFlow(prefs.getFloat("speech_rate", 1.05f))
    val userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")

    // Managers
    private val toolManager = JarvisToolManager(context, repository)
    private lateinit var voiceManager: JarvisVoiceManager

    init {
        initVoiceSystem()
        updateBatteryAndNetwork()
        // Periodically refresh battery status and console alerts
        startSystemPolling()
    }

    private fun initVoiceSystem() {
        voiceManager = JarvisVoiceManager(context) {
            voiceManager.setSpeechSettings(speechRate.value, speechPitch.value)
            addConsoleLog("Ses sentez motoru başarıyla entegre edildi.")
        }
    }

    fun saveConfig(apiKey: String, model: String, rate: Float, pitch: Float, nameOfUser: String) {
        viewModelScope.launch {
            prefs.edit().apply {
                putString("api_key", apiKey)
                putString("model_name", model)
                putFloat("speech_pitch", pitch)
                putFloat("speech_rate", rate)
                putString("user_name", nameOfUser)
                apply()
            }
            customApiKey.value = apiKey
            selectedModel.value = model
            speechRate.value = rate
            speechPitch.value = pitch
            userName.value = nameOfUser
            voiceManager.setSpeechSettings(rate, pitch)
            addConsoleLog("Sistem konfigürasyonları başarıyla güncellendi.")
        }
    }

    fun addConsoleLog(log: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _consoleLogs.value = _consoleLogs.value.takeLast(40) + "[$timestamp] $log"
    }

    private fun updateBatteryAndNetwork() {
        try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            _batteryLevel.value = if (level in 0..100) level else 99
            _isBatteryCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            // Update network status from Tool Manager helper
            viewModelScope.launch {
                val net = toolManager.executeTool("network_info", null)["response"] ?: "Çevrimiçi"
                _networkStatus.value = net.replace("Sistem ", "")
            }
        } catch (e: Exception) {
            // Silence
        }
    }

    private fun startSystemPolling() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                updateBatteryAndNetwork()
                handler.postDelayed(this, 15000) // Poll every 15s
            }
        })
    }

    // --- REPOSITORY / DATABASE WRAPPERS ---
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            addConsoleLog("Görev silindi: '${reminder.title}'.")
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isCompleted = !reminder.isCompleted)
            repository.updateReminder(updated)
            addConsoleLog("Görev durumu güncellendi: '${reminder.title}' (${if (updated.isCompleted) "Tamamlandı" else "Beklemede"}).")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            repository.clearAllMemories()
            _consoleLogs.value = listOf("Jarvis Mainframe: Tüm geçmiş veriler ve önbellek temizlendi.")
        }
    }

    // --- MAIN CHAT WORKFLOW WITH GEMINI AND TOOL CALLING ---
    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        viewModelScope.launch {
            // 1. Add User Message to Room Database
            val userLog = ChatLog(role = "user", message = userText)
            repository.insertChatLog(userLog)
            addConsoleLog("Kullanıcı komutu: '$userText'")

            // 2. Perform AI Loop
            executeAiInteraction(userText)
        }
    }

    // Voice action triggers SpeechRecognizer
    fun startVoiceInteraction() {
        viewModelScope.launch {
            voiceManager.stopSpeaking()
            _partialSpeechText.value = "Dinleniyor..."
            _orbState.value = OrbState.Listening

            voiceManager.startListening(
                onReady = {
                    _partialSpeechText.value = "Konuşun, sizi dinliyorum efendim..."
                    addConsoleLog("Mikrofon kanalı aktif. Dinleme moduna geçildi.")
                },
                onPartialResult = { partial ->
                    _partialSpeechText.value = partial
                },
                onResult = { text ->
                    _partialSpeechText.value = ""
                    _orbState.value = OrbState.Idle
                    sendMessage(text)
                },
                onError = { err ->
                    _partialSpeechText.value = ""
                    _orbState.value = OrbState.Error
                    addConsoleLog("Ses sistemi hatası: $err")
                    speakResponse("Mikrofon girişini algılayamadım efendim. Lütfen tekrar deneyin.")
                }
            )
        }
    }

    fun stopVoiceInteraction() {
        voiceManager.stopListening()
        _orbState.value = OrbState.Idle
        _partialSpeechText.value = ""
    }

    /**
     * Executes robust recursive interaction with Gemini supporting multiple tool calls
     */
    private suspend fun executeAiInteraction(
        promptText: String,
        imageBitmap: Bitmap? = null
    ) {
        _orbState.value = OrbState.Thinking
        addConsoleLog("Gemini API ile zihinsel bağ kuruluyor...")

        val apiKey = RetrofitClient.getResolvedApiKey(customApiKey.value)
        if (apiKey.isBlank()) {
            _orbState.value = OrbState.Error
            speakResponse("Efendim, sistem kontrol panelinde geçerli bir API anahtarı bulunamadı. Lütfen ayarlar bölümünden anahtarınızı yapılandırın.")
            return
        }

        val uName = userName.value.ifBlank { "Bay Stark" }
        val systemInstruction = """
            Sen J.A.R.V.I.S. (Just A Rather Very Intelligent System) adında, Tony Stark'ın (Iron Man) o meşhur yapay zeka asistanısın.
            Kullanıcıyla konuşurken son derece kibar, saygılı, hafif iğneleyici, esprili ve sadık bir asistan olacaksın.
            Kullanıcının ismi ya da hitap edilmesini istediği ad: $uName.
            Kullanıcıya kesinlikle "$uName", "Efendim" veya "Sir" tarzında hitap etmelisin.
            Dilin akıcı ve net bir Türkçe olmalıdır. Yanıtlarını çok uzun paragraflarla boğma, asistan gibi hızlı ve net cevaplar ver.
            Sana entegre edilmiş yerel Android sistem araçları (tools) bulunuyor. Bu araçları kullanarak telefondaki bataryayı, hava durumunu sorgulayabilir, uygulamaları başlatabilir, rehberi tarayabilir, hatırlatıcılar oluşturabilirsin.
            Komutları yürüttüğünde sonucu kullanıcıya J.A.R.V.I.S ses tonu ve tavrıyla sun.
        """.trimIndent()

        // Fetch past logs for chat context (take last 20 for memory efficiency)
        val historyLogs = chatLogs.value.takeLast(20)
        val contentsList = mutableListOf<Content>()

        // Map ChatLog entries to API Content models
        historyLogs.forEach { log ->
            contentsList.add(
                Content(
                    role = if (log.role == "user") "user" else "model",
                    parts = listOf(Part(text = log.message))
                )
            )
        }

        // Add current input
        if (imageBitmap != null) {
            // Encode image as Base64 for multi-modal analysis
            val base64Image = encodeImageToBase64(imageBitmap)
            contentsList.add(
                Content(
                    role = "user",
                    parts = listOf(
                        Part(text = promptText),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            )
        } else {
            // If the last content was already added by map above, avoid duplication
            val lastAdded = contentsList.lastOrNull()
            if (lastAdded == null || lastAdded.parts.firstOrNull()?.text != promptText) {
                contentsList.add(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = promptText))
                    )
                )
            }
        }

        try {
            // Construct request payload with definitions of our local tools
            val request = GenerateContentRequest(
                contents = contentsList,
                tools = toolManager.getToolDefinitions(),
                systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
            )

            val apiResponse = RetrofitClient.service.generateContent(selectedModel.value, apiKey, request)
            val candidate = apiResponse.candidates?.firstOrNull()
            val responseContent = candidate?.content
            val firstPart = responseContent?.parts?.firstOrNull()

            if (firstPart?.functionCall != null) {
                // GEMINI INSTRUCTED TO EXECUTE A TOOL
                val functionCall = firstPart.functionCall
                val toolName = functionCall.name
                val toolArgs = functionCall.args

                addConsoleLog("Yapay Zeka Protokolü Tetiklendi: $toolName()")
                _orbState.value = OrbState.Thinking

                // Execute the actual local tool
                val executionMap = toolManager.executeTool(toolName, toolArgs)
                val executionResult = executionMap["response"] ?: "İşlem tamamlandı fakat geri dönüş yok."

                addConsoleLog("Protokol Sonucu: $executionResult")

                // Inject the Tool response back to the chat context so Gemini can interpret it
                val updatedContents = contentsList.toMutableList().apply {
                    // Gemini expects the assistant's intermediate functionCall content first
                    add(
                        Content(
                            role = "model",
                            parts = listOf(Part(functionCall = functionCall))
                        )
                    )
                    // Then the system's functionResponse
                    add(
                        Content(
                            role = "function",
                            parts = listOf(
                                Part(
                                    functionResponse = FunctionResponse(
                                        name = toolName,
                                        response = mapOf("result" to executionResult)
                                    )
                                )
                            )
                        )
                    )
                }

                // Call Gemini again to compile final conversational response
                val finalRequest = GenerateContentRequest(
                    contents = updatedContents,
                    systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
                )

                val finalApiResponse = RetrofitClient.service.generateContent(selectedModel.value, apiKey, finalRequest)
                val finalResponse = finalApiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Protokol başarıyla tamamlandı efendim."

                saveAndSpeakAiResponse(finalResponse)

            } else if (firstPart?.text != null) {
                // NORMAL CHAT TEXT RESPONSE
                saveAndSpeakAiResponse(firstPart.text)
            } else {
                _orbState.value = OrbState.Error
                saveAndSpeakAiResponse("Sistem bir anomalilik saptadı efendim. Verileri derleyemedim.")
            }

        } catch (e: Exception) {
            _orbState.value = OrbState.Error
            val errorMsg = "Bağlantı hatası: ${e.localizedMessage}"
            addConsoleLog(errorMsg)
            saveAndSpeakAiResponse("Üzgünüm efendim, ana sunucularla bağlantı kurarken bir aksaklık yaşadım.")
        }
    }

    /**
     * CameraX Image analysis capability
     */
    fun analyzeCameraImage(bitmap: Bitmap, prompt: String) {
        viewModelScope.launch {
            addConsoleLog("Optik tarayıcı görüntü verisi işleniyor...")
            executeAiInteraction(
                promptText = if (prompt.isBlank()) "Bu görüntüyü detaylı olarak tarayıp analiz et efendim." else prompt,
                imageBitmap = bitmap
            )
        }
    }

    private suspend fun saveAndSpeakAiResponse(text: String) {
        // Save to Database
        val aiLog = ChatLog(role = "assistant", message = text)
        repository.insertChatLog(aiLog)

        // Trigger Speaking & Update State
        speakResponse(text)
    }

    private fun speakResponse(text: String) {
        voiceManager.speak(
            text = text,
            onStart = {
                _orbState.value = OrbState.Speaking
            },
            onDone = {
                _orbState.value = OrbState.Idle
            }
        )
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Compress to keep bandwidth light
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT).replace("\n", "")
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}

class JarvisViewModelFactory(
    private val application: Application,
    private val repository: JarvisRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JarvisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JarvisViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
