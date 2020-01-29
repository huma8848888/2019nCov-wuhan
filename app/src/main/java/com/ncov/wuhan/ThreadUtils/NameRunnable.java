package com.ncov.wuhan.ThreadUtils;


import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class NameRunnable implements Runnable {

    public static final int IDLE = 0;//未执行
    public static final int RUNNING = 1;//执行中
    public static final int CANCEL = 2;//执行取消
    public static final int FINISH = 3;//执行完成
    private static final String NONE_NAME = "NoName";
    /**
     * 0：未执行
     * 1：执行中
     * 2：执行取消
     * 3：执行完成
     */
    private final AtomicInteger mStatus = new AtomicInteger(IDLE);
    private String threadName = NONE_NAME;


    public NameRunnable(){
        this(NONE_NAME);
    }

    public NameRunnable(String name){
        threadName = name;
        updateStatus(IDLE);
    }

    void cancel(){
        updateStatus(CANCEL);
        handleCancel();
    }

    public void handleCancel(){}

    public boolean isCancel(){
        return mStatus.get() == CANCEL;
    }

    public boolean isRunning(){
        return mStatus.get() == RUNNING;
    }

    public boolean isFinish(){
        return mStatus.get() == FINISH;
    }

    public String getThreadName(){
        return threadName;
    }

    void updateStatus(int status){
        mStatus.set(status);
    }


    @NonNull
    @Override
    public String toString() {
        return super.toString() +"(name:"+threadName+")";
    }
}
