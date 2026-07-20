package com.example.myapplication.domain.repository

import android.net.Uri

interface BackupManager {
    suspend fun exportToJson(uri: Uri): Result<Unit>
    suspend fun importFromJson(uri: Uri): Result<Unit>
    suspend fun exportToCsv(uri: Uri): Result<Unit>
}
