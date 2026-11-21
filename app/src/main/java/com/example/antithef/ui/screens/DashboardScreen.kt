package com.example.antithef.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.antithef.ui.components.ArmDisarmButton
import com.example.antithef.ui.components.SecurityShield
import com.example.antithef.ui.components.SecurityStatCard
import com.example.antithef.ui.components.SensorStatusCard
import com.example.antithef.ui.theme.SecurityAmber
import com.example.antithef.ui.theme.SecurityBorder
import com.example.antithef.ui.theme.SecurityCyan
import com.example.antithef.ui.theme.SecurityDarkBlue
import com.example.antithef.ui.theme.SecurityDarkSurface
import com.example.antithef.ui.theme.SecurityGray
import com.example.antithef.ui.theme.SecurityGreen
import com.example.antithef.ui.theme.SecurityNavy
import com.example.antithef.ui.theme.SecurityRed
import com.example.antithef.ui.theme.SecurityWhite
import com.example.antithef.viewmodel.SecurityViewModel

@Composable
fun DashboardScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sensorReading by viewModel.sensorReading.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecurityNavy)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AntiTheft",
                    color = SecurityWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Smart Phone Security",
                    color = SecurityCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Status Chip
            Box(
                modifier = Modifier
                    .background(
                        color = if (uiState.isArmed) SecurityGreen.copy(alpha = 0.15f) else SecurityDarkSurface,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (uiState.isArmed) SecurityGreen else SecurityBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (uiState.isArmed) SecurityGreen else SecurityGray,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isArmed) "PROTECTED" else "DISARMED",
                        color = if (uiState.isArmed) SecurityGreen else SecurityGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Shield Visual
        SecurityShield(
            isArmed = uiState.isArmed,
            isAlarmActive = uiState.isAlarmActive,
            isGracePeriod = uiState.isArmingGracePeriod,
            graceCountdown = uiState.graceCountdown
        )

        // Status Text Banner
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when {
                    uiState.isAlarmActive -> "UNAUTHORIZED MOVEMENT DETECTED!"
                    uiState.isArmingGracePeriod -> "ARMING IN PROGRESS..."
                    uiState.isArmed -> "SYSTEM SECURE"
                    else -> "SYSTEM UNPROTECTED"
                },
                color = when {
                    uiState.isAlarmActive -> SecurityRed
                    uiState.isArmingGracePeriod -> SecurityAmber
                    uiState.isArmed -> SecurityGreen
                    else -> SecurityGray
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    uiState.isAlarmActive -> uiState.alarmTriggerReason
                    uiState.isArmingGracePeriod -> "Place device on table before countdown ends (${uiState.graceCountdown}s)"
                    uiState.isArmed -> "Security mode is currently active. Monitoring sensors."
                    else -> "Tap the button below to arm device protection."
                },
                color = SecurityGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Arm / Disarm Button
        ArmDisarmButton(
            isArmed = uiState.isArmed,
            isGracePeriod = uiState.isArmingGracePeriod,
            graceCountdown = uiState.graceCountdown,
            onClick = {
                if (uiState.isArmed || uiState.isArmingGracePeriod) {
                    viewModel.disarmSystem()
                } else {
                    viewModel.armSystem()
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Sensor Status Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Hardware Sensors Status",
                color = SecurityWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Accelerometer Card
        SensorStatusCard(
            sensorName = "Accelerometer",
            sensorCategory = "Motion Sensor (3-Axis X, Y, Z)",
            isAvailable = uiState.isAccelerometerAvailable,
            isActive = uiState.isArmed,
            valueSummary = "Mag: ${"%.2f".format(sensorReading.magnitude)} m/s² | Δ: ${"%.2f".format(sensorReading.delta)}",
            icon = Icons.Default.Speed
        )

        // Proximity Card
        SensorStatusCard(
            sensorName = "Proximity Sensor",
            sensorCategory = "Position Sensor (Front Obstruction)",
            isAvailable = uiState.isProximityAvailable,
            isActive = uiState.isArmed && settings.proximityEnabled,
            valueSummary = if (!uiState.isProximityAvailable) {
                "Sensor not present on device"
            } else {
                "Dist: ${sensorReading.proximityDistance} cm | ${if (sensorReading.isProximityNear) "NEAR" else "FAR"}"
            },
            icon = Icons.Default.Sensors
        )

        // Quick Stats Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecurityStatCard(
                title = "Total Events",
                value = "${uiState.totalEventsCount}",
                subtitle = "Logged in SQLite",
                icon = Icons.Default.NotificationsActive,
                iconTint = SecurityCyan,
                modifier = Modifier.weight(1f)
            )
            SecurityStatCard(
                title = "Alarms Fired",
                value = "${uiState.totalAlarmsCount}",
                subtitle = "Tripwire triggers",
                icon = Icons.Default.Shield,
                iconTint = SecurityRed,
                modifier = Modifier.weight(1f)
            )
        }

        // Test Alarm Button
        OutlinedButton(
            onClick = { viewModel.testAlarm() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SecurityCyan
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, SecurityCyan.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (uiState.isTestingAlarm) "Playing 2s Test Tone..." else "Test Alarm Sound",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
