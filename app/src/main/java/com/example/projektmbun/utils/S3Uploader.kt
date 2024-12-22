package com.example.projektmbun.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtilityOptions
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.example.projektmbun.BuildConfig
import java.io.File
import java.lang.Exception

class S3Uploader(private val context: Context) {

    val BUCKET_NAME = "zerowastecook"
    val CREDENTIALS = BasicAWSCredentials(BuildConfig.AWS_IAM_ACCESS_KEY, BuildConfig.AWS_IAM_SECRET_KEY)
    val REGION = Region.getRegion(Regions.EU_NORTH_1)

    private val s3Client by lazy {
        AmazonS3Client(CREDENTIALS, REGION)
    }

    private val transferUtility by lazy {
        TransferUtility.builder()
            .context(context)
            .s3Client(s3Client)
            .transferUtilityOptions(TransferUtilityOptions())
            .build()
    }
    /**
     * Löscht eine Datei aus dem S3-Bucket basierend auf dem Dateinamen (Key).
     *
     * @param fileName Der Schlüssel (Pfad) der Datei im S3-Bucket.
     * @param onDeleteComplete Callback, um den Erfolg oder Fehler zurückzumelden.
     */
    fun deleteFile(fileKey: String, onComplete: (Boolean) -> Unit) {
        // Führe die Löschoperation in einem Hintergrund-Thread aus
        Thread {
            try {
                Log.d("S3 Uploader", "Trying to delete file: $fileKey")
                s3Client.deleteObject(BUCKET_NAME, fileKey)
                Log.d("S3 Uploader", "File deleted successfully: $fileKey")
                onComplete(true)
            } catch (e: Exception) {
                Log.e("S3 Uploader", "Fehler beim Löschen der Datei: ${e.message}")
                onComplete(false)
            }
        }.start()
    }



    fun uploadFile(fileUri: Uri, fileName: String, onUploadComplete: (success: Boolean) -> Unit) {
        val file = File(fileUri.path ?: return)

        val uploadObserver = transferUtility.upload(BUCKET_NAME, fileName, file)

        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                if (state == TransferState.COMPLETED) {
                    Log.d("S3 Uploader", "Upload successful")
                    onUploadComplete(true)
                } else if (state == TransferState.FAILED) {
                    Log.e("S3 Uploader", "Upload failed")
                    onUploadComplete(false)
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val progess = (bytesCurrent.toDouble() / bytesTotal * 100).toInt()
                Log.d("S3 Uploader", "Progress: $progess%")
            }

            override fun onError(id: Int, ex: Exception?) {
                Log.e("S3 Uploader", "Upload error: ${ex?.message}")
                onUploadComplete(false)
            }
        })
    }
}