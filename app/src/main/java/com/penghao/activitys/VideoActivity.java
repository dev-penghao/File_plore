package com.penghao.activitys;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.penghao.file_plroe.R;

public class VideoActivity extends Activity {

	VideoView video;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_activity);
		video=(VideoView) findViewById(R.id.vedio_activityVideoView);
		MediaController mediaController = new MediaController(this);
        video.setMediaController(mediaController);
        mediaController.setMediaPlayer(video);
        //为videoView设置视频路径
        Intent intent=getIntent();
		Uri uri=intent.getData();
        video.setVideoPath(uri.getPath());
	}
	
}
