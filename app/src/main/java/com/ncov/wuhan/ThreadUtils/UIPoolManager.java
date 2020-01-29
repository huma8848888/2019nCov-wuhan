package com.ncov.wuhan.ThreadUtils;

import android.os.Handler;
import android.os.Looper;


final class UIPoolManager {

    private static final Handler sMainLooper = new Handler(Looper.getMainLooper());

    static void runOnUIThread(Runnable runnable) {
        sMainLooper.post(runnable);
    }

    static void runOnUIThreadDelayed(Runnable runnable, long delayed) {
        sMainLooper.postDelayed(runnable, delayed);
    }

    static void cancelRunnableOnUIThread(Runnable runnable) {
        sMainLooper.removeCallbacks(runnable);
    }

}
