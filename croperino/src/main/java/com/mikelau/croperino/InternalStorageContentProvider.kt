package com.mikelau.croperino

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.mikelau.croperino.CroperinoConfig.Companion.getsImageName
import java.io.File
import java.io.FileNotFoundException

class InternalStorageContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return try {
            val mFile = File(context?.filesDir, getsImageName())
            if (!mFile.exists()) {
                mFile.createNewFile()
                context?.contentResolver?.notifyChange(CONTENT_URI, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getType(uri: Uri): String? {
        val path = uri.toString()
        for (extension in MIME_TYPES.keys) {
            if (path.endsWith(extension)) {
                return MIME_TYPES[extension]
            }
        }
        return null
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val f = File(context?.filesDir, getsImageName())
        if (f.exists()) {
            return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_WRITE)
        }
        throw FileNotFoundException(uri.path)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    companion object {
        val CONTENT_URI = Uri.parse("content://com.mikelau.croperino/")
        private val MIME_TYPES = HashMap<String, String>()

        init {
            MIME_TYPES[".jpg"] = "image/jpeg"
            MIME_TYPES[".jpeg"] = "image/jpeg"
        }
    }
}