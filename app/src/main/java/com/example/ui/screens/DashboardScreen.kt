package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InjuryWithDetails
import com.example.ui.components.EmergencyBanner
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.StatCard
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.NavyTypography
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryContainerLight
import com.example.ui.theme.SecondaryContainerLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.viewmodel.InjuryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InjuryViewModel,
    onNavigateToNewAssessment: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val injuries by viewModel.filteredInjuries.collectAsState()
    val allInjuriesList by viewModel.allInjuries.collectAsState()
    val totalInjuriesCount by viewModel.totalInjuriesCount.collectAsState()
    val totalLogsCount by viewModel.totalLogsCount.collectAsState()
    val emergencyAlert by viewModel.emergencyAlert.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedBodyArea by viewModel.selectedBodyAreaFilter.collectAsState()
    val profile by viewModel.profile.collectAsState()

    val resolvedCount = allInjuriesList.count { it.record.status.equals("Resolved", ignoreCase = true) }
    val activeCount = allInjuriesList.count { !it.record.status.equals("Resolved", ignoreCase = true) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "InjuryAI Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "InjuryAI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = NavyTypography
                            )
                            Text(
                                text = "Smart Injury Assessment & Recovery",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToEmergency,
                        modifier = Modifier.testTag("emergency_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Emergency Protocols",
                            tint = Color(0xFFDC2626)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.testTag("profile_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Settings",
                            tint = NavyTypography
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewAssessment,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("new_assessment_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Assessment")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Assessment", fontWeight = FontWeight.Bold)
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
            // Emergency Alert intercept if triggered
            if (emergencyAlert != null) {
                item {
                    EmergencyBanner(
                        emergencyResult = emergencyAlert!!,
                        onCallEmergency = onNavigateToEmergency,
                        onDismiss = { viewModel.dismissEmergencyAlert() }
                    )
                }
            }

            // Disclaimer Banner
            item {
                MedicalDisclaimerCard()
            }

            // Stat Cards Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Total Cases",
                            value = totalInjuriesCount.toString(),
                            icon = Icons.Default.FolderShared,
                            iconBgColor = PrimaryContainerLight,
                            iconTintColor = PrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Under Care",
                            value = activeCount.toString(),
                            icon = Icons.Default.Healing,
                            iconBgColor = WarningAmberContainer,
                            iconTintColor = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Recovered",
                            value = resolvedCount.toString(),
                            icon = Icons.Default.CheckCircleOutline,
                            iconBgColor = SuccessGreenContainer,
                            iconTintColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Recovery Logs",
                            value = totalLogsCount.toString(),
                            icon = Icons.AutoMirrored.Filled.EventNote,
                            iconBgColor = SecondaryContainerLight,
                            iconTintColor = SecondaryTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search by body area, description, or diagnosis...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_injuries_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val filterChips = listOf("All", "Ankle", "Knee", "Wrist", "Hand", "Shoulder", "Head", "Back")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filterChips.forEach { chipName ->
                            val isSelected = if (chipName == "All") selectedBodyArea == null else selectedBodyArea == chipName
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectedBodyAreaFilter.value = if (chipName == "All") null else chipName
                                },
                                label = { Text(chipName, fontSize = 12.sp) },
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

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Injury Records & Triage History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyTypography
                    )
                    Text(
                        text = "${injuries.size} found",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (injuries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("empty_injuries_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainerLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Injury Records Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NavyTypography
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Upload an injury photo or X-ray image to receive a preliminary decision-support assessment and structured recovery guidance.",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(injuries, key = { it.record.id }) { item ->
                    InjuryRecordCard(
                        item = item,
                        onClick = { onNavigateToDetail(item.record.id) }
                    )
                }
            }

            // Bottom space for FAB
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun InjuryRecordCard(
    item: InjuryWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val record = item.record
    val assessment = item.latestAssessment

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("injury_card_${record.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryContainerLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = record.bodyArea,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.injuryDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (record.status == "Resolved") SuccessGreenContainer else PrimaryContainerLight
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = record.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (record.status == "Resolved") SuccessGreen else PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = assessment?.possibleCategory ?: record.injuryMechanism,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyTypography
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = record.injuryDescription.ifBlank { "Mechanism: ${record.injuryMechanism}" },
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (assessment != null) {
                    UrgencyBadge(urgencyLevel = assessment.urgencyLevel)
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pain: ${record.painLevel}/10",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (record.painLevel >= 7) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${item.recoveryLogs.size} logs",
                        fontSize = 11.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
