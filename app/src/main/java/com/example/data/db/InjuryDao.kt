package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AiAssessmentEntity
import com.example.data.model.InjuryImageEntity
import com.example.data.model.InjuryRecordEntity
import com.example.data.model.InjuryWithDetails
import com.example.data.model.ProfileEntity
import com.example.data.model.RecoveryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InjuryDao {

    // Profiles
    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Query("SELECT * FROM profiles LIMIT 1")
    fun getProfileFlow(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    // Injury Records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInjuryRecord(record: InjuryRecordEntity)

    @Update
    suspend fun updateInjuryRecord(record: InjuryRecordEntity)

    @Query("DELETE FROM injury_records WHERE id = :id")
    suspend fun deleteInjuryRecord(id: String)

    @Transaction
    @Query("SELECT * FROM injury_records ORDER BY createdAt DESC")
    fun getAllInjuriesWithDetails(): Flow<List<InjuryWithDetails>>

    @Transaction
    @Query("SELECT * FROM injury_records WHERE id = :id")
    suspend fun getInjuryWithDetailsById(id: String): InjuryWithDetails?

    @Transaction
    @Query("SELECT * FROM injury_records WHERE id = :id")
    fun getInjuryWithDetailsByIdFlow(id: String): Flow<InjuryWithDetails?>

    // Images
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInjuryImage(image: InjuryImageEntity)

    @Query("SELECT * FROM injury_images WHERE injuryRecordId = :recordId")
    suspend fun getImagesForRecord(recordId: String): List<InjuryImageEntity>

    // AI Assessments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AiAssessmentEntity)

    @Query("SELECT * FROM ai_assessments WHERE injuryRecordId = :recordId ORDER BY createdAt DESC")
    suspend fun getAssessmentsForRecord(recordId: String): List<AiAssessmentEntity>

    // Recovery Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecoveryLog(log: RecoveryLogEntity)

    @Query("DELETE FROM recovery_logs WHERE id = :id")
    suspend fun deleteRecoveryLog(id: String)

    @Query("SELECT * FROM recovery_logs WHERE injuryRecordId = :recordId ORDER BY createdAt DESC")
    fun getRecoveryLogsForRecordFlow(recordId: String): Flow<List<RecoveryLogEntity>>

    @Query("SELECT COUNT(*) FROM injury_records")
    fun getTotalInjuriesCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM recovery_logs")
    fun getTotalLogsCountFlow(): Flow<Int>
}
