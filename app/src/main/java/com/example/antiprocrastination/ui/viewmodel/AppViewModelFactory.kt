package com.example.antiprocrastination.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.antiprocrastination.domain.repository.SettingsRepository
import com.example.antiprocrastination.data.DistractionDao
import com.example.antiprocrastination.data.TaskDao
import com.example.antiprocrastination.data.UsageTrackerImpl

class AppViewModelFactory(
    private val taskDao: TaskDao,
    private val distractionDao: DistractionDao,
    private val settingsRepository: SettingsRepository,
    private val usageTrackerImpl: UsageTrackerImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(taskDao, distractionDao, settingsRepository, usageTrackerImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
