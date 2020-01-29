package com.ncov.wuhan.NetUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class WMRequestBuild<R> {

    //请求相关
    private String url;

    //解析相关
    private IResponseParser<R> responseParser;

    //参数相关
    private String formFileName;//表单形式提交文件
    private File file;//提交的文件
    private Map<String, String> urlParams = new LinkedHashMap<String, String>();
    private Map<String, String> headerParams = new LinkedHashMap<String, String>();
    private Map<String, String> bodyParams = new LinkedHashMap<String, String>();

    /**
     * 响应得数据是否交给本地Handler去处理，默认为TRUE，此值会影响所有挑战类型，如果不需要处理,交给具体业务设置成FALSE
     */
    private boolean isGlobalHandleResponse = true;

    //回调相关
    private IResponseCallBack<R> callBack;
    private boolean responseOnMain = false;//返回结果在 UI 线程处理，默认 false

    WMRequestBuild(String url) {
        setUrl(url);
    }

    public abstract WMRequest<R> build();

    /**
     * 开始真正的请求
     * @return 返回的jsonStr
     */
    protected abstract WMResponse request() throws Exception;

    public WMRequestBuild<R> addHeaderParams(Map<String, String> params) {
        headerParams.putAll(params);
        return this;
    }

    public WMRequestBuild<R> addHeaderParams(String key, String value) {
        headerParams.put(key, value);
        return this;
    }

    public WMRequestBuild<R> addUrlParams(Map<String, String> params) {
        urlParams.putAll(params);
        return this;
    }

    public WMRequestBuild<R> addUrlParams(String key, String value) {
        urlParams.put(key, value);
        return this;
    }

    public WMRequestBuild<R> addBodyParams(Map<String, String> params) {
        bodyParams.putAll(params);
        return this;
    }

    public WMRequestBuild<R> addBodyParams(String key, String value) {
        bodyParams.put(key, value);
        return this;
    }

    public WMRequestBuild<R> jsonParser(IResponseParser<R> responseParser) {
        this.responseParser = responseParser;
        return this;
    }

    public WMRequestBuild<R> response(IResponseCallBack<R> callBack) {
        this.callBack = callBack;
        return this;
    }

    public WMRequestBuild<R> responseOnMain(boolean responseOnMain) {
        this.responseOnMain = responseOnMain;
        return this;
    }

    public WMRequestBuild<R> addFile(String formName, File file) {
        this.formFileName = formName;
        this.file = file;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public WMRequestBuild<R> setUrl(String url) {
        this.url = url;
        return this;
    }

    public IResponseParser<R> getResponseParser() {
        return responseParser;
    }

    public String getFormFileName() {
        return formFileName;
    }

    public File getFile() {
        return file;
    }

    public Map<String, String> getHeaderParams() {
        if (headerParams == null){
            headerParams = new LinkedHashMap<>();
        }
        return headerParams;
    }

    public Map<String, String> getBodyParams() {
        if (bodyParams == null){
            bodyParams = new LinkedHashMap<>();
        }
        return bodyParams;
    }

    public Map<String, String> getUrlParams() {
        if (urlParams == null){
            urlParams = new LinkedHashMap<>();
        }
        return urlParams;
    }

    public IResponseCallBack<R> getCallBack() {
        return callBack;
    }

    public boolean isResponseOnMain() {
        return responseOnMain;
    }

    public boolean isGlobalHandleResponse() {
        return isGlobalHandleResponse;
    }

    public void setGlobalHandleResponse(boolean globalHandleResponse) {
        isGlobalHandleResponse = globalHandleResponse;
    }
}
