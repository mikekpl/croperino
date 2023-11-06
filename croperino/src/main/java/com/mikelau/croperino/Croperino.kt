package com.mikelau.croperino

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.mikelau.croperino.CameraDialog.getConfirmDialog
import java.io.File
import java.io.IOException

object Croperino {

    fun runCropImage(
        file: File,
        ctx: Activity,
        isScalable: Boolean,
        aspectX: Int,
        aspectY: Int,
        color: Int,
        bgColor: Int
    ) {
        val intent = Intent(ctx, CropImage::class.java)
        intent.putExtra(CropImage.IMAGE_PATH, file.path)
        intent.putExtra(CropImage.SCALE, isScalable)
        intent.putExtra(CropImage.ASPECT_X, aspectX)
        intent.putExtra(CropImage.ASPECT_Y, aspectY)
        intent.putExtra("color", color)
        intent.putExtra("bgColor", bgColor)
        ctx.startActivityForResult(intent, CroperinoConfig.REQUEST_CROP_PHOTO)
    }

    fun prepareChooser(ctx: Activity, message: String = "", color: Int) {
        getConfirmDialog(ctx,
            ctx.resources.getString(R.string.app_name),
            message,
            "CAMERA",
            "GALLERY",
            "CLOSE",
            color,
            true,
            object : WithNeutral {
                override fun PositiveMethod(dialog: DialogInterface?, id: Int) {
                    if (CroperinoFileUtil.verifyCameraPermissions(ctx)) {
                        prepareCamera(ctx)
                    }
                }

                override fun NeutralMethod(dialog: DialogInterface?, id: Int) {
                    prepareGallery(ctx)
                }

                override fun NegativeMethod(dialog: DialogInterface?, id: Int) {}
            })
    }

    fun prepareCamera(ctx: Activity) {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val mImageCaptureUri: Uri
            val state = Environment.getExternalStorageState()
            mImageCaptureUri = if (Environment.MEDIA_MOUNTED == state) {
                if (Uri.fromFile(CroperinoFileUtil.newCameraFile()) != null) {
                    FileProvider.getUriForFile(
                        ctx,
                        ctx.applicationContext.packageName + ".provider",
                        CroperinoFileUtil.newCameraFile()!!
                    )
                } else {
                    FileProvider.getUriForFile(
                        ctx,
                        ctx.applicationContext.packageName + ".provider",
                        CroperinoFileUtil.newCameraFile()!!
                    )
                }
            } else {
                InternalStorageContentProvider.CONTENT_URI
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri)
            intent.putExtra("return-data", true)
            ctx.startActivityForResult(intent, CroperinoConfig.REQUEST_TAKE_PHOTO)
        } catch (e: Exception) {
            when (e) {
                is ActivityNotFoundException -> Toast.makeText(ctx, "Activity not found", Toast.LENGTH_SHORT).show()
                is IOException -> Toast.makeText(ctx, "Image file captured not found", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(ctx, "Camera access failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun prepareGallery(ctx: Activity) {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        ctx.startActivityForResult(i, CroperinoConfig.REQUEST_PICK_FILE)
    }
}
