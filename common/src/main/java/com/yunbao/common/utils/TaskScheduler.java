package com.yunbao.common.utils;

import android.os.Handler;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by http://www.yunbaokj.com on 2023/12/4.
 */
public class TaskScheduler {
    private static final String TAG = "TaskScheduler";
    private final Handler mHandler;
    private final Map<String, Task> mTaskMap;

    public TaskScheduler() {
        mHandler = new Handler();
        mTaskMap = new HashMap<>();
    }

    public void startTask(String tag, long delay, long interval, Runnable runnable) {
        Task task = mTaskMap.get(tag);
        if (task != null) {
            task.cancel();
        }
        task = new Task(runnable, delay, interval);
        mTaskMap.put(tag, task);
        task.start();
        L.e(TAG, "startTask--->" + tag);
    }

    public void cancelTask(String tag) {
        Task task = mTaskMap.get(tag);
        if (task != null) {
            task.cancel();
            mTaskMap.remove(tag);
            L.e(TAG, "cancelTask--->" + tag);
        }
    }

    public void cancelAllTasks() {
        if (mTaskMap.size() > 0) {
            for (Map.Entry<String, Task> entry : mTaskMap.entrySet()) {
                entry.getValue().cancel();
                L.e(TAG, "cancelAllTasks--->" + entry.getKey());
            }
            mTaskMap.clear();
        }
    }


    public class Task {
        private final Runnable mRunnable;
        private final long mDelay;
        private final long mInterval;
        private long mNextTime;

        public Task(final Runnable runnable, long delay, long interval) {
            if (delay < 0) {
                delay = 0;
            }
            if (interval < 0) {
                interval = 0;
            }
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (mInterval > 0) {
                        mNextTime += mInterval;
                        mHandler.postAtTime(mRunnable, mNextTime);
                    }
                }
            };
            mDelay = delay;
            mInterval = interval;
        }

        void start() {
            mNextTime = SystemClock.uptimeMillis();
            if (mDelay == 0) {
                mRunnable.run();
            } else {
                mNextTime += mDelay;
                mHandler.postAtTime(mRunnable, mNextTime);
            }
        }

        void cancel() {
            mHandler.removeCallbacks(mRunnable);
        }

    }
}
