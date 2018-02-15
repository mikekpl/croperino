package com.mikelau.croperino;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class CameraDialog {

    private static AlertDialog.Builder sBuilder;

    public static void getConfirmDialog(Context mContext, String mTitle, String mMessage, String mPositive, String mNeutral, String mNegative,
                                        int color, boolean mIsCancelable, final AlertInterface.WithNeutral mTarget) {
        LayoutInflater i = LayoutInflater.from(mContext);
        View v = i.inflate(R.layout.dialog_camera, null);

        if (Build.VERSION.SDK_INT >= 17) {
            sBuilder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        } else {
            sBuilder = new AlertDialog.Builder(mContext);
        }

        sBuilder.setView(v).setCancelable(false).setPositiveButton(mPositive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mTarget.PositiveMethod(dialog, id);
            }
        }).setNeutralButton(mNeutral, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mTarget.NeutralMethod(dialog, id);
            }
        }).setNegativeButton(mNegative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mTarget.NegativeMethod(dialog, id);
            }
        });

        TextView tvTitle = v.findViewById(R.id.tv_dialog_title);
        TextView tvMessage = v.findViewById(R.id.tv_dialog_message);

        tvTitle.setBackgroundColor(color);
        tvTitle.setText(mTitle);
        tvMessage.setText(mMessage);

        final AlertDialog alert = sBuilder.create();
        alert.setCancelable(mIsCancelable);
        alert.setCanceledOnTouchOutside(true);
        alert.show();
        if (mIsCancelable) {
            alert.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    mTarget.NegativeMethod(alert, 0);
                }
            });
        }
    }
}
