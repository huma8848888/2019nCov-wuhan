package com.ncov.wuhan.NetUtils;

import android.util.Log;

import com.ncov.wuhan.ThreadUtils.NameRunnable;
import com.ncov.wuhan.ThreadUtils.TaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;


public final class WMRequest<R> {

    static final String TAG = "WMRequest";
    private static final int IDLE = 0;//未开始
    private static final int START = 1;//请求中
    private static final int END = 2;//结束
    private static final int CANCEL = 3;//取消
    private static final String DATA_NULL_EXCEPTION = "data is null";
    //状态相关 0 未开始 1 请求中 2 结束 3 取消
    private final AtomicInteger mState = new AtomicInteger(IDLE);
    private WMRequestBuild<R> mRequestBuild;
    private WMResponse mWMResponse;
    private R result;
    private Exception exception;

    private WMRequest(WMRequestBuild<R> build) {
        mRequestBuild = build;
    }

    public WMRequest<R> submit() {
        synchronized (mState) {
            if (mState.get() != IDLE) {
                return this;
            } else {
                mState.set(START);
            }
        }
        TaskManager.startRunnable(new NameRunnable("NetWork") {
            @Override
            public void run() {
                doExecute();
                doResponse();
                doDispatch();
            }
        });
        return this;
    }

    public boolean isRunning() {
        return mState.get() == START;
    }

    public boolean isCancel() {
        return mState.get() == CANCEL;
    }

    public void cancel() {
        mState.set(CANCEL);
    }

    private void doExecute() {
        try {
            Log.i(TAG, "request , url = " + mRequestBuild.getUrl());
            mWMResponse = mRequestBuild.request();
            Log.i(TAG, "success , jsonResult = " + mWMResponse.body);
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
            Log.i(TAG, "error , ", e);
        }
    }

    private void doResponse() {
        if (exception != null) {
            return;
        }
        if (mWMResponse == null) {
            exception = new Exception("jsonResult is null");
            return;
        }
        if (mRequestBuild.getResponseParser() != null) {
            try {
                String jsonResponse = generateResultStr();
                result = mRequestBuild.getResponseParser().parser(jsonResponse);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (result == null) {
                exception = new ResultNullException(DATA_NULL_EXCEPTION + " , jsonResult = " + mWMResponse.body);
            }
        }
    }

    private String generateResultStr() {
        String jsonResponse;
        if (mWMResponse.code == 200) {
            return mWMResponse.body;
        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("code", -777);
                jsonObject.put("msg", "网络连接失败，请重试");
                jsonObject.put("httpcode", mWMResponse.code);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(TAG, "generateResultStr encode jsonObject error", e);
            }
            jsonResponse = jsonObject.toString();
        }
        return jsonResponse;
    }

    private void doDispatch() {
        TaskManager.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (mWMResponse.code == 200) {
                    mRequestBuild.getCallBack().onSuccess(result);
                } else if (exception != null) {
                    mRequestBuild.getCallBack().onError(exception);
                } else if (result == null) {
                    mRequestBuild.getCallBack().onError(new ResultNullException("获取用户信息为空值"));
                }
            }
        });

    }

    public WMRequestBuild<R> getBuild() {
        return mRequestBuild;
    }

//    public void dispatchCallback(R resultInfo, Exception e) {
//        if (mRequestBuild.getCallBack() != null) {
//            if (isCancel()) {
//                mRequestBuild.getCallBack().onCancel();
//            } else {
//                mState.set(END);
//                if (e != null) {
//                    mRequestBuild.getCallBack().onError(e);
//                } else {
//                    mRequestBuild.getCallBack().onSuccess(resultInfo);
//                }
//            }
//        }
//    }

    public static class ResultNullException extends Exception {
        public ResultNullException(String msg) {
            super(msg);
        }
    }

    public static class GetRequestBuild<R> extends WMRequestBuild<R> {

        public GetRequestBuild(String url) {
            super(url);
        }

        @Override
        public WMRequest<R> build() {
            return new WMRequest<>(this);
        }

        @Override
        protected WMResponse request() throws Exception {
            return OkHttpUtils.request(this);
        }
    }

    public static class PostRequestBuild<R> extends WMRequestBuild<R> {

        public PostRequestBuild(String url) {
            super(url);
        }

        @Override
        public WMRequest<R> build() {
            return new WMRequest<>(this);
        }

        @Override
        protected WMResponse request() throws Exception {
            return OkHttpUtils.request(this);
        }
    }

}
