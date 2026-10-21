package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "injury_records")
data class InjuryRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "user_default_1",
    val injuryDate: String,
    val bodyArea: String,
    val injuryDescription: String,
    val injuryMechanism: String,
    val symptoms: List<String>,
    val painLevel: Int = 3,
    val status: String = "Active",
    val emergencyFlag: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
