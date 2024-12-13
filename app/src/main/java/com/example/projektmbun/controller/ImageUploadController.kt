package com.example.projektmbun.controller

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.projektmbun.utils.S3Uploader
import java.io.File
import java.io.FileOutputStream

class ImageUploadController(private val context: Context, private val s3Uploader: S3Uploader) {

    fun checkPermission(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }


    fun uploadInstructionImage(
        uri: Uri,
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        uploadImage(
            uri = uri,
            context = context,
            uploadPath = "recipe_instructions_images/", // Spezifischer Pfad
            onSuccess = onSuccess,
            onError = onError
        )
    }


    fun uploadImage(
        uri: Uri,
        context: Context,
        uploadPath: String, // Speicherpfad z. B. "recipe_images/"
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val contentResolver = context.contentResolver
        val fileName = "image_${System.currentTimeMillis()}.jpg"

        val inputStream = contentResolver.openInputStream(uri) ?: run {
            onError("Fehler beim Öffnen der Datei")
            return
        }

        val tempFile = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(tempFile)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        val fullPath = "$uploadPath$fileName" // Kombinierter Pfad
        s3Uploader.uploadFile(Uri.fromFile(tempFile), fullPath) { success ->
            if (success) {
                val imageUrl = "https://zerowastecook.s3.eu-north-1.amazonaws.com/$fullPath"
                onSuccess(imageUrl)
            } else {
                onError("Upload fehlgeschlagen")
            }
        }
    }
}
