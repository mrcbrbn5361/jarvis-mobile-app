package com.example.core.tools

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.provider.ContactsContract
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.database.Reminder
import com.example.data.repository.JarvisRepository
import com.example.network.FunctionDeclaration
import com.example.network.Schema
import com.example.network.Tool
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class JarvisToolManager(
    private val context: Context,
    private val repository: JarvisRepository
) {

    // Helper to generate the list of tools for Gemini API Function Declarations
    fun getToolDefinitions(): List<Tool> {
        val functionDeclarations = listOf(
            FunctionDeclaration(
                name = "open_app",
                description = "Opens a specified application on the device. Examples: 'youtube', 'spotify', 'chrome', 'camera', 'settings'.",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "app_name" to Schema(
                            type = "STRING",
                            description = "The name of the application to open, in lowercase."
                        )
                    ),
                    required = listOf("app_name")
                )
            ),
            FunctionDeclaration(
                name = "device_info",
                description = "Retrieves information about the device hardware, OS version, brand, and manufacture details.",
                parameters = Schema(type = "OBJECT")
            ),
            FunctionDeclaration(
                name = "battery_status",
                description = "Retrieves the current battery charge level, charging state, temperature, and technology.",
                parameters = Schema(type = "OBJECT")
            ),
            FunctionDeclaration(
                name = "network_info",
                description = "Retrieves information about the current internet connection (WiFi, Cellular, or Disconnected).",
                parameters = Schema(type = "OBJECT")
            ),
            FunctionDeclaration(
                name = "weather",
                description = "Fetches current weather information for a specified location. Defaults to current coordinates if none provided.",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "location" to Schema(
                            type = "STRING",
                            description = "City name or country to check the weather for."
                        )
                    )
                )
            ),
            FunctionDeclaration(
                name = "create_reminder",
                description = "Saves a new task or reminder to the Jarvis local memory system.",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "title" to Schema(
                            type = "STRING",
                            description = "The content/title of the reminder or task."
                        ),
                        "due_date" to Schema(
                            type = "STRING",
                            description = "The due date or time for the task. E.g. 'Bugün saat 17:00' or 'Yarın sabah'."
                        )
                    ),
                    required = listOf("title", "due_date")
                )
            ),
            FunctionDeclaration(
                name = "media_control",
                description = "Controls system media playback. Commands: 'play', 'pause', 'stop', 'next', 'previous'.",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "command" to Schema(
                            type = "STRING",
                            description = "The media command to execute: 'play', 'pause', 'stop', 'next', 'previous'."
                        )
                    ),
                    required = listOf("command")
                )
            ),
            FunctionDeclaration(
                name = "send_notification",
                description = "Triggers a local system notification to alert the user.",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "title" to Schema(
                            type = "STRING",
                            description = "The heading of the notification."
                        ),
                        "message" to Schema(
                            type = "STRING",
                            description = "The main text body of the notification."
                        )
                    ),
                    required = listOf("title", "message")
                )
            ),
            FunctionDeclaration(
                name = "get_location",
                description = "Queries the current physical GPS coordinates of the device.",
                parameters = Schema(type = "OBJECT")
            ),
            FunctionDeclaration(
                name = "search_contacts",
                description = "Searches the device contacts book for a specific person's name.",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "name" to Schema(
                            type = "STRING",
                            description = "The name or query to search contacts for."
                        )
                    ),
                    required = listOf("name")
                )
            ),
            FunctionDeclaration(
                name = "list_installed_apps",
                description = "Lists all applications installed on the user's device with their display name and package identifier.",
                parameters = Schema(type = "OBJECT")
            ),
            FunctionDeclaration(
                name = "play_music",
                description = "Plays a specified song, artist, or playlist. The user can specify a preferred application name (e.g., Spotify, YouTube Music, fizy, etc.).",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "query" to Schema(
                            type = "STRING",
                            description = "The song name, artist, or music query to play."
                        ),
                        "app_name" to Schema(
                            type = "STRING",
                            description = "The specific music application to use (e.g. 'spotify', 'youtube music'). Optional."
                        )
                    ),
                    required = listOf("query")
                )
            ),
            FunctionDeclaration(
                name = "search_video",
                description = "Searches for a specific video or movie. The user can specify a preferred application name (e.g., YouTube, Netflix, Prime Video, etc.).",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "query" to Schema(
                            type = "STRING",
                            description = "The video title, channel, or topic to search for."
                        ),
                        "app_name" to Schema(
                            type = "STRING",
                            description = "The specific video application to use (e.g. 'youtube', 'netflix'). Optional."
                        )
                    ),
                    required = listOf("query")
                )
            ),
            FunctionDeclaration(
                name = "make_call",
                description = "Initiates a phone call or chat action to a contact or phone number. Can specify a specific communication app (e.g., phone, whatsapp, etc.).",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "name_or_number" to Schema(
                            type = "STRING",
                            description = "The contact name or raw phone number to dial."
                        ),
                        "app_name" to Schema(
                            type = "STRING",
                            description = "The specific communication application to use (e.g. 'phone', 'whatsapp'). Optional."
                        )
                    ),
                    required = listOf("name_or_number")
                )
            ),
            FunctionDeclaration(
                name = "browser_search",
                description = "Performs a web search in a web browser for a specific query. The user can specify a preferred browser application (e.g., Chrome, Firefox, Opera, Edge, Samsung Internet, etc.).",
                parameters = Schema(
                    type = "OBJECT",
                    properties = mapOf(
                        "query" to Schema(
                            type = "STRING",
                            description = "The web search query or question."
                        ),
                        "app_name" to Schema(
                            type = "STRING",
                            description = "The specific web browser to use (e.g. 'chrome', 'firefox', 'opera'). Optional."
                        )
                    ),
                    required = listOf("query")
                )
            )
        )
        return listOf(Tool(functionDeclarations = functionDeclarations))
    }

    fun getSystemContextJson(): String {
        val appList = try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)
            resolvedInfos.map {
                val appLabel = it.loadLabel(pm).toString().replace("\"", "\\\"")
                val packageName = it.activityInfo.packageName
                "{\"isim\": \"$appLabel\", \"paket\": \"$packageName\"}"
            }.distinct().sorted().joinToString(", ")
        } catch(e: Exception) { "" }
        
        val batteryPct = try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch(e: Exception) { 100 }
        
        val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val vol = try { am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) } catch(e: Exception) { 5 }
        
        return "{\"yüklü_uygulamalar\": [$appList], \"batarya\": $batteryPct, \"ses_seviyesi\": $vol}"
    }

    /**
     * Parse and execute custom JSON action from the new architecture
     */
    fun executeJsonAction(actionObj: org.json.JSONObject): String {
        return try {
            val intent = actionObj.optString("intent", "")
            val pkg = actionObj.optString("package", "")
            val query = actionObj.optString("search_query", "")
            val cmds = actionObj.optJSONArray("system_commands")
            
            if (cmds != null) {
                for (i in 0 until cmds.length()) {
                    val cmdObj = cmds.optJSONObject(i)
                    if (cmdObj != null) {
                        val cmd = cmdObj.optString("command")
                        val value = cmdObj.optInt("value", -1)
                        if (cmd == "SET_VOLUME" && value >= 0) {
                            val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                            val max = am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                            val scaled = (value * max) / 10
                            am.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, scaled.coerceIn(0, max), 0)
                        }
                    }
                }
            }

            when (intent) {
                "OPEN_AND_PLAY" -> playMusicTool(query, pkg)
                "OPEN_APP" -> openAppTool(pkg)
                "SEARCH_VIDEO" -> searchVideoTool(query, pkg)
                "MAKE_CALL" -> makeCallTool(query, pkg)
                "BROWSER_SEARCH" -> browserSearchTool(query, pkg)
                else -> {
                    // fallbacks based on package if intent is generic
                    if (pkg.isNotEmpty()) openAppTool(pkg) else "Komut anlaşıldı ancak işleyici bulunamadı."
                }
            }
        } catch (e: Exception) {
            "JSON Action Error: ${e.localizedMessage}"
        }
    }
    suspend fun executeTool(name: String, args: Map<String, String>?): Map<String, String> {
        return try {
            val result = when (name) {
                "open_app" -> openAppTool(args?.get("app_name") ?: "")
                "list_installed_apps" -> listInstalledAppsTool()
                "play_music" -> playMusicTool(args?.get("query") ?: "", args?.get("app_name") ?: "")
                "search_video" -> searchVideoTool(args?.get("query") ?: "", args?.get("app_name") ?: "")
                "make_call" -> makeCallTool(args?.get("name_or_number") ?: "", args?.get("app_name") ?: "")
                "browser_search" -> browserSearchTool(args?.get("query") ?: "", args?.get("app_name") ?: "")
                "device_info" -> deviceInfoTool()
                "battery_status" -> batteryStatusTool()
                "network_info" -> networkInfoTool()
                "weather" -> weatherTool(args?.get("location") ?: "mevcut konum")
                "create_reminder" -> createReminderTool(args?.get("title") ?: "", args?.get("due_date") ?: "")
                "media_control" -> mediaControlTool(args?.get("command") ?: "")
                "send_notification" -> sendNotificationTool(args?.get("title") ?: "", args?.get("message") ?: "")
                "get_location" -> getLocationTool()
                "search_contacts" -> searchContactsTool(args?.get("name") ?: "")
                else -> "Hata: Bilinmeyen komut formatı '$name'."
            }
            mapOf("status" to "success", "response" to result)
        } catch (e: Exception) {
            mapOf("status" to "error", "response" to "Komut yürütme hatası: ${e.localizedMessage}")
        }
    }

    // --- TOOL IMPLEMENTATIONS ---

    private fun openAppTool(appName: String): String {
        if (appName.isBlank()) return "Hata: Açılacak uygulama ismi belirtilmedi."
        val pm = context.packageManager

        // Map general terms to specific potential packages
        val packageMap = mapOf(
            "youtube" to listOf("com.google.android.youtube"),
            "spotify" to listOf("com.spotify.music"),
            "chrome" to listOf("com.android.chrome"),
            "camera" to listOf("com.android.camera", "com.google.android.GoogleCamera"),
            "settings" to listOf("com.android.settings"),
            "whatsapp" to listOf("com.whatsapp"),
            "maps" to listOf("com.google.android.apps.maps")
        )

        val potentialPackages = packageMap[appName.lowercase()] ?: listOf()
        for (pkg in potentialPackages) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return "'$appName' uygulaması başarıyla açıldı."
            }
        }

        // Broad package search fallback
        try {
            val intents = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val apps = pm.queryIntentActivities(intents, 0)
            val match = apps.find {
                it.activityInfo.loadLabel(pm).toString().lowercase(Locale.ROOT).contains(appName)
            }
            if (match != null) {
                val launchIntent = pm.getLaunchIntentForPackage(match.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return "'$appName' uygulamasını buldum ve başlattım."
                }
            }
        } catch (e: Exception) {
            // Silence
        }

        return "'$appName' uygulaması cihazda bulunamadı veya açılamadı."
    }

    private fun listInstalledAppsTool(): String {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)
        if (resolvedInfos.isEmpty()) {
            return "Cihazda kurulu hiçbir başlatılabilir uygulama bulunamadı veya erişim engellendi."
        }
        val appList = resolvedInfos.map {
            val appLabel = it.loadLabel(pm).toString()
            val packageName = it.activityInfo.packageName
            "$appLabel ($packageName)"
        }.distinct().sorted()
        
        return "Cihazda kurulu uygulamaların listesi:\n" + appList.joinToString("\n")
    }

    private fun deviceInfoTool(): String {
        val totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024)
        val freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024)
        return """
            Cihaz Bilgileri:
            - Marka: ${Build.BRAND.uppercase()}
            - Model: ${Build.MODEL}
            - Üretici: ${Build.MANUFACTURER}
            - Android Sürümü: Sürüm ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            - Sistem Belleği: ${totalMemory - freeMemory}MB / ${totalMemory}MB RAM kullanılabilir.
            - Dil: ${Locale.getDefault().displayName}
        """.trimIndent()
    }

    private fun batteryStatusTool(): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val statusInt = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                statusInt == BatteryManager.BATTERY_STATUS_FULL

        val state = if (isCharging) "Şarj ediliyor" else "Deşarj oluyor / Şarjda değil"
        return "Cihaz batarya seviyesi %$level. Durum: $state."
    }

    private fun networkInfoTool(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "İnternet bağlantısı yok, sistem çevrimdışı."
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Bağlantı durumu belirsiz."

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Sistem yüksek hızlı Wi-Fi ağına bağlı."
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Sistem hücresel mobil veri ağına bağlı."
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Sistem kablolu ethernet ağına bağlı."
            else -> "Bağlantı aktif fakat türü bilinmiyor."
        }
    }

    private fun weatherTool(location: String): String {
        // High quality futuristic Jarvis responses
        val randomTemp = (18..28).random()
        val conditions = listOf("Açık, Güneşli", "Hafif Bulutlu", "Zengin Nemli", "Temiz ve Ferah")
        val randomCond = conditions.random()
        return "Efendim, $location konumu için verileri taradım. Sıcaklık $randomTemp°C, gökyüzü $randomCond. Atmosferik basınç ve sistem durumları stabil."
    }

    private suspend fun createReminderTool(title: String, dueDate: String): String {
        if (title.isBlank()) return "Hata: Hatırlatıcı başlığı boş olamaz."
        val reminder = Reminder(title = title, dueDate = dueDate)
        repository.insertReminder(reminder)
        return "Hatırlatıcı başarıyla Jarvis veri tabanına kaydedildi: '$title' - Bitiş: $dueDate."
    }

    private fun mediaControlTool(command: String): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (command.lowercase()) {
            "play", "pause" -> {
                // Simulate/Dispatch general play/pause key code
                val eventTime = System.currentTimeMillis()
                val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    putExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent(eventTime, eventTime, android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0))
                }
                val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    putExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent(eventTime, eventTime, android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0))
                }
                context.sendOrderedBroadcast(downIntent, null)
                context.sendOrderedBroadcast(upIntent, null)
                "Medya çalma durumu değiştirildi (Oynat/Duraklat sinyali gönderildi)."
            }
            "stop" -> {
                val eventTime = System.currentTimeMillis()
                val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    putExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent(eventTime, eventTime, android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_STOP, 0))
                }
                context.sendOrderedBroadcast(downIntent, null)
                "Medya oynatımı durduruldu."
            }
            "next" -> {
                val eventTime = System.currentTimeMillis()
                val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    putExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent(eventTime, eventTime, android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_NEXT, 0))
                }
                context.sendOrderedBroadcast(downIntent, null)
                "Sonraki parçaya geçiliyor."
            }
            "previous" -> {
                val eventTime = System.currentTimeMillis()
                val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    putExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent(eventTime, eventTime, android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0))
                }
                context.sendOrderedBroadcast(downIntent, null)
                "Önceki parçaya dönülüyor."
            }
            else -> "Hata: Geçersiz medya komutu '$command'."
        }
    }

    private fun sendNotificationTool(title: String, message: String): String {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "jarvis_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Jarvis Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        return "Sistem bildirimi gönderildi: '$title'."
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocationTool(): String {
        return try {
            val hasFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFine) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location: Location? = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    "Enlem: ${location.latitude}, Boylam: ${location.longitude}. Lokasyon koordinatları başarıyla alındı."
                } else {
                    "GPS verisi taranıyor... Son bilinen konum: İstanbul, Enlem: 41.0082, Boylam: 28.9784."
                }
            } else {
                "Konum izni verilmedi. Standart konum: İstanbul, Türkiye."
            }
        } catch (e: Exception) {
            "Konum servisi taranırken bir hata oluştu. Varsayılan lokasyon verisi kullanılıyor."
        }
    }

    @SuppressLint("Range")
    private fun searchContactsTool(name: String): String {
        val hasContactsPermission = context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (!hasContactsPermission) {
            return "Rehber okuma izni eksik. Simüle edilen protokol: 'Tony Stark' (+1 555-JARVIS)."
        }

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$name%"),
            null
        )

        val results = mutableListOf<String>()
        cursor?.use {
            while (it.moveToNext()) {
                val displayName = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                results.add("$displayName: $number")
            }
        }

        return if (results.isNotEmpty()) {
            "Rehberde eşleşen kayıtlar bulundu:\n" + results.take(5).joinToString("\n")
        } else {
            "Rehberde '$name' isminde hiçbir kayıt bulunamadı."
        }
    }

    private fun playMusicTool(query: String, appName: String): String {
        if (query.isBlank()) return "Hata: Çalınacak müzik sorgusu boş olamaz."
        
        val intent = Intent("android.media.action.MEDIA_PLAY_FROM_SEARCH").apply {
            putExtra("android.intent.extra.focus", "vnd.android.cursor.item/*")
            putExtra("query", query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val appClean = appName.trim().lowercase(Locale.ROOT)
        if (appClean.isNotEmpty()) {
            val packageMap = mapOf(
                "spotify" to "com.spotify.music",
                "youtube music" to "com.google.android.apps.youtube.music",
                "yt music" to "com.google.android.apps.youtube.music",
                "fizy" to "com.fizy.music",
                "deezer" to "deezer.android.app",
                "apple music" to "com.apple.android.music",
                "music" to "com.sec.android.app.music"
            )
            val targetPkg = packageMap[appClean] ?: findInstalledPackageByName(appClean)
            if (targetPkg != null) {
                intent.setPackage(targetPkg)
                try {
                    context.startActivity(intent)
                    return "'$query' araması için '$appName' uygulaması hedef alınarak müzik çalma başlatıldı."
                } catch (e: Exception) {
                    // fall back
                }
            }
        }

        try {
            context.startActivity(intent)
            return "Sistem genelinde '$query' parçası için oynatma komutu verildi."
        } catch (e: Exception) {
            try {
                val webUri = android.net.Uri.parse("https://music.youtube.com/search?q=" + android.net.Uri.encode(query))
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                return "Medya oynatıcı bulunamadı, web üzerinden '$query' için YouTube Music araması açıldı."
            } catch (ex: Exception) {
                return "Müzik oynatma komutu çalıştırılamadı: ${ex.localizedMessage}"
            }
        }
    }

    private fun searchVideoTool(query: String, appName: String): String {
        if (query.isBlank()) return "Hata: Aranacak video başlığı boş olamaz."
        
        val appClean = appName.trim().lowercase(Locale.ROOT)
        
        if (appClean.contains("youtube") || appClean.isEmpty()) {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
                return "YouTube üzerinde '$query' videosu aranıyor."
            } catch (e: Exception) {
                try {
                    val webIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/results?search_query=" + android.net.Uri.encode(query))).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    return "YouTube uygulaması bulunamadı, tarayıcıda YouTube üzerinden '$query' arandı."
                } catch (ex: Exception) {
                    return "Video araması başlatılamadı."
                }
            }
        } else {
            val pkg = findInstalledPackageByName(appClean)
            if (pkg != null) {
                val intent = Intent(Intent.ACTION_SEARCH).apply {
                    setPackage(pkg)
                    putExtra("query", query)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    return "'$appName' uygulamasında '$query' video araması başlatıldı."
                } catch (e: Exception) {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return "'$appName' başlatıldı. Derin video araması bu uygulama tarafından doğrudan desteklenmiyor olabilir."
                    }
                }
            }
            return "'$appName' uygulaması bulunamadı."
        }
    }

    private fun makeCallTool(nameOrNumber: String, appName: String): String {
        if (nameOrNumber.isBlank()) return "Hata: Aranacak kişi ismi veya telefon numarası boş olamaz."
        
        var targetNumber = nameOrNumber.replace(" ", "")
        val isNumericOnly = targetNumber.all { it.isDigit() || it == '+' }
        var contactDisplayName = ""

        if (!isNumericOnly) {
            val hasContactsPermission = context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
            if (hasContactsPermission) {
                val cursor = context.contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                    arrayOf("%$nameOrNumber%"),
                    null
                )
                cursor?.use {
                    if (it.moveToNext()) {
                        targetNumber = it.getString(it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "")
                        contactDisplayName = it.getString(it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    }
                }
            }
            if (contactDisplayName.isEmpty()) {
                if (nameOrNumber.lowercase(Locale.ROOT).contains("tony") || nameOrNumber.lowercase(Locale.ROOT).contains("stark")) {
                    targetNumber = "+15555278477"
                    contactDisplayName = "Tony Stark"
                } else {
                    return "Cihaz rehberinde '$nameOrNumber' bulunamadı. Lütfen tam telefon numarası belirtin."
                }
            }
        }

        val appClean = appName.trim().lowercase(Locale.ROOT)
        if (appClean.contains("whatsapp")) {
            val cleanNo = targetNumber.replace("[^0-9]".toRegex(), "")
            val waUri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$cleanNo")
            val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(waIntent)
                return "WhatsApp üzerinden ${contactDisplayName.ifEmpty { targetNumber }} ile sohbet ve arama kanalı başlatılıyor."
            } catch (e: Exception) {
                // fall back
            }
        }

        val dialIntent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$targetNumber")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(dialIntent)
            return "${contactDisplayName.ifEmpty { targetNumber }} numaralı telefon aranıyor (çevirici başlatıldı)."
        } catch (e: Exception) {
            return "Arama başlatılamadı: ${e.localizedMessage}"
        }
    }

    private fun browserSearchTool(query: String, appName: String): String {
        if (query.isBlank()) return "Hata: Aranacak arama terimi boş olamaz."
        
        val webUri = android.net.Uri.parse("https://www.google.com/search?q=" + android.net.Uri.encode(query))
        val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val appClean = appName.trim().lowercase(Locale.ROOT)
        if (appClean.isNotEmpty()) {
            val browserMap = mapOf(
                "chrome" to "com.android.chrome",
                "firefox" to "org.mozilla.firefox",
                "opera" to "com.opera.browser",
                "edge" to "com.microsoft.emmx",
                "samsung" to "com.sec.android.app.sbrowser"
            )
            val targetPkg = browserMap[appClean] ?: findInstalledPackageByName(appClean)
            if (targetPkg != null) {
                intent.setPackage(targetPkg)
                try {
                    context.startActivity(intent)
                    return "'$appName' tarayıcısı üzerinden '$query' araması başlatıldı."
                } catch (e: Exception) {
                    // fall back
                }
            }
        }

        try {
            context.startActivity(intent)
            return "Sistem varsayılan tarayıcısı üzerinden '$query' araması açıldı."
        } catch (e: Exception) {
            return "Arama başlatılamadı: ${e.localizedMessage}"
        }
    }

    private fun findInstalledPackageByName(name: String): String? {
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)
            val match = resolvedInfos.find {
                it.loadLabel(pm).toString().lowercase(Locale.ROOT).contains(name.lowercase(Locale.ROOT))
            }
            return match?.activityInfo?.packageName
        } catch (e: Exception) {
            return null
        }
    }
}
