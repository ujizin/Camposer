package com.ujizin.sample.data.local.datasource

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.util.UUID

class FileDataSource {

    private val externalDir = "${Environment.DIRECTORY_DCIM}${File.separator}$RELATIVE_PATH"

    private val currentFileName: String
        get() = "${System.currentTimeMillis()}-${UUID.randomUUID()}"

    private val externalStorage
        get() = Environment.getExternalStoragePublicDirectory(externalDir).apply { mkdirs() }

    val externalFiles
        get() = externalStorage.listFiles()?.sortedByDescending { it.lastModified() }

    val lastPicture get() = externalFiles?.firstOrNull()

    fun getFile(
        extension: String = "jpg",
    ): File = File(externalStorage.path, "$currentFileName.$extension").apply {
        if (parentFile?.exists() == false) parentFile?.mkdirs()
        createNewFile()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    val imageContentValues: ContentValues = getContentValues(JPEG_MIME_TYPE)

    @RequiresApi(Build.VERSION_CODES.Q)
    val videoContentValues: ContentValues = getContentValues(VIDEO_MIME_TYPE)

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getContentValues(mimeType: String) = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, currentFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, externalDir)
    }

    companion object {
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val VIDEO_MIME_TYPE = "video/mp4"
        private const val RELATIVE_PATH = "Camposer"
    }
}
