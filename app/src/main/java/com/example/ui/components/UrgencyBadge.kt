package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.OnEmergencyRedContainer
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryContainerLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer

@Composable
fun UrgencyBadge(
    urgencyLevel: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (urgencyLevel.uppercase()) {
        "EMERGENCY" -> Quadruple(
            EmergencyRedContainer,
            OnEmergencyRedContainer,
            "EMERGENCY — Immediate Care",
            Icons.Default.Warning
        )
        "URGENT_CARE" -> Quadruple(
            WarningAmberContainer,
            Color(0xFF92400E),
            "URGENT CARE — Within 24h",
            Icons.Default.WarningAmber
        )
        "ROUTINE_EVALUATION" -> Quadruple(
            PrimaryContainerLight,
            PrimaryBlue,
            "ROUTINE CLINICAL EVALUATION",
            Icons.Default.Info
        )
        else -> Quadruple(
            SuccessGreenContainer,
            SuccessGreen,
            "HOME CARE & OBSERVATION",
            Icons.Default.CheckCircle
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("urgency_badge")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
