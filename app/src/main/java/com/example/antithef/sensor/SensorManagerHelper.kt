package com.example.antithef.sensor

import android.content.Context
import com.example.antithef.data.model.SensorReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SensorManagerHelper(
    context: Context,
    private val onMotionAlarmTriggered: (delta: Float) -> Unit,
    private val onProximityAlarmTriggered: () -> Unit
) {

    private val _sensorReading = MutableStateFlow(SensorReading())
    val sensorReading: StateFlow<SensorReading> = _sensorReading.asStateFlow()

    private val motionDetector = MotionDetector(
        context = context,
        onReadingChanged = { x, y, z, magnitude, delta ->
            _sensorReading.update {
                it.copy(
                    x = x,
                    y = y,
                    z = z,
                    magnitude = magnitude,
                    delta = delta,
                    isAccelerometerAvailable = true
                )
            }
        },
        onMotionDetected = { delta ->
            onMotionAlarmTriggered(delta)
        }
    )

    private val proximityDetector = ProximityDetector(
        context = context,
        onReadingChanged = { distance, isNear, maxRange ->
            _sensorReading.update {
                it.copy(
                    proximityDistance = distance,
                    proximityMaxRange = maxRange,
                    isProximityNear = isNear,
                    isProximityAvailable = true
                )
            }
        },
        onNearDetected = {
            onProximityAlarmTriggered()
        }
    )

    init {
        _sensorReading.update {
            it.copy(
                isAccelerometerAvailable = motionDetector.isAvailable,
                isProximityAvailable = proximityDetector.isAvailable,
                proximityMaxRange = proximityDetector.maximumRange
            )
        }
    }

    val isAccelerometerAvailable: Boolean
        get() = motionDetector.isAvailable

    val isProximityAvailable: Boolean
        get() = proximityDetector.isAvailable

    fun setSensitivity(sensitivity: Int) {
        motionDetector.setSensitivity(sensitivity)
    }

    fun getMotionThreshold(): Float {
        return motionDetector.getThreshold()
    }

    fun startMonitoring() {
        motionDetector.startListening()
        proximityDetector.startListening()
    }

    fun stopMonitoring() {
        motionDetector.stopListening()
        proximityDetector.stopListening()
    }

    fun setArmed(armed: Boolean, proximityEnabled: Boolean) {
        motionDetector.setArmDetection(armed)
        proximityDetector.setArmDetection(armed && proximityEnabled)
    }
}
