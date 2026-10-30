package com.shiyue.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.Window;
import android.view.KeyEvent;

public class MainActivity extends Activity {
    private WebView mWebView;

    public class ShiYueBridge {
        Context mContext;

        ShiYueBridge(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void updateWidget(String title, String days, String label, String subLabel, String target) {
            SharedPreferences prefs = mContext.getSharedPreferences("ShiYueWidgetPrefs", Context.MODE_PRIVATE);
            prefs.edit()
                .putString("title", title)
                .putString("days", days)
                .putString("label", label)
                .putString("subLabel", subLabel)
                .putString("target", target)
                .apply();

            ShiYueWidgetProvider.updateAllWidgets(mContext);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mWebView = new WebView(this);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        mWebView.addJavascriptInterface(new ShiYueBridge(this), "ShiYueNative");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.loadUrl("file:///android_asset/index.html");
        setContentView(mWebView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
