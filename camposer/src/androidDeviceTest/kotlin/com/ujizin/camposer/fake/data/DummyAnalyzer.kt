package com.ujizin.camposer.fake.data

import android.graphics.Rect
import android.media.Image
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy

val dummyImageProxy = object : ImageProxy {
  override fun close() = Unit

  override fun getCropRect(): Rect {
    TODO("Not yet implemented")
  }

  override fun setCropRect(rect: Rect?) {
    TODO("Not yet implemented")
  }

  override fun getFormat(): Int {
    TODO("Not yet implemented")
  }

  override fun getHeight(): Int {
    TODO("Not yet implemented")
  }

  override fun getWidth(): Int {
    TODO("Not yet implemented")
  }

  override fun getPlanes(): Array<out ImageProxy.PlaneProxy?> {
    TODO("Not yet implemented")
  }

  override fun getImageInfo(): ImageInfo {
    TODO("Not yet implemented")
  }

  override fun getImage(): Image? {
    TODO("Not yet implemented")
  }
}
