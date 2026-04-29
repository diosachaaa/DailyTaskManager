package com.example.dailytaskmanager.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dailytaskmanager.data.local.Task
import com.example.dailytaskmanager.ui.theme.DangerRed
import com.example.dailytaskmanager.ui.theme.MintPrimary
import com.example.dailytaskmanager.ui.theme.MintPrimaryDark
import com.example.dailytaskmanager.ui.theme.MintSecondary
import com.example.dailytaskmanager.ui.theme.MintSurface
import com.example.dailytaskmanager.ui.theme.MintSurfaceSoft
import com.example.dailytaskmanager.ui.theme.SuccessGreen
import com.example.dailytaskmanager.ui.theme.TextPrimary
import com.example.dailytaskmanager.ui.theme.TextSecondary
import com.example.dailytaskmanager.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    val activeTasks = tasks.filter {
        !it.isCompleted
    }

    val completedTasks = tasks.filter {
        it.isCompleted
    }

    val overdueCount = activeTasks.count {
        it.deadlineMillis < System.currentTimeMillis()
    }

    val taskSections = when (sortType) {
        TaskSortType.DEADLINE -> {
            listOf(
                TaskSection(
                    title = "All Tasks",
                    tasks = tasks
                )
            )
        }

        TaskSortType.STATUS_UNCOMPLETED_FIRST -> {
            listOf(
                TaskSection(
                    title = "Pending",
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
                    title = "Pending",
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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddTaskDialog = true
                },
                containerColor = MintPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ModernHeader(
                    totalTask = tasks.size,
                    activeCount = activeTasks.size,
                    completedCount = completedTasks.size,
                    overdueCount = overdueCount
                )
            }

            item {
                QuickAddCard(
                    onClick = {
                        showAddTaskDialog = true
                    }
                )
            }

            item {
                FilterTabs(
                    selectedSortType = sortType,
                    onSortSelected = viewModel::changeSortType
                )
            }

            if (tasks.isEmpty()) {
                item {
                    EmptyTaskState(
                        onAddClick = {
                            showAddTaskDialog = true
                        }
                    )
                }
            } else {
                taskSections.forEach { section ->
                    item {
                        SectionTitle(
                            title = section.title,
                            count = section.tasks.size
                        )
                    }

                    items(
                        items = section.tasks,
                        key = { task -> task.id }
                    ) { task ->
                        ModernTaskItem(
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
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun ModernHeader(
    totalTask: Int,
    activeCount: Int,
    completedCount: Int,
    overdueCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MintPrimary,
                        MintPrimaryDark,
                        MintSecondary
                    )
                ),
                shape = RoundedCornerShape(
                    bottomStart = 34.dp,
                    bottomEnd = 34.dp
                )
            )
            .padding(
                start = 22.dp,
                end = 22.dp,
                top = 34.dp,
                bottom = 22.dp
            )
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "My Tasks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Stay organized, get things done.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeaderStatCard(
                    modifier = Modifier.weight(1f),
                    value = totalTask.toString(),
                    label = "Total"
                )

                HeaderStatCard(
                    modifier = Modifier.weight(1f),
                    value = activeCount.toString(),
                    label = "Pending"
                )

                HeaderStatCard(
                    modifier = Modifier.weight(1f),
                    value = completedCount.toString(),
                    label = "Done"
                )

                HeaderStatCard(
                    modifier = Modifier.weight(1f),
                    value = overdueCount.toString(),
                    label = "Late"
                )
            }
        }
    }
}

@Composable
private fun HeaderStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.17f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun QuickAddCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MintSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add a new task...",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MintPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FilterTabs(
    selectedSortType: TaskSortType,
    onSortSelected: (TaskSortType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ModernFilterChip(
            text = "All",
            selected = selectedSortType == TaskSortType.DEADLINE,
            onClick = {
                onSortSelected(TaskSortType.DEADLINE)
            }
        )

        ModernFilterChip(
            text = "Pending",
            selected = selectedSortType == TaskSortType.STATUS_UNCOMPLETED_FIRST,
            onClick = {
                onSortSelected(TaskSortType.STATUS_UNCOMPLETED_FIRST)
            }
        )

        ModernFilterChip(
            text = "Completed",
            selected = selectedSortType == TaskSortType.STATUS_COMPLETED_FIRST,
            onClick = {
                onSortSelected(TaskSortType.STATUS_COMPLETED_FIRST)
            }
        )
    }
}

@Composable
private fun ModernFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(
            onClick = onClick
        ),
        shape = RoundedCornerShape(50),
        color = if (selected) {
            MintPrimary
        } else {
            MintSurface
        },
        shadowElevation = if (selected) {
            3.dp
        } else {
            1.dp
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 22.dp,
                vertical = 10.dp
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = if (selected) {
                Color.White
            } else {
                TextSecondary
            }
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "$count task",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun ModernTaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    val overdue = isOverdue(
        deadlineMillis = task.deadlineMillis,
        isCompleted = task.isCompleted
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MintSurfaceSoft
            } else {
                MintSurface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) {
                            SuccessGreen
                        } else {
                            MintSurfaceSoft
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SuccessGreen,
                        uncheckedColor = MintPrimary,
                        checkmarkColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) {
                        TextSecondary
                    } else {
                        TextPrimary
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📅",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = formatDeadline(task.deadlineMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overdue) {
                            WarningOrange
                        } else {
                            TextSecondary
                        }
                    )
                }

                Spacer(modifier = Modifier.height(9.dp))

                StatusBadge(
                    text = when {
                        task.isCompleted -> "Completed"
                        overdue -> "Overdue"
                        else -> "Pending"
                    },
                    containerColor = when {
                        task.isCompleted -> SuccessGreen.copy(alpha = 0.12f)
                        overdue -> WarningOrange.copy(alpha = 0.14f)
                        else -> MintSecondary.copy(alpha = 0.10f)
                    },
                    textColor = when {
                        task.isCompleted -> SuccessGreen
                        overdue -> WarningOrange
                        else -> MintSecondary
                    }
                )
            }

            TextButton(
                onClick = onDeleteClick
            ) {
                Text(
                    text = "Delete",
                    color = DangerRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    containerColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
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
        shape = RoundedCornerShape(28.dp),
        containerColor = MintSurface,
        title = {
            Column {
                Text(
                    text = "Add New Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Create a task with a clear deadline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
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
                        Text(text = "Task name")
                    },
                    supportingText = {
                        Text(text = "${title.length}/60")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )

                OutlinedButton(
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (isDateSelected) {
                            "Date: ${formatDateOnly(deadlineMillis)}"
                        } else {
                            "Choose deadline date"
                        }
                    )
                }

                OutlinedButton(
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (isTimeSelected) {
                            "Time: ${formatTimeOnly(deadlineMillis)}"
                        } else {
                            "Choose deadline time"
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
                enabled = canSave,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintPrimary
                )
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancel")
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
        shape = RoundedCornerShape(28.dp),
        containerColor = MintSurface,
        title = {
            Text(
                text = "Delete Task?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "This task will be permanently removed:",
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
private fun EmptyTaskState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 46.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(MintSurfaceSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MintPrimary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "No tasks yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Start by adding your first daily task.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MintPrimary
            )
        ) {
            Text(text = "+ Add Task")
        }
    }
}

private data class TaskSection(
    val title: String,
    val tasks: List<Task>
)

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