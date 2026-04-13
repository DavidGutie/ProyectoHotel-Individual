package com.example.aplicacion_hotel.utils

import retrofit2.HttpException
import org.json.JSONObject

fun httpErrorMessage(e: HttpException): String {
    return try {
        val errorBody = e.response()?.errorBody()?.string()

        if (errorBody.isNullOrBlank()) {
            "Error HTTP ${e.code()}"
        } else {
            val json = JSONObject(errorBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("msg") -> json.getString("msg")
                json.has("error") -> json.getString("error")
                else -> "Error HTTP ${e.code()}"
            }
        }
    } catch (_: Exception) {
        "Error HTTP ${e.code()}"
    }
}