package com.example.antithef.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.antithef.data.model.SecurityLog
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
fun HistoryScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val historyLogs by viewModel.historyLogs.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedLogForDetail by remember { mutableStateOf<SecurityLog?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(historyLogs, selectedFilter) {
        when (selectedFilter) {
            "MOTION" -> historyLogs.filter { it.eventType == "MOTION" }
            "PROXIMITY" -> historyLogs.filter { it.eventType == "PROXIMITY" }
            "SYSTEM" -> historyLogs.filter { it.eventType == "SYSTEM" || it.eventType == "ALARM_STOPPED" }
            else -> historyLogs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecurityNavy)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Security History",
                    color = SecurityWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${historyLogs.size} recorded incidents in SQLite",
                    color = SecurityGray,
                    fontSize = 13.sp
                )
            }

            if (historyLogs.isNotEmpty()) {
                IconButton(onClick = { showClearConfirmDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear History",
                        tint = SecurityRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "MOTION", "PROXIMITY", "SYSTEM").forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = filter,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecurityGreen.copy(alpha = 0.2f),
                        selectedLabelColor = SecurityGreen,
                        containerColor = SecurityDarkBlue,
                        labelColor = SecurityGray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) SecurityGreen else SecurityBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logs List or Empty State
        if (filteredLogs.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 30.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(70.dp)
                            .background(SecurityDarkSurface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = SecurityGray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Security Incidents Yet",
                        color = SecurityWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Events triggered by sensors will be stored in SQLite database.",
                        color = SecurityGray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    LogItemCard(
                        log = log,
                        onClick = { selectedLogForDetail = log },
                        onDelete = { viewModel.deleteLog(log.id) }
                    )
                }
            }
        }
    }

    // Detail Dialog
    selectedLogForDetail?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedLogForDetail = null },
            containerColor = SecurityDarkBlue,
            title = {
                Text(
                    text = "Event Detail #${log.id}",
                    color = SecurityWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Type: ${log.eventType}", color = SecurityCyan, fontWeight = FontWeight.SemiBold)
                    Text(text = "Time: ${log.timestamp}", color = SecurityWhite)
                    Text(text = "Alarm Fired: ${if (log.alarmTriggered) "YES (Warning Alert)" else "NO"}", color = if (log.alarmTriggered) SecurityRed else SecurityGreen)
                    if (log.sessionId != null) {
                        Text(text = "Session ID: #${log.sessionId}", color = SecurityGray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Description:", color = SecurityGray, fontSize = 12.sp)
                    Text(text = log.description, color = SecurityWhite, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedLogForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityDarkSurface)
                ) {
                    Text("Close", color = SecurityWhite)
                }
            }
        )
    }

    // Clear History Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = SecurityDarkBlue,
            title = {
                Text(
                    text = "Clear All Security Logs?",
                    color = SecurityWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all records from the SQLite security_logs table. This action cannot be undone.",
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
                    Text("Clear All", color = SecurityWhite, fontWeight = FontWeight.Bold)
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

@Composable
fun LogItemCard(
    log: SecurityLog,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, tint, statusText, statusColor) = when (log.eventType) {
        "MOTION" -> Quad(Icons.Default.Speed, SecurityRed, "Motion Alert", SecurityRed)
        "PROXIMITY" -> Quad(Icons.Default.Sensors, SecurityAmber, "Obstruction", SecurityAmber)
        "ALARM_STOPPED" -> Quad(Icons.Default.StopCircle, SecurityGreen, "Alarm Stopped", SecurityGreen)
        else -> {
            if (log.description.contains("Armed")) {
                Quad(Icons.Default.Lock, SecurityGreen, "System Armed", SecurityGreen)
            } else {
                Quad(Icons.Default.LockOpen, SecurityCyan, "System Disarmed", SecurityCyan)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SecurityDarkBlue, RoundedCornerShape(14.dp))
            .border(1.dp, SecurityBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(SecurityDarkSurface, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.timestamp.takeLast(8),
                        color = SecurityGray,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = log.description,
                    color = SecurityWhite,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
