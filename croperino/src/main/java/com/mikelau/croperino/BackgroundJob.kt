package com.mikelau.croperino

import android.app.ProgressDialog
import android.os.Handler
import com.mikelau.croperino.MonitoredActivity.LifeCycleAdapter

class BackgroundJob(
    private val mActivity: MonitoredActivity,
    private val mJob: Runnable,
    private val mDialog: ProgressDialog,
    handler: Handler
) : LifeCycleAdapter(), Runnable {
    private val mHandler: Handler
    private val mCleanupRunner = Runnable {
        mActivity.removeLifeCycleListener(this@BackgroundJob)
        if (mDialog.window != null) mDialog.dismiss()
    }

    init {
        mActivity.addLifeCycleListener(this)
        mHandler = handler
    }

    override fun run() {
        try {
            mJob.run()
        } finally {
            mHandler.post(mCleanupRunner)
        }
    }

    override fun onActivityDestroyed(activity: MonitoredActivity?) {
        mCleanupRunner.run()
        mHandler.removeCallbacks(mCleanupRunner)
    }

    override fun onActivityStopped(activity: MonitoredActivity?) {
        mDialog.hide()
    }

    override fun onActivityStarted(activity: MonitoredActivity?) {
        mDialog.show()
    }
}
