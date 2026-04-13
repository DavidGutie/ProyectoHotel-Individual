package com.example.aplicacion_hotel.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object MultipartUtils {

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        fieldName: String
    ): MultipartBody.Part {

        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"

        val ext = when (mime) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            else -> ".jpg"
        }

        val input = context.contentResolver.openInputStream(uri)!!
        val tempFile = File.createTempFile("perfil_", ext, context.cacheDir)

        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }

        val requestFile = tempFile.asRequestBody(mime.toMediaType())

        return MultipartBody.Part.createFormData(
            fieldName,
            tempFile.name,
            requestFile
        )
    }
}