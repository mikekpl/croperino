package com.mikelau.croperino

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.mikelau.croperino.CroperinoConfig.Companion.getsDirectory
import com.mikelau.croperino.CroperinoConfig.Companion.getsImageName
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object CroperinoFileUtil {
    var PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    const val REQUEST_EXTERNAL_STORAGE = 1
    const val REQUEST_CAMERA = 2
    var tempFile: File? = null
    fun setupDirectory(ctx: Context) {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            if (Environment.getExternalStorageDirectory().exists()) {
                tempFile = File(
                    Environment.getExternalStorageDirectory().toString() + getsDirectory(),
                    getsImageName()
                )
            } else {
                tempFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .toString() + getsDirectory(), getsImageName()
                )
            }
        } else {
            tempFile = File(ctx.filesDir, getsImageName())
        }
        if (!tempFile!!.exists()) {
            tempFile = File(CroperinoConfig.getsRawDirectory())
            tempFile!!.mkdirs()
        }
    }

    @Throws(IOException::class)
    fun newCameraFile(): File? {
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Camera"
        )
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        tempFile = File.createTempFile(getsImageName(), ".jpg", storageDir)
        return tempFile
    }

    fun newGalleryFile(data: Intent, ctx: Activity): File? {
        return try {
            tempFile = File(CroperinoConfig.getsRawDirectory() + getsImageName())
            copyFile(File(getPath(ctx, data.data)), tempFile)
            tempFile
        } catch (e: Exception) {
            if (e is IOException) {
                tempFile = File(getPath(ctx, data.data))
            } else {
                Toast.makeText(ctx, "Gallery is empty or access is prohibited by device", Toast.LENGTH_SHORT).show()
            }
            tempFile
        }
    }

    @Throws(IOException::class)
    private fun copyFile(sourceFile: File, destFile: File?) {
        if (!sourceFile.exists()) {
            return
        }
        val source = FileInputStream(sourceFile).channel
        val destination = FileOutputStream(destFile).channel
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size())
        }
        source?.close()
        destination?.close()
    }

    fun verifyStoragePermissions(activity: Activity): Boolean {
        val writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        return if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            false
        } else {
            true
        }
    }

    fun verifyCameraPermissions(activity: Activity): Boolean {
        val cameraPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        return if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
            false
        } else {
            true
        }
    }

    fun getPath(context: Context, uri: Uri?): String? {
        val result: String?
        val cursor = context.contentResolver.query(uri!!, null, null, null, null)
        if (cursor == null) {
            result = uri.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }
}
