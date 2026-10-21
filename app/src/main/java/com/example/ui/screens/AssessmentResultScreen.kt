package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.NavyTypography
import com.example.ui.theme.OnEmergencyRedContainer
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryContainerLight
import com.example.ui.theme.SecondaryContainerLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.InjuryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentResultScreen(
    injuryId: String,
    viewModel: InjuryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecoveryTracker: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedInjury by viewModel.selectedInjury.collectAsState()
    val injury = selectedInjury ?: return

    val record = injury.record
    val assessment = injury.latestAssessment
    val primaryImage = injury.primaryImage

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("assessment_result_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clinical Assessment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NavyTypography
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyTypography
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Triage Urgency Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (assessment?.urgencyLevel == "EMERGENCY") EmergencyRedContainer else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${record.bodyArea} Assessment",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = NavyTypography
                            )
                            if (assessment != null) {
                                UrgencyBadge(urgencyLevel = assessment.urgencyLevel)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = assessment?.possibleCategory ?: "Preliminary Evaluation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (assessment?.urgencyLevel == "EMERGENCY") OnEmergencyRedContainer else PrimaryBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Assessed on ${record.injuryDate} | Mechanism: ${record.injuryMechanism}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (assessment?.urgencyLevel == "EMERGENCY") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToEmergency,
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("emergency_action_banner_btn")
                            ) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Immediate Emergency Action Required", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Image Thumbnail if available
            if (primaryImage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Submitted ${if (primaryImage.imageType == "XRAY") "X-Ray Radiograph" else "External Photo"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NavyTypography
                                )
                                Text(
                                    text = "Private Secure Storage",
                                    fontSize = 11.sp,
                                    color = PrimaryBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0A192F))
                            ) {
                                AsyncImage(
                                    model = primaryImage.storagePath,
                                    contentDescription = "Injury Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            // 1. Visible Observations
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Visible Characteristics & Observations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyTypography
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = assessment?.visibleObservations ?: "Observations not generated.",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Assessment Limitations & Uncertainty Explanation (Prompt Section 5 Requirement)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Assessment Limitations & Uncertainty",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyTypography
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = assessment?.uncertaintyNotes ?: "Images alone cannot evaluate subcutaneous or structural tissue integrity.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Approximate Recovery-Time Range (Prompt Section 6 Requirement)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Approximate Recovery Timeline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyTypography
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondaryContainerLight)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = assessment?.recoveryEstimate ?: "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SecondaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Factors Influencing Recovery:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = assessment?.recoveryExplanation ?: "Actual recovery duration varies considerably between individuals depending on rest adherence, age, systemic health factors, and progressive physical rehabilitation.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Medically Reviewed First Aid Guidance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Healing,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "General First Aid & Care Principles",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyTypography
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = assessment?.firstAidGuidance ?: "Rest, ice intermittently, elevate limb, and protect affected area from re-injury.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 5. Medical References
            if (!assessment?.medicalReferences.isNullOrEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Clinical Practice References",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NavyTypography
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            assessment!!.medicalReferences.forEach { ref ->
                                Text(
                                    text = "• $ref",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Mandatory Disclaimer Card
            item {
                MedicalDisclaimerCard()
            }

            // Navigation Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onNavigateToRecoveryTracker(record.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("track_recovery_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Recovery & Track Progress", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToDashboard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("return_dashboard_button")
                    ) {
                        Text("Return to Dashboard", color = NavyTypography, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
