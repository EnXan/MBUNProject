package com.example.projektmbun.controller

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projektmbun.R
import com.example.projektmbun.utils.S3Uploader
import java.io.File
import java.io.FileOutputStream

class ImageUploadController(
    private val context: Context,
    private val s3Uploader: S3Uploader
) {
    companion object {
        private const val BASE_PATH = "recipe_images/"
        const val S3_BASE_URL = "https://zerowastecook.s3.eu-north-1.amazonaws.com/"
    }

    fun checkPermission(): String = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun uploadImage(
        uri: Uri,
        path: String = BASE_PATH,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val fullPath = "$path$fileName"

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File(context.cacheDir, fileName).also { file ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                s3Uploader.uploadFile(Uri.fromFile(tempFile), fullPath) { success ->
                    if (success) {
                        onSuccess("$S3_BASE_URL$fullPath")
                    } else {
                        onError("Upload fehlgeschlagen")
                    }
                    tempFile.delete() // Cleanup temporary file
                }
            } ?: onError("Fehler beim Öffnen der Datei")
        } catch (e: Exception) {
            onError("Fehler beim Upload: ${e.message}")
            Log.e("ImageUploadController", "Upload error", e)
        }
    }

    fun deleteImage(
        imageUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val key = extractKeyFromUrl(imageUrl)
            Log.d("ImageUploadController", "Deleting image with key: $key")
            s3Uploader.deleteFile(key) { success ->
                if (success) {
                    onSuccess()
                } else {
                    onError("Fehler beim Löschen des Bildes in S3.")
                }
            }
        } catch (e: Exception) {
            onError("Exception beim Löschen des Bildes: ${e.message}")
            Log.e("ImageUploadController", "Deletion error", e)
        }
    }

    fun displayImage(
        imageUrl: String,
        container: ViewGroup,
        placeholderView: android.view.View? = null,
        errorResId: Int = R.drawable.bg_darkgreen,
        placeholderResId: Int = R.drawable.placeolder_receipt
    ) {
        placeholderView?.visibility = android.view.View.GONE
        container.visibility = android.view.View.VISIBLE

        val imageView = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        Glide.with(context)
            .load(imageUrl)
            .placeholder(placeholderResId)
            .error(errorResId)
            .into(imageView)

        container.removeAllViews()
        container.addView(imageView)
    }

    private fun extractKeyFromUrl(imageUrl: String): String =
        imageUrl.substringAfter("s3.eu-north-1.amazonaws.com/")
}