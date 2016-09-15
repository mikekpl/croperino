package com.mikelau.croperino;

import android.app.ProgressDialog;
import android.os.Handler;

/**
 * Created by Mike on 9/14/2016.
 */
public class BackgroundJob extends MonitoredActivity.LifeCycleAdapter implements Runnable {

    private final MonitoredActivity mActivity;
    private final ProgressDialog mDialog;
    private final Runnable          mJob;
    private final Handler mHandler;
    private final Runnable mCleanupRunner = new Runnable() {
        public void run() {

            mActivity.removeLifeCycleListener(BackgroundJob.this);
            if (mDialog.getWindow() != null) mDialog.dismiss();
        }
    };

    public BackgroundJob(MonitoredActivity activity, Runnable job,
                         ProgressDialog dialog, Handler handler) {

        mActivity = activity;
        mDialog = dialog;
        mJob = job;
        mActivity.addLifeCycleListener(this);
        mHandler = handler;
    }

    public void run() {
        try {
            mJob.run();
        } finally {
            mHandler.post(mCleanupRunner);
        }
    }


    @Override
    public void onActivityDestroyed(MonitoredActivity activity) {
        mCleanupRunner.run();
        mHandler.removeCallbacks(mCleanupRunner);
    }

    @Override
    public void onActivityStopped(MonitoredActivity activity) {

        mDialog.hide();
    }

    @Override
    public void onActivityStarted(MonitoredActivity activity) {

        mDialog.show();
    }
}
