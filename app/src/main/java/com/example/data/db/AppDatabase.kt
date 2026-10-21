package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AiAssessmentEntity
import com.example.data.model.InjuryImageEntity
import com.example.data.model.InjuryRecordEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.RecoveryLogEntity

@Database(
    entities = [
        ProfileEntity::class,
        InjuryRecordEntity::class,
        InjuryImageEntity::class,
        AiAssessmentEntity::class,
        RecoveryLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun injuryDao(): InjuryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "injury_ai_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
