package com.penghao.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.penghao.views.MyEditText;

import java.io.File;

/**
 * Created by penghao on 18-1-22.
 */

public class TextActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyEditText(TextActivity.this,new File(getIntent().getData().getPath())));
    }
}