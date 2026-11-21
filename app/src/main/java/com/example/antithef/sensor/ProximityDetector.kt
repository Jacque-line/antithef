package com.example.antithef.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ProximityDetector(
    context: Context,
    private val onReadingChanged: ((distance: Float, isNear: Boolean, maxRange: Float) -> Unit)? = null,
    private val onNearDetected: (isNear: Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var isListening: Boolean = false
    private var isArmDetectionActive: Boolean = false
    private var lastNearState: Boolean? = null

    val isAvailable: Boolean
        get() = proximitySensor != null

    val maximumRange: Float
        get() = proximitySensor?.maximumRange ?: 5f

    fun startListening() {
        if (isListening || proximitySensor == null) return
        lastNearState = null
        sensorManager?.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        isArmDetectionActive = false
        lastNearState = null
    }

    fun setArmDetection(active: Boolean) {
        this.isArmDetectionActive = active
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return

        val distance = event.values[0]
        val maxRange = proximitySensor?.maximumRange ?: 5f
        // Many proximity sensors return a binary 0.0 (near) or maxRange (far)
        val isNear = distance < maxRange && distance < 5f

        onReadingChanged?.invoke(distance, isNear, maxRange)

        if (isArmDetectionActive) {
            // Trigger when changing from far to near (e.g. someone covers or reaches for the phone)
            if (isNear && lastNearState != true) {
                onNearDetected(true)
            }
        }
        lastNearState = isNear
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
