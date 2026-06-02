package com.example.antiprocrastination

import com.example.antiprocrastination.domain.repository.SettingsRepository
import org.junit.Test
import org.junit.Assert.assertEquals

// El "doble" falso: cumple el contrato sin usar Android
class FakeSettingsRepository : SettingsRepository {
    override var monitoringInterval = 5
    override var notificationsEnabled = true
    override var reminderMinutesBefore = 30
    override var youtubeLimitMin = 60
    override var tiktokLimitMin = 30
    private val limits = mutableMapOf<String, Int>()
    override fun getAppLimit(packageName: String) = limits[packageName] ?: 60
    override fun setAppLimit(packageName: String, limitMinutes: Int) { limits[packageName] = limitMinutes }
}

class SettingsLogicTest {

    @Test
    fun `setAppLimit guarda y getAppLimit recupera el valor`() {
        val repo = FakeSettingsRepository()
        repo.setAppLimit("com.ejemplo.app", 45)
        assertEquals(90, repo.getAppLimit("com.ejemplo.app"))
    }

    @Test
    fun `getAppLimit devuelve 60 por defecto si no hay limite`() {
        val repo = FakeSettingsRepository()
        assertEquals(60, repo.getAppLimit("com.desconocida"))
    }
}