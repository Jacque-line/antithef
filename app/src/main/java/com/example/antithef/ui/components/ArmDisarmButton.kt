package com.example.antithef.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antithef.ui.theme.SecurityAmber
import com.example.antithef.ui.theme.SecurityGreen
import com.example.antithef.ui.theme.SecurityNavy
import com.example.antithef.ui.theme.SecurityRed

@Composable
fun ArmDisarmButton(
    isArmed: Boolean,
    isGracePeriod: Boolean,
    graceCountdown: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetColor = when {
        isGracePeriod -> SecurityAmber
        isArmed -> SecurityRed
        else -> SecurityGreen
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 350),
        label = "btn_color"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedColor,
            contentColor = if (isGracePeriod) SecurityNavy else Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isGracePeriod -> Icons.Default.Shield
                    isArmed -> Icons.Default.LockOpen
                    else -> Icons.Default.Lock
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = when {
                    isGracePeriod -> "ARMING IN $graceCountdown S..."
                    isArmed -> "DISARM SYSTEM"
                    else -> "ARM SYSTEM"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
