package com.penghao.activitys

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.penghao.file_plroe.R

class AboutActivity : AppCompatActivity() {

    lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)
        webView = findViewById(R.id.about_avtivity_webview)
        webView.loadUrl("file:///android_asset/File_plore_about.mkd")
    }
}