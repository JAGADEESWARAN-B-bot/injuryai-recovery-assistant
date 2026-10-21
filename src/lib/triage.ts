export interface EmergencyCheckResult {
  isEmergency: boolean;
  redFlagReason: string;
  recommendedAction: string;
  emergencyPhoneNumbers: string[];
}

export interface AssessmentResult {
  assessmentStatus: string;
  imageType: string;
  visibleObservations: string;
  possibleCategory: string;
  uncertaintyNotes: string;
  urgencyLevel: 'EMERGENCY' | 'URGENT_CARE' | 'ROUTINE_EVALUATION' | 'HOME_CARE_OBSERVATION';
  recoveryEstimate: string;
  recoveryExplanation: string;
  firstAidGuidance: string;
  medicalReferences: string[];
  requiresInPersonDoctor: boolean;
  modelIdentifier: string;
}

export const RED_FLAG_SYMPTOMS = [
  "Uncontrolled or pulsating bleeding",
  "Visible bone deformity or exposed bone",
  "Loss of consciousness, dizziness, or head trauma confusion",
  "Difficulty breathing or acute chest pressure",
  "Limb is cold, pale, numb, or tingling distally",
  "Deep or extensive thermal / chemical burn",
  "Sudden inability to move limb or complete sensory loss"
];

export function evaluateEmergency(
  selectedSymptoms: string[],
  injuryMechanism: string,
  description: string
): EmergencyCheckResult {
  const textCorpus = (selectedSymptoms.join(" ") + " " + injuryMechanism + " " + description).toLowerCase();
  const matchedRedFlags: string[] = [];

  if (textCorpus.includes("uncontrolled bleeding") || textCorpus.includes("pulsating") || textCorpus.includes("spurting")) {
    matchedRedFlags.push("Active severe hemorrhage / uncontrolled bleeding reported");
  }
  if (textCorpus.includes("exposed bone") || textCorpus.includes("deformed") || textCorpus.includes("compound fracture") || textCorpus.includes("crooked")) {
    matchedRedFlags.push("Suspected open fracture or gross anatomic deformity");
  }
  if (textCorpus.includes("loss of consciousness") || textCorpus.includes("blackout") || textCorpus.includes("passed out") || textCorpus.includes("concussion")) {
    matchedRedFlags.push("Altered consciousness or neuro-trauma red flag");
  }
  if (textCorpus.includes("difficulty breathing") || textCorpus.includes("shortness of breath") || textCorpus.includes("chest pain")) {
    matchedRedFlags.push("Respiratory compromise or thoracic trauma");
  }
  if (textCorpus.includes("numb") || textCorpus.includes("cold limb") || textCorpus.includes("pale") || textCorpus.includes("loss of pulse")) {
    matchedRedFlags.push("Neurovascular compromise distal to injury");
  }

  for (const redFlag of RED_FLAG_SYMPTOMS) {
    if (selectedSymptoms.includes(redFlag) && !matchedRedFlags.includes(redFlag)) {
      matchedRedFlags.push(redFlag);
    }
  }

  if (matchedRedFlags.length > 0) {
    return {
      isEmergency: true,
      redFlagReason: matchedRedFlags.join("; "),
      recommendedAction: "IMMEDIATE EMERGENCY ACTION REQUIRED: Do not wait for standard image analysis or home recovery. Contact emergency medical services immediately or proceed to the nearest emergency trauma department.",
      emergencyPhoneNumbers: ["911 (US/Canada)", "112 (Europe/India)", "999 (UK)", "000 (Australia)"]
    };
  }

  return {
    isEmergency: false,
    redFlagReason: "",
    recommendedAction: "",
    emergencyPhoneNumbers: ["911 (US/Canada)", "112 (Europe/India)", "999 (UK)", "000 (Australia)"]
  };
}

export function generateClinicalAssessment(params: {
  imageType: string;
  bodyArea: string;
  injuryMechanism: string;
  symptoms: string[];
  painLevel: number;
  description: string;
  hasImage: boolean;
}): AssessmentResult {
  const { imageType, bodyArea, injuryMechanism, symptoms, painLevel, description } = params;
  const emergencyCheck = evaluateEmergency(symptoms, injuryMechanism, description);

  if (emergencyCheck.isEmergency) {
    return {
      assessmentStatus: "EMERGENCY_TRIAGED",
      imageType,
      visibleObservations: `Critical red flag warning signs detected in user clinical presentation: ${emergencyCheck.redFlagReason}.`,
      possibleCategory: "Immediate Medical Emergency / High-Acuity Trauma",
      uncertaintyNotes: "Reassuring visual appearances cannot rule out deep tissue compromise, severe internal hemorrhage, or neurovascular injury.",
      urgencyLevel: "EMERGENCY",
      recoveryEstimate: "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional.",
      recoveryExplanation: "Clinical safety mandate: In acute emergency presentations, recovery estimates cannot be provided until urgent stabilization and formal diagnostic imaging are completed by medical personnel.",
      firstAidGuidance: "Call emergency medical services immediately (911 / 112 / 999). Apply firm, steady pressure with a sterile dressing if external bleeding is present. Keep patient calm and warm. Do not attempt to manipulate or straighten exposed bones.",
      medicalReferences: [
        "American College of Emergency Physicians (ACEP) Trauma Triage Guidelines",
        "ATLS (Advanced Trauma Life Support) Hemorrhage Protocols"
      ],
      requiresInPersonDoctor: true,
      modelIdentifier: "InjuryAI-ClinicalSafety-v2.5"
    };
  }

  // X-RAY handling rule (Prompt Section 5)
  if (imageType.toUpperCase() === "XRAY") {
    return {
      assessmentStatus: "COMPLETED",
      imageType: "X-Ray Radiograph",
      visibleObservations: `Radiographic submission detected for ${bodyArea}. General-purpose computer vision algorithms are not certified medical software and cannot reliably detect non-displaced fractures, cortical disruptions, hairline fissures, or joint effusions.`,
      possibleCategory: "Radiographic Examination Requiring Formal Reading",
      uncertaintyNotes: "CRITICAL LIMITATION: Automated fracture detection is clinically unavailable on this general system. Sub-millimeter bony alignment, radiopaque foreign bodies, growth plate involvement, and occult fractures require a calibrated DICOM viewing station and licensed radiologist evaluation.",
      urgencyLevel: painLevel >= 7 ? "URGENT_CARE" : "ROUTINE_EVALUATION",
      recoveryEstimate: "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional.",
      recoveryExplanation: "Skeletal healing timelines depend strictly on certified radiology findings (e.g. undisplaced vs. displaced, articular involvement, stability, immobilization requirement).",
      firstAidGuidance: `Maintain immobilization of the affected ${bodyArea} with a supportive splint or sling if already fitted. Avoid heavy loading or torsion until formal radiologist report and orthopedic consultation.`,
      medicalReferences: [
        "American College of Radiology (ACR) Appropriateness Criteria for Musculoskeletal Trauma",
        "Ottawa Rules for Ankle, Knee, and Foot Radiographic Assessment"
      ],
      requiresInPersonDoctor: true,
      modelIdentifier: "InjuryAI-ClinicalSafety-v2.5"
    };
  }

  // External photograph assessment
  const symptomsLower = symptoms.map(s => s.toLowerCase());
  const mechanismLower = injuryMechanism.toLowerCase();

  let observations = `Evaluation of visible ${bodyArea} soft tissue presentation. `;
  if (symptomsLower.includes("swelling") || symptomsLower.includes("bruising")) {
    observations += "Visible characteristics may include localized edema, discoloration consistent with interstitial extravasation (contusion/ecchymosis). ";
  }
  if (symptomsLower.includes("redness") || symptomsLower.includes("warmth")) {
    observations += "Erythema and localized inflammatory signs noted. ";
  }
  if (mechanismLower.includes("cut") || mechanismLower.includes("sharp") || symptomsLower.includes("abrasion")) {
    observations += "Superficial skin barrier disruption / cutaneous lesion noted. ";
  }
  if (observations.length < 60) {
    observations += "Presentation reflects localized soft-tissue strain, sprain, or contusion without apparent gross skin rupture.";
  }

  let category: string;
  let urgency: 'EMERGENCY' | 'URGENT_CARE' | 'ROUTINE_EVALUATION' | 'HOME_CARE_OBSERVATION';
  let recoveryEstimate: string;
  let recoveryExplanation: string;
  let firstAid: string;
  let references: string[];
  let requiresDoctor: boolean;

  if (mechanismLower.includes("cut") || mechanismLower.includes("laceration") || mechanismLower.includes("sharp")) {
    category = "Superficial Cut / Laceration / Abrasion";
    urgency = (painLevel >= 7 || symptomsLower.includes("deep cut")) ? "URGENT_CARE" : "HOME_CARE_OBSERVATION";
    recoveryEstimate = "Approximately 5 to 14 days for uncomplicated re-epithelialization.";
    recoveryExplanation = "Estimate applies only to clean, superficial epithelial disruptions. Recovery can be delayed by infection, wound tension, diabetes, or inadequate sterile dressing care.";
    firstAid = "1. Cleanse gently with mild soap and potable water or saline.\n2. Apply light direct pressure with sterile gauze.\n3. Apply a thin layer of sterile petroleum jelly or antibiotic ointment and cover with sterile adhesive bandage.\n4. Seek medical attention if wound edges gape (>6mm), bleeding persists, or tetanus vaccination is outdated (>5-10 years).";
    references = ["CDC Wound Care & Tetanus Guidelines", "AAFP Minor Wound Management Review"];
    requiresDoctor = urgency !== "HOME_CARE_OBSERVATION";
  } else if (mechanismLower.includes("twist") || mechanismLower.includes("slip") || symptomsLower.includes("reduced mobility")) {
    category = "Suspected Ligamentous Sprain or Musculotendinous Strain (Grade I-II)";
    urgency = (painLevel >= 6 || symptomsLower.includes("inability to bear weight")) ? "URGENT_CARE" : "ROUTINE_EVALUATION";
    recoveryEstimate = (symptomsLower.includes("inability to bear weight") || painLevel >= 7)
      ? "Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional."
      : "Approximately 2 to 4 weeks for mild-to-moderate ligamentous/tendinous soft-tissue recovery.";
    recoveryExplanation = "Actual recovery depends on exact tissue grade, presence of partial tears, joint stability, and progressive rehabilitation. High-stress joints (e.g. ankle, knee) may require functional bracing.";
    firstAid = "Adopt the modern P.E.A.C.E. & L.O.V.E. / R.I.C.E. protocol:\n- Protect: Unload and restrict movement for the first 24-48 hours.\n- Elevate: Keep limb elevated above heart level when resting.\n- Compress: Use an elastic bandage to control acute edema without compromising arterial flow.\n- Gentle Movement: Pain-free active range of motion as tolerated after acute phase.";
    references = ["British Journal of Sports Medicine: Soft Tissue Injury Protocol", "American Academy of Orthopaedic Surgeons (AAOS) Sprain Guidelines"];
    requiresDoctor = urgency === "URGENT_CARE";
  } else if (mechanismLower.includes("burn") || mechanismLower.includes("thermal")) {
    category = "Superficial Partial-Thickness Burn";
    urgency = (painLevel >= 6 || symptomsLower.includes("blistering")) ? "URGENT_CARE" : "HOME_CARE_OBSERVATION";
    recoveryEstimate = "Approximately 7 to 21 days for superficial epidermal healing.";
    recoveryExplanation = "Healing depends on burn depth, total surface area, blister integrity, and avoidance of bacterial infection. Burns to hands, face, feet, or groin warrant prompt medical evaluation.";
    firstAid = "1. Cool with cool (not iced) running tap water for 10-20 minutes.\n2. Do NOT burst or deroof intact blisters.\n3. Cover loosely with sterile, non-adherent dressing.\n4. Avoid applying butter, oil, or home ointments.";
    references = ["American Burn Association Practice Guidelines", "World Health Organization Burn First Aid"];
    requiresDoctor = urgency !== "HOME_CARE_OBSERVATION";
  } else {
    category = "Mild Soft-Tissue Contusion / Blunt Impact";
    urgency = painLevel >= 6 ? "ROUTINE_EVALUATION" : "HOME_CARE_OBSERVATION";
    recoveryEstimate = "Approximately 1 to 3 weeks for resolution of subcutaneous contusion.";
    recoveryExplanation = "Extravasated blood reabsorption timeline varies by tissue vascularity, patient age, coagulopathy status, and resting adherence.";
    firstAid = `1. Intermittent cold application (15-20 min every 2-3 hours wrapped in cloth) during first 48 hours to minimize bruising.\n2. Elevation of ${bodyArea} above heart level.\n3. Avoid high-impact loading or vigorous massage in acute phase.`;
    references = ["Mayo Clinic Guidelines on Contusion Management", "AAOS Musculoskeletal Recovery Fundamentals"];
    requiresDoctor = false;
  }

  return {
    assessmentStatus: "COMPLETED",
    imageType: imageType === "EXTERNAL_PHOTO" ? "External Injury Photograph" : imageType,
    visibleObservations: observations,
    possibleCategory: category,
    uncertaintyNotes: "ASSESSMENT LIMITATIONS: Two-dimensional surface photographs cannot evaluate underlying osseous integrity, ligamentous laxity, occult joint effusions, or microvascular compromise. Visual appearances vary with skin tone, ambient lighting, and resolution.",
    urgencyLevel: urgency,
    recoveryEstimate,
    recoveryExplanation,
    firstAidGuidance: firstAid,
    medicalReferences: references,
    requiresInPersonDoctor: requiresDoctor,
    modelIdentifier: "InjuryAI-ClinicalSafety-v2.5"
  };
}
