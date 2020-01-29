package com.ncov.wuhan.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ncov.wuhan.NetUtils.Params.GlobalParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

@Keep
public class DeviceUtils {


    private static final String TAG = "DeviceUtils";
    private static String PHONE_NUMBER = null;
    private static String mDeviceImei;
    private static String mReallyImei;
    private static String mDeviceId;
    private static String mUniqueId;

    /**
     * 获取手机的电话号码 注意：不是所有的手机都可以得到
     *
     * @param context
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getPhoneNumber(Context context) {
        if (PHONE_NUMBER != null) {
            return PHONE_NUMBER;
        }

        TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyMgr != null) {
            PHONE_NUMBER = telephonyMgr.getLine1Number();
        }

        if (PHONE_NUMBER == null) {
            PHONE_NUMBER = "";
        }
        return PHONE_NUMBER;
    }

    //将搜索页面使用的判断wifi的方法提出来
    public static boolean isWifi(Context context) {
        boolean isWifi = false;
        String NetType = getNetType(context);
        if (!TextUtils.isEmpty(NetType)) {
            if ("WIFI".equalsIgnoreCase(NetType)) {
                isWifi = true;
            }
        }
        return isWifi;
    }

    /**
     * 获取网络连接类型
     *
     * @param context
     *
     * @return
     */
    public static final String getNetType(Context context) {
        String networkType = null;

        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();// NULL

            if (networkInfo != null && networkInfo.isAvailable()) {
                String typeName = networkInfo.getTypeName(); // MOBILE/WIFI
                if (!"MOBILE".equalsIgnoreCase(typeName)) {
                    networkType = typeName;
                } else {
                    networkType = networkInfo.getExtraInfo(); // cmwap/cmnet/wifi/uniwap/uninet
                    if (networkType == null) {
                        networkType = typeName + "#[]";
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "getNetType", e);
        }
        Log.d(TAG, "networkType:" + networkType);
        return networkType == null ? "" : networkType;
    }

    public static boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) GlobalParams.applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable();
        } catch (Exception e) {
            Log.d(TAG, "isNetworkAvailable:" + e.getMessage());
            return false;
        }
    }

    public static void setWindowSecure(Window window) {
        //为了安全添加 ,暂时注释
//		window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    /**
     * 不是所有的键盘都可以用该方法消失，比如Dialog中的不可以
     *
     * @param activity
     */
    public static void hideSoftInputFromWindow(@NonNull Activity activity) {
        View currentView = activity.getWindow().peekDecorView();
        if (currentView != null) {
            DeviceUtils.hideSoftInputFromWindow(activity, currentView.getWindowToken());
        }
    }

    /**
     * 能获取响应焦点控件的都都用此方法
     *
     * @param context
     * @param editText
     */
    public static void hideSoftInputFromWindow(@NonNull Context context, EditText editText) {
        if (editText == null) {
            return;
        }
        editText.clearFocus();
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void hideSoftInputFromWindow(@NonNull Context context, IBinder windowToken) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(windowToken, 0);
    }

    public static void showSoftInput(@NonNull Context context, @Nullable View view) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, 0);
    }

    /**
     * 获取uuid
     */
    public static String getUUID() {

        String uuidstr;

        try {
            UUID uuid = UUID.randomUUID();

            uuidstr = uuid.toString();

            uuidstr = uuidstr.substring(0, 8) + uuidstr.substring(9, 13) + uuidstr.substring(14, 18) + uuidstr.substring(19, 23) + uuidstr.substring(24);

        } catch (Exception e) {
            Log.e(TAG, "get uuid exception : ", e);
            return null;
        }

        return uuidstr;

    }

    /**
     * 随机获取来源ID
     */
    public static String getSourceID() {
        UUID uuid = UUID.randomUUID();
        String uuidstr = uuid.toString();
        return uuidstr;
    }

    /**
     * 取得屏幕宽度像素
     *
     * @param activity 设备上下文
     *
     * @return 屏幕的宽度
     */
    public static int getScreenWidth(Activity activity) {
        DisplayMetrics dm = getDisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm); // 得到屏幕大小
        return dm.widthPixels;
    }

    /**
     * 取得屏幕高度像素
     *
     * @param activity 设备上下文
     *
     * @return 屏幕的高度
     */
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics dm = getDisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm); // 得到屏幕大小
        return dm.heightPixels;
    }

    /**
     * 取得状态栏高度
     *
     * @param activity 设备上下文
     *
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Activity activity) {
        Rect rectgle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        return rectgle.top;
    }

    /**
     * 获取屏幕分辨率  宽X高
     *
     * @param context
     *
     * @return 获取到的屏幕分辨率
     */
    public static String getDisplay(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        String resolution = screenWidth + "*" + screenHeight;
        Log.d(TAG, resolution);
        return resolution;
    }

    /**
     * 获取屏幕分辨率 高X宽
     *
     * @param context
     *
     * @return 获取到的屏幕分辨率
     */
    public static String getDisplayHxW(Context context) {
        try {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            String resolution = screenHeight + "_" + screenWidth;
            Log.d(TAG, resolution);
            return resolution;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获得屏幕的尺寸
     *
     * @param context
     *
     * @return 获得屏幕的尺寸
     */
    public static Point getDisplayMetrics(Context context) {
        Point dp = null;
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        dp = new Point(screenWidth, screenHeight);
        return dp;
    }

    /**
     * 获取真正的imei号 7.0.0版本增加
     *
     * @param context
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getmReallyImei(final Context context) {
        if (!TextUtils.isEmpty(mReallyImei)) {
            return mReallyImei;
        }

        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei;

        try {
            imei = tm.getDeviceId();// imei
        } catch (Exception e) {
            Log.e(TAG, "imei obtained exception", e);
            imei = "-";
        }
        if (TextUtils.isEmpty(imei)) {
            imei = "-";
        }
        mReallyImei = imei;
        return imei;
    }

    /**
     * 取得版本信息VersionName
     *
     * @param context
     *
     * @return 版本Name
     */
    public static String getVersionName(Context context) {
        String sPackageName = context.getPackageName();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(sPackageName, 0);
            return String.valueOf(pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Could not retrieve package info", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取应用名称
     *
     * @param context
     *
     * @return 版本Name
     */
    public static String getApplicationName(Context context) {
        if (context == null) {
            return "";
        }
        String sPackageName = context.getPackageName();
        try {
            PackageManager pm = context.getPackageManager();
            String applicationName = pm.getApplicationLabel(pm.getApplicationInfo(sPackageName, PackageManager.GET_META_DATA)).toString();
            return applicationName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Could not retrieve application info", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得版本信息VersionCode
     *
     * @param context
     *
     * @return 版本Name
     */
    public static int getVersionCode(Context context) {
        String sPackageName = context.getPackageName();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(sPackageName, 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Could not retrieve package info", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取string类型VersionCode
     *
     * @param context
     *
     * @return
     */
    public static String getVersionCodeString(Context context) {
        if(context == null){
            return "";
        }
        int versionCode = getVersionCode(context);
        String verStr = String.valueOf(versionCode);
        String result = "";
        char[] chars = verStr.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                result += chars[i] + ".";
            } else {
                result += chars[i];
            }
        }
        return result;
    }

    /**
     * 判断当前设备是否是模拟器
     *
     * @return MAC地址
     */

    /**
     * 获取屏幕的DisplayMetrics对象
     *
     * @return DisplayMetrics对象
     */
    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics mDm;

        mDm = new DisplayMetrics();

        return mDm;
    }

    private static String getStringIP(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    /**
     * 是否是模拟器 * * @return
     */
    @SuppressLint("MissingPermission")
    public static String getEmulatorMsg(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (imei != null && imei.equals("000000000000000")) {
                return "000000000000000";
            }
        } catch (Exception e) {
        }
        Log.d("DeviceUtils", "Build.PRODUCT:" + Build.PRODUCT
                + "\nBuild.MANUFACTURER:" + Build.MANUFACTURER
                + "\nBuild.BRAND:" + Build.BRAND
                + "\nBuild.DEVICE:" + Build.DEVICE
                + "\nBuild.MODEL:" + Build.MODEL
                + "\nBuild.HARDWARE:" + Build.HARDWARE
                + "\nBuild.FINGERPRINT:" + Build.FINGERPRINT);

        if ((Build.PRODUCT.equals("sdk"))
                || (Build.PRODUCT.equals("google_sdk"))
                || (Build.PRODUCT.equals("sdk_x86"))
                || (Build.PRODUCT.equals("vbox86p"))) {
            return "product_sdk";
        }

        if ((Build.MANUFACTURER.equals("unknown"))
                || (Build.MANUFACTURER.equals("Genymotion"))) {
            return "manufacturer_genymotion";
        }
        if ((Build.BRAND.equals("generic"))
                || (Build.BRAND.equals("generic_x86"))) {
            return "brand_generic";
        }

        if ((Build.DEVICE.equals("generic"))
                || (Build.DEVICE.equals("generic_x86"))
                || (Build.DEVICE.equals("vbox86p"))) {
            return "device_generic";
        }

        if ((Build.MODEL.equals("sdk"))
                || (Build.MODEL.equals("google_sdk"))
                || (Build.MODEL.equals("Android SDK built for x86"))) {
            return "model_sdk";
        }

        if ((Build.HARDWARE.equals("goldfish"))
                || (Build.HARDWARE.equals("vbox86"))) {
            return "hardware_goldfish";
        }

        if ((Build.FINGERPRINT.contains("generic/sdk/generic"))
                || (Build.FINGERPRINT.contains("generic_x86/sdk_x86/generic_x86"))
                || (Build.FINGERPRINT.contains("generic/google_sdk/generic"))
                || (Build.FINGERPRINT.contains("generic/vbox86p/vbox86p"))) {
            return "fingerprint_generic";
        }
        return "";
    }

    /**
     * 获取手机的IMEI号
     *
     * @return IMEI号
     */
    public static String getIMEI(Context context) {
        String imei = null;
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imei = telephonyManager.getDeviceId();
		} catch (Exception e) {

		}
		if (TextUtils.isEmpty(imei)) {
			return "";
		} else {
			return imei;
		}
    }

    /**
     * 从dip转化为px
     *
     * @param context
     * @param dipNum  要转换的dip值
     *
     * @return 转化后的像素值
     */
    public static int fromDipToPx(Context context, int dipNum) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dipNum * dm.densityDpi / 160;
    }

    /**
     * 从dip转化为px
     *
     * @param context
     * @param dipNum  要转换的dip值
     *
     * @return 转化后的像素值
     */
    public static float fromDipToPx(Context context, float dipNum) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dipNum * dm.densityDpi / 160;
    }

    /**
     * 取得density比例
     *
     * @param context
     *
     * @return 当前设备Density的比例
     */
    public static float getDensityScale(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.densityDpi * 1.0f / 160;
    }

    /**
     * 获取设备的类型
     *
     * @param context
     *
     * @return 设备的类型phoneType，如"GSM"
     */
    public static final String getPhoneType(Context context) {
        String phoneType = null;

        try {
            TelephonyManager tel = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            int type = tel.getPhoneType();
            switch (type) {
                case TelephonyManager.PHONE_TYPE_GSM: {
                    phoneType = "GSM";
                }
                break;
                case TelephonyManager.PHONE_TYPE_CDMA: {
                    phoneType = "CDMA";
                }
                break;
                case TelephonyManager.PHONE_TYPE_NONE: {
                    phoneType = "NONE";
                }
                break;
                default: {
                    phoneType = String.valueOf(tel.getPhoneType());
                }
                break;
            }

        } catch (Exception e) {
            phoneType = "error";
        }

        return phoneType;
    }

    /**
     * 获取sim卡运营商的类型
     *
     * @param context
     *
     * @return 获取sim卡运营商的类型"ct"
     */
    public static final String getSimOperatorType(Context context) {
        String simOperatorType = null;

        try {
            TelephonyManager tel = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            simOperatorType = tel.getSimOperator();
            if ("46000".equals(simOperatorType)) {
                simOperatorType = "cm";
            } else if ("46001".equals(simOperatorType)) {
                simOperatorType = "cuni";
            } else if ("46002".equals(simOperatorType)) {
                simOperatorType = "cm2";
            } else if ("46003".equals(simOperatorType)) {
                simOperatorType = "ct";
            }

        } catch (Exception e) {
            simOperatorType = "error";
        }

        return simOperatorType;
    }

    /**
     * 获取网通电信等运营商数据，但是上面方法不一样
     *
     * @param context
     */
    @SuppressLint("MissingPermission")
    public static String getCellInfo(Context context) {
        StringBuilder stringBuffer = new StringBuilder();
        /** 调用API获取基站信息 */
        try {
            /*TelephonyManager mTelephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            // 返回值MCC + MNC
            String operator = mTelephonyManager.getNetworkOperator();*/
            TelephonyManager mTelephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            String operator = mTelephonyManager.getSimOperator();
            if (TextUtils.isEmpty(operator)) {
                return null;
            }
            // int mcc = Integer.parseInt(operator.substring(0, 3));
            // int mnc = Integer.parseInt(operator.substring(3));

            CellLocation cellLocation = null;
            int lac = 0;
            int cellId = 0;
            stringBuffer.append("");
            switch (Integer.valueOf(operator)) {
                // 中国移动
                case 46000:
                case 46002:
                case 46007:
                    // 中国联通
                case 46001:
                    cellLocation = mTelephonyManager.getCellLocation();
                    if (cellLocation instanceof GsmCellLocation) {
                        lac = ((GsmCellLocation) cellLocation).getLac();
                        cellId = ((GsmCellLocation) cellLocation).getCid();
                    }
                    break;
                // 中国电信
                case 46003:
                    cellLocation = mTelephonyManager.getCellLocation();
                    if (cellLocation instanceof CdmaCellLocation) {
                        lac = ((CdmaCellLocation) cellLocation).getNetworkId();
                        cellId = ((CdmaCellLocation) cellLocation).getBaseStationId();
                        cellId /= 16;
                    }
                    break;
            }

            stringBuffer.append(lac);
            stringBuffer.append(cellId);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    public static String getAndroidId(Context context) {
        String androidId = "";
        try {
            //ANDROID_ID是设备第一次启动时产生和存储的64bit的一个数，当设备被wipe后该数重置
            //它在Android <=2.1 or Android >=2.3的版本是可靠、稳定的，但在2.2的版本并不是100%可靠的
            //在主流厂商生产的设备上，有一个很经常的bug，就是每个设备都会产生相同的ANDROID_ID：9774d56d682e549c
            androidId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        } catch (Exception e) {
        }
        return androidId;
    }

    public static String getAndroidID(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            return "";
        }
    }

    private static String md5(String string) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 获取设备语言
     */
    public static String getDeivceLanguage() {
        String language = "";
        try {
            language = Locale.getDefault().getLanguage();
        } catch (Exception e) {
        }
        return language;
    }

    /**
     * 获取设备所在国家
     */
    public static String getDeviceCountry() {
        String country = "";
        try {
            country = Locale.getDefault().getCountry();
        } catch (Exception e) {
        }
        return country;
    }

    /**
     * 获取设备所在时区
     */
    public static String getTimeZone() {
        String timeZone = "";
        try {
            timeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
        } catch (Exception e) {
        }
        return timeZone;
    }

    /**
     * 获取CPU序列号
     *
     * @return CPU序列号(16位)
     * 读取失败为"0000000000000000"
     */
    public static String getCPUSerial() {
        String str = "", strCPU = "", cpuAddress = "0000000000000000";
        InputStreamReader ir = null;
        InputStream inputStream = null;
        LineNumberReader input = null;
        try {
            //读取CPU信息
            Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            inputStream = pp.getInputStream();
            ir = new InputStreamReader(inputStream);
            input = new LineNumberReader(ir);
            //查找CPU序列号
            for (int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    //查找到序列号所在行
                    if (str.contains("Serial")) {
                        //提取序列号
                        strCPU = str.substring(str.indexOf(":") + 1
                        );
                        //去空格
                        cpuAddress = strCPU.trim();
                        break;
                    }
                } else {
                    //文件结尾
                    break;
                }
            }
        } catch (Exception ex) {
            //赋予默认值
        } finally {
            FileUtils.close(input, ir, inputStream);
        }
        return cpuAddress;
    }

    /**
     * 获取设备语言
     */
    public static String getDeviceMemory(Context context) {
        String memoryInfo = "";
        try {
            final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            if (info == null) {
                return "";
            }
            activityManager.getMemoryInfo(info);
            memoryInfo = (info.availMem >> 10) + "k";
        } catch (Exception e) {
        }
        return memoryInfo;
    }

    /**
     * 判断手机是否root
     */
    public static boolean isDeviceRoot() {
        Process process = null;
        BufferedReader in = null;
        InputStreamReader inputStreamReader = null;
        InputStream inputStream = null;
        try {
            String buildTags = Build.TAGS;
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true;
            }
            String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                    "/system/bin/failsafe/su", "/data/local/su"};
            for (String path : paths) {
                if (new File(path).exists()) {
                    return true;
                }
            }
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            inputStream = process.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            in = new BufferedReader(inputStreamReader);
            return (in.readLine() != null);
        } catch (Exception e) {
        } finally {
            if (process != null) process.destroy();
            FileUtils.close(in, inputStreamReader, inputStream);
        }
        return false;
    }

    public static String getSignMd5Str(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            String ms5SignStr = encryptionMD5(sign.toByteArray());
            return ms5SignStr;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * MD5加密
     *
     * @param byteStr 需要加密的内容
     *
     * @return 返回 byteStr的md5值
     */
    public static String encryptionMD5(byte[] byteStr) {
        MessageDigest messageDigest = null;
        StringBuilder md5StrBuff = new StringBuilder();
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(byteStr);
            byte[] byteArray = messageDigest.digest();
//            return Base64.encodeToString(byteArray,Base64.NO_WRAP);
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5StrBuff.toString();
    }

    /**
     * 获取当前操作系统版本
     *
     * @return 操作系统版本
     */
    public static String getOsVersion() {
        try {
            String osVersion = URLEncoder.encode(Build.VERSION.RELEASE, "utf-8");
            return osVersion == null ? "-1" : osVersion;
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getModel() {
        String model;
        try {
            model = Build.MODEL;
        } catch (Exception e) {
            model = "-1";
        }
        return model == null ? "-1" : model;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getBrand() {
        String brand;
        try {
            brand = Build.BRAND;
        } catch (Exception e) {
            brand = "-1";
        }
        return brand == null ? "-1" : brand;
    }

    public static int getStatusBarHeight(Context context) {
        try {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            int height = resources.getDimensionPixelSize(resourceId);
            Log.v("dbw", "Status height:" + height);
            return height;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getNavigationBarHeight(Context context) {
        try {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            int height = resources.getDimensionPixelSize(resourceId);
            Log.v("dbw", "Navi height:" + height);
            return height;
        } catch (Exception e) {
            return 0;
        }
    }

    //获取UA头
    public static String getSysUserAgent(){
        String userAgent = "";
        try {
            userAgent = System.getProperty("http.agent");
        }catch (Exception e){
            Log.d(TAG,"getSysUserAgent",e);
        }
        return userAgent;
    }

}

