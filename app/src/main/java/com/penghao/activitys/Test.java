package com.penghao.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.penghao.file_plroe.R;

/**
 * Created by penghao on 18-2-17.
 */

public class Test extends Activity {

    WebView webView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        webView=findViewById(R.id.about_avtivity_webview);
        webView.loadUrl("file:///android_asset/File_plore_about.mkd");
    }
}
