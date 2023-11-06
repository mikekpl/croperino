package com.mikelau.croperino

import android.graphics.BitmapFactory
import java.util.WeakHashMap

class BitmapManager private constructor() {
    private val mThreadStatus = WeakHashMap<Thread, ThreadStatus>()

    private enum class State {
        CANCEL,
        ALLOW
    }

    private class ThreadStatus {
        var mState = State.ALLOW
        var mOptions: BitmapFactory.Options? = null
        override fun toString(): String {
            var s: String = when (mState) {
                State.CANCEL -> "Cancel"
                State.ALLOW -> "Allow"
                else -> "?"
            }
            s = "thread state = $s, options = $mOptions"
            return s
        }
    }

    class ThreadSet : Iterable<Thread?> {
        private val mWeakCollection = WeakHashMap<Thread, Any?>()
        fun add(t: Thread) {
            mWeakCollection[t] = null
        }

        override fun iterator(): MutableIterator<Thread> {
            return mWeakCollection.keys.iterator()
        }
    }

    @Synchronized
    private fun getOrCreateThreadStatus(t: Thread): ThreadStatus {
        var status = mThreadStatus[t]
        if (status == null) {
            status = ThreadStatus()
            mThreadStatus[t] = status
        }
        return status
    }

    @Synchronized
    fun cancelThreadDecoding(threads: ThreadSet) {
        for (t in threads) {
            cancelThreadDecoding(t)
        }
    }

    @Synchronized
    fun cancelThreadDecoding(t: Thread) {
        val status = getOrCreateThreadStatus(t)
        status.mState = State.CANCEL
        if (status.mOptions != null) {
            status.mOptions?.requestCancelDecode()
        }
        (this as Object).notifyAll()
    }

    companion object {
        private var sManager: BitmapManager? = null
        @Synchronized
        fun instance(): BitmapManager? {
            if (sManager == null) {
                sManager = BitmapManager()
            }
            return sManager
        }
    }
}
