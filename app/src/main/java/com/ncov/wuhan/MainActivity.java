package com.ncov.wuhan;

import android.Manifest;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ncov.wuhan.NetUtils.IResponseCallBack;
import com.ncov.wuhan.NetUtils.IResponseParser;
import com.ncov.wuhan.NetUtils.Params.GlobalParams;
import com.ncov.wuhan.NetUtils.WMRequest;
import com.ncov.wuhan.Permission.PermissionsManager;
import com.ncov.wuhan.Utils.DeviceUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String requestUrl = "http://2019ncov.nosugartech.com/data.json?";
    private EditText checiEditText;
    private TextView dateText;
    private Button button;
    private TextView resultTextView;
    IResponseCallBack callBack = new IResponseCallBack() {
        @Override
        public void onSuccess(Object result) {
            if (result != null && ((ArrayList)result).size()>0){
                ResponseBean resultBean = searchResult(checiEditText.getText().toString().toUpperCase(), dateText.getText().toString(), (List<ResponseBean>) result);
                showResult(resultStringFormat(resultBean));
            } else {
                showResult("未获取到数据，可能远程服务器已关闭");
            }
        }

        @Override
        public void onError(Exception e) {
            showResult("未知异常");
        }
    };
    private int nowYear;
    private int nowMonth;
    private int nowDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionsManager.get(this).requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE});
        initParams();
        initViews();
    }

    private void initParams(){
        GlobalParams.applicationContext = getApplicationContext();
        Calendar calendar = Calendar.getInstance();
        nowYear = calendar.get(Calendar.YEAR);
        nowMonth = calendar.get(Calendar.MONTH) + 1;
        nowDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void initViews(){
        checiEditText = findViewById(R.id.checiEditText);
        checiEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        dateText = findViewById(R.id.datePicker);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String monthRevert = "";
                                if (month < 10){
                                    monthRevert = "0" + (month+1);
                                }
                                dateText.setText(String.format("%s-%s-%s", year, monthRevert, dayOfMonth));
                            }
                        }, nowYear, nowMonth -1 , nowDay).show();
            }
        });

        button = findViewById(R.id.search);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeRequest();
            }
        });

        resultTextView = findViewById(R.id.resultContent);
    }

    private void executeRequest() {
        if (DeviceUtils.isNetworkAvailable()) {
            new WMRequest.GetRequestBuild<>(requestUrl)
                    .responseOnMain(true)
                    .jsonParser(new IResponseParser<Object>() {
                                @Override
                                public Object parser(String jsonStr) throws Exception {
                                    return ResponseBean.parse(jsonStr);
                                }
                            }
                    )
                    .response(callBack)
                    .build()
                    .submit();
        } else {
            Toast.makeText(this, "网络不可用，请检查后重试", Toast.LENGTH_SHORT).show();
        }
    }
    //搜索结果
    private ResponseBean searchResult(String trainNumber, String travelTime, List<ResponseBean> list){
        Iterator<ResponseBean> iterator = list.iterator();
        while (iterator.hasNext()){
            ResponseBean next = iterator.next();
            if (TextUtils.equals(next.getStartTime(), travelTime)){
                if (next.getCarNumber().contains(trainNumber)){
                    return next;
                } else {
                    continue;
                }
            } else {
                continue;
            }
        }
        return getNoContentBean();
    }
    //返回空数据
    private ResponseBean getNoContentBean(){
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(ResponseBean.CODE.NO_CONTENT);
        return responseBean;
    }

    private void showResult(String result){
        if (!TextUtils.isEmpty(result)){
            resultTextView.setText(result);
        } else {
            resultTextView.setText("未查询到结果");
        }
    }

    private String resultStringFormat(ResponseBean bean){
        if (bean.getCode() == ResponseBean.CODE.SUCCESS){
            StringBuilder string = new StringBuilder();
            string.append(String.format("交通工具：%s \r\n", bean.getTravelMethod()));
            string.append(String.format("行程开始时间：%s \r\n", bean.getStartTime()));
            string.append(String.format("行程结束时间：%s \r\n", bean.getEndTime()));
            string.append(String.format("行程起始地点：%s \r\n", bean.getStartPlace()));
            string.append(String.format("行程结束地点：%s \r\n", bean.getEndPlace()));
            string.append(String.format("班次：%s \r\n", bean.getCarNumber()));
            string.append(String.format("车厢号（高铁，火车有此项）：%s \r\n", bean.getCabinNumber()));
            string.append(String.format("特别说明：%s \r\n", bean.getAppendMsg()));
            string.append(String.format("消息来源：%s \r\n", bean.getNewsSource()));
            string.append(String.format("链接：%s \r\n", bean.getNewsUrl()));
            return string.toString();
        } else {
            return "未找到数据";
        }


    }

    //规范时间格式
    private String timeFormat(int year, int month, int day){
        return year + "-" + month + "-" + day;
    }
}
