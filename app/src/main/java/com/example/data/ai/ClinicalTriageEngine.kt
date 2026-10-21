package com.example.data.ai

data class EmergencyCheckResult(
    val isEmergency: Boolean,
    val redFlagReason: String,
    val recommendedAction: String,
    val emergencyPhoneNumbers: List<String> = listOf("911 (US/Canada)", "112 (Europe/International)", "999 (UK)", "000 (Australia)")
)

data class AssessmentOutput(
    val assessmentStatus: String, // "COMPLETED", "EMERGENCY_TRIAGED", "FAILED"
    val imageType: String,
    val visibleObservations: String,
    val possibleCategory: String,
    val uncertaintyNotes: String,
    val urgencyLevel: String, // "EMERGENCY", "URGENT_CARE", "ROUTINE_EVALUATION", "HOME_CARE_OBSERVATION"
    val recoveryEstimate: String,
    val recoveryExplanation: String,
    val firstAidGuidance: String,
    val medicalReferences: List<String>,
    val requiresInPersonDoctor: Boolean,
    val modelIdentifier: String = "InjuryAI-ClinicalSafety-v2.5"
)

object ClinicalTriageEngine {

    val RED_FLAG_SYMPTOMS = listOf(
        "Uncontrolled or pulsating bleeding",
        "Visible bone deformity or exposed bone",
        "Loss of consciousness, dizziness, or head trauma confusion",
        "Difficulty breathing or acute chest pressure",
        "Limb is cold, pale, numb, or tingling distally",
        "Deep or extensive thermal / chemical burn",
        "Sudden inability to move limb or complete sensory loss"
    )

    fun evaluateEmergency(
        selectedSymptoms: List<String>,
        injuryMechanism: String,
        description: String
    ): EmergencyCheckResult {
        val textCorpus = (selectedSymptoms.joinToString(" ") + " " + injuryMechanism + " " + description).lowercase()

        val matchedRedFlags = mutableListOf<String>()

        if (textCorpus.contains("uncontrolled bleeding") || textCorpus.contains("pulsating") || textCorpus.contains("spurting")) {
            matchedRedFlags.add("Active severe hemorrhage / uncontrolled bleeding reported")
        }
        if (textCorpus.contains("exposed bone") || textCorpus.contains("deformed") || textCorpus.contains("compound fracture") || textCorpus.contains("crooked")) {
            matchedRedFlags.add("Suspected open fracture or gross anatomic deformity")
        }
        if (textCorpus.contains("loss of consciousness") || textCorpus.contains("blackout") || textCorpus.contains("passed out") || textCorpus.contains("concussion")) {
            matchedRedFlags.add("Altered consciousness or neuro-trauma red flag")
        }
        if (textCorpus.contains("difficulty breathing") || textCorpus.contains("shortness of breath") || textCorpus.contains("chest pain")) {
            matchedRedFlags.add("Respiratory compromise or thoracic trauma")
        }
        if (textCorpus.contains("numb") || textCorpus.contains("cold limb") || textCorpus.contains("pale") || textCorpus.contains("loss of pulse")) {
            matchedRedFlags.add("Neurovascular compromise distal to injury")
        }

        for (redFlag in RED_FLAG_SYMPTOMS) {
            if (selectedSymptoms.contains(redFlag)) {
                matchedRedFlags.add(redFlag)
            }
        }

        return if (matchedRedFlags.isNotEmpty()) {
            EmergencyCheckResult(
                isEmergency = true,
                redFlagReason = matchedRedFlags.distinct().joinToString("; "),
                recommendedAction = "IMMEDIATE EMERGENCY ACTION REQUIRED: Do not wait for standard image analysis or home recovery. Contact emergency medical services immediately or proceed to the nearest emergency trauma department."
            )
        } else {
            EmergencyCheckResult(
                isEmergency = false,
                redFlagReason = "",
                recommendedAction = ""
            )
        }
    }

    fun generateClinicalAssessment(
        imageType: String,
        bodyArea: String,
        injuryMechanism: String,
        symptoms: List<String>,
        painLevel: Int,
        description: String,
        hasImage: Boolean
    ): AssessmentOutput {
        val emergencyCheck = evaluateEmergency(symptoms, injuryMechanism, description)

        if (emergencyCheck.isEmergency) {
            return AssessmentOutput(
                assessmentStatus = "EMERGENCY_TRIAGED",
                imageType = imageType,
                visibleObservations = "Critical red flag warning signs detected in user clinical presentation: ${emergencyCheck.redFlagReason}.",
                possibleCategory = "Immediate Medical Emergency / High-Acuity Trauma",
                uncertaintyNotes = "Reassuring visual appearances cannot rule out deep tissue compromise, severe internal hemorrhage, or neurovascular injury.",
                urgencyLevel = "EMERGENCY",
                recoveryEstimate = "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional.",
                recoveryExplanation = "Clinical safety mandate: In acute emergency presentations, recovery estimates cannot be provided until urgent stabilization and formal diagnostic imaging are completed by medical personnel.",
                firstAidGuidance = "Call emergency medical services immediately (911 / 112 / 999). Apply firm, steady pressure with a sterile dressing if external bleeding is present. Keep patient calm and warm. Do not attempt to manipulate, push back, or realign exposed bones or deformed joints. Do not administer food or oral fluids prior to emergency evaluation.",
                medicalReferences = listOf(
                    "American College of Emergency Physicians (ACEP) Trauma Triage Guidelines",
                    "ATLS (Advanced Trauma Life Support) Hemorrhage & Airway Protocols"
                ),
                requiresInPersonDoctor = true
            )
        }

        // Special mandated handling for X-RAY images (Prompt Section 5)
        if (imageType.equals("XRAY", ignoreCase = true)) {
            return AssessmentOutput(
                assessmentStatus = "COMPLETED",
                imageType = "X-Ray Radiograph",
                visibleObservations = "Radiographic submission detected for $bodyArea. General-purpose computer vision algorithms are not certified medical software and cannot reliably detect non-displaced fractures, cortical disruptions, hairline fissures, or joint effusions.",
                possibleCategory = "Radiographic Examination Requiring Formal Reading",
                uncertaintyNotes = "CRITICAL LIMITATION: Automated fracture detection is clinically unavailable on this general system. Sub-millimeter bony alignment, radiopaque foreign bodies, growth plate involvement, and occult fractures require a calibrated DICOM viewing station and licensed radiologist evaluation.",
                urgencyLevel = if (painLevel >= 7) "URGENT_CARE" else "ROUTINE_EVALUATION",
                recoveryEstimate = "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional.",
                recoveryExplanation = "Skeletal healing timelines depend strictly on certified radiology findings (e.g. undisplaced vs. displaced, articular involvement, stability, immobilization requirement).",
                firstAidGuidance = "Maintain immobilization of the affected $bodyArea with a supportive splint or sling if already fitted. Avoid heavy loading or torsion until formal radiologist report and orthopedic consultation.",
                medicalReferences = listOf(
                    "American College of Radiology (ACR) Appropriateness Criteria for Musculoskeletal Trauma",
                    "Ottawa Rules for Ankle, Knee, and Foot Radiographic Assessment"
                ),
                requiresInPersonDoctor = true
            )
        }

        // External Photograph Assessment (Prompt Section 5 & 6)
        val symptomsLower = symptoms.map { it.lowercase() }
        val mechanismLower = injuryMechanism.lowercase()

        val observationsBuilder = StringBuilder()
        observationsBuilder.append("Evaluation of visible $bodyArea soft tissue presentation. ")

        if (symptomsLower.contains("swelling") || symptomsLower.contains("bruising")) {
            observationsBuilder.append("Visible characteristics may include localized edema, discoloration consistent with interstitial extravasation (contusion/ecchymosis). ")
        }
        if (symptomsLower.contains("redness") || symptomsLower.contains("warmth")) {
            observationsBuilder.append("Erythema and localized inflammatory signs noted. ")
        }
        if (mechanismLower.contains("cut") || mechanismLower.contains("sharp") || symptomsLower.contains("abrasion")) {
            observationsBuilder.append("Superficial skin barrier disruption / cutaneous lesion noted. ")
        }
        if (observationsBuilder.length < 60) {
            observationsBuilder.append("Presentation reflects localized soft-tissue strain, sprain, or contusion without apparent gross skin rupture.")
        }

        val category: String
        val urgency: String
        val recoveryEstimate: String
        val recoveryExplanation: String
        val firstAid: String
        val references: List<String>
        val requiresDoctor: Boolean

        when {
            mechanismLower.contains("cut") || mechanismLower.contains("laceration") || mechanismLower.contains("sharp") -> {
                category = "Superficial Cut / Laceration / Abrasion"
                urgency = if (painLevel >= 7 || symptomsLower.contains("deep cut")) "URGENT_CARE" else "HOME_CARE_OBSERVATION"
                recoveryEstimate = "Approximately 5 to 14 days for uncomplicated re-epithelialization."
                recoveryExplanation = "Estimate applies only to clean, superficial epithelial disruptions. Recovery can be delayed by infection, wound tension, diabetes, or inadequate sterile dressing care."
                firstAid = "1. Cleanse gently with mild soap and potable water or saline.\n2. Apply light direct pressure with sterile gauze.\n3. Apply a thin layer of sterile petroleum jelly or antibiotic ointment and cover with sterile adhesive bandage.\n4. Seek medical attention if wound edges gape (>6mm), bleeding persists, or tetanus vaccination is outdated (>5-10 years)."
                references = listOf("CDC Wound Care & Tetanus Guidelines", "AAFP Minor Wound Management Review")
                requiresDoctor = urgency != "HOME_CARE_OBSERVATION"
            }

            mechanismLower.contains("twist") || mechanismLower.contains("slip") || symptomsLower.contains("reduced mobility") -> {
                category = "Suspected Ligamentous Sprain or Musculotendinous Strain (Grade I-II)"
                urgency = if (painLevel >= 6 || symptomsLower.contains("inability to bear weight")) "URGENT_CARE" else "ROUTINE_EVALUATION"
                recoveryEstimate = if (symptomsLower.contains("inability to bear weight") || painLevel >= 7) {
                    "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional."
                } else {
                    "Approximately 2 to 4 weeks for mild-to-moderate ligamentous/tendinous soft-tissue recovery."
                }
                recoveryExplanation = "Actual recovery depends on exact tissue grade, presence of partial tears, joint stability, and progressive rehabilitation. High-stress joints (e.g. ankle, knee) may require functional bracing."
                firstAid = "Adopt the modern P.E.A.C.E. & L.O.V.E. / R.I.C.E. protocol:\n- Protect: Unload and restrict movement for the first 24-48 hours.\n- Elevate: Keep limb elevated above heart level when resting.\n- Compress: Use an elastic bandage to control acute edema without compromising arterial flow.\n- Gentle Movement: Pain-free active range of motion as tolerated after acute phase."
                references = listOf("British Journal of Sports Medicine: Soft Tissue Injury Protocol", "American Academy of Orthopaedic Surgeons (AAOS) Sprain Guidelines")
                requiresDoctor = urgency == "URGENT_CARE"
            }

            mechanismLower.contains("burn") || mechanismLower.contains("thermal") -> {
                category = "Superficial Partial-Thickness Burn"
                urgency = if (painLevel >= 6 || symptomsLower.contains("blistering")) "URGENT_CARE" else "HOME_CARE_OBSERVATION"
                recoveryEstimate = "Approximately 7 to 21 days for superficial epidermal healing."
                recoveryExplanation = "Healing depends on burn depth, total surface area, blister integrity, and avoidance of bacterial infection. Burns to hands, face, feet, or groin warrant prompt medical evaluation."
                firstAid = "1. Cool with cool (not iced) running tap water for 10-20 minutes.\n2. Do NOT burst or deroof intact blisters.\n3. Cover loosely with sterile, non-adherent dressing.\n4. Avoid applying butter, oil, or home ointments."
                references = listOf("American Burn Association Practice Guidelines", "World Health Organization Burn First Aid")
                requiresDoctor = urgency != "HOME_CARE_OBSERVATION"
            }

            else -> {
                category = "Mild Soft-Tissue Contusion / Blunt Impact"
                urgency = if (painLevel >= 6) "ROUTINE_EVALUATION" else "HOME_CARE_OBSERVATION"
                recoveryEstimate = "Approximately 1 to 3 weeks for resolution of subcutaneous contusion."
                recoveryExplanation = "Extravasated blood reabsorption timeline varies by tissue vascularity, patient age, coagulopathy status, and resting adherence."
                firstAid = "1. Intermittent cold application (15-20 min every 2-3 hours wrapped in cloth) during first 48 hours to minimize bruising.\n2. Elevation of $bodyArea above heart level.\n3. Avoid high-impact loading or vigorous massage in acute phase."
                references = listOf("Mayo Clinic Guidelines on Contusion Management", "AAOS Musculoskeletal Recovery Fundamentals")
                requiresDoctor = false
            }
        }

        return AssessmentOutput(
            assessmentStatus = "COMPLETED",
            imageType = if (imageType == "EXTERNAL_PHOTO") "External Injury Photograph" else imageType,
            visibleObservations = observationsBuilder.toString(),
            possibleCategory = category,
            uncertaintyNotes = "ASSESSMENT LIMITATIONS: Two-dimensional surface photographs cannot evaluate underlying osseous integrity, ligamentous laxity, occult joint effusions, or microvascular compromise. Visual appearances vary with skin tone, ambient lighting, and resolution.",
            urgencyLevel = urgency,
            recoveryEstimate = recoveryEstimate,
            recoveryExplanation = recoveryExplanation,
            firstAidGuidance = firstAid,
            medicalReferences = references,
            requiresInPersonDoctor = requiresDoctor
        )
    }
}
