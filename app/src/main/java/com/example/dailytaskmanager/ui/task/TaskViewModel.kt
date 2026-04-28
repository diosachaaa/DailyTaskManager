package com.example.dailytaskmanager.ui.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailytaskmanager.data.local.Task
import com.example.dailytaskmanager.data.local.TaskDatabase
import com.example.dailytaskmanager.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    private val _sortType = MutableStateFlow(TaskSortType.DEADLINE)
    val sortType: StateFlow<TaskSortType> = _sortType

    val tasks: StateFlow<List<Task>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)

        tasks = _sortType
            .flatMapLatest { selectedSortType ->
                when (selectedSortType) {
                    TaskSortType.DEADLINE -> {
                        repository.getTasksByDeadline()
                    }

                    TaskSortType.STATUS_UNCOMPLETED_FIRST -> {
                        repository.getTasksByStatusUncompletedFirst()
                    }

                    TaskSortType.STATUS_COMPLETED_FIRST -> {
                        repository.getTasksByStatusCompletedFirst()
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun changeSortType(newSortType: TaskSortType) {
        _sortType.value = newSortType
    }

    fun addTask(
        title: String,
        deadlineMillis: Long
    ) {
        val trimmedTitle = title.trim()

        if (trimmedTitle.isBlank()) return

        viewModelScope.launch {
            val newTask = Task(
                title = trimmedTitle,
                deadlineMillis = deadlineMillis,
                isCompleted = false
            )

            repository.insertTask(newTask)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun deleteTaskById(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }

    fun updateTaskStatus(
        taskId: Int,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            repository.updateTaskStatus(
                taskId = taskId,
                isCompleted = isCompleted
            )
        }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            repository.updateTaskStatus(
                taskId = task.id,
                isCompleted = !task.isCompleted
            )
        }
    }
}