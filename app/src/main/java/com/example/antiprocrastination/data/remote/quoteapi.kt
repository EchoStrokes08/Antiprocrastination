package com.example.antiprocrastination.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Interfaz que define los endpoints del servicio REST.
 * Retrofit genera la implementación automáticamente.
 */
interface QuoteApi {

    // GET https://zenquotes.io/api/random  -> devuelve una frase aleatoria
    @GET("api/random")
    suspend fun getRandomQuote(): List<QuoteDto>

    companion object {
        private const val BASE_URL = "https://zenquotes.io/"

        /** Crea una instancia lista para usar del servicio REST. */
        fun create(): QuoteApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QuoteApi::class.java)
        }
    }
}