package com.ncov.wuhan.ThreadUtils;


public final class TaskManager {


    /**
     * 执行一个任务
     * @param runnable 任务
     */
    public static void startRunnable(NameRunnable runnable){
        if (runnable == null){
            return;
        }
        ThreadPoolManager.getInstance().execute(runnable);
    }

    /**
     * 执行一个可以取消的任务
     * @param runnable 任务
     * @return key 任务id
     */
    public static int startCancelableRunnable(NameRunnable runnable){
        if (runnable == null){
            return 0;
        }
        return ThreadPoolManager.getInstance().submit(runnable);
    }

    /**
     * 取消一个任务
     * @param key 任务id
     * @return true 成功，false 失败
     */
    public static boolean cancelRunnable(int key){
        if (key == 0){
            return false;
        }
        return ThreadPoolManager.getInstance().cancelRunnable(key);
    }

    /**
     * 根据 key 判断相应任务是否已经取消
     * @param key  任务对应的key
     * @return true 取消，false 未取消
     */
    public static boolean isCancel(int key){
        return ThreadPoolManager.getInstance().isCancel(key);
    }

    /**
     * UI线程执行runnable
     */
    public static void runOnUIThread(Runnable runnable) {
        if (runnable == null){
            return;
        }
        UIPoolManager.runOnUIThread(runnable);
    }

    /**
     * UI线程执行延迟runnable
     */
    public static void runOnUIThreadDelayed(Runnable runnable, long delayed) {
        if (runnable == null){
            return;
        }
        UIPoolManager.runOnUIThreadDelayed(runnable, delayed);
    }

    /**
     * UI线程取消执行runnable
     */
    public static void cancelRunnableOnUIThread(Runnable runnable) {
        if (runnable == null){
            return;
        }
        UIPoolManager.cancelRunnableOnUIThread(runnable);
    }
}
