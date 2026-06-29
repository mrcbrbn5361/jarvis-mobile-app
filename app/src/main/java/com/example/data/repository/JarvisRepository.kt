package com.example.data.repository

import com.example.data.database.ChatLog
import com.example.data.database.ChatLogDao
import com.example.data.database.Reminder
import com.example.data.database.ReminderDao
import com.example.data.database.UserMemory
import com.example.data.database.UserMemoryDao
import kotlinx.coroutines.flow.Flow

class JarvisRepository(
    private val chatLogDao: ChatLogDao,
    private val reminderDao: ReminderDao,
    private val userMemoryDao: UserMemoryDao
) {
    val allChatLogs: Flow<List<ChatLog>> = chatLogDao.getAllChatLogs()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val allMemories: Flow<List<UserMemory>> = userMemoryDao.getAllMemories()

    // Chat Logs
    suspend fun insertChatLog(log: ChatLog) = chatLogDao.insertChatLog(log)
    suspend fun clearChatHistory() = chatLogDao.clearChatHistory()

    // Reminders
    suspend fun insertReminder(reminder: Reminder) = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: Reminder) = reminderDao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.deleteReminder(reminder)
    suspend fun deleteReminderById(id: Int) = reminderDao.deleteReminderById(id)

    // User Memories
    suspend fun insertMemory(key: String, value: String) {
        userMemoryDao.insertMemory(UserMemory(key = key, value = value))
    }
    suspend fun getMemory(key: String): String? {
        return userMemoryDao.getMemoryByKey(key)?.value
    }
    suspend fun deleteMemory(key: String) = userMemoryDao.deleteMemoryByKey(key)
    suspend fun clearAllMemories() = userMemoryDao.clearAllMemories()
}
