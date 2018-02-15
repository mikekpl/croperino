package com.mikelau.croperino;

import android.graphics.BitmapFactory;

import java.util.Iterator;
import java.util.WeakHashMap;

public class BitmapManager {

    private final WeakHashMap<Thread, ThreadStatus> mThreadStatus = new WeakHashMap<>();
    private static BitmapManager sManager = null;
    private enum State {CANCEL, ALLOW}

    private BitmapManager() {}

    private static class ThreadStatus {
        public State mState = State.ALLOW;
        public BitmapFactory.Options mOptions;

        @Override
        public String toString() {
            String s;
            if (mState == State.CANCEL) {
                s = "Cancel";
            } else if (mState == State.ALLOW) {
                s = "Allow";
            } else {
                s = "?";
            }
            s = "thread state = " + s + ", options = " + mOptions;
            return s;
        }
    }

    public static class ThreadSet implements Iterable<Thread> {
        private final WeakHashMap<Thread, Object> mWeakCollection = new WeakHashMap<>();

        public void add(Thread t) {
            mWeakCollection.put(t, null);
        }

        public Iterator<Thread> iterator() {
            return mWeakCollection.keySet().iterator();
        }
    }

    private synchronized ThreadStatus getOrCreateThreadStatus(Thread t) {
        ThreadStatus status = mThreadStatus.get(t);
        if (status == null) {
            status = new ThreadStatus();
            mThreadStatus.put(t, status);
        }
        return status;
    }

    public synchronized void cancelThreadDecoding(ThreadSet threads) {
        for (Thread t : threads) {
            cancelThreadDecoding(t);
        }
    }

    public synchronized void cancelThreadDecoding(Thread t) {
        ThreadStatus status = getOrCreateThreadStatus(t);
        status.mState = State.CANCEL;
        if (status.mOptions != null) {
            status.mOptions.requestCancelDecode();
        }

        notifyAll();
    }

    public static synchronized BitmapManager instance() {
        if (sManager == null) {
            sManager = new BitmapManager();
        }
        return sManager;
    }
}
