package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "injury_images",
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
data class InjuryImageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val injuryRecordId: String,
    val userId: String = "user_default_1",
    val storagePath: String,
    val imageType: String, // "EXTERNAL_PHOTO" or "XRAY"
    val originalFilename: String,
    val mimeType: String = "image/jpeg",
    val fileSize: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
