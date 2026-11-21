package com.example.antithef.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

class MotionDetector(
    context: Context,
    private val onReadingChanged: ((x: Float, y: Float, z: Float, magnitude: Float, delta: Float) -> Unit)? = null,
    private val onMotionDetected: (delta: Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var previousMagnitude: Float = 0f
    private var isFirstReading: Boolean = true
    private var isListening: Boolean = false
    private var isArmDetectionActive: Boolean = false
    private var sensitivity: Int = 50

    val isAvailable: Boolean
        get() = accelerometer != null

    fun setSensitivity(value: Int) {
        this.sensitivity = value.coerceIn(1, 100)
    }

    /**
     * Calculates acceleration delta threshold based on sensitivity (1 to 100).
     * High sensitivity (e.g. 90-100) -> lower threshold (1.0f - 1.5f).
     * Low sensitivity (e.g. 10-20) -> higher threshold (6.0f - 7.5f).
     */
    fun getThreshold(): Float {
        return 1.0f + (100 - sensitivity) * (7.0f / 99.0f)
    }

    fun startListening() {
        if (isListening || accelerometer == null) return
        isFirstReading = true
        previousMagnitude = 0f
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        isArmDetectionActive = false
    }

    fun setArmDetection(active: Boolean) {
        this.isArmDetectionActive = active
        if (active) {
            isFirstReading = true
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (isFirstReading) {
            previousMagnitude = magnitude
            isFirstReading = false
            onReadingChanged?.invoke(x, y, z, magnitude, 0f)
            return
        }

        val delta = abs(magnitude - previousMagnitude)
        previousMagnitude = magnitude

        onReadingChanged?.invoke(x, y, z, magnitude, delta)

        if (isArmDetectionActive) {
            val threshold = getThreshold()
            if (delta > threshold) {
                onMotionDetected(delta)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
