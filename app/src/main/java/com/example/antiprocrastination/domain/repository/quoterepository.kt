package com.example.antiprocrastination.domain.repository

import com.example.antiprocrastination.data.remote.QuoteApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Resultado del consumo del servicio REST.
 * Permite a la UI distinguir entre exito, error o carga sin que crashee.
 */
sealed class QuoteResult {
    data class Success(val quote: String, val author: String) : QuoteResult()
    data class Error(val message: String) : QuoteResult()
}

/**
 * Repositorio que consume el servicio REST de frases motivacionales.
 * Sigue el mismo patron MVVM que UsageRepository y SettingsRepository:
 * la UI nunca llama a la red directamente, siempre pasa por el repositorio.
 */
class QuoteRepository(
    private val api: QuoteApi = QuoteApi.create()
) {
    suspend fun getMotivationalQuote(): QuoteResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getRandomQuote()
            val first = response.firstOrNull()
            if (first != null) {
                QuoteResult.Success(quote = first.quote, author = first.author)
            } else {
                QuoteResult.Error("No se recibieron datos del servidor")
            }
        } catch (e: Exception) {
            QuoteResult.Error("Sin conexion: ${e.localizedMessage ?: "error desconocido"}")
        }
    }
}