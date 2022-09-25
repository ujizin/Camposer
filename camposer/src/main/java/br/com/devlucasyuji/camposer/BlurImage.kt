package br.com.devlucasyuji.camposer

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp

/**
 * Blur Image composable using RenderScript instead RenderEffect.
 * */
@Composable
fun BlurImage(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    contentDescription: String?,
    radius: Dp
) {
    Image(
        modifier = modifier,
        bitmap = bitmap.asBlurImageBitmap(radius),
        contentDescription = contentDescription
    )
}

/**
 * Transform bitmap to a blur bitmap using Render Script.
 * */
@Composable
private fun Bitmap.asBlurImageBitmap(blur: Dp): ImageBitmap {
    val context = LocalContext.current
    return remember(this) {
        val blurValue = blur.value.coerceIn(1F, 25F)
        this.blurRenderScript(context, blurValue)
        asImageBitmap()
    }
}

private fun Bitmap.blurRenderScript(context: Context, blur: Float) {
    val rs = RenderScript.create(context)
    val bitmapAlloc = Allocation.createFromBitmap(rs, this)
    ScriptIntrinsicBlur.create(rs, bitmapAlloc.element).apply {
        setRadius(blur)
        setInput(bitmapAlloc)
        forEach(bitmapAlloc)
    }
    bitmapAlloc.copyTo(this)
    rs.destroy()
}