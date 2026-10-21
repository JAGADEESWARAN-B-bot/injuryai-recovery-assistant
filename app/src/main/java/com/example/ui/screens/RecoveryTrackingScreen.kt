package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NavyTypography
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.InjuryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryTrackingScreen(
    injuryId: String,
    viewModel: InjuryViewModel,
    onNavigateBack: () -> Unit,
    onLogSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedInjury by viewModel.selectedInjury.collectAsState()
    val record = selectedInjury?.record

    var painLevel by remember { mutableFloatStateOf(2f) }
    var swellingStatus by remember { mutableStateOf("Mild") }
    var mobilityScore by remember { mutableIntStateOf(3) }
    var progressNotes by remember { mutableStateOf("") }
    var reportedStatus by remember { mutableStateOf("Healing well") }

    val swellingOptions = listOf("None", "Mild", "Moderate", "Noticeably Reduced", "Severe")
    val statusOptions = listOf("Healing well", "Plateaued", "Worsening", "Fully Recovered")

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("recovery_tracking_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Log Recovery Progress",
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
            // Header summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Case: ${record?.bodyArea ?: "Injury"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavyTypography
                        )
                        Text(
                            text = "Incident on ${record?.injuryDate ?: "N/A"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 1. Current Pain Level
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Pain Rating",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyTypography
                            )
                            Text(
                                text = "${painLevel.toInt()} / 10",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (painLevel >= 7) EmergencyRed else PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = painLevel,
                            onValueChange = { painLevel = it },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = if (painLevel >= 7) EmergencyRed else PrimaryBlue,
                                activeTrackColor = if (painLevel >= 7) EmergencyRed else PrimaryBlue
                            ),
                            modifier = Modifier.testTag("recovery_pain_slider")
                        )
                    }
                }
            }

            // 2. Swelling Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Swelling & Edema Level",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            swellingOptions.forEach { opt ->
                                val isSelected = swellingStatus == opt
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { swellingStatus = opt },
                                    label = { Text(opt, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Mobility Score (1-5)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Functional Mobility Score",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyTypography
                            )
                            Text(
                                text = "Score: $mobilityScore / 5",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SecondaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1: Fully immobile | 3: Partial motion with mild discomfort | 5: Complete normal range",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..5).forEach { score ->
                                FilterChip(
                                    selected = mobilityScore == score,
                                    onClick = { mobilityScore = score },
                                    label = { Text("$score", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SecondaryTeal,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Overall Recovery Assessment
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Overall Recovery Impression",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            statusOptions.forEach { st ->
                                val isSelected = reportedStatus == st
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { reportedStatus = st },
                                    label = { Text(st, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (st) {
                                            "Fully Recovered" -> SuccessGreen
                                            "Worsening" -> EmergencyRed
                                            else -> PrimaryBlue
                                        },
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 5. Notes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Daily Progress Notes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = progressNotes,
                            onValueChange = { progressNotes = it },
                            placeholder = { Text("e.g. Swelling noticeably reduced after 20 mins ice application; able to take light steps...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("progress_notes_input"),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        if (record != null) {
                            viewModel.addRecoveryLog(
                                injuryRecordId = record.id,
                                painLevel = painLevel.toInt(),
                                swellingStatus = swellingStatus,
                                mobilityScore = mobilityScore,
                                progressNotes = progressNotes,
                                symptoms = emptyList(),
                                reportedRecoveryStatus = reportedStatus,
                                onComplete = onLogSubmitted
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_recovery_log_button")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Recovery Entry", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            item {
                MedicalDisclaimerCard(isCompact = true)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
