package com.example.antiprocrastination

import com.example.antiprocrastination.domain.usecase.CalculateTaskScoreUseCase
import com.example.antiprocrastination.domain.model.Task
import org.junit.Test
import org.junit.Assert.assertEquals
import java.time.LocalDate

class CalculateTaskScoreTest {

    private val useCase = CalculateTaskScoreUseCase()
    private val hoy = LocalDate.of(2025, 6, 1)

    // ─── BONO DE PRODUCTIVIDAD ──────────────────────────────────────────────

    @Test
    fun `tarea completada hoy que vencia hoy da bono minimo de 6`() {
        // daysAdvance = 0 → factor 0.1 → 0.1 * 60 = 6
        val tareas = listOf(
            Task(name = "T", dueDate = hoy, completed = true, completedDate = hoy)
        )
        assertEquals(6, useCase.productivityBonus(tareas, hoy))
    }

    @Test
    fun `tarea completada con 7 dias de antelacion da bono maximo de 60`() {
        // daysAdvance = 7 → factor 1.0 → 60
        val tareas = listOf(
            Task(name = "T", dueDate = hoy.plusDays(7), completed = true, completedDate = hoy)
        )
        assertEquals(60, useCase.productivityBonus(tareas, hoy))
    }

    @Test
    fun `antelacion mayor a 7 dias sigue topada en 60`() {
        // daysAdvance = 30 → factor se limita a 1.0 → 60 (no más)
        val tareas = listOf(
            Task(name = "T", dueDate = hoy.plusDays(30), completed = true, completedDate = hoy)
        )
        assertEquals(60, useCase.productivityBonus(tareas, hoy))
    }

    @Test
    fun `dos tareas completadas hoy suman sus bonos`() {
        val tareas = listOf(
            Task(name = "A", dueDate = hoy, completed = true, completedDate = hoy),          // 6
            Task(name = "B", dueDate = hoy.plusDays(7), completed = true, completedDate = hoy) // 60
        )
        assertEquals(66, useCase.productivityBonus(tareas, hoy))
    }

    @Test
    fun `tarea completada en OTRO dia no cuenta para hoy`() {
        val tareas = listOf(
            Task(name = "T", dueDate = hoy, completed = true, completedDate = hoy.minusDays(1))
        )
        assertEquals(0, useCase.productivityBonus(tareas, hoy))
    }

    @Test
    fun `tarea sin completar no da bono`() {
        val tareas = listOf(
            Task(name = "T", dueDate = hoy, completed = false, completedDate = null)
        )
        assertEquals(0, useCase.productivityBonus(tareas, hoy))
    }

    @Test
    fun `sin tareas el bono es cero`() {
        assertEquals(0, useCase.productivityBonus(emptyList(), hoy))
    }

    // ─── PENALIZACIÓN POR VENCIDAS ──────────────────────────────────────────

    @Test
    fun `una tarea vencida sin completar penaliza 20`() {
        val tareas = listOf(
            Task(name = "T", dueDate = hoy.minusDays(1), completed = false)
        )
        assertEquals(20, useCase.overduePenalty(tareas, hoy))
    }

    @Test
    fun `tres tareas vencidas dan 60 de penalizacion`() {
        val tareas = listOf(
            Task(name = "A", dueDate = hoy.minusDays(1), completed = false),
            Task(name = "B", dueDate = hoy.minusDays(2), completed = false),
            Task(name = "C", dueDate = hoy.minusDays(3), completed = false)
        )
        assertEquals(60, useCase.overduePenalty(tareas, hoy))  // 3 * 20
    }

    @Test
    fun `tarea futura no penaliza`() {
        val tareas = listOf(
            Task(name = "T", dueDate = hoy.plusDays(2), completed = false)
        )
        assertEquals(0, useCase.overduePenalty(tareas, hoy))
    }

    @Test
    fun `tarea que vence hoy todavia no penaliza`() {
        // isBefore(hoy) es false si vence exactamente hoy
        val tareas = listOf(
            Task(name = "T", dueDate = hoy, completed = false)
        )
        assertEquals(0, useCase.overduePenalty(tareas, hoy))
    }

    @Test
    fun `tarea vencida pero completada a tiempo no penaliza`() {
        val tareas = listOf(
            Task(name = "T", dueDate = hoy.minusDays(1), completed = true, completedDate = hoy.minusDays(1))
        )
        assertEquals(0, useCase.overduePenalty(tareas, hoy))
    }
}