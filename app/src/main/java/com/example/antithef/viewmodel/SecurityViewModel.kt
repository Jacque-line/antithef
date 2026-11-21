package com.example.antithef.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.antithef.data.dao.SecurityDao
import com.example.antithef.data.local.AppDatabaseHelper
import com.example.antithef.data.model.AppSettings
import com.example.antithef.data.model.SecurityLog
import com.example.antithef.data.model.SensorReading
import com.example.antithef.data.repository.SecurityRepository
import com.example.antithef.sensor.SensorManagerHelper
import com.example.antithef.service.AlarmSoundPlayer
import com.example.antithef.service.SecurityForegroundService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SecurityUiState(
    val isArmed: Boolean = false,
    val isAlarmActive: Boolean = false,
    val alarmTriggerReason: String = "",
    val alarmTriggerTime: String = "",
    val isArmingGracePeriod: Boolean = false,
    val graceCountdown: Int = 0,
    val currentSessionId: Long? = null,
    val totalEventsCount: Int = 0,
    val totalAlarmsCount: Int = 0,
    val isAccelerometerAvailable: Boolean = true,
    val isProximityAvailable: Boolean = true,
    val isTestingAlarm: Boolean = false
)

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = AppDatabaseHelper(application)
    private val dao = SecurityDao(dbHelper)
    private val repository = SecurityRepository(dao)

    private val alarmPlayer = AlarmSoundPlayer(application)

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    private val _historyLogs = MutableStateFlow<List<SecurityLog>>(emptyList())
    val historyLogs: StateFlow<List<SecurityLog>> = _historyLogs.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private var graceJob: Job? = null

    private val sensorHelper = SensorManagerHelper(
        context = application,
        onMotionAlarmTriggered = { delta ->
            onMotionDetected(delta)
        },
        onProximityAlarmTriggered = {
            onProximityDetected()
        }
    )

    val sensorReading: StateFlow<SensorReading> = sensorHelper.sensorReading

    init {
        // Initialize sensors and status
        _uiState.update {
            it.copy(
                isAccelerometerAvailable = sensorHelper.isAccelerometerAvailable,
                isProximityAvailable = sensorHelper.isProximityAvailable
            )
        }
        sensorHelper.startMonitoring()

        viewModelScope.launch {
            loadSettings()
            loadStats()
            loadHistory()
        }
    }

    private suspend fun loadSettings() {
        val currentSettings = repository.getSettings()
        _settings.value = currentSettings
        sensorHelper.setSensitivity(currentSettings.motionSensitivity)
    }

    private suspend fun loadStats() {
        val total = repository.getTotalLogsCount()
        val alarms = repository.getTotalAlarmsTriggeredCount()
        _uiState.update {
            it.copy(totalEventsCount = total, totalAlarmsCount = alarms)
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            val logs = repository.getAllLogs()
            _historyLogs.value = logs
            loadStats()
        }
    }

    fun getMotionThreshold(): Float {
        return sensorHelper.getMotionThreshold()
    }

    /**
     * Arms the system. A 3-second grace countdown is initiated so the user can
     * position the smartphone safely before active tripwire triggers start.
     */
    fun armSystem() {
        if (_uiState.value.isArmed || _uiState.value.isArmingGracePeriod) return

        graceJob?.cancel()
        graceJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isArmingGracePeriod = true, graceCountdown = 3)
            }

            for (sec in 3 downTo 1) {
                _uiState.update { it.copy(graceCountdown = sec) }
                delay(1000)
            }

            // Start Session in SQLite
            val sessionId = repository.startSecuritySession()
            repository.logEvent(
                sessionId = sessionId,
                eventType = "SYSTEM",
                description = "Security Armed. Sensors Active.",
                alarmTriggered = false
            )

            // Start Android Foreground Service
            SecurityForegroundService.startService(getApplication(), isAlarming = false)

            // Enable detection
            val currentSettings = _settings.value
            sensorHelper.setSensitivity(currentSettings.motionSensitivity)
            sensorHelper.setArmed(true, currentSettings.proximityEnabled)

            _uiState.update {
                it.copy(
                    isArmed = true,
                    isArmingGracePeriod = false,
                    graceCountdown = 0,
                    currentSessionId = sessionId
                )
            }
            loadHistory()
        }
    }

    fun disarmSystem() {
        graceJob?.cancel()
        graceJob = null

        val sessionId = _uiState.value.currentSessionId
        alarmPlayer.stopAlarm()
        sensorHelper.setArmed(false, false)
        SecurityForegroundService.stopService(getApplication())

        viewModelScope.launch {
            if (sessionId != null) {
                repository.endSecuritySession(sessionId)
                repository.logEvent(
                    sessionId = sessionId,
                    eventType = "SYSTEM",
                    description = "Security Disarmed by User.",
                    alarmTriggered = false
                )
            }
            _uiState.update {
                it.copy(
                    isArmed = false,
                    isAlarmActive = false,
                    isArmingGracePeriod = false,
                    graceCountdown = 0,
                    currentSessionId = null,
                    alarmTriggerReason = ""
                )
            }
            loadHistory()
        }
    }

    private fun onMotionDetected(delta: Float) {
        val state = _uiState.value
        if (!state.isArmed || state.isAlarmActive || state.isArmingGracePeriod) return

        val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val desc = "Motion detected! Acceleration change: ${"%.2f".format(delta)} m/s²"

        _uiState.update {
            it.copy(
                isAlarmActive = true,
                alarmTriggerReason = "Accelerometer: Motion Detected ($desc)",
                alarmTriggerTime = now
            )
        }

        if (_settings.value.alarmEnabled) {
            alarmPlayer.startAlarm(_settings.value.alarmVolume)
        }
        SecurityForegroundService.startService(getApplication(), isAlarming = true)

        viewModelScope.launch {
            repository.logEvent(
                sessionId = state.currentSessionId,
                eventType = "MOTION",
                description = desc,
                alarmTriggered = true
            )
            loadHistory()
        }
    }

    private fun onProximityDetected() {
        val state = _uiState.value
        if (!state.isArmed || state.isAlarmActive || state.isArmingGracePeriod) return
        if (!_settings.value.proximityEnabled) return

        val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val desc = "Object detected near front screen!"

        _uiState.update {
            it.copy(
                isAlarmActive = true,
                alarmTriggerReason = "Proximity Sensor: Object Nearby ($desc)",
                alarmTriggerTime = now
            )
        }

        if (_settings.value.alarmEnabled) {
            alarmPlayer.startAlarm(_settings.value.alarmVolume)
        }
        SecurityForegroundService.startService(getApplication(), isAlarming = true)

        viewModelScope.launch {
            repository.logEvent(
                sessionId = state.currentSessionId,
                eventType = "PROXIMITY",
                description = desc,
                alarmTriggered = true
            )
            loadHistory()
        }
    }

    fun stopAlarm() {
        alarmPlayer.stopAlarm()
        val sessionId = _uiState.value.currentSessionId

        SecurityForegroundService.startService(getApplication(), isAlarming = false)

        _uiState.update {
            it.copy(isAlarmActive = false)
        }

        viewModelScope.launch {
            repository.logEvent(
                sessionId = sessionId,
                eventType = "ALARM_STOPPED",
                description = "Alarm stopped by user. Monitoring continues.",
                alarmTriggered = false
            )
            loadHistory()
        }
    }

    fun testAlarm() {
        if (_uiState.value.isTestingAlarm) return
        _uiState.update { it.copy(isTestingAlarm = true) }
        alarmPlayer.playTestTone(_settings.value.alarmVolume) {
            _uiState.update { it.copy(isTestingAlarm = false) }
        }
    }

    fun triggerSimulatedAlarm(sensorName: String) {
        if (_uiState.value.isArmed) {
            if (sensorName == "Accelerometer") {
                onMotionDetected(5.5f)
            } else {
                onProximityDetected()
            }
        } else {
            // Test alarm
            testAlarm()
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        sensorHelper.setSensitivity(newSettings.motionSensitivity)
        sensorHelper.setArmed(_uiState.value.isArmed, newSettings.proximityEnabled)
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            loadHistory()
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLog(id)
            loadHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        alarmPlayer.stopAlarm()
        sensorHelper.stopMonitoring()
    }
}
