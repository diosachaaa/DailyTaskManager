package com.example.dailytaskmanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY deadlineMillis ASC")
    fun getTasksByDeadline(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, deadlineMillis ASC")
    fun getTasksByStatusUncompletedFirst(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY isCompleted DESC, deadlineMillis ASC")
    fun getTasksByStatusCompletedFirst(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean)
}