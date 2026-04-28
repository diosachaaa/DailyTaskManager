package com.example.dailytaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailytaskmanager.ui.task.TaskScreen
import com.example.dailytaskmanager.ui.task.TaskViewModel
import com.example.dailytaskmanager.ui.theme.DailyTaskManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DailyTaskManagerTheme {
                val taskViewModel: TaskViewModel = viewModel()

                TaskScreen(
                    viewModel = taskViewModel
                )
            }
        }
    }
}