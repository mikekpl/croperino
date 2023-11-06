package com.mikelau.croperino

import android.graphics.Bitmap
import android.graphics.Matrix

class RotateBitmap(var bitmap: Bitmap?) {
    var rotation = 0
    val rotateMatrix: Matrix
        get() {
            // By default this is an identity matrix.
            val matrix = Matrix()
            if (rotation != 0) {
                // We want to do the rotation at origin, but since the bounding
                // rectangle will be changed after rotation, so the delta values
                // are based on old & new width/height respectively.
                val cx = bitmap?.width!! / 2
                val cy = bitmap?.height!! / 2
                matrix.preTranslate(-cx.toFloat(), -cy.toFloat())
                matrix.postRotate(rotation.toFloat())
                matrix.postTranslate((width / 2).toFloat(), (height / 2).toFloat())
            }
            return matrix
        }
    private val isOrientationChanged: Boolean
        get() = rotation / 90 % 2 != 0
    val height: Int
        get() = if (isOrientationChanged) bitmap?.width!! else bitmap?.height!!
    val width: Int
        get() = if (isOrientationChanged) bitmap?.height!! else bitmap?.width!!
}
