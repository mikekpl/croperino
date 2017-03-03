package com.mikelau.croperino;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import com.mikelau.magictoast.MagicToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Mike on 9/15/2016.
 */
public class CroperinoFileUtil {

    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_CAMERA = 2;
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static File mFileTemp;

    public static File getmFileTemp() {
        return mFileTemp;
    }

    public static void setupDirectory(Context ctx) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Environment.getExternalStorageDirectory().exists()) {
                mFileTemp = new File(Environment.getExternalStorageDirectory() + CroperinoConfig.getsDirectory(), CroperinoConfig.getsImageName());
            } else {
                mFileTemp = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + CroperinoConfig.getsDirectory(), CroperinoConfig.getsImageName());
            }
        } else {
            mFileTemp = new File(ctx.getFilesDir(), CroperinoConfig.getsImageName());
        }

        if (!mFileTemp.exists()) {
            mFileTemp = new File(CroperinoConfig.getsRawDirectory());
            mFileTemp.mkdirs();
        }
    }

    public static File newCameraFile() throws IOException {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Camera");
        if(!storageDir.exists()) {
            storageDir.mkdir();
        }
        mFileTemp = File.createTempFile(CroperinoConfig.getsImageName(), ".jpg", storageDir);
        return mFileTemp;
    }

    public static File newGalleryFile(Intent data, Context ctx) {
        try {
            mFileTemp = new File(CroperinoConfig.getsRawDirectory() + CroperinoConfig.getsImageName());
            copyFile(new File(CroperinoFileUtil.getPath(ctx, data.getData())), mFileTemp);
            return mFileTemp;
        } catch (Exception e) {
            if(e instanceof  IOException) {
                mFileTemp = new File(CroperinoFileUtil.getPath(ctx, data.getData()));
            } else {
                MagicToast.showError(ctx, "Gallery is empty or access is prohibited by device.");
            }
            return mFileTemp;
        }

    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    public static Boolean verifyStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static Boolean verifyCameraPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int cameraPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{(Manifest.permission.CAMERA)},
                        REQUEST_CAMERA
                );
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static String getPath(final Context context, final Uri uri) {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
