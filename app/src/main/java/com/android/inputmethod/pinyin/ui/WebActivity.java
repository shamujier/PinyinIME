package com.android.inputmethod.pinyin.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.android.inputmethod.pinyin.R;

public class WebActivity extends Activity {
    WebView webView;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Intent intent = getIntent();
        if (intent != null) {
            url = intent.getStringExtra("URL");
        }else{
            finish();
        }

        initView();
//        initWebView();
        initWeb();
    }




    private void initView() {
        webView=findViewById(R.id.webview);
    }

    private void initWeb() {
        webView.getSettings().setJavaScriptEnabled(true);//getSettiongs()用于设置一些浏览器属性，这里让WebView支持JavaScript脚本
        webView.setWebViewClient(new WebViewClient());//当需要从一个网页跳转到另一个网页是，希望目标网页仍然在当前WebView显示，而不是打开浏览器
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);//开启DOM
        settings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        webView.loadUrl(url);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initWebView() {
        WebSettings webSetting = webView.getSettings();
        // 设置JS脚本是否允许自动打开弹窗
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置在WebView内部是否允许访问文件
        webSetting.setAllowFileAccess(true);
        // 设置在WebView宽度自适应(NARROW_COLUMNS表示:尽可能使所有列的宽度不超过屏幕宽度)
        //webSetting.setLayoutAlgorithm(contextLayoutAlgorithm.NARROW_COLUMNS);
        // 使WebView支持缩放
        webSetting.setSupportZoom(true);
        // 启用WebView内置缩放功能
        webSetting.setBuiltInZoomControls(true);
        // 使WebView支持可任意比例缩放
        webSetting.setUseWideViewPort(true);
        // 设置WebView支持打开多窗口
        webSetting.setSupportMultipleWindows(true);
        // 开启Application H5 Caches 功能
        webSetting.setAppCacheEnabled(true);
        // 设置最大缓存大小
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // 不使用缓存，每次都从网络上获取
        // 四种模式可选:LOAD_DEFAULT, LOAD_CACHE_ONLY, LOAD_NO_CACHE, LOAD_CACHE_ELSE_NETWORK
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 让WebView支持DOM storage API
        webSetting.setDomStorageEnabled(true);
        // 启用定位功能
        webSetting.setGeolocationEnabled(true);
        // 让WebView支持播放插件
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // 缩放至屏幕的大小
        webSetting.setLoadWithOverviewMode(true);
        // WebView启用javascript支持
        webSetting.setJavaScriptEnabled(true);

        // 设置混合加载,解决https页面嵌入了http的链接问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.setMixedContentMode(0);
        }
        // 启用第三方cookie,解决iframe跨域问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        //响应 window.close
        // webView.setWebChromeClient(new BaseWebChromeClient());


        webView.setWebChromeClient(new WebChromeClient() {

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String
                    acceptType) {
                Log.i("test", "openFileChooser 1");
            }

            // For Android  > 4.1.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String
                    acceptType, String capture) {
                Log.i("test", "openFileChooser 3");
            }

            // For Android  >= 5.0
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                Log.i("test", "openFileChooser 4:" + filePathCallback.toString());
                return true;
            }

       /*     public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);

            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
;
                super.onPageStarted(view, url, favicon);

            }*/
        });


        webView.loadUrl(url);
    }
}
