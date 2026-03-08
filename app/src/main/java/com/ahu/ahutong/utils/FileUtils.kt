package com.ahu.ahutong.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import okhttp3.ResponseBody

object FileUtils {

    fun getImagesDir(context: Context): File {
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getImageFile(context: Context, name: String): File {
        return File(getImagesDir(context), name)
    }

    suspend fun saveResponseBodyToFile(
        context: Context,
        body: ResponseBody,
        fileName: String,
        onProgress: (Float) -> Unit = {}
    ): File? {
        val outFile = getImageFile(context, fileName)
        return try {
            val total = body.contentLength()
            var completed: Long = 0
            body.byteStream().use { input: InputStream ->
                FileOutputStream(outFile).use { output: OutputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var read = input.read(buffer)
                    while (read >= 0) {
                        output.write(buffer, 0, read)
                        completed += read
                        if (total > 0) {
                            onProgress(completed.toFloat() / total.toFloat())
                        }
                        read = input.read(buffer)
                    }
                    output.flush()
                }
            }
            outFile
        } catch (e: Exception) {
            null
        }
    }

    fun saveImageToGallery(context: Context, imageFile: File) {
        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "AHU_Calendar_${System.currentTimeMillis()}.jpg"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/AHUTong"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = context.contentResolver.insert(collection, values)
        uri?.let {
            context.contentResolver.openOutputStream(it).use { outputStream ->
                java.io.FileInputStream(imageFile).use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)
            }
        }
    }
}

