package com.penghao.activitys;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.penghao.views.ZoomImageView;

public class ImageActivity extends Activity
{
	ZoomImageView zoomimg;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent intent=getIntent();
		Uri uri=intent.getData();
		Bitmap bitmap= BitmapFactory.decodeFile(uri.getPath());
		zoomimg=new ZoomImageView(this,bitmap);
		setContentView(zoomimg);
	}
	
}
