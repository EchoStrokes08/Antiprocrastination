package com.example.antiprocrastination.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.antiprocrastination.data.SettingsManager
import com.example.antiprocrastination.model.DistractionDao
import com.example.antiprocrastination.model.TaskDao
import com.example.antiprocrastination.usage.UsageTracker

class AppViewModelFactory(
    private val taskDao: TaskDao,
    private val distractionDao: DistractionDao,
    private val settingsManager: SettingsManager,
    private val usageTracker: UsageTracker
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(taskDao, distractionDao, settingsManager, usageTracker) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
