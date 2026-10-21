package com.example.data.repository

import com.example.data.ai.AssessmentOutput
import com.example.data.ai.ClinicalTriageEngine
import com.example.data.ai.EmergencyCheckResult
import com.example.data.db.InjuryDao
import com.example.data.model.AiAssessmentEntity
import com.example.data.model.InjuryImageEntity
import com.example.data.model.InjuryRecordEntity
import com.example.data.model.InjuryWithDetails
import com.example.data.model.ProfileEntity
import com.example.data.model.RecoveryLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InjuryRepository(private val dao: InjuryDao) {

    val allInjuriesFlow: Flow<List<InjuryWithDetails>> = dao.getAllInjuriesWithDetails()
    val profileFlow: Flow<ProfileEntity?> = dao.getProfileFlow()
    val totalInjuriesFlow: Flow<Int> = dao.getTotalInjuriesCountFlow()
    val totalLogsFlow: Flow<Int> = dao.getTotalLogsCountFlow()

    fun getInjuryWithDetailsFlow(id: String): Flow<InjuryWithDetails?> {
        return dao.getInjuryWithDetailsByIdFlow(id)
    }

    fun getRecoveryLogsFlow(injuryId: String): Flow<List<RecoveryLogEntity>> {
        return dao.getRecoveryLogsForRecordFlow(injuryId)
    }

    suspend fun getProfile(): ProfileEntity {
        return withContext(Dispatchers.IO) {
            dao.getProfile() ?: ProfileEntity().also { dao.upsertProfile(it) }
        }
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        withContext(Dispatchers.IO) {
            dao.upsertProfile(profile.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun checkImmediateEmergency(
        symptoms: List<String>,
        injuryMechanism: String,
        description: String
    ): EmergencyCheckResult {
        return ClinicalTriageEngine.evaluateEmergency(symptoms, injuryMechanism, description)
    }

    suspend fun submitInjuryAssessment(
        injuryDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        bodyArea: String,
        injuryMechanism: String,
        symptoms: List<String>,
        painLevel: Int,
        description: String,
        imageUri: String?,
        imageType: String // "EXTERNAL_PHOTO" or "XRAY"
    ): Result<InjuryWithDetails> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Safety triage evaluation
            val emergencyCheck = ClinicalTriageEngine.evaluateEmergency(symptoms, injuryMechanism, description)

            val recordId = UUID.randomUUID().toString()
            val userId = dao.getProfile()?.id ?: "user_default_1"

            val injuryRecord = InjuryRecordEntity(
                id = recordId,
                userId = userId,
                injuryDate = injuryDate,
                bodyArea = bodyArea,
                injuryDescription = description,
                injuryMechanism = injuryMechanism,
                symptoms = symptoms,
                painLevel = painLevel,
                status = if (emergencyCheck.isEmergency) "Active - Emergency Alert" else "Active",
                emergencyFlag = emergencyCheck.isEmergency,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            dao.insertInjuryRecord(injuryRecord)

            // Step 2: Save Image if provided
            var imageEntity: InjuryImageEntity? = null
            if (!imageUri.isNullOrBlank()) {
                imageEntity = InjuryImageEntity(
                    id = UUID.randomUUID().toString(),
                    injuryRecordId = recordId,
                    userId = userId,
                    storagePath = imageUri,
                    imageType = imageType,
                    originalFilename = if (imageType == "XRAY") "radiograph_$recordId.jpg" else "injury_photo_$recordId.jpg",
                    mimeType = "image/jpeg",
                    fileSize = 1024L * 150L,
                    createdAt = System.currentTimeMillis()
                )
                dao.insertInjuryImage(imageEntity)
            }

            // Step 3: Clinical AI Assessment
            val clinicalOutput = ClinicalTriageEngine.generateClinicalAssessment(
                imageType = imageType,
                bodyArea = bodyArea,
                injuryMechanism = injuryMechanism,
                symptoms = symptoms,
                painLevel = painLevel,
                description = description,
                hasImage = !imageUri.isNullOrBlank()
            )

            val assessmentEntity = AiAssessmentEntity(
                id = UUID.randomUUID().toString(),
                injuryRecordId = recordId,
                userId = userId,
                assessmentStatus = clinicalOutput.assessmentStatus,
                imageType = clinicalOutput.imageType,
                visibleObservations = clinicalOutput.visibleObservations,
                possibleCategory = clinicalOutput.possibleCategory,
                uncertaintyNotes = clinicalOutput.uncertaintyNotes,
                urgencyLevel = clinicalOutput.urgencyLevel,
                recoveryEstimate = clinicalOutput.recoveryEstimate,
                recoveryExplanation = clinicalOutput.recoveryExplanation,
                firstAidGuidance = clinicalOutput.firstAidGuidance,
                medicalReferences = clinicalOutput.medicalReferences,
                modelIdentifier = clinicalOutput.modelIdentifier,
                disclaimerNotice = "InjuryAI is a decision-support & educational tool. It is NOT a substitute for professional clinical or radiologic diagnosis.",
                createdAt = System.currentTimeMillis()
            )
            dao.insertAssessment(assessmentEntity)

            val fullDetail = dao.getInjuryWithDetailsById(recordId)
                ?: InjuryWithDetails(
                    record = injuryRecord,
                    images = listOfNotNull(imageEntity),
                    assessments = listOf(assessmentEntity),
                    recoveryLogs = emptyList()
                )

            Result.success(fullDetail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addRecoveryLog(
        injuryRecordId: String,
        painLevel: Int,
        swellingStatus: String,
        mobilityScore: Int,
        progressNotes: String,
        symptoms: List<String>,
        reportedRecoveryStatus: String
    ): Result<RecoveryLogEntity> = withContext(Dispatchers.IO) {
        try {
            val userId = dao.getProfile()?.id ?: "user_default_1"
            val log = RecoveryLogEntity(
                id = UUID.randomUUID().toString(),
                injuryRecordId = injuryRecordId,
                userId = userId,
                painLevel = painLevel,
                swellingStatus = swellingStatus,
                mobilityScore = mobilityScore,
                progressNotes = progressNotes,
                symptoms = symptoms,
                reportedRecoveryStatus = reportedRecoveryStatus,
                createdAt = System.currentTimeMillis()
            )
            dao.insertRecoveryLog(log)

            // Also update injury record status if recovered or worsening
            val injury = dao.getInjuryWithDetailsById(injuryRecordId)
            if (injury != null) {
                val newStatus = when (reportedRecoveryStatus) {
                    "Fully Recovered" -> "Resolved"
                    "Worsening" -> "Worsening"
                    "Healing well" -> "Healing"
                    "Plateaued" -> "Plateaued"
                    else -> injury.record.status
                }
                dao.updateInjuryRecord(injury.record.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
            }

            Result.success(log)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInjuryRecord(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteInjuryRecord(id)
        }
    }

    suspend fun deleteRecoveryLog(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteRecoveryLog(id)
        }
    }

    suspend fun updateInjuryStatus(id: String, status: String) {
        withContext(Dispatchers.IO) {
            val record = dao.getInjuryWithDetailsById(id)?.record
            if (record != null) {
                dao.updateInjuryRecord(record.copy(status = status, updatedAt = System.currentTimeMillis()))
            }
        }
    }
}
