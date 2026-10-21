package com.example

import com.example.data.ai.ClinicalTriageEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalTriageUnitTest {

    @Test
    fun emergencyTriage_detectsUncontrolledBleeding() {
        val symptoms = listOf("Uncontrolled or pulsating bleeding", "Severe pain")
        val result = ClinicalTriageEngine.evaluateEmergency(
            selectedSymptoms = symptoms,
            injuryMechanism = "Accidental laceration with power tool",
            description = "Heavy bleeding that won't stop with pressure"
        )

        assertTrue("Should trigger emergency triage for uncontrolled bleeding", result.isEmergency)
        assertTrue(result.redFlagReason.contains("bleeding", ignoreCase = true))
    }

    @Test
    fun emergencyTriage_detectsGrossDeformityOrExposedBone() {
        val symptoms = listOf("Visible bone deformity or exposed bone")
        val result = ClinicalTriageEngine.evaluateEmergency(
            selectedSymptoms = symptoms,
            injuryMechanism = "Fall from ladder",
            description = "Arm appears crooked and bone is visible through skin"
        )

        assertTrue("Should trigger emergency triage for suspected compound fracture", result.isEmergency)
        assertTrue(result.redFlagReason.contains("fracture", ignoreCase = true) || result.redFlagReason.contains("bone", ignoreCase = true))
    }

    @Test
    fun emergencyTriage_normalMildSprain_isNotEmergency() {
        val symptoms = listOf("Localized swelling", "Pain on movement")
        val result = ClinicalTriageEngine.evaluateEmergency(
            selectedSymptoms = symptoms,
            injuryMechanism = "Twisting motion",
            description = "Inverted ankle while jogging, mild swelling"
        )

        assertFalse("Mild sprain should not trigger emergency red flag", result.isEmergency)
    }

    @Test
    fun xrayAssessment_mandatesRadiologistReviewAndStatesFractureDetectionUnavailable() {
        val output = ClinicalTriageEngine.generateClinicalAssessment(
            imageType = "XRAY",
            bodyArea = "Wrist",
            injuryMechanism = "Fall onto outstretched hand",
            symptoms = listOf("Localized swelling", "Pain on movement"),
            painLevel = 5,
            description = "Wrist radiograph following fall",
            hasImage = true
        )

        assertTrue(
            "X-ray uncertainty notes must explicitly state fracture detection is unavailable on general vision",
            output.uncertaintyNotes.contains("Automated fracture detection is clinically unavailable", ignoreCase = true)
        )
        assertEquals(
            "X-ray recovery estimate must state the required exact phrase when clinical imaging reading is pending",
            "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional.",
            output.recoveryEstimate
        )
        assertTrue(output.requiresInPersonDoctor)
    }

    @Test
    fun highAcuityUncertainInjury_returnsMandatedRecoveryExactSentence() {
        // High pain + inability to bear weight
        val output = ClinicalTriageEngine.generateClinicalAssessment(
            imageType = "EXTERNAL_PHOTO",
            bodyArea = "Ankle",
            injuryMechanism = "Twisting motion",
            symptoms = listOf("Localized swelling", "Inability to bear weight"),
            painLevel = 8,
            description = "Cannot put any weight on foot at all",
            hasImage = true
        )

        assertEquals(
            "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional.",
            output.recoveryEstimate
        )
    }

    @Test
    fun superficialCut_providesReasonableHealingTimeline() {
        val output = ClinicalTriageEngine.generateClinicalAssessment(
            imageType = "EXTERNAL_PHOTO",
            bodyArea = "Forearm",
            injuryMechanism = "Laceration / Cut",
            symptoms = listOf("Minor superficial bleeding"),
            painLevel = 2,
            description = "Kitchen paper cut / shallow skin scratch",
            hasImage = false
        )

        assertTrue(
            "Superficial cut should indicate 5 to 14 days healing range",
            output.recoveryEstimate.contains("5 to 14 days")
        )
        assertFalse(output.medicalReferences.isEmpty())
    }
}
