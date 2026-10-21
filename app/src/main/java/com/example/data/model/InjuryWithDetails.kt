package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class InjuryWithDetails(
    @Embedded
    val record: InjuryRecordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "injuryRecordId"
    )
    val images: List<InjuryImageEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "injuryRecordId"
    )
    val assessments: List<AiAssessmentEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "injuryRecordId"
    )
    val recoveryLogs: List<RecoveryLogEntity> = emptyList()
) {
    val latestAssessment: AiAssessmentEntity?
        get() = assessments.maxByOrNull { it.createdAt }

    val primaryImage: InjuryImageEntity?
        get() = images.firstOrNull()
}
