package com.example.antithef.data.repository

import com.example.antithef.data.dao.SecurityDao
import com.example.antithef.data.model.AppSettings
import com.example.antithef.data.model.SecurityLog
import com.example.antithef.data.model.SecuritySession
import com.example.antithef.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecurityRepository(private val dao: SecurityDao) {

    suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        dao.getUser()
    }

    suspend fun startSecuritySession(userId: Long = 1): Long = withContext(Dispatchers.IO) {
        dao.startSession(userId)
    }

    suspend fun endSecuritySession(sessionId: Long) = withContext(Dispatchers.IO) {
        dao.endSession(sessionId)
    }

    suspend fun getActiveSession(): SecuritySession? = withContext(Dispatchers.IO) {
        dao.getActiveSession()
    }

    suspend fun logEvent(
        sessionId: Long?,
        eventType: String,
        description: String,
        alarmTriggered: Boolean
    ): Long = withContext(Dispatchers.IO) {
        dao.insertLog(sessionId, eventType, description, alarmTriggered)
    }

    suspend fun getAllLogs(): List<SecurityLog> = withContext(Dispatchers.IO) {
        dao.getAllLogs()
    }

    suspend fun getTotalLogsCount(): Int = withContext(Dispatchers.IO) {
        dao.getTotalLogsCount()
    }

    suspend fun getTotalAlarmsTriggeredCount(): Int = withContext(Dispatchers.IO) {
        dao.getTotalAlarmsTriggeredCount()
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearAllLogs()
    }

    suspend fun deleteLog(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteLog(id)
    }

    suspend fun getSettings(): AppSettings = withContext(Dispatchers.IO) {
        dao.getSettings()
    }

    suspend fun updateSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        dao.updateSettings(settings)
    }
}
