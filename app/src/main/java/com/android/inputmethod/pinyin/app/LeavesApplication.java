package com.android.inputmethod.pinyin.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LeavesApplication extends Application {
    public static Context applicationContext;
    private static LeavesApplication instance;
    private static OkHttpUtils mOkHttpUtils;


    private static final String TAG = "LeavesApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext=this;
        instance=this;

        initOKHTTP();


    }

    public static LeavesApplication getInstance() {
        return instance;
    }

    public static OkHttpUtils getOKHTTPUtils() {
        if (null == mOkHttpUtils) {
            initOKHTTP();
        }
        return mOkHttpUtils;
    }

    private static void initOKHTTP() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(100000L, TimeUnit.MILLISECONDS)
                .readTimeout(100000L, TimeUnit.MILLISECONDS)
                .build();

        mOkHttpUtils = OkHttpUtils.initClient(okHttpClient);
    }
}
