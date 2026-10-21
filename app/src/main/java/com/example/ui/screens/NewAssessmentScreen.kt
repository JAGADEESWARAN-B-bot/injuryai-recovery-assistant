package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.ai.ClinicalTriageEngine
import com.example.ui.components.EmergencyBanner
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.NavyTypography
import com.example.ui.theme.OnEmergencyRedContainer
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryContainerLight
import com.example.ui.viewmodel.InjuryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAssessmentScreen(
    viewModel: InjuryViewModel,
    onNavigateBack: () -> Unit,
    onAssessmentCompleted: (String) -> Unit,
    onNavigateToEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionError by viewModel.submissionError.collectAsState()

    var selectedBodyArea by remember { mutableStateOf("Ankle") }
    var injuryMechanism by remember { mutableStateOf("Twisting motion") }
    var injuryDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var painLevel by remember { mutableFloatStateOf(4f) }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageType by remember { mutableStateOf("EXTERNAL_PHOTO") } // EXTERNAL_PHOTO or XRAY

    val selectedSymptoms = remember {
        mutableStateListOf(
            "Localized swelling",
            "Pain on movement"
        )
    }

    // Photo picker launcher (standard zero-permission Android contract)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    // Real-time Emergency check
    val emergencyResult by remember {
        derivedStateOf {
            ClinicalTriageEngine.evaluateEmergency(
                selectedSymptoms.toList(),
                injuryMechanism,
                description
            )
        }
    }

    val bodyAreas = listOf("Ankle", "Knee", "Wrist", "Hand / Fingers", "Foot / Toes", "Shoulder", "Elbow", "Head / Face", "Back / Spine", "Chest / Ribs", "Skin / Soft Tissue")
    val mechanisms = listOf("Twisting motion", "Slip and Fall", "Blunt Impact", "Sports Collision", "Laceration / Cut", "Thermal / Heat Burn", "Repetitive Strain")
    val regularSymptoms = listOf(
        "Localized swelling",
        "Discoloration / Bruising",
        "Erythema / Redness",
        "Warmth around area",
        "Pain on movement",
        "Reduced range of motion",
        "Throbbing sensation",
        "Difficulty bearing weight",
        "Minor superficial bleeding"
    )
    val redFlagSymptoms = ClinicalTriageEngine.RED_FLAG_SYMPTOMS

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("new_assessment_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Injury Assessment",
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Red Flag Banner
            if (emergencyResult.isEmergency) {
                item {
                    EmergencyBanner(
                        emergencyResult = emergencyResult,
                        onCallEmergency = onNavigateToEmergency
                    )
                }
            }

            // Disclaimer Notice
            item {
                MedicalDisclaimerCard(isCompact = true)
            }

            // Step 1: Body Area Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Affected Body Area",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            bodyAreas.forEach { area ->
                                val isSelected = selectedBodyArea == area
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedBodyArea = area },
                                    label = { Text(area, fontSize = 12.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
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

            // Step 2: Injury Mechanism & Date
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. How Did the Injury Occur?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mechanisms.forEach { mech ->
                                val isSelected = injuryMechanism == mech
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { injuryMechanism = mech },
                                    label = { Text(mech, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Injury Date",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = NavyTypography
                            )
                            OutlinedTextField(
                                value = injuryDate,
                                onValueChange = { injuryDate = it },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .width(160.dp)
                                    .testTag("injury_date_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pain scale
                        Text(
                            text = "Pain Level: ${painLevel.toInt()} / 10",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (painLevel >= 7) EmergencyRed else NavyTypography
                        )
                        Slider(
                            value = painLevel,
                            onValueChange = { painLevel = it },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = if (painLevel >= 7) EmergencyRed else PrimaryBlue,
                                activeTrackColor = if (painLevel >= 7) EmergencyRed else PrimaryBlue
                            ),
                            modifier = Modifier.testTag("pain_level_slider")
                        )
                    }
                }
            }

            // Step 3: Symptoms Checklist & Emergency Warning signs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. Reported Symptoms",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select all observable signs that currently apply:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Regular symptoms chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            regularSymptoms.chunked(2).forEach { rowList ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowList.forEach { symptom ->
                                        val isChecked = selectedSymptoms.contains(symptom)
                                        FilterChip(
                                            selected = isChecked,
                                            onClick = {
                                                if (isChecked) selectedSymptoms.remove(symptom) else selectedSymptoms.add(symptom)
                                            },
                                            label = { Text(symptom, fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryContainerLight,
                                                selectedLabelColor = PrimaryBlue
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Red flag symptoms box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmergencyRedContainer.copy(alpha = 0.5f))
                                .border(1.dp, EmergencyRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Red Flag warning",
                                        tint = EmergencyRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Emergency Warning Signs (Requires immediate hospital care):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnEmergencyRedContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                redFlagSymptoms.forEach { redFlag ->
                                    val isChecked = selectedSymptoms.contains(redFlag)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                if (isChecked) selectedSymptoms.remove(redFlag) else selectedSymptoms.add(redFlag)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isChecked) EmergencyRed else Color.White)
                                                .border(1.dp, if (isChecked) EmergencyRed else Color.Gray, RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isChecked) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = redFlag,
                                            fontSize = 11.sp,
                                            color = if (isChecked) EmergencyRed else NavyTypography,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 4: Injury Image or X-ray Upload
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "4. Image Submission",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyTypography
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select image type and upload an external photograph or X-ray radiograph:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = imageType == "EXTERNAL_PHOTO",
                                onClick = { imageType = "EXTERNAL_PHOTO" },
                                label = { Text("External Photo", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = imageType == "XRAY",
                                onClick = { imageType = "XRAY" },
                                label = { Text("X-Ray Radiograph", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // X-Ray notice
                        if (imageType == "XRAY") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Notice: Automated fracture detection is clinically unavailable on this general system. X-ray submissions will be analyzed for reference context, but require certified radiologist evaluation.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Image Upload / Preview Box
                        if (imageUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Selected injury image preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = { imageUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .testTag("remove_image_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("upload_image_picker_area"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Add image",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap to Select Injury Photo / X-Ray",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = PrimaryBlue
                                    )
                                    Text(
                                        text = "Supports JPG, PNG, WebP (Validated client-side)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Additional Notes text field
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Clinical Context / Additional Notes") },
                            placeholder = { Text("e.g. Inverted foot stepping off curb, heard mild pop, swelling occurred within 2 hours...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("injury_description_input"),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            // Error Message
            if (submissionError != null) {
                item {
                    Text(
                        text = submissionError!!,
                        color = EmergencyRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        viewModel.submitAssessment(
                            injuryDate = injuryDate,
                            bodyArea = selectedBodyArea,
                            injuryMechanism = injuryMechanism,
                            symptoms = selectedSymptoms.toList(),
                            painLevel = painLevel.toInt(),
                            description = description,
                            imageUri = imageUri?.toString(),
                            imageType = imageType,
                            onSuccess = { completedInjury ->
                                onAssessmentCompleted(completedInjury.record.id)
                            }
                        )
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (emergencyResult.isEmergency) EmergencyRed else PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_assessment_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyzing Clinical Presentation...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = if (emergencyResult.isEmergency) Icons.Default.Emergency else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (emergencyResult.isEmergency) "Submit Case & Alert EMS" else "Generate Clinical Assessment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
