package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.RecoveryLogEntity
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NavyTypography
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryContainerLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.viewmodel.InjuryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjuryDetailScreen(
    injuryId: String,
    viewModel: InjuryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAssessmentResult: (String) -> Unit,
    onNavigateToRecoveryTracker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedInjury by viewModel.selectedInjury.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val injury = selectedInjury ?: return
    val record = injury.record
    val assessment = injury.latestAssessment
    val images = injury.images
    val recoveryLogs = injury.recoveryLogs.sortedByDescending { it.createdAt }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Injury Record?",
                    fontWeight = FontWeight.Bold,
                    color = NavyTypography
                )
            },
            text = {
                Text(
                    text = "This action will permanently delete this injury case, its associated AI assessment, and all ${recoveryLogs.size} recovery log entries from the database. This cannot be undone.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteInjury(record.id) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = NavyTypography)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("injury_detail_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${record.bodyArea} Case Record",
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
                actions = {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("delete_record_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Record",
                            tint = EmergencyRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToRecoveryTracker(record.id) },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_recovery_log_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Log")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Recovery Log", fontWeight = FontWeight.Bold)
                }
            }
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
            // Case Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = record.bodyArea,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyTypography
                                )
                                Text(
                                    text = "Date of Incident: ${record.injuryDate}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (assessment != null) {
                                UrgencyBadge(urgencyLevel = assessment.urgencyLevel)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Status Selector
                        Text(
                            text = "Recovery Status:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val statusList = listOf("Active", "Healing", "Plateaued", "Resolved")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            statusList.forEach { statusOption ->
                                val isSelected = record.status.equals(statusOption, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        // Update status
                                    },
                                    label = { Text(statusOption, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (statusOption == "Resolved") SuccessGreen else PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Mechanism & Clinical Context:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = record.injuryDescription.ifBlank { "Mechanism: ${record.injuryMechanism}" },
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Reported Symptoms: ${record.symptoms.joinToString(", ")}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Attached Images
            if (images.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Attached Clinical Images (${images.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyTypography
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            images.forEach { img ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0F172A))
                                ) {
                                    AsyncImage(
                                        model = img.storagePath,
                                        contentDescription = "Injury Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.7f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (img.imageType == "XRAY") "X-Ray Radiograph" else "External Photo",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Latest Assessment Summary Card
            if (assessment != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAssessmentResult(record.id) },
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
                                    text = "AI Clinical Assessment",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = NavyTypography
                                )
                                TextButton(onClick = { onNavigateToAssessmentResult(record.id) }) {
                                    Text("View Full Report", color = PrimaryBlue, fontSize = 12.sp)
                                }
                            }

                            Text(
                                text = assessment.possibleCategory,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = assessment.recoveryEstimate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SecondaryTeal
                            )
                        }
                    }
                }
            }

            // Recovery Progress Timeline Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recovery Log Timeline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyTypography
                    )
                    Text(
                        text = "${recoveryLogs.size} entries",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (recoveryLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Recovery Entries Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyTypography
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Log daily pain levels, swelling reduction, and functional mobility to track your healing trajectory.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(recoveryLogs, key = { it.id }) { log ->
                    RecoveryLogItemCard(
                        log = log,
                        onDelete = { viewModel.deleteRecoveryLog(log.id) }
                    )
                }
            }

            item {
                MedicalDisclaimerCard(isCompact = true)
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun RecoveryLogItemCard(
    log: RecoveryLogEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(log.createdAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recovery_log_item_${log.id}"),
        shape = RoundedCornerShape(12.dp),
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
                    text = dateFormatted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NavyTypography
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (log.reportedRecoveryStatus) {
                                "Fully Recovered" -> SuccessGreenContainer
                                "Worsening" -> Color(0xFFFEE2E2)
                                else -> PrimaryContainerLight
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = log.reportedRecoveryStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (log.reportedRecoveryStatus) {
                            "Fully Recovered" -> SuccessGreen
                            "Worsening" -> EmergencyRed
                            else -> PrimaryBlue
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Pain Rating: ${log.painLevel}/10",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (log.painLevel >= 7) EmergencyRed else NavyTypography
                )
                Text(
                    text = "Swelling: ${log.swellingStatus}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Mobility: ${log.mobilityScore}/5",
                    fontSize = 12.sp,
                    color = SecondaryTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (log.progressNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = log.progressNotes,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
