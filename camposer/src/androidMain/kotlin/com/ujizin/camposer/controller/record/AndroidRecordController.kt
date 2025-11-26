package com.ujizin.camposer.controller.record

import android.Manifest
import android.net.Uri
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.view.video.AudioConfig
import com.ujizin.camposer.CaptureResult

public interface AndroidRecordController : RecordController {

    /**
     * Start recording camera.
     *
     * @param fileOutputOptions file output options where the video will be saved
     * @param onResult Callback called when [CaptureResult<Uri?>] is ready
     * */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        fileOutputOptions: FileOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    )

    /**
     * Start recording camera.
     *
     * @param fileDescriptorOutputOptions file output options where the video will be saved
     * @param onResult Callback called when [CaptureResult<Uri?>] is ready
     * */
    @RequiresApi(VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        fileDescriptorOutputOptions: FileDescriptorOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    )

    /**
     * Start recording camera.
     *
     *  @param mediaStoreOutputOptions media store output options to the video to be saved.
     *  @param onResult Callback called when [CaptureResult<Uri?>] is ready
     *  */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        mediaStoreOutputOptions: MediaStoreOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    )

    /**
     * Toggle recording camera.
     * */
    @RequiresApi(VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        fileDescriptorOutputOptions: FileDescriptorOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(fileDescriptorOutputOptions, audioConfig, onResult)
        }
    }

    /**
     * Toggle recording camera.
     * */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        mediaStoreOutputOptions: MediaStoreOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(mediaStoreOutputOptions, audioConfig, onResult)
        }
    }

    /**
     * Toggle recording camera.
     * */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        fileOutputOptions: FileOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(
                fileOutputOptions = fileOutputOptions, audioConfig, onResult
            )
        }
    }
}