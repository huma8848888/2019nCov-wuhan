package com.ncov.wuhan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResponseBean {

    private static List<ResponseBean> responseBeans = new ArrayList<>();//将结果整体返回
    private int code;//内部使用的状态码
    private String startTime;//行程开始时间
    private String endTime;//行程结束时间
    private String carNumber;//车辆编号（火车车次，汽车车牌号）
    private String cabinNumber;//火车车厢号
    private String startPlace;//行程开始地点
    private String endPlace;//行程结束地点
    private String appendMsg;//附加说明信息
    private String newsSource;//新闻来源
    private String newsUrl;//新闻url，
    private String travelMethod;//出行方式

    public static List<ResponseBean> parse(String json){

        try {
            responseBeans.clear();
            JSONObject rawJson = new JSONObject(json);
            if (rawJson.has("data")){
                JSONArray dataJsonArray = rawJson.optJSONArray("data");
                for (int i = 0; i < dataJsonArray.length(); i++){
                    JSONObject jsonIndexObject = dataJsonArray.getJSONObject(i);
                    ResponseBean beanItem = new ResponseBean();
                    if (jsonIndexObject.has("t_start")){//开始行程时间
                        String rawString = jsonIndexObject.optString("t_start");
                        beanItem.setStartTime(jsonIndexObject.optString("t_start").split("T")[0]);//只截取日期出来
                    }
                    if (jsonIndexObject.has("t_end")){//结束行程时间
                        beanItem.setEndTime(jsonIndexObject.optString("t_end").split("T")[0]);//只截取日期出来
                    }
                    if (jsonIndexObject.has("t_no")){//车号
                        beanItem.setCarNumber(jsonIndexObject.optString("t_no"));
                    }
                    if (jsonIndexObject.has("t_no_sub")){//车厢号
                        beanItem.setCabinNumber(jsonIndexObject.optString("t_no_sub"));
                    }
                    if (jsonIndexObject.has("t_pos_start")){//开始地点
                        beanItem.setStartPlace(jsonIndexObject.optString("t_pos_start"));
                    }
                    if (jsonIndexObject.has("t_pos_end")){//结束地点
                        beanItem.setEndPlace(jsonIndexObject.optString("t_pos_end"));
                    }
                    if (jsonIndexObject.has("t_memo")){//特别说明
                        beanItem.setAppendMsg(jsonIndexObject.optString("t_memo"));
                    }
                    if (jsonIndexObject.has("who")){//消息来源
                        beanItem.setNewsSource(jsonIndexObject.optString("who"));
                    }
                    if (jsonIndexObject.has("source")){//消息url
                        beanItem.setNewsUrl(jsonIndexObject.optString("source"));
                    }
                    if (jsonIndexObject.has("t_type")){//出行方式
                        beanItem.setTravelMethod(travelMethodSelector(jsonIndexObject.optInt("t_type")));
                    }
                    beanItem.setCode(0);
                    responseBeans.add(beanItem);
                }
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            return responseBeans;
        }
    }

    //转换成字符串传出去
    private static String travelMethodSelector(int method){
        switch (method){
            case 1:
                return "飞机";
            case 2:
                return "火车";
            case 3:
                return "地铁";
            case 4:
                return "长途客车/大巴";
            case 5:
                return "公交车";
            case 6:
                return "出租车";
            case 7:
                return "轮船";
            default:
                return "其他公共场所";
        }
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getCabinNumber() {
        return cabinNumber;
    }

    public void setCabinNumber(String cabinNumber) {
        this.cabinNumber = cabinNumber;
    }

    public String getStartPlace() {
        return startPlace;
    }

    public void setStartPlace(String startPlace) {
        this.startPlace = startPlace;
    }

    public String getEndPlace() {
        return endPlace;
    }

    public void setEndPlace(String endPlace) {
        this.endPlace = endPlace;
    }

    public String getAppendMsg() {
        return appendMsg;
    }

    public void setAppendMsg(String appendMsg) {
        this.appendMsg = appendMsg;
    }

    public String getNewsSource() {
        return newsSource;
    }

    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    public String getNewsUrl() {
        return newsUrl;
    }

    public void setNewsUrl(String newsUrl) {
        this.newsUrl = newsUrl;
    }

    public String getTravelMethod() {
        return travelMethod;
    }

    public void setTravelMethod(String travelMethod) {
        this.travelMethod = travelMethod;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    interface CODE{
        int SUCCESS = 0;
        int NO_CONTENT = -1;
        int FAIL = -2;
    }



}
