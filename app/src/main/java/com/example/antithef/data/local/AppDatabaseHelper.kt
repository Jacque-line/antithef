package com.example.antithef.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "antitheft_database.db"
        const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_USERS = "users"
        const val TABLE_SECURITY_SESSIONS = "security_sessions"
        const val TABLE_SECURITY_LOGS = "security_logs"
        const val TABLE_SETTINGS = "settings"

        // Users Columns
        const val COL_USER_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_USER_CREATED_AT = "created_at"

        // Sessions Columns
        const val COL_SESSION_ID = "id"
        const val COL_SESSION_USER_ID = "user_id"
        const val COL_SESSION_START_TIME = "start_time"
        const val COL_SESSION_END_TIME = "end_time"
        const val COL_SESSION_STATUS = "status"
        const val COL_SESSION_TOTAL_EVENTS = "total_events"

        // Logs Columns
        const val COL_LOG_ID = "id"
        const val COL_LOG_SESSION_ID = "session_id"
        const val COL_LOG_EVENT_TYPE = "event_type"
        const val COL_LOG_DESCRIPTION = "description"
        const val COL_LOG_TIMESTAMP = "timestamp"
        const val COL_LOG_ALARM_TRIGGERED = "alarm_triggered"

        // Settings Columns
        const val COL_SETTINGS_ID = "id"
        const val COL_SETTINGS_MOTION_SENSITIVITY = "motion_sensitivity"
        const val COL_SETTINGS_PROXIMITY_ENABLED = "proximity_enabled"
        const val COL_SETTINGS_ALARM_ENABLED = "alarm_enabled"
        const val COL_SETTINGS_ALARM_VOLUME = "alarm_volume"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL,
                $COL_USER_CREATED_AT TEXT
            )
        """.trimIndent()

        val createSessionsTable = """
            CREATE TABLE $TABLE_SECURITY_SESSIONS (
                $COL_SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SESSION_USER_ID INTEGER,
                $COL_SESSION_START_TIME TEXT NOT NULL,
                $COL_SESSION_END_TIME TEXT,
                $COL_SESSION_STATUS TEXT NOT NULL,
                $COL_SESSION_TOTAL_EVENTS INTEGER DEFAULT 0,
                FOREIGN KEY($COL_SESSION_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """.trimIndent()

        val createLogsTable = """
            CREATE TABLE $TABLE_SECURITY_LOGS (
                $COL_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOG_SESSION_ID INTEGER,
                $COL_LOG_EVENT_TYPE TEXT NOT NULL,
                $COL_LOG_DESCRIPTION TEXT,
                $COL_LOG_TIMESTAMP TEXT NOT NULL,
                $COL_LOG_ALARM_TRIGGERED INTEGER DEFAULT 0,
                FOREIGN KEY($COL_LOG_SESSION_ID) REFERENCES $TABLE_SECURITY_SESSIONS($COL_SESSION_ID)
            )
        """.trimIndent()

        val createSettingsTable = """
            CREATE TABLE $TABLE_SETTINGS (
                $COL_SETTINGS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SETTINGS_MOTION_SENSITIVITY INTEGER DEFAULT 50,
                $COL_SETTINGS_PROXIMITY_ENABLED INTEGER DEFAULT 1,
                $COL_SETTINGS_ALARM_ENABLED INTEGER DEFAULT 1,
                $COL_SETTINGS_ALARM_VOLUME INTEGER DEFAULT 80
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createSessionsTable)
        db.execSQL(createLogsTable)
        db.execSQL(createSettingsTable)

        // Seed default local user
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val userValues = ContentValues().apply {
            put(COL_USERNAME, "Device Owner")
            put(COL_USER_CREATED_AT, now)
        }
        db.insert(TABLE_USERS, null, userValues)

        // Seed default settings
        val settingsValues = ContentValues().apply {
            put(COL_SETTINGS_MOTION_SENSITIVITY, 50)
            put(COL_SETTINGS_PROXIMITY_ENABLED, 1)
            put(COL_SETTINGS_ALARM_ENABLED, 1)
            put(COL_SETTINGS_ALARM_VOLUME, 80)
        }
        db.insert(TABLE_SETTINGS, null, settingsValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SECURITY_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SECURITY_SESSIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}
