package br.com.devlucasyuji.sample.extensions

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

fun File.getDuration(context: Context): Int? {
    return if (extension == "mp4") {
        val retriever = MediaMetadataRetriever()
        return try {
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
