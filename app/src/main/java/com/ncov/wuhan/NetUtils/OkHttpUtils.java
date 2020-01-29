package com.ncov.wuhan.NetUtils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ncov.wuhan.Utils.DeviceUtils;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {

    private static final String TAG = WMRequest.TAG;

    private static final String USER_AGENT = "User-Agent";
    private static final Object lock = new Object();
    private static final String uaSegment = "/";
    private static OkHttpClient client;
    private static volatile String userAgent = null;
    private static MyTrustManager mMyTrustManager;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        builder.addInterceptor(new CommonParamsInterceptor());
        //禁止重定向，交给客户端处理
//        builder.followRedirects(false);
//        builder.followSslRedirects(false);
        //打开重试机制
        builder.retryOnConnectionFailure(true);
        builder.sslSocketFactory(createSSLSocketFactory(), mMyTrustManager)
                .hostnameVerifier(new TrustAllHostnameVerifier());
        client = builder.build();
    }
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            mMyTrustManager = new MyTrustManager();
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{mMyTrustManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return ssfFactory;
    }

    /************************************************************************************************/

    static WMResponse request(@NonNull WMRequestBuild wbRequestBuild) {
        HttpUrl httpUrl = HttpUrl.parse(wbRequestBuild.getUrl());
        if (httpUrl == null) {
            return null;
        }
        HttpUrl.Builder httpUrlBuilder = addUrlParams(wbRequestBuild.getUrlParams(), httpUrl);

        Request.Builder okRequestBuilder = new Request.Builder().url(httpUrlBuilder.build());
        Map<String, String> header = wbRequestBuild.getHeaderParams();

        addHeaderParams(header, okRequestBuilder);

        if (wbRequestBuild instanceof WMRequest.GetRequestBuild) {
            okRequestBuilder.method("GET", null);
        } else {
            okRequestBuilder.method("POST", getBodyParams(wbRequestBuild));
        }
        WMResponse wmResponse = new WMResponse();
        try {
            Response response = client.newCall(okRequestBuilder.build()).execute();
            Log.i(TAG, "ok request ok , response : " + response.toString());
            wmResponse.code = response.code();
            wmResponse.headers = response.headers();
            if (response.body() == null) {
                Log.i(TAG, "ok request ok , but response body is null");
            } else {
                wmResponse.body = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "ok request exception", e);
        }
        return wmResponse;
    }

    /**
     * 添加 url 参数
     */
    private static HttpUrl.Builder addUrlParams(Map<String, String> urlParams, HttpUrl httpUrl) {
        HttpUrl.Builder builder = httpUrl.newBuilder();
        //添加 url 参数
        for (Map.Entry<String, String> entry : urlParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!TextUtils.isEmpty(key) && value != null) {
                builder.addEncodedQueryParameter(key, value);
            } else {
                Log.i(TAG, "UrlParams key or value is null, key=" + key + " , value=" + value);
            }
        }
        return builder;
    }

    /**
     * 添加 header 参数
     */
    private static void addHeaderParams(Map<String, String> headerParams, Request.Builder okRequestBuilder) {
        for (Map.Entry<String, String> entry : headerParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!TextUtils.isEmpty(key) && value != null) {
                okRequestBuilder.addHeader(entry.getKey(), entry.getValue());
            } else {
                Log.i(TAG, "HeaderParams key or value is null, key=" + key + " , value=" + value);
            }
        }
    }

    /**
     * 添加 body 参数
     */
    private static RequestBody getBodyParams(@NonNull WMRequestBuild wbRequestBuild) {
        Map<String, String> bodyParams = wbRequestBuild.getBodyParams();
        if (wbRequestBuild.getFile() != null) {
            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!TextUtils.isEmpty(key) && value != null) {
                    multipartBuilder.addFormDataPart(key, value);
                } else {
                    Log.i(TAG, "MultipartBody key or value is null, key=" + key + " , value=" + value);
                }
            }
            multipartBuilder.addFormDataPart(wbRequestBuild.getFormFileName(), wbRequestBuild.getFile().getName(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), wbRequestBuild.getFile()));
            return multipartBuilder.build();
        } else {
            FormBody.Builder formBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!TextUtils.isEmpty(key) && value != null) {
                    formBuilder.add(key, value);
                } else {
                    Log.i(TAG, "FormBody key or value is null, key=" + key + " , value=" + value);
                }
            }
            return formBuilder.build();
        }
    }

    public static void cancelImageRequest(Call imageRequest) {
        try {
            if (imageRequest != null && !imageRequest.isCanceled()) {
                imageRequest.cancel();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static String getUserAgent() {
        if (TextUtils.isEmpty(userAgent)) {
            synchronized (lock) {
                if (TextUtils.isEmpty(userAgent)) {
                    StringBuilder sb = new StringBuilder();

                    try {
//                        sb.append("Passport测试Test数据");
//                        sb.append(uaSegment);
//                        sb.append(DeviceUtils.getApplicationName(WMSetting.applicationContext));
//                        sb.append(uaSegment);
//                        sb.append(DeviceUtils.getVersionCodeString(WMSetting.applicationContext));
                        sb.append(DeviceUtils.getSysUserAgent());
                    } catch (Exception e) {

                    }
                    userAgent = sb.toString();
                }
            }
        }
        Log.i(TAG, "getUserAgent:" + userAgent);
        return userAgent;
    }

    private static String filterUAStr(String oldValue) {
        if (TextUtils.isEmpty(oldValue)) {
            return "";
        }

        return "";
    }

    //实现HostnameVerifier接口
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    //实现X509TrustManager接口
    public static class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 添加通用参数
     */
    private static class CommonParamsInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            HttpUrl httpUrl = chain.request().url();
            Request.Builder builder = chain.request().newBuilder();

            //添加全局通用 不可变参数
//            addHeaderParams(CommonHeaderUtils.getImmutableHeaders(), builder);
            //添加全局通用 可变参数
//            addHeaderParams(CommonHeaderUtils.getVariableHeaderParams(), builder);
            //构建公共参数时候，Cookie需要从当前请求的域获取，其他的不能有
//            addHeaderParams(CommonHeaderUtils.getCookieParamsWithUrl(httpUrl.toString()), builder);
            //移除原有的UA头，
//            builder.removeHeader(USER_AGENT);
//            //添加新的UA头
//            builder.addHeader(USER_AGENT, getUserAgent());
            builder.url(httpUrl);
            return chain.proceed(builder.build());
        }
    }
}
