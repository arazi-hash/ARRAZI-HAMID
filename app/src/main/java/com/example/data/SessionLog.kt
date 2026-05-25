package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val pointsChange: Int,
    val status: String // complete, missed, bonus, redemption
)
