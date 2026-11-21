package com.example.antithef.data.model

data class AppSettings(
    val id: Long = 1,
    val motionSensitivity: Int = 50, // 1 to 100
    val proximityEnabled: Boolean = true,
    val alarmEnabled: Boolean = true,
    val alarmVolume: Int = 80 // 0 to 100
)
