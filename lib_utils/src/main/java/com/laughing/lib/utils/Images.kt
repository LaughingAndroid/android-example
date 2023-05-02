package com.laughing.lib.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.IntRange
import java.io.File
import java.io.InputStream

@JvmOverloads
fun Bitmap?.toBytes(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): ByteArray? {
    return ImageUtils.bitmap2Bytes(this, format, quality)
}

fun Drawable?.toBytes(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): ByteArray? {
    return toBitmap()?.toBytes(format, quality)
}

fun ByteArray?.toBitmap(): Bitmap? {
    return ImageUtils.bytes2Bitmap(this)
}

fun ByteArray?.toDrawable(): Drawable? {
    return toBitmap()?.toDrawable()
}

fun Drawable?.toBitmap(): Bitmap? {
    return ImageUtils.drawable2Bitmap(this)
}

fun Bitmap?.toDrawable(): Drawable? {
    return ImageUtils.bitmap2Drawable(this)
}

fun View?.toBitmap(): Bitmap? {
    return ImageUtils.view2Bitmap(this)
}

fun String?.toBitmap(maxWidth: Int = 0, maxHeight: Int = 0): Bitmap? {
    val filePath = this ?: return null
    return File(filePath).toBitmap(maxWidth, maxHeight)
}

fun File?.toBitmap(maxWidth: Int = 0, maxHeight: Int = 0): Bitmap? {
    return if (maxWidth == 0 || maxHeight == 0) {
        ImageUtils.getBitmap(this)
    } else {
        ImageUtils.getBitmap(this, maxWidth, maxHeight)
    }
}

fun InputStream?.toBitmap(maxWidth: Int = 0, maxHeight: Int = 0): Bitmap? {
    return if (maxWidth == 0 || maxHeight == 0) {
        ImageUtils.getBitmap(this)
    } else {
        ImageUtils.getBitmap(this, maxWidth, maxHeight)
    }
}

fun Bitmap.scale(newWidth: Int, newHeight: Int, recycle: Boolean = false): Bitmap {
    return ImageUtils.scale(this, newWidth, newHeight, recycle)
}

fun Bitmap.clip(x: Int = 0, y: Int = 0, width: Int, height: Int, recycle: Boolean = false): Bitmap {
    return ImageUtils.clip(this, x, y, width, height, recycle)
}

fun Bitmap.rotate(
    degrees: Int,
    px: Float,
    py: Float,
    recycle: Boolean = false
): Bitmap? {
    return ImageUtils.rotate(this, degrees, px, py, recycle)
}

fun String.getRotateDegree(): Int {
    return ImageUtils.getRotateDegree(this)
}

fun Bitmap.toRound(borderSize: Float = 0f, borderColor: Int = 0, recycle: Boolean = false): Bitmap {
    return ImageUtils.toRound(this, borderSize, borderColor, recycle)
}

fun Bitmap.toRoundCorner(
    radius: Float,
    borderSize: Float = 0f,
    borderColor: Int = 0,
    recycle: Boolean = false
): Bitmap {
    return ImageUtils.toRoundCorner(this, radius, borderSize, borderColor, recycle)
}

fun Bitmap.toRoundCorners(
    radius: FloatArray,
    borderSize: Float = 0f,
    borderColor: Int = 0,
    recycle: Boolean = false
): Bitmap {
    return ImageUtils.toRoundCorner(this, radius, borderSize, borderColor, recycle)
}

fun Bitmap.addTextWatermark(
    content: String,
    textSize: Float,
    color: Int,
    x: Float,
    y: Float,
    recycle: Boolean = false
): Bitmap {
    return ImageUtils.addTextWatermark(this, content, textSize, color, x, y, recycle)
}

fun Bitmap.addImageWatermark(
    watermark: Bitmap,
    x: Int,
    y: Int,
    alpha: Int,
    recycle: Boolean = false
): Bitmap {
    return ImageUtils.addImageWatermark(this, watermark, x, y, alpha, recycle)
}

fun Bitmap.toAlpha(): Bitmap {
    return ImageUtils.toAlpha(this)
}

fun Bitmap.toGray(recycle: Boolean = false): Bitmap {
    return ImageUtils.toGray(this, recycle)
}

fun Bitmap.fastBlur(
    scale: Float,
    radius: Float,
    recycle: Boolean = false,
    isReturnScale: Boolean = false
): Bitmap? {
    return ImageUtils.fastBlur(this, scale, radius, recycle, isReturnScale)
}

fun Bitmap?.save(
    file: File = File(FilePathManager.getFilePathByName("app_image", "$randomUUIDString.png")),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100,
    recycle: Boolean = false
): File {
    ImageUtils.save(this, file, format, quality, recycle)
    return file
}

fun Bitmap.save2Album(
    dirName: String,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100,
    recycle: Boolean = false
): File? {
    return ImageUtils.save2Album(this, dirName, format, quality, recycle)
}

fun String?.isImage(): Boolean {
    return ImageUtils.isImage(this)
}

fun File?.isImage(): Boolean {
    return ImageUtils.isImage(this)
}

fun String.getImageType(): ImageUtils.ImageType {
    return ImageUtils.getImageType(this)
}

fun Bitmap.compressByQuality(
    @IntRange(from = 0, to = 100) quality: Int,
    recycle: Boolean = false
): ByteArray? {
    return ImageUtils.compressByQuality(this, quality, recycle)
}

fun Bitmap.compressBySampleSize(
    maxWidth: Int,
    maxHeight: Int = maxWidth,
    recycle: Boolean = false
): Bitmap? {
    return ImageUtils.compressBySampleSize(this, maxWidth, maxHeight, recycle)
}


fun ByteArray.isJPEG(): Boolean = ImageUtils.isJPEG(this)
fun ByteArray.isGIF(): Boolean = ImageUtils.isGIF(this)
fun ByteArray.isPNG(): Boolean = ImageUtils.isPNG(this)