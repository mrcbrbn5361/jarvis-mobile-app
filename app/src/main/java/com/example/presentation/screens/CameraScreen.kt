package com.example.presentation.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.presentation.JarvisViewModel
import com.example.presentation.OrbState
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val orbState by viewModel.orbState.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var promptInput by remember { mutableStateOf("Bu nesneyi benim için analiz et efendim.") }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(CyberDark),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "OPTİK TARAMA MODÜLÜ",
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberDark)
                .testTag("camera_screen")
        ) {
            if (cameraPermissionState.status.isGranted) {
                // --- CAMERA VIEWPORT WITH CYBER RETICLE OVERLAY ---
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. CameraX Preview Node
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    
                                    imageCapture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                } catch (e: Exception) {
                                    Log.e("CameraScreen", "CameraX initialization failed", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 2. Beautiful Sci-Fi HUD HUD Target Overlay Drawn Dynamically
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val center = Offset(w / 2f, h / 2f)

                        // Reticle bounding lines
                        drawCircle(
                            color = CyberCyan.copy(alpha = 0.4f),
                            radius = 180f,
                            style = Stroke(width = 2f)
                        )
                        drawCircle(
                            color = CyberCyan.copy(alpha = 0.15f),
                            radius = 210f,
                            style = Stroke(width = 4f)
                        )

                        // Corners technical framing markers
                        val pad = 60f
                        val len = 120f
                        // Top-Left
                        drawLine(CyberCyan, Offset(pad, pad), Offset(pad + len, pad), strokeWidth = 5f)
                        drawLine(CyberCyan, Offset(pad, pad), Offset(pad, pad + len), strokeWidth = 5f)
                        // Top-Right
                        drawLine(CyberCyan, Offset(w - pad, pad), Offset(w - pad - len, pad), strokeWidth = 5f)
                        drawLine(CyberCyan, Offset(w - pad, pad), Offset(w - pad, pad + len), strokeWidth = 5f)
                        // Bottom-Left
                        drawLine(CyberCyan, Offset(pad, h - pad), Offset(pad + len, h - pad), strokeWidth = 5f)
                        drawLine(CyberCyan, Offset(pad, h - pad), Offset(pad, h - pad - len), strokeWidth = 5f)
                        // Bottom-Right
                        drawLine(CyberCyan, Offset(w - pad, h - pad), Offset(w - pad - len, h - pad), strokeWidth = 5f)
                        drawLine(CyberCyan, Offset(w - pad, h - pad), Offset(w - pad, h - pad - len), strokeWidth = 5f)

                        // Holographic crosshair lines
                        drawLine(CyberCyan.copy(alpha = 0.3f), Offset(center.x - 300f, center.y), Offset(center.x - 100f, center.y), strokeWidth = 2f)
                        drawLine(CyberCyan.copy(alpha = 0.3f), Offset(center.x + 100f, center.y), Offset(center.x + 300f, center.y), strokeWidth = 2f)
                        drawLine(CyberCyan.copy(alpha = 0.3f), Offset(center.x, center.y - 300f), Offset(center.x, center.y - 100f), strokeWidth = 2f)
                        drawLine(CyberCyan.copy(alpha = 0.3f), Offset(center.x, center.y + 100f), Offset(center.x, center.y + 300f), strokeWidth = 2f)
                    }

                    // 3. Command HUD Overlay Card (Floating Action Bar)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(bottom = 64.dp) // Adjust above navigation
                            .fillMaxWidth()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "YAPAY ZEKA TARAMA ANALİZİ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = promptInput,
                                    onValueChange = { promptInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(color = CyberWhite),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberCyan,
                                        unfocusedBorderColor = CyberMuted.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Capture Button
                                Button(
                                    onClick = {
                                        val capture = imageCapture
                                        if (capture != null && orbState != OrbState.Thinking) {
                                            viewModel.addConsoleLog("Optik Deklanşör tetiklendi, görüntü yakalanıyor...")
                                            capture.takePicture(
                                                ContextCompat.getMainExecutor(context),
                                                object : ImageCapture.OnImageCapturedCallback() {
                                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                                        try {
                                                            val buffer = imageProxy.planes[0].buffer
                                                            val bytes = ByteArray(buffer.remaining())
                                                            buffer.get(bytes)
                                                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                            
                                                            // Pass to ViewModel for multi-modal Gemini analysis
                                                            viewModel.analyzeCameraImage(bitmap, promptInput)
                                                        } catch (e: Exception) {
                                                            viewModel.addConsoleLog("Hata: Görüntü işlenemedi.")
                                                        } finally {
                                                            imageProxy.close()
                                                        }
                                                    }

                                                    override fun onError(exception: ImageCaptureException) {
                                                        viewModel.addConsoleLog("Kamera yakalama hatası: ${exception.localizedMessage}")
                                                    }
                                                }
                                            )
                                        } else {
                                            // Fallback simulation if ImageCapture setup is delayed/virtual
                                            viewModel.addConsoleLog("Optik simülatör devrede: Görüntü yakalanıyor...")
                                            // Let's create a small dummy blank/colored bitmap so user can test scanning on emulators!
                                            val dummyBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                                            viewModel.analyzeCameraImage(dummyBitmap, promptInput)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("capture_analyze_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Capture",
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (orbState == OrbState.Thinking) "ANALİZ EDİLİYOR..." else "GÖRÜNTÜYÜ TARA & ANALİZ ET",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // --- PERMISSION REQUEST PROMPT CARD ---
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberGray),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Camera Permission Required",
                            tint = CyberCyan,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Kamera İzni Gerekli",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberWhite,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "J.A.R.V.I.S'in optik tarayıcısını ve gerçek zamanlı nesne tahlil modülünü kullanabilmek için kamera iznini onaylamanız gerekmektedir.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = CyberMuted),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("İzin Ver", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
