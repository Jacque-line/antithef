package com.example.antithef.data.model

data class SensorReading(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,
    val delta: Float = 0f,
    val isAccelerometerAvailable: Boolean = false,
    val proximityDistance: Float = 0f,
    val proximityMaxRange: Float = 5f,
    val isProximityNear: Boolean = false,
    val isProximityAvailable: Boolean = false
)
