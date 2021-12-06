package io.github.mzdluo123.mirai.android.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class DeviceStatus {
    public static String getSystemAvaialbeMemorySize(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 获得MemoryInfo对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        // 获得系统可用内存，保存在MemoryInfo对象上
        mActivityManager.getMemoryInfo(memoryInfo);
        long memSize = memoryInfo.availMem;
        // 字符类型转换
        String availMemStr = Formatter.formatFileSize(context, memSize);// 调用系统函数，字符串转换 long -String KB/MB
        return availMemStr;
    }

    public static String getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        String initial_memory = "";
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("//s+");
            String mom = arrayOfString[0].split(":")[1].split("kB")[0];
            initial_memory = Formatter.formatFileSize(context, Integer.valueOf(mom.trim()).intValue() * 1024); // 获得系统总内存，单位是MB，转换为GB
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return initial_memory;// Byte转换为KB或者MB，内存大小规格化
    }


    enum NetState {
        WIFI("wifi", 1), CDMA("2G", 2), UMTS("3G", 3), LTE("4G", 4), UNKOWN("unkonw", 5);
        private int state;
        private String type;

        NetState(String type, int state) {
            this.state = state;
            this.type = type;
        }
    }

    public static String getCurrentNetType(Context context) {
        // String type = "unknown";
        int state = NetState.UNKOWN.state;
        String type = NetState.UNKOWN.type;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info == null) {
            // type = "unknown";
            state = NetState.UNKOWN.state;
            ;
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            // type = "wifi";
            state = NetState.WIFI.state;
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS
                    || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                // type = "2g";
                state = NetState.CDMA.state;
            } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                // type = "3g";
                state = NetState.UMTS.state;
            } else {// LTE是3g到4g的过渡，是3.9G的全球标准 if (subType ==
                // TelephonyManager.NETWORK_TYPE_LTE)
                // type = "4g";
                state = NetState.LTE.state;
            }
        }
        switch (state) {
            case 1:
                type = NetState.WIFI.type;
                break;
            case 2:
                type = NetState.CDMA.type;
                break;
            case 3:
                type = NetState.UMTS.type;
                break;
            case 4:
                type = NetState.LTE.type;
                break;
            case 5:
            default:
                type = NetState.UNKOWN.type;
                break;
        }
        return type;

    }

}
