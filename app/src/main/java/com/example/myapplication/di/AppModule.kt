package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.StreaksDatabase
import com.example.myapplication.data.local.dao.CompletionDao
import com.example.myapplication.data.local.dao.HabitDao
import com.example.myapplication.data.repository.dataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE habits ADD COLUMN category TEXT NOT NULL DEFAULT 'CUSTOM'")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE habits ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE completions ADD COLUMN mood TEXT")
            db.execSQL("ALTER TABLE completions ADD COLUMN notes TEXT")
            db.execSQL("ALTER TABLE completions ADD COLUMN energyLevel INTEGER")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Default frequency to '{"type":"Daily"}' which matches our Daily serialized JSON
            db.execSQL("ALTER TABLE habits ADD COLUMN frequency TEXT NOT NULL DEFAULT '{\"type\":\"Daily\"}'")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StreaksDatabase {
        return Room.databaseBuilder(
            context,
            StreaksDatabase::class.java,
            "streaks_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()
    }

    @Provides
    fun provideHabitDao(database: StreaksDatabase): HabitDao {
        return database.habitDao
    }

    @Provides
    fun provideCompletionDao(database: StreaksDatabase): CompletionDao {
        return database.completionDao
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): com.example.myapplication.domain.repository.UserPreferencesRepository {
        return com.example.myapplication.data.repository.UserPreferencesRepositoryImpl(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson {
        return com.google.gson.Gson()
    }

    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext context: Context,
        db: StreaksDatabase,
        gson: com.google.gson.Gson
    ): com.example.myapplication.domain.repository.BackupManager {
        return com.example.myapplication.data.backup.BackupManagerImpl(
            context = context,
            habitDao = db.habitDao,
            completionDao = db.completionDao,
            gson = gson
        )
    }
}
