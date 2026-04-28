package com.example.dailytaskmanager.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dailytaskmanager.data.local.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel
) {
    val tasks by viewModel.tasks.collectAsState()
    val sortType by viewModel.sortType.collectAsState()

    var showAddTaskDialog by remember {
        mutableStateOf(false)
    }

    var taskToDelete by remember {
        mutableStateOf<Task?>(null)
    }

    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    val taskSections = when (sortType) {
        TaskSortType.DEADLINE -> {
            listOf(
                TaskSection(
                    title = "Semua Task",
                    tasks = tasks
                )
            )
        }

        TaskSortType.STATUS_UNCOMPLETED_FIRST -> {
            listOf(
                TaskSection(
                    title = "Task",
                    tasks = activeTasks
                ),
                TaskSection(
                    title = "Completed",
                    tasks = completedTasks
                )
            )
        }

        TaskSortType.STATUS_COMPLETED_FIRST -> {
            listOf(
                TaskSection(
                    title = "Completed",
                    tasks = completedTasks
                ),
                TaskSection(
                    title = "Task",
                    tasks = activeTasks
                )
            )
        }
    }.filter { section ->
        section.tasks.isNotEmpty()
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = {
                showAddTaskDialog = false
            },
            onAddTask = { title, deadlineMillis ->
                viewModel.addTask(
                    title = title,
                    deadlineMillis = deadlineMillis
                )
                showAddTaskDialog = false
            }
        )
    }

    taskToDelete?.let { selectedTask ->
        ConfirmDeleteDialog(
            task = selectedTask,
            onDismiss = {
                taskToDelete = null
            },
            onConfirmDelete = {
                viewModel.deleteTask(selectedTask)
                taskToDelete = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Task Manager",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kelola tugas harianmu",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddTaskDialog = true
                }
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TaskSummary(
                activeCount = activeTasks.size,
                completedCount = completedTasks.size
            )

            Spacer(modifier = Modifier.height(12.dp))

            SortingCard(
                selectedSortType = sortType,
                onSortSelected = viewModel::changeSortType
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                EmptyTaskState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    taskSections.forEach { section ->
                        item {
                            SectionTitle(title = section.title)
                        }

                        items(
                            items = section.tasks,
                            key = { task -> task.id }
                        ) { task ->
                            TaskItem(
                                task = task,
                                onCheckedChange = {
                                    viewModel.updateTaskStatus(
                                        taskId = task.id,
                                        isCompleted = it
                                    )
                                },
                                onDeleteClick = {
                                    taskToDelete = task
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, Long) -> Unit
) {
    val context = LocalContext.current

    var title by remember {
        mutableStateOf("")
    }

    var deadlineMillis by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var isDateSelected by remember {
        mutableStateOf(false)
    }

    var isTimeSelected by remember {
        mutableStateOf(false)
    }

    val canSave = title.trim().isNotBlank() && isDateSelected && isTimeSelected

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Tambah Task")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= 60) {
                            title = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Nama task")
                    },
                    supportingText = {
                        Text(text = "${title.length}/60")
                    },
                    singleLine = true
                )

                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = deadlineMillis
                        }

                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val updatedCalendar = Calendar.getInstance().apply {
                                    timeInMillis = deadlineMillis
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                deadlineMillis = updatedCalendar.timeInMillis
                                isDateSelected = true
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isDateSelected) {
                            "Tanggal: ${formatDateOnly(deadlineMillis)}"
                        } else {
                            "Pilih tanggal deadline"
                        }
                    )
                }

                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = deadlineMillis
                        }

                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val updatedCalendar = Calendar.getInstance().apply {
                                    timeInMillis = deadlineMillis
                                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                deadlineMillis = updatedCalendar.timeInMillis
                                isTimeSelected = true
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isTimeSelected) {
                            "Waktu: ${formatTimeOnly(deadlineMillis)}"
                        } else {
                            "Pilih waktu deadline"
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddTask(
                        title.trim(),
                        deadlineMillis
                    )
                },
                enabled = canSave
            ) {
                Text(text = "Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Hapus Task?")
        },
        text = {
            Column {
                Text(text = "Task ini akan dihapus permanen dari daftar:")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete
            ) {
                Text(text = "Hapus")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
private fun TaskSummary(
    activeCount: Int,
    completedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = {},
            label = {
                Text(text = "Aktif: $activeCount")
            }
        )

        AssistChip(
            onClick = {},
            label = {
                Text(text = "Selesai: $completedCount")
            }
        )
    }
}

@Composable
private fun SortingCard(
    selectedSortType: TaskSortType,
    onSortSelected: (TaskSortType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "Urutkan task",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = selectedSortType.toDescription(),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortChip(
                    text = "Deadline",
                    selected = selectedSortType == TaskSortType.DEADLINE,
                    onClick = {
                        onSortSelected(TaskSortType.DEADLINE)
                    }
                )

                SortChip(
                    text = "Belum selesai",
                    selected = selectedSortType == TaskSortType.STATUS_UNCOMPLETED_FIRST,
                    onClick = {
                        onSortSelected(TaskSortType.STATUS_UNCOMPLETED_FIRST)
                    }
                )

                SortChip(
                    text = "Selesai dulu",
                    selected = selectedSortType == TaskSortType.STATUS_COMPLETED_FIRST,
                    onClick = {
                        onSortSelected(TaskSortType.STATUS_COMPLETED_FIRST)
                    }
                )
            }
        }
    }
}

@Composable
private fun SortChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = text)
        }
    )
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    val overdue = isOverdue(
        deadlineMillis = task.deadlineMillis,
        isCompleted = task.isCompleted
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onCheckedChange
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Deadline: ${formatDeadline(task.deadlineMillis)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = if (task.isCompleted) {
                                        "Selesai"
                                    } else {
                                        "Belum selesai"
                                    }
                                )
                            }
                        )

                        if (overdue) {
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(text = "Terlambat")
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteClick
                ) {
                    Text(
                        text = "×",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTaskState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Belum ada task",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tekan tombol + untuk menambahkan task.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private data class TaskSection(
    val title: String,
    val tasks: List<Task>
)

private fun TaskSortType.toDescription(): String {
    return when (this) {
        TaskSortType.DEADLINE -> {
            "Menampilkan semua task berdasarkan deadline terdekat."
        }

        TaskSortType.STATUS_UNCOMPLETED_FIRST -> {
            "Menampilkan task yang belum selesai terlebih dahulu."
        }

        TaskSortType.STATUS_COMPLETED_FIRST -> {
            "Menampilkan task yang sudah selesai terlebih dahulu."
        }
    }
}

private fun formatDeadline(deadlineMillis: Long): String {
    val formatter = SimpleDateFormat(
        "dd MMM yyyy, HH:mm",
        Locale.getDefault()
    )

    return formatter.format(Date(deadlineMillis))
}

private fun formatDateOnly(deadlineMillis: Long): String {
    val formatter = SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    )

    return formatter.format(Date(deadlineMillis))
}

private fun formatTimeOnly(deadlineMillis: Long): String {
    val formatter = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    )

    return formatter.format(Date(deadlineMillis))
}

private fun isOverdue(
    deadlineMillis: Long,
    isCompleted: Boolean
): Boolean {
    return !isCompleted && deadlineMillis < System.currentTimeMillis()
}