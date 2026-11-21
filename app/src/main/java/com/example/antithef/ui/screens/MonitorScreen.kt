package com.example.antithef.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun MonitorScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sensorReading by viewModel.sensorReading.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentThreshold = viewModel.getMotionThreshold()

    val scrollState = rememberScrollState()

    val animatedDelta by animateFloatAsState(
        targetValue = (sensorReading.delta / 10f).coerceIn(0f, 1f),
        label = "delta_anim"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecurityNavy)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Screen Header
        Column {
            Text(
                text = "Live Sensor Monitor",
                color = SecurityWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Real-time telemetry for sensor inspection & demonstration",
                color = SecurityGray,
                fontSize = 13.sp
            )
        }

        // Formula & Algorithm Explanation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecurityDarkBlue, RoundedCornerShape(16.dp))
                .border(1.dp, SecurityBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SecurityCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Motion Detection Algorithm",
                        color = SecurityCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A = √(x² + y² + z²)",
                    color = SecurityGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Current Magnitude: ${"%.3f".format(sensorReading.magnitude)} m/s²\n" +
                            "Change Rate (ΔA): ${"%.3f".format(sensorReading.delta)} m/s²\n" +
                            "Sensitivity Threshold: ${"%.2f".format(currentThreshold)} m/s² (Set at ${settings.motionSensitivity}%)",
                    color = SecurityGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Accelerometer 3-Axis Readout Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecurityDarkBlue, RoundedCornerShape(16.dp))
                .border(1.dp, SecurityBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = SecurityWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Accelerometer (3-Axis)",
                            color = SecurityWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = if (uiState.isAccelerometerAvailable) "OPERATIONAL" else "NOT FOUND",
                        color = if (uiState.isAccelerometerAvailable) SecurityGreen else SecurityRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Axis Bars
                AxisGaugeRow(axis = "X (Lateral)", value = sensorReading.x, color = Color(0xFF38BDF8))
                AxisGaugeRow(axis = "Y (Vertical)", value = sensorReading.y, color = Color(0xFFA855F7))
                AxisGaugeRow(axis = "Z (Perpendicular)", value = sensorReading.z, color = Color(0xFFF59E0B))

                Spacer(modifier = Modifier.height(4.dp))

                // Delta Movement Level Meter
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Motion Intensity vs Threshold",
                            color = SecurityGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (sensorReading.delta > currentThreshold) "EXCEEDED (TRIGGER)" else "NORMAL",
                            color = if (sensorReading.delta > currentThreshold) SecurityRed else SecurityGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedDelta },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (sensorReading.delta > currentThreshold) SecurityRed else SecurityGreen,
                        trackColor = SecurityDarkSurface
                    )
                }
            }
        }

        // Proximity Sensor Readout Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecurityDarkBlue, RoundedCornerShape(16.dp))
                .border(1.dp, SecurityBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = SecurityWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Proximity Sensor",
                            color = SecurityWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = if (uiState.isProximityAvailable) "OPERATIONAL" else "ABSENT",
                        color = if (uiState.isProximityAvailable) SecurityGreen else SecurityRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.isProximityAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Distance",
                                color = SecurityGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${sensorReading.proximityDistance} cm",
                                color = SecurityWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Near/Far Visual Chip
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (sensorReading.isProximityNear) SecurityRed.copy(alpha = 0.2f) else SecurityGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (sensorReading.isProximityNear) SecurityRed else SecurityGreen,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (sensorReading.isProximityNear) "NEAR (OBSTRUCTED)" else "FAR (CLEAR)",
                                color = if (sensorReading.isProximityNear) SecurityRed else SecurityGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Hardware Max Range: ${sensorReading.proximityMaxRange} cm | Protection: ${if (settings.proximityEnabled) "Enabled" else "Disabled in Settings"}",
                        color = SecurityGray,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "This device hardware does not provide a hardware proximity sensor. Proximity protection is automatically bypassed safely without app crashes.",
                        color = SecurityGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Demonstration & Sensor Testing Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecurityDarkBlue, RoundedCornerShape(16.dp))
                .border(1.dp, SecurityBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = SecurityCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demonstration & Test Triggers",
                        color = SecurityWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Use these triggers to demonstrate sensor handling to faculty/evaluators without physical movement.",
                    color = SecurityGray,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerSimulatedAlarm("Accelerometer") },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityDarkSurface)
                    ) {
                        Text("Simulate Motion", fontSize = 12.sp, color = SecurityCyan)
                    }

                    Button(
                        onClick = { viewModel.triggerSimulatedAlarm("Proximity") },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityDarkSurface)
                    ) {
                        Text("Simulate Near", fontSize = 12.sp, color = SecurityCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AxisGaugeRow(
    axis: String,
    value: Float,
    color: Color
) {
    // Range normalized from -15 to +15 m/s²
    val normalizedProgress = ((value + 15f) / 30f).coerceIn(0f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = axis,
                color = SecurityGray,
                fontSize = 12.sp
            )
            Text(
                text = "${"%.2f".format(value)} m/s²",
                color = SecurityWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { normalizedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = SecurityDarkSurface
        )
    }
}
