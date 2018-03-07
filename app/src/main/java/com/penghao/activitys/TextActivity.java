package com.penghao.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.penghao.file_plroe.R;
import com.penghao.views.MyEditText;

import java.io.File;

/**
 * Created by penghao on 18-1-22.
 */

public class TextActivity extends Activity {

    MyEditText myEditText;
    public static View view;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view= LayoutInflater.from(this).inflate(R.layout.edittext_activity,null);
        setContentView(view);
        myEditText=view.findViewById(R.id.edittext_activity_myedittext);
        File file=new File(getIntent().getData().getPath());
        if (file!=null){
            myEditText.setFile(file);
        }
    }
}