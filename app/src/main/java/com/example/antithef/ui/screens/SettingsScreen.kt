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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun SettingsScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecurityNavy)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Settings",
                color = SecurityWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Customize tripwire thresholds & alarm behavior",
                color = SecurityGray,
                fontSize = 13.sp
            )
        }

        // Section 1: Detection Sensitivity
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
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = SecurityCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Motion Sensitivity",
                            color = SecurityWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val levelLabel = when {
                        settings.motionSensitivity > 80 -> "Ultra"
                        settings.motionSensitivity > 60 -> "High"
                        settings.motionSensitivity > 35 -> "Medium"
                        else -> "Low"
                    }

                    Text(
                        text = "${settings.motionSensitivity}% ($levelLabel)",
                        color = SecurityGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Controls how easily accelerometer movement triggers the alarm. Higher values detect slight tilts; lower values require intentional displacement.",
                    color = SecurityGray,
                    fontSize = 12.sp
                )

                Slider(
                    value = settings.motionSensitivity.toFloat(),
                    onValueChange = { newValue ->
                        viewModel.updateSettings(
                            settings.copy(motionSensitivity = newValue.toInt())
                        )
                    },
                    valueRange = 1f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = SecurityGreen,
                        activeTrackColor = SecurityGreen,
                        inactiveTrackColor = SecurityDarkSurface
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1% (Vigorous Shake)", color = SecurityGray, fontSize = 11.sp)
                    Text("100% (Slight Touch)", color = SecurityGray, fontSize = 11.sp)
                }
            }
        }

        // Section 2: Proximity Sensor Switch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecurityDarkBlue, RoundedCornerShape(16.dp))
                .border(1.dp, SecurityBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = SecurityCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Proximity Detection",
                            color = SecurityWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.isProximityAvailable) {
                                "Detect object reaching over screen"
                            } else {
                                "Hardware not present on device"
                            },
                            color = SecurityGray,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = settings.proximityEnabled && uiState.isProximityAvailable,
                    enabled = uiState.isProximityAvailable,
                    onCheckedChange = { isChecked ->
                        viewModel.updateSettings(
                            settings.copy(proximityEnabled = isChecked)
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SecurityNavy,
                        checkedTrackColor = SecurityGreen,
                        uncheckedThumbColor = SecurityGray,
                        uncheckedTrackColor = SecurityDarkSurface
                    )
                )
            }
        }

        // Section 3: Alarm Sound & Volume
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
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = SecurityCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sound Siren on Breach",
                            color = SecurityWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Switch(
                        checked = settings.alarmEnabled,
                        onCheckedChange = { isChecked ->
                            viewModel.updateSettings(
                                settings.copy(alarmEnabled = isChecked)
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SecurityNavy,
                            checkedTrackColor = SecurityGreen,
                            uncheckedThumbColor = SecurityGray,
                            uncheckedTrackColor = SecurityDarkSurface
                        )
                    )
                }

                if (settings.alarmEnabled) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Alarm Siren Volume",
                                color = SecurityGray,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${settings.alarmVolume}%",
                                color = SecurityCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = settings.alarmVolume.toFloat(),
                            onValueChange = { newVol ->
                                viewModel.updateSettings(
                                    settings.copy(alarmVolume = newVol.toInt())
                                )
                            },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = SecurityCyan,
                                activeTrackColor = SecurityCyan,
                                inactiveTrackColor = SecurityDarkSurface
                            )
                        )
                    }

                    Button(
                        onClick = { viewModel.testAlarm() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityDarkSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = SecurityCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isTestingAlarm) "Playing Siren..." else "Play Test Siren",
                            color = SecurityCyan,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section 4: Data Management
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecurityDarkBlue, RoundedCornerShape(16.dp))
                .border(1.dp, SecurityBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Data & Database",
                    color = SecurityWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Manage SQLite persistent database records (antitheft_database.db).",
                    color = SecurityGray,
                    fontSize = 12.sp
                )

                OutlinedButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecurityRed.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Security History Logs", fontSize = 13.sp)
                }
            }
        }

        // Section 5: About App
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecurityDarkBlue, RoundedCornerShape(16.dp))
                .border(1.dp, SecurityBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AntiTheft Security",
                        color = SecurityWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Version 1.0.0 • MVVM • Jetpack Compose",
                        color = SecurityGray,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { showAboutDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityDarkSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SecurityWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("About", color = SecurityWhite, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // About App Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = SecurityDarkBlue,
            title = {
                Text(
                    text = "About AntiTheft",
                    color = SecurityWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "AntiTheft – Smart Phone Security",
                        color = SecurityGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Aplikasi keamanan smartphone berbasis Android dengan deteksi sensor pergerakan otomatis dan basis data SQLite.",
                        color = SecurityGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• UI: Jetpack Compose Material 3", color = SecurityWhite, fontSize = 12.sp)
                    Text(text = "• Architecture: MVVM", color = SecurityWhite, fontSize = 12.sp)
                    Text(text = "• Database: SQLite (SQLiteOpenHelper)", color = SecurityWhite, fontSize = 12.sp)
                    Text(text = "• Sensors: Accelerometer & Proximity", color = SecurityWhite, fontSize = 12.sp)
                    Text(text = "• Audio: High-Frequency Siren Synthesizer", color = SecurityWhite, fontSize = 12.sp)
                    Text(text = "• Background: Foreground Service & Notification", color = SecurityWhite, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityDarkSurface)
                ) {
                    Text("Close", color = SecurityWhite)
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = SecurityDarkBlue,
            title = {
                Text(
                    text = "Clear Security Logs?",
                    color = SecurityWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all security log records from the local SQLite database?",
                    color = SecurityGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityRed)
                ) {
                    Text("Delete", color = SecurityWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = SecurityGray)
                }
            }
        )
    }
}
