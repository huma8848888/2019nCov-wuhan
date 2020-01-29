package com.ncov.wuhan.NetUtils;

import android.util.Log;

public abstract class IResponseCallBack<R> {
    private static final String TAG = "IResponseCallBack";
    public abstract void onSuccess(R result);
    public abstract void onError(Exception e);

    public void onCancel() {
        Log.i(TAG, "onCancel: ");
    }
}
