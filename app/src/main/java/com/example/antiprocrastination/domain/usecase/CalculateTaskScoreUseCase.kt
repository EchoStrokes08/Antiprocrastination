package com.example.antiprocrastination.domain.usecase

import com.example.antiprocrastination.domain.model.Task
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Lógica PURA de puntuación de tareas. No conoce Android.
 * Calcula el bono de productividad y la penalización por distracción de un día concreto.
 */
class CalculateTaskScoreUseCase {

    /**
     * Bono por tareas completadas EN este día.
     * Premia la antelación: terminar el mismo día que vence da poco, terminar con 7+ días de adelanto da el máximo.
     * @return minutos de productividad a sumar.
     */
    fun productivityBonus(tasks: List<Task>, day: LocalDate): Int {
        return tasks
            .filter { it.completed && it.completedDate == day }
            .sumOf { task ->
                val daysAdvance = ChronoUnit.DAYS.between(day, task.dueDate).coerceAtLeast(0)
                val bonusFactor = (0.1f + (daysAdvance.toFloat() / 7f)).coerceIn(0.1f, 1.0f)
                (bonusFactor * 60).toInt()
            }
    }

    /**
     * Penalización por tareas vencidas vistas desde este día.
     * Una tarea cuenta como vencida si su fecha límite es anterior al día y sigue sin completarse a tiempo.
     * @return minutos de distracción a sumar (20 por cada tarea vencida).
     */
    fun overduePenalty(tasks: List<Task>, day: LocalDate): Int {
        val overdue = tasks.filter {
            it.dueDate.isBefore(day) &&
                    (!it.completed || (it.completedDate != null && it.completedDate?.isAfter(day) == true))
        }
        return overdue.size * 20
    }
}