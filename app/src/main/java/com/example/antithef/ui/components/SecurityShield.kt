package com.example.antithef.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antithef.ui.theme.SecurityAmber
import com.example.antithef.ui.theme.SecurityDarkBlue
import com.example.antithef.ui.theme.SecurityDarkSurface
import com.example.antithef.ui.theme.SecurityGreen
import com.example.antithef.ui.theme.SecurityRed
import com.example.antithef.ui.theme.SecurityWhite

@Composable
fun SecurityShield(
    isArmed: Boolean,
    isAlarmActive: Boolean,
    isGracePeriod: Boolean = false,
    graceCountdown: Int = 0,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAlarmActive) 1.25f else if (isArmed) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isAlarmActive) 400 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val primaryColor = when {
        isAlarmActive -> SecurityRed
        isGracePeriod -> SecurityAmber
        isArmed -> SecurityGreen
        else -> Color(0xFF64748B)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(190.dp)
    ) {
        // Outer animated glowing wave
        if (isArmed || isAlarmActive || isGracePeriod) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = if (isAlarmActive) 0.45f else 0.22f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Inner Shield Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(140.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SecurityDarkSurface,
                            SecurityDarkBlue
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 3.dp,
                    color = primaryColor,
                    shape = CircleShape
                )
        ) {
            if (isGracePeriod) {
                Text(
                    text = "$graceCountdown",
                    color = SecurityAmber,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = when {
                        isAlarmActive -> Icons.Default.Warning
                        isArmed -> Icons.Default.Lock
                        else -> Icons.Default.LockOpen
                    },
                    contentDescription = "Security Status Icon",
                    tint = primaryColor,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}
