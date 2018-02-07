package com.penghao.activitys;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.penghao.file_plroe.R;

import java.io.File;
import java.io.IOException;

public class AudioActivity extends Activity
{
	TextView alreadyTime;
	TextView totalTime;
	TextView musicName;
	SeekBar seek;
	ImageView play;
	String musicPath;
	int progress;
	MediaPlayer player=new MediaPlayer();
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_activity);
		init();
	}
	
	public void init(){
		Intent intent=getIntent();
		Uri uri=intent.getData();
		musicPath=uri.getPath();

		try
		{
			player.setDataSource(musicPath);
			player.prepare();
			player.start();
		}
		catch (IOException e)
		{}
		
		alreadyTime=(TextView) findViewById(R.id.audio_activityTextView1);
		totalTime=(TextView) findViewById(R.id.audio_activityTextView2);
		musicName=(TextView) findViewById(R.id.audio_activityTextView3);
		play=(ImageView) findViewById(R.id.audio_activityImageView);
		seek=(SeekBar) findViewById(R.id.audio_activitySeekBar);
		
		musicName.setText(new File(musicPath).getName());
		totalTime.setText(formatMusicTime(player.getDuration()));
		seek.setMax(player.getDuration());
		seek.setOnSeekBarChangeListener(seekListener);
		play.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if(player.isPlaying()){
						play.setImageResource(android.R.drawable.ic_media_play);
						player.pause();
					}else{
						play.setImageResource(android.R.drawable.ic_media_pause);
						player.start();
					}
				}
			});
		new Thread(){
			public void run(){
				while(true){
					seek.setProgress(player.getCurrentPosition());
					try
					{
						sleep(200);
					}
					catch (InterruptedException e)
					{}
				}
			}
		}.start();
	}
	
	SeekBar.OnSeekBarChangeListener seekListener=new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar p1, int p2, boolean p3)
		{
			alreadyTime.setText(formatMusicTime(p2));
		}

		@Override
		public void onStartTrackingTouch(SeekBar p1)
		{
			// TODO: Implement this method
		}

		@Override
		public void onStopTrackingTouch(SeekBar p1)
		{
			player.seekTo(p1.getProgress());
		}
	};
	
	//格式化音乐时间
	public String formatMusicTime(int i){
		int min=(i-i%60)/60000,//分钟
			second=(i/1000)%60;//秒钟
		return min+":"+second;//运算结果
	}

	@Override
	protected void onDestroy()
	{
		player.stop();
		player.release();
		super.onDestroy();
	}
	
}
