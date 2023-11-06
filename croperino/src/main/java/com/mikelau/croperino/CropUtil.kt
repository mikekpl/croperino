package com.mikelau.croperino

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Handler
import android.view.Surface
import java.io.Closeable
import kotlin.math.max
import kotlin.math.min

object CropUtil {

    fun transform(
        scaler: Matrix?,
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        scaleUp: Boolean
    ): Bitmap {
        var scaler = scaler
        val deltaX = source.width - targetWidth
        val deltaY = source.height - targetHeight
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            val b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b2)
            val deltaXHalf = max(0.0, (deltaX / 2).toDouble()).toInt()
            val deltaYHalf = max(0.0, (deltaY / 2).toDouble()).toInt()
            val src = Rect(
                deltaXHalf,
                deltaYHalf,
                (deltaXHalf + min(targetWidth.toDouble(), source.width.toDouble())).toInt(),
                (deltaYHalf + min(targetHeight.toDouble(), source.height.toDouble())).toInt()
            )
            val dstX = (targetWidth - src.width()) / 2
            val dstY = (targetHeight - src.height()) / 2
            val dst = Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY)
            c.drawBitmap(source, src, dst, null)
            return b2
        }
        val bitmapWidthF = source.width.toFloat()
        val bitmapHeightF = source.height.toFloat()
        val bitmapAspect = bitmapWidthF / bitmapHeightF
        val viewAspect = targetWidth.toFloat() / targetHeight
        if (bitmapAspect > viewAspect) {
            val scale = targetHeight / bitmapHeightF
            if (scale < .9f || scale > 1f) {
                scaler?.setScale(scale, scale)
            } else {
                scaler = null
            }
        } else {
            val scale = targetWidth / bitmapWidthF
            if (scale < .9f || scale > 1f) {
                scaler?.setScale(scale, scale)
            } else {
                scaler = null
            }
        }
        val b1: Bitmap
        b1 = if (scaler != null) {
            Bitmap.createBitmap(
                source,
                0,
                0,
                source.width,
                source.height,
                scaler,
                true
            )
        } else {
            source
        }
        val dx1 = max(0.0, (b1.width - targetWidth).toDouble()).toInt()
        val dy1 = max(0.0, (b1.height - targetHeight).toDouble()).toInt()
        val b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight)
        if (b1 != source) {
            b1.recycle()
        }
        return b2
    }

    fun closeSilently(c: Closeable?) {
        if (c == null) return
        try {
            c.close()
        } catch (_: Throwable) {
        }
    }

    fun startBackgroundJob(
        activity: MonitoredActivity,
        title: String?,
        message: String?,
        job: Runnable,
        handler: Handler
    ) {
        val dialog = ProgressDialog.show(activity, title, message, true, false)
        Thread(BackgroundJob(activity, job, dialog, handler)).start()
    }

    fun rotateImage(src: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun getOrientationInDegree(activity: Activity): Int {
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        return degrees
    }
}
