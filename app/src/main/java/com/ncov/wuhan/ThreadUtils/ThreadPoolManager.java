package com.ncov.wuhan.ThreadUtils;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class ThreadPoolManager {

    private static final String TAG = "ThreadPoolManager";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    //核心线程数
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    //最大线程数
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    //等待时间
    private static final int KEEP_ALIVE_SECONDS = 30;

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            String threadName = "ThreadPoolManager #" + mCount.getAndIncrement();
            if (r instanceof NameRunnable){
                threadName += ((NameRunnable) r).getThreadName();
            }
            return new Thread(r, threadName);
        }
    };

    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     */
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR;

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue, sThreadFactory,new CustomRejectedHandler("ThreadPoolManager"));
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

    private ThreadPoolManager(){}

    /************************  ThreadPoolManager  *************************/
    public static ThreadPoolManager getInstance(){
        return InstanceHolder.instance;
    }

    /**
     * 执行一个任务
     * @param runnable 任务
     */
    void execute(@NonNull Runnable runnable){
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    /**
     * 提交一个可以取消的任务
     * @param runnable 任务
     * @return key 任务对应的id
     */
    int submit(@NonNull NameRunnable runnable){
        int key = 0;

        NameFutureTask nameFutureTask = new NameFutureTask<>(runnable);

        key = FutureCacheManager.getInstance().put(nameFutureTask);
        nameFutureTask.setKey(key);

        THREAD_POOL_EXECUTOR.submit(nameFutureTask);
        return key;
    }

    /**
     * 取消任务
     * @param key 任务对应的key
     * @return true 成功，false 失败
     */
    boolean cancelRunnable(int key){
        return FutureCacheManager.getInstance().cancelFuture(key);
    }

    /**
     * 根据 key 判断相应任务是否已经取消
     * @param key  任务对应的key
     * @return true 取消，false 未取消
     */
    boolean isCancel(int key){
        return FutureCacheManager.getInstance().isCancel(key);
    }

    /**
     * 可取消的Future
     */
    private static final class NameFutureTask<T> extends FutureTask<T> {

        private int key = -1;
        private NameRunnable runnable;

        NameFutureTask(@NonNull NameRunnable runnable) {
            super(runnable, null);
            this.runnable = runnable;
        }

        @Override
        protected void done() {
            try {
                runnable.updateStatus(NameRunnable.RUNNING);
                get();
            }  catch (Throwable e) {
                Log.e(TAG,"done:",e);
            }  finally {
                runnable.updateStatus(NameRunnable.FINISH);
                FutureCacheManager.getInstance().remove(key);
            }
        }

        @Override
        public boolean isCancelled() {
            return runnable.isCancel() || super.isCancelled();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (runnable != null){
                runnable.cancel();
                return true;
            }
            return super.cancel(mayInterruptIfRunning);
        }

        public void setKey(int key) {
            this.key = key;
        }
    }

    /**
     * FutureTask 缓存管理类
     */
    private static final class FutureCacheManager{

        private final Object mCacheLock = new Object();
        private final AtomicInteger mKeyIndex = new AtomicInteger(1);
        private final SparseArray<NameFutureTask> mFutureCache = new SparseArray<>();

        private FutureCacheManager(){}

        private static FutureCacheManager getInstance(){
            return InstanceHolder.instance;
        }

        /**
         * 添加一个任务到缓存中
         * @param future 任务
         * @return key 任务对应的key
         */
        int put(@NonNull NameFutureTask future){
            int key = -1;
            synchronized (mCacheLock){
                if(mKeyIndex.get() < Integer.MAX_VALUE - 10) {
                    key = mKeyIndex.getAndIncrement();
                }else{
                    mKeyIndex.set(1);
                    key = 1;
                }
                mFutureCache.put(key,future);
            }
            return key;
        }

        /**
         * 根据 key 取消相应的任务
         * @param key 任务对应的key
         * @return true 取消成功，false 取消失败
         */
        boolean cancelFuture(int key){
            boolean ret = false;
            synchronized (mCacheLock){
                NameFutureTask future = mFutureCache.get(key);
                if (future != null){
                    ret = future.cancel(false);
                }
            }
            if (ret){
                remove(key);
            }
            return ret;
        }

        /**
         * 根据 key 判断相应任务是否已经取消
         * @param key  任务对应的key
         * @return true 取消，false 未取消
         */
        boolean isCancel(int key){
            boolean isCancel;
            synchronized (mCacheLock){
                NameFutureTask future = mFutureCache.get(key);
                isCancel = future == null || future.isCancelled();
            }
            return isCancel;
        }

        /**
         * 根据 key 从换从中移除相应的任务
         * @param key 任务对应的key
         */
        void remove(int key){
            synchronized (mCacheLock){
                mFutureCache.remove(key);
            }
        }

        private static class InstanceHolder{
            static FutureCacheManager instance = new FutureCacheManager();
        }

    }

    /**
     * 基本上只是用于兜底策略
     * 自定义溢出策略，溢出后重心开启一个线程池进行执行
     */
    private static final class CustomRejectedHandler implements RejectedExecutionHandler {

        private String threadPoolName = "RejectedHandlerThread";

        CustomRejectedHandler(String threadPoolName){
            this.threadPoolName = threadPoolName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Log.i(TAG, "too much execute reject called " + threadPoolName);
            RejectExecutorHolder.sRejectExecutor.execute(r);
        }

        private static class RejectExecutorHolder {
            private static final LinkedBlockingQueue<Runnable> rejectQueue = new LinkedBlockingQueue<>();
            private static final ThreadPoolExecutor sRejectExecutor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.SECONDS, rejectQueue, sThreadFactory,
                    new CustomRejectedHandler("RejectedHandlerThread"));
        }
    }

    private static class InstanceHolder {
        static ThreadPoolManager instance = new ThreadPoolManager();
    }
}
