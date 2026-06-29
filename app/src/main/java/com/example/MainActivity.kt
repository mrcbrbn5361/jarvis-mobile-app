package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.JarvisDatabase
import com.example.data.repository.JarvisRepository
import com.example.presentation.JarvisApp
import com.example.presentation.JarvisViewModel
import com.example.presentation.JarvisViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = JarvisDatabase.getDatabase(applicationContext)
    val repository = JarvisRepository(
        chatLogDao = database.chatLogDao(),
        reminderDao = database.reminderDao(),
        userMemoryDao = database.userMemoryDao()
    )
    val viewModelFactory = JarvisViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, viewModelFactory)[JarvisViewModel::class.java]

    setContent {
      MyApplicationTheme {
        JarvisApp(viewModel = viewModel)
      }
    }
  }
}
