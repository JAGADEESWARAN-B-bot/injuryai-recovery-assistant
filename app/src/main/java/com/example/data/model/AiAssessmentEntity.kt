package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "ai_assessments",
    foreignKeys = [
        ForeignKey(
            entity = InjuryRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["injuryRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["injuryRecordId"])]
)
data class AiAssessmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val injuryRecordId: String,
    val userId: String = "user_default_1",
    val assessmentStatus: String = "COMPLETED", // "COMPLETED", "FAILED", "EMERGENCY_TRIAGED"
    val imageType: String = "EXTERNAL_PHOTO",
    val visibleObservations: String,
    val possibleCategory: String,
    val uncertaintyNotes: String,
    val urgencyLevel: String, // "EMERGENCY", "URGENT_CARE", "ROUTINE_EVALUATION", "HOME_CARE_OBSERVATION"
    val recoveryEstimate: String,
    val recoveryExplanation: String,
    val firstAidGuidance: String,
    val medicalReferences: List<String>,
    val modelIdentifier: String = "Gemini-Vision-ClinicalTriage-v2.5",
    val disclaimerNotice: String = "Decision-support & educational tool only. Not a medical diagnosis or radiology report.",
    val createdAt: Long = System.currentTimeMillis()
)
