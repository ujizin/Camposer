package br.com.devlucasyuji.sample.feature.camera.datasource

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class FileDataSource {

    private val externalDir = "${Environment.DIRECTORY_PICTURES}${File.separator}$RELATIVE_PATH"

    private val currentFileName: String
        get() = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.US).format(
            System.currentTimeMillis()
        )

    private val externalStorage
        get() = Environment.getExternalStoragePublicDirectory(externalDir).apply { mkdirs() }

    val externalFiles
        get() = externalStorage.listFiles()?.sortedByDescending { it.lastModified() }

    val lastPicture get() = externalFiles?.firstOrNull()

    fun getFile(
        extension: String = ".jpg"
    ): File = File(externalStorage.path, "$currentFileName.$extension").apply { createNewFile() }

    @RequiresApi(Build.VERSION_CODES.Q)
    val imageContentValues: ContentValues = getContentValues(
        MediaStore.Images.Media.RELATIVE_PATH,
        JPEG_MIME_TYPE
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    val videoContentValues: ContentValues = getContentValues(
        MediaStore.Video.Media.RELATIVE_PATH,
        VIDEO_MIME_TYPE
    )

    private fun getContentValues(relativePath: String, mimeType: String) = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, currentFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(relativePath, externalDir)
        }
    }

    companion object {
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val VIDEO_MIME_TYPE = "video/mp4"
        private const val DEFAULT_DATE_FORMAT = "YYYY-HH:MM:SS"
        private const val RELATIVE_PATH = "Camposer"
    }
}
