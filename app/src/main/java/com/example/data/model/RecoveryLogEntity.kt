package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "recovery_logs",
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
data class RecoveryLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val injuryRecordId: String,
    val userId: String = "user_default_1",
    val painLevel: Int = 2,
    val swellingStatus: String = "Mild",
    val mobilityScore: Int = 3,
    val progressNotes: String = "",
    val symptoms: List<String> = emptyList(),
    val reportedRecoveryStatus: String = "Healing well", // "Healing well", "Plateaued", "Worsening", "Fully Recovered"
    val createdAt: Long = System.currentTimeMillis()
)
