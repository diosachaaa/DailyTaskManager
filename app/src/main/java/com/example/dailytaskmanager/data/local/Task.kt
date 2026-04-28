package com.example.dailytaskmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val deadlineMillis: Long,

    val isCompleted: Boolean = false,

    val createdAtMillis: Long = System.currentTimeMillis()
)