package com.example.dailytaskmanager.data.repository

import com.example.dailytaskmanager.data.local.Task
import com.example.dailytaskmanager.data.local.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    fun getTasksByDeadline(): Flow<List<Task>> {
        return taskDao.getTasksByDeadline()
    }

    fun getTasksByStatusUncompletedFirst(): Flow<List<Task>> {
        return taskDao.getTasksByStatusUncompletedFirst()
    }

    fun getTasksByStatusCompletedFirst(): Flow<List<Task>> {
        return taskDao.getTasksByStatusCompletedFirst()
    }

    suspend fun getTaskById(taskId: Int): Task? {
        return taskDao.getTaskById(taskId)
    }

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(taskId: Int) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun updateTaskStatus(taskId: Int, isCompleted: Boolean) {
        taskDao.updateTaskStatus(taskId, isCompleted)
    }
}