package com.example.antithef.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antithef.ui.theme.SecurityBorder
import com.example.antithef.ui.theme.SecurityCyan
import com.example.antithef.ui.theme.SecurityDarkBlue
import com.example.antithef.ui.theme.SecurityDarkSurface
import com.example.antithef.ui.theme.SecurityGray
import com.example.antithef.ui.theme.SecurityGreen
import com.example.antithef.ui.theme.SecurityMuted
import com.example.antithef.ui.theme.SecurityRed
import com.example.antithef.ui.theme.SecurityWhite

@Composable
fun SensorStatusCard(
    sensorName: String,
    sensorCategory: String,
    isAvailable: Boolean,
    isActive: Boolean,
    valueSummary: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SecurityDarkBlue, RoundedCornerShape(16.dp))
            .border(1.dp, SecurityBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .background(SecurityDarkSurface, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = sensorName,
                    tint = if (isAvailable) SecurityCyan else SecurityMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sensorName,
                    color = SecurityWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = sensorCategory,
                    color = SecurityGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = valueSummary,
                    color = SecurityCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Status Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = when {
                            !isAvailable -> SecurityRed.copy(alpha = 0.15f)
                            isActive -> SecurityGreen.copy(alpha = 0.18f)
                            else -> SecurityDarkSurface
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            !isAvailable -> SecurityRed
                            isActive -> SecurityGreen
                            else -> SecurityBorder
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = when {
                                    !isAvailable -> SecurityRed
                                    isActive -> SecurityGreen
                                    else -> SecurityMuted
                                },
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = when {
                            !isAvailable -> "Unavailable"
                            isActive -> "Active"
                            else -> "Standby"
                        },
                        color = when {
                            !isAvailable -> SecurityRed
                            isActive -> SecurityGreen
                            else -> SecurityGray
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
