package com.example.antithef.data.model

data class SecurityLog(
    val id: Long = 0,
    val sessionId: Long? = null,
    val eventType: String, // MOTION, PROXIMITY, ALARM, SYSTEM
    val description: String,
    val timestamp: String,
    val alarmTriggered: Boolean = false
)
