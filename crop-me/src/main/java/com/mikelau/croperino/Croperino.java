package com.mikelau.croperino;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.mikelau.magictoast.MagicToast;

import java.io.File;
import java.io.IOException;

/**
 * Created by Mike on 9/15/2016.
 */
public class Croperino {
    private static String TAG = Croperino.class.getSimpleName();

    public static void runCropImage(File file, Activity ctx, boolean isScalable, int aspectX, int aspectY, int color, int bgColor) {
        Intent intent = new Intent(ctx, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, file.getPath());
        intent.putExtra(CropImage.SCALE, isScalable);
        intent.putExtra(CropImage.ASPECT_X, aspectX);
        intent.putExtra(CropImage.ASPECT_Y, aspectY);
        intent.putExtra("color", color);
        intent.putExtra("bgColor", bgColor);
        ctx.startActivityForResult(intent, CroperinoConfig.REQUEST_CROP_PHOTO);
    }

    public static void prepareChooser(final Activity ctx, String message, int color) {
        CameraDialog.getConfirmDialog(ctx, ctx.getResources().getString(R.string.app_name),
                message,
                "CAMERA",
                "GALLERY",
                "CLOSE",
                color,
                true,
                new AlertInterface.WithNeutral() {
                    @Override
                    public void PositiveMethod(final DialogInterface dialog, final int id) {
                        if (CroperinoFileUtil.verifyCameraPermissions(ctx)) {
                            prepareCamera(ctx);
                        }
                    }

                    @Override
                    public void NeutralMethod(final DialogInterface dialog, final int id) {
                        if (CroperinoFileUtil.verifyStoragePermissions(ctx)) {
                            prepareGallery(ctx);
                        }
                    }

                    @Override
                    public void NegativeMethod(final DialogInterface dialog, final int id) {

                    }
                });
    }

    public static void prepareCamera(Activity ctx) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri mImageCaptureUri;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                if (Uri.fromFile(CroperinoFileUtil.newCameraFile()) != null && !Uri.EMPTY.equals(CroperinoFileUtil.newCameraFile())) {
                    mImageCaptureUri = Uri.fromFile(CroperinoFileUtil.newCameraFile());
                } else {
                    mImageCaptureUri = FileProvider.getUriForFile(ctx,
                            ctx.getApplicationContext().getPackageName() + ".provider",
                            CroperinoFileUtil.newCameraFile());
                }
            } else {
                mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
            }
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            ctx.startActivityForResult(intent, CroperinoConfig.REQUEST_TAKE_PHOTO);
        } catch (Exception e) {
            if (e instanceof ActivityNotFoundException) {
                MagicToast.showError(ctx, "Activity not found.");
            } else if (e instanceof IOException) {
                MagicToast.showError(ctx, "Image file captured not found.");
            } else if (e instanceof CameraAccessException) {
                MagicToast.showError(ctx, "Camera access was denied.");
            } else {
                MagicToast.showError(ctx, "Camera access failed.");
            }
            Log.e(TAG, "Failed to prepare camera: ", e);
        }
    }

    public static void prepareGallery(Activity ctx) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ctx.startActivityForResult(i, CroperinoConfig.REQUEST_PICK_FILE);
    }
}
