package com.example.antithef.data.model

data class SecuritySession(
    val id: Long = 0,
    val userId: Long = 1,
    val startTime: String,
    val endTime: String? = null,
    val status: String = "ACTIVE", // ACTIVE or INACTIVE
    val totalEvents: Int = 0
)
