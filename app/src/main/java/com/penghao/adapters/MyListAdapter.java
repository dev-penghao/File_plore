package com.penghao.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.penghao.file_plroe.MainActivity;
import com.penghao.file_plroe.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyListAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    Context context;
    public MyListAdapter(Context context){
        this.context=context;
        mInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        // TODO: Implement this method
        return MainActivity.list.size();
    }

    @Override
    public Object getItem(int p1)
    {
        // TODO: Implement this method
        return null;
    }

    @Override
    public long getItemId(int p1)
    {
        // TODO: Implement this method
        return 0;
    }

    @Override
    public View getView(int p1, View p2, ViewGroup p3)
    {
        p2=mInflater.inflate(R.layout.file_item,null);
        TextView t1 = p2.findViewById(R.id.fileitemTextView1);
        TextView t2 =p2.findViewById(R.id.file_itemTextView2);
        ImageView img=p2.findViewById(R.id.file_itemImageView);
        t1.setTextColor(Color.BLACK);
        File file=MainActivity.list.get(p1);
        t1.setText(file.getName());
        if(file.isFile()){
            //显示文件的大小
            long size=file.length();
            String details;
            DecimalFormat df=new DecimalFormat("#.00");
            if(size<1000){//如果文件小于1KB
                details=size+"B";
            }else if(size<1000000){//如果文件小于1MB
                details=df.format(((float)size)/1024)+"KB";
            }else if(size<1000000000){//如果文件小于1GB
                details=df.format(((float)size)/1024/1024)+"MB";
            }else{//如果文件大于1GB
                details=df.format(((float)size)/1024/1024/1024)+"GB";
            }
            //显示文件可读可写可运行
            details=details+"				";
            if(file.canRead()){
                details=details+"r";
            }else{
                details=details+"-";
            }
            if(file.canWrite()){
                details=details+"w";
            }else{
                details=details+"-";
            }
            if(file.canExecute()){
                details=details+"x";
            }else{
                details=details+"-";
            }
            //显示文件的日期
            details=details+"				";
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            Date date=new Date(file.lastModified());
            details=details+sdf.format(date);
            t2.setText(details);

            //为不同类型的文件设置不同的图标
            String fileName=file.getName();
            String prefix=fileName.substring(fileName.lastIndexOf(".")+1);
            if(prefix.equals("txt")){
                img.setImageResource(R.drawable.format_text);
            }else if(prefix.equals("zip")){
                img.setImageResource(R.drawable.format_zip);
            }else if(prefix.equals("mp3")){
                img.setImageResource(R.drawable.format_music);
            }else if(prefix.equals("apk")){
                img.setImageResource(R.drawable.format_app);
            }else if(prefix.equals("pdf")){
                img.setImageResource(R.drawable.format_pdf);
            }else if(prefix.equals("html")|prefix.equals("mht")){
                img.setImageResource(R.drawable.format_html);
            }else{
                img.setImageResource(R.drawable.format_unkown);
            }
        }else{
            img.setImageResource(R.drawable.format_folder_smartlock);
            t2.setText(file.listFiles().length+"项");
        }

        if(MainActivity.listview.isItemChecked(p1)) {
            t1.setBackgroundColor(Color.RED);
        } else {
            t1.setBackgroundColor(Color.TRANSPARENT);
        }
        return p2;
    }

}