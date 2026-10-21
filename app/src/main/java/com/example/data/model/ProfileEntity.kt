package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val displayName: String = "Patient User",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val allergies: String = "None declared",
    val underlyingConditions: String = "None",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
