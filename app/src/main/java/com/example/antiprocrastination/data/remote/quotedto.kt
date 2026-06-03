package com.example.antiprocrastination.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos (DTO) que representa la respuesta JSON del servicio REST.
 * La API ZenQuotes devuelve una lista de objetos con estos campos.
 */
data class QuoteDto(
    @SerializedName("q") val quote: String,   // texto de la frase
    @SerializedName("a") val author: String   // autor
)