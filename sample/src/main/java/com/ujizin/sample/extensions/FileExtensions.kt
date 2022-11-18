package com.ujizin.sample.extensions

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@WorkerThread
suspend fun File.getDuration(context: Context): Int? = withContext(Dispatchers.IO) {
    if (isVideo) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.run {
                setDataSource(context, Uri.fromFile(this@getDuration))
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?.milliseconds?.toInt(DurationUnit.SECONDS)
            }
        } catch (e: Exception) {
            null
        }
    } else null
}

fun File.getExternalUri(contentResolver: ContentResolver): Uri? {
    val projection = arrayOf(MediaStore.MediaColumns._ID)
    val selection = MediaStore.MediaColumns.DATA + "=?"
    val selectionArgs = arrayOf(absolutePath)
    val filesUri = MediaStore.Files.getContentUri("external")
    return contentResolver.query(
        filesUri,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        val id = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        if (cursor.moveToFirst()) {
            return@use ContentUris.withAppendedId(
                when {
                    isVideo -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                },
                cursor.getLong(id)
            )
        }

        null
    }
}

val File.isVideo: Boolean get() = extension == "mp4"

@WorkerThread
suspend fun File.delete(
    contentResolver: ContentResolver,
    intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) = withContext(dispatcher) {
    val uri = getExternalUri(contentResolver) ?: return@withContext
    try {
        contentResolver.delete(uri, null, null)
    } catch (e: SecurityException) {
        val intentSender = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                MediaStore.createDeleteRequest(contentResolver, listOf(uri)).intentSender
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val recoverySecurityException = e as? RecoverableSecurityException
                recoverySecurityException?.userAction?.actionIntent?.intentSender
            }

            else -> null
        }
        intentSender?.let { sender ->
            intentSenderLauncher.launch(IntentSenderRequest.Builder(sender).build())
        }
    }
}