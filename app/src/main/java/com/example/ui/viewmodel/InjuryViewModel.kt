package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ClinicalTriageEngine
import com.example.data.ai.EmergencyCheckResult
import com.example.data.db.AppDatabase
import com.example.data.model.InjuryWithDetails
import com.example.data.model.ProfileEntity
import com.example.data.repository.InjuryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InjuryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InjuryRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = InjuryRepository(db.injuryDao())
    }

    val allInjuries: StateFlow<List<InjuryWithDetails>> = repository.allInjuriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<ProfileEntity?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalInjuriesCount: StateFlow<Int> = repository.totalInjuriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalLogsCount: StateFlow<Int> = repository.totalLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedBodyAreaFilter = MutableStateFlow<String?>(null)
    val selectedStatusFilter = MutableStateFlow<String?>(null)

    val filteredInjuries: StateFlow<List<InjuryWithDetails>> = combine(
        allInjuries,
        searchQuery,
        selectedBodyAreaFilter,
        selectedStatusFilter
    ) { list, query, bodyArea, status ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.record.bodyArea.contains(query, ignoreCase = true) ||
                item.record.injuryDescription.contains(query, ignoreCase = true) ||
                item.record.injuryMechanism.contains(query, ignoreCase = true) ||
                (item.latestAssessment?.possibleCategory?.contains(query, ignoreCase = true) == true)

            val matchesBodyArea = bodyArea.isNullOrBlank() ||
                item.record.bodyArea.equals(bodyArea, ignoreCase = true)

            val matchesStatus = status.isNullOrBlank() ||
                item.record.status.equals(status, ignoreCase = true)

            matchesQuery && matchesBodyArea && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected injury detail
    private val _selectedInjuryId = MutableStateFlow<String?>(null)
    val selectedInjury: StateFlow<InjuryWithDetails?> = combine(
        allInjuries,
        _selectedInjuryId
    ) { list, id ->
        list.firstOrNull { it.record.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Submission & UI States
    val isSubmitting = MutableStateFlow(false)
    val submissionError = MutableStateFlow<String?>(null)
    val lastSubmittedInjury = MutableStateFlow<InjuryWithDetails?>(null)
    val emergencyAlert = MutableStateFlow<EmergencyCheckResult?>(null)

    init {
        // Ensure default profile is created
        viewModelScope.launch {
            repository.getProfile()
        }
    }

    fun selectInjury(id: String) {
        _selectedInjuryId.value = id
    }

    fun checkRedFlagsLive(symptoms: List<String>, mechanism: String, description: String): EmergencyCheckResult {
        val check = ClinicalTriageEngine.evaluateEmergency(symptoms, mechanism, description)
        if (check.isEmergency) {
            emergencyAlert.value = check
        }
        return check
    }

    fun dismissEmergencyAlert() {
        emergencyAlert.value = null
    }

    fun submitAssessment(
        injuryDate: String,
        bodyArea: String,
        injuryMechanism: String,
        symptoms: List<String>,
        painLevel: Int,
        description: String,
        imageUri: String?,
        imageType: String,
        onSuccess: (InjuryWithDetails) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            submissionError.value = null

            // Prior to submission, check emergency triage
            val emergencyCheck = ClinicalTriageEngine.evaluateEmergency(symptoms, injuryMechanism, description)
            if (emergencyCheck.isEmergency) {
                emergencyAlert.value = emergencyCheck
            }

            val result = repository.submitInjuryAssessment(
                injuryDate = injuryDate,
                bodyArea = bodyArea,
                injuryMechanism = injuryMechanism,
                symptoms = symptoms,
                painLevel = painLevel,
                description = description,
                imageUri = imageUri,
                imageType = imageType
            )

            isSubmitting.value = false

            result.onSuccess { injuryDetails ->
                lastSubmittedInjury.value = injuryDetails
                _selectedInjuryId.value = injuryDetails.record.id
                onSuccess(injuryDetails)
            }.onFailure { err ->
                submissionError.value = err.localizedMessage ?: "Failed to save assessment. Please try again."
            }
        }
    }

    fun addRecoveryLog(
        injuryRecordId: String,
        painLevel: Int,
        swellingStatus: String,
        mobilityScore: Int,
        progressNotes: String,
        symptoms: List<String>,
        reportedRecoveryStatus: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addRecoveryLog(
                injuryRecordId = injuryRecordId,
                painLevel = painLevel,
                swellingStatus = swellingStatus,
                mobilityScore = mobilityScore,
                progressNotes = progressNotes,
                symptoms = symptoms,
                reportedRecoveryStatus = reportedRecoveryStatus
            )
            onComplete()
        }
    }

    fun deleteInjury(id: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteInjuryRecord(id)
            if (_selectedInjuryId.value == id) {
                _selectedInjuryId.value = null
            }
            onDeleted()
        }
    }

    fun deleteRecoveryLog(logId: String) {
        viewModelScope.launch {
            repository.deleteRecoveryLog(logId)
        }
    }

    fun updateProfile(displayName: String, emergencyContact: String, emergencyPhone: String, allergies: String, conditions: String) {
        viewModelScope.launch {
            val current = repository.getProfile()
            repository.updateProfile(
                current.copy(
                    displayName = displayName,
                    emergencyContactName = emergencyContact,
                    emergencyContactPhone = emergencyPhone,
                    allergies = allergies,
                    underlyingConditions = conditions
                )
            )
        }
    }
}
