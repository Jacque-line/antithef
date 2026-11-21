package com.example.antithef.data.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.antithef.data.local.AppDatabaseHelper
import com.example.antithef.data.model.AppSettings
import com.example.antithef.data.model.SecurityLog
import com.example.antithef.data.model.SecuritySession
import com.example.antithef.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecurityDao(private val dbHelper: AppDatabaseHelper) {

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    // --- Users ---
    fun getUser(): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDatabaseHelper.TABLE_USERS,
            null,
            null,
            null,
            null,
            null,
            "${AppDatabaseHelper.COL_USER_ID} ASC",
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getLong(it.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_ID)),
                    username = it.getString(it.getColumnIndexOrThrow(AppDatabaseHelper.COL_USERNAME)),
                    createdAt = it.getString(it.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_CREATED_AT)) ?: ""
                )
            } else null
        }
    }

    // --- Sessions ---
    fun startSession(userId: Long = 1): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COL_SESSION_USER_ID, userId)
            put(AppDatabaseHelper.COL_SESSION_START_TIME, getCurrentTimestamp())
            put(AppDatabaseHelper.COL_SESSION_STATUS, "ACTIVE")
            put(AppDatabaseHelper.COL_SESSION_TOTAL_EVENTS, 0)
        }
        return db.insert(AppDatabaseHelper.TABLE_SECURITY_SESSIONS, null, values)
    }

    fun endSession(sessionId: Long) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COL_SESSION_END_TIME, getCurrentTimestamp())
            put(AppDatabaseHelper.COL_SESSION_STATUS, "INACTIVE")
        }
        db.update(
            AppDatabaseHelper.TABLE_SECURITY_SESSIONS,
            values,
            "${AppDatabaseHelper.COL_SESSION_ID} = ?",
            arrayOf(sessionId.toString())
        )
    }

    fun getActiveSession(): SecuritySession? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDatabaseHelper.TABLE_SECURITY_SESSIONS,
            null,
            "${AppDatabaseHelper.COL_SESSION_STATUS} = ?",
            arrayOf("ACTIVE"),
            null,
            null,
            "${AppDatabaseHelper.COL_SESSION_ID} DESC",
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) mapCursorToSession(it) else null
        }
    }

    fun incrementSessionEvents(sessionId: Long) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${AppDatabaseHelper.TABLE_SECURITY_SESSIONS} " +
                    "SET ${AppDatabaseHelper.COL_SESSION_TOTAL_EVENTS} = ${AppDatabaseHelper.COL_SESSION_TOTAL_EVENTS} + 1 " +
                    "WHERE ${AppDatabaseHelper.COL_SESSION_ID} = ?",
            arrayOf(sessionId)
        )
    }

    // --- Security Logs ---
    fun insertLog(
        sessionId: Long?,
        eventType: String,
        description: String,
        alarmTriggered: Boolean
    ): Long {
        val db = dbHelper.writableDatabase
        val now = getCurrentTimestamp()
        val values = ContentValues().apply {
            if (sessionId != null && sessionId > 0) {
                put(AppDatabaseHelper.COL_LOG_SESSION_ID, sessionId)
            }
            put(AppDatabaseHelper.COL_LOG_EVENT_TYPE, eventType)
            put(AppDatabaseHelper.COL_LOG_DESCRIPTION, description)
            put(AppDatabaseHelper.COL_LOG_TIMESTAMP, now)
            put(AppDatabaseHelper.COL_LOG_ALARM_TRIGGERED, if (alarmTriggered) 1 else 0)
        }
        val id = db.insert(AppDatabaseHelper.TABLE_SECURITY_LOGS, null, values)
        if (sessionId != null && sessionId > 0) {
            incrementSessionEvents(sessionId)
        }
        return id
    }

    fun getAllLogs(): List<SecurityLog> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<SecurityLog>()
        val cursor = db.query(
            AppDatabaseHelper.TABLE_SECURITY_LOGS,
            null,
            null,
            null,
            null,
            null,
            "${AppDatabaseHelper.COL_LOG_ID} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapCursorToLog(it))
            }
        }
        return list
    }

    fun getTotalLogsCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${AppDatabaseHelper.TABLE_SECURITY_LOGS}", null)
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    fun getTotalAlarmsTriggeredCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${AppDatabaseHelper.TABLE_SECURITY_LOGS} WHERE ${AppDatabaseHelper.COL_LOG_ALARM_TRIGGERED} = 1",
            null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    fun clearAllLogs() {
        val db = dbHelper.writableDatabase
        db.delete(AppDatabaseHelper.TABLE_SECURITY_LOGS, null, null)
    }

    fun deleteLog(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(
            AppDatabaseHelper.TABLE_SECURITY_LOGS,
            "${AppDatabaseHelper.COL_LOG_ID} = ?",
            arrayOf(id.toString())
        )
    }

    // --- Settings ---
    fun getSettings(): AppSettings {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDatabaseHelper.TABLE_SETTINGS,
            null,
            null,
            null,
            null,
            null,
            "${AppDatabaseHelper.COL_SETTINGS_ID} ASC",
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) {
                AppSettings(
                    id = it.getLong(it.getColumnIndexOrThrow(AppDatabaseHelper.COL_SETTINGS_ID)),
                    motionSensitivity = it.getInt(it.getColumnIndexOrThrow(AppDatabaseHelper.COL_SETTINGS_MOTION_SENSITIVITY)),
                    proximityEnabled = it.getInt(it.getColumnIndexOrThrow(AppDatabaseHelper.COL_SETTINGS_PROXIMITY_ENABLED)) == 1,
                    alarmEnabled = it.getInt(it.getColumnIndexOrThrow(AppDatabaseHelper.COL_SETTINGS_ALARM_ENABLED)) == 1,
                    alarmVolume = it.getInt(it.getColumnIndexOrThrow(AppDatabaseHelper.COL_SETTINGS_ALARM_VOLUME))
                )
            } else {
                AppSettings()
            }
        }
    }

    fun updateSettings(settings: AppSettings) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COL_SETTINGS_MOTION_SENSITIVITY, settings.motionSensitivity)
            put(AppDatabaseHelper.COL_SETTINGS_PROXIMITY_ENABLED, if (settings.proximityEnabled) 1 else 0)
            put(AppDatabaseHelper.COL_SETTINGS_ALARM_ENABLED, if (settings.alarmEnabled) 1 else 0)
            put(AppDatabaseHelper.COL_SETTINGS_ALARM_VOLUME, settings.alarmVolume)
        }
        db.update(
            AppDatabaseHelper.TABLE_SETTINGS,
            values,
            "${AppDatabaseHelper.COL_SETTINGS_ID} = ?",
            arrayOf(settings.id.toString())
        )
    }

    // --- Helpers ---
    private fun mapCursorToSession(cursor: Cursor): SecuritySession {
        return SecuritySession(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_SESSION_ID)),
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_SESSION_USER_ID)),
            startTime = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_SESSION_START_TIME)),
            endTime = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_SESSION_END_TIME)),
            status = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_SESSION_STATUS)),
            totalEvents = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_SESSION_TOTAL_EVENTS))
        )
    }

    private fun mapCursorToLog(cursor: Cursor): SecurityLog {
        val sessionIdCol = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_LOG_SESSION_ID)
        val sessionId = if (cursor.isNull(sessionIdCol)) null else cursor.getLong(sessionIdCol)
        return SecurityLog(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_LOG_ID)),
            sessionId = sessionId,
            eventType = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_LOG_EVENT_TYPE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_LOG_DESCRIPTION)) ?: "",
            timestamp = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_LOG_TIMESTAMP)),
            alarmTriggered = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_LOG_ALARM_TRIGGERED)) == 1
        )
    }
}
