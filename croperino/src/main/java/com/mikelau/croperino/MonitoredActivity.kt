package com.mikelau.croperino

import android.app.Activity
import android.os.Bundle

open class MonitoredActivity : Activity() {
    private val mListeners = ArrayList<LifeCycleListener>()

    interface LifeCycleListener {
        fun onActivityCreated(activity: MonitoredActivity?)
        fun onActivityDestroyed(activity: MonitoredActivity?)
        fun onActivityStarted(activity: MonitoredActivity?)
        fun onActivityStopped(activity: MonitoredActivity?)
    }

    open class LifeCycleAdapter : LifeCycleListener {
        override fun onActivityCreated(activity: MonitoredActivity?) {}
        override fun onActivityDestroyed(activity: MonitoredActivity?) {}
        override fun onActivityStarted(activity: MonitoredActivity?) {}
        override fun onActivityStopped(activity: MonitoredActivity?) {}
    }

    fun addLifeCycleListener(listener: LifeCycleListener) {
        if (mListeners.contains(listener)) return
        mListeners.add(listener)
    }

    fun removeLifeCycleListener(listener: LifeCycleListener) {
        mListeners.remove(listener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (listener in mListeners) {
            listener.onActivityCreated(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (listener in mListeners) {
            listener.onActivityDestroyed(this)
        }
    }

    override fun onStart() {
        super.onStart()
        for (listener in mListeners) {
            listener.onActivityStarted(this)
        }
    }

    override fun onStop() {
        super.onStop()
        for (listener in mListeners) {
            listener.onActivityStopped(this)
        }
    }
}
