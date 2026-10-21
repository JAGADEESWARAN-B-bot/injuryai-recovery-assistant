package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.EmergencyCheckResult
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.OnEmergencyRedContainer

@Composable
fun EmergencyBanner(
    emergencyResult: EmergencyCheckResult,
    onCallEmergency: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = EmergencyRedContainer
        ),
        border = BorderStroke(1.5.dp, EmergencyRed)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = "Emergency Red Flag Alert",
                    tint = EmergencyRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EMERGENCY RED FLAG DETECTED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OnEmergencyRedContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = emergencyResult.redFlagReason,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = OnEmergencyRedContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Do NOT wait for image processing or home treatment. Seek immediate in-person emergency trauma evaluation.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = OnEmergencyRedContainer
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onCallEmergency,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("call_emergency_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call EMS",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call 911 / 112", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (onDismiss != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, EmergencyRed),
                        modifier = Modifier.testTag("dismiss_emergency_banner_button")
                    ) {
                        Text("Review Details", color = OnEmergencyRedContainer, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
