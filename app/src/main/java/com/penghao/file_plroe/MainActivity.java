/**
 *
 *----------Dragon be here!----------/
 *  　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　∪　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃神兽保佑
 * 　　　　┃　　　┃代码无BUG！
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━神兽出没━━━━━━by:coder-pig
 */

package com.penghao.file_plroe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.penghao.activitys.SettingsActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    ListView listview;
    List<File> dirList=new ArrayList<>();
    List<File> fileList=new ArrayList<>();
    List<File> list=new ArrayList<>();//list用于dir与file之并
    List<File> shearPlate=new ArrayList<>();//剪切板;修饰为static，要为其他类共享剪切板
    List<Integer> lvPosition=new ArrayList<>();//listview的第一个可见项的位置
    int lvTop=0;//listview第一个可见项距顶部的距离
    ListAdapter adapter;
    SwipeRefreshLayout swipeRefresh;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    static final String HOMEPATH="/sdcard";
    static String nowPath=HOMEPATH;//当前所处的路径
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();

    }

    public void init(){
        listview=findViewById(R.id.mainListView);
        swipeRefresh=findViewById(R.id.main_srl);
        toolbar=findViewById(R.id.main_toolbar);
        drawerLayout=findViewById(R.id.dl_left);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        toolbar.setTitle("文件管理");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.main_createNewFile:
                        createFileDialog();
                        break;
                    case R.id.main_paste:
                        paste();
                        break;
                    case R.id.main_finish:
                        finish();
                        break;
                    case R.id.main_search:
                        showSearchDialog();
                        break;
                    case R.id.main_settings:
                        Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        drawerToggle=new ActionBarDrawerToggle(MainActivity.this,drawerLayout,toolbar,0,0){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerToggle.syncState();
        drawerLayout.setDrawerListener(drawerToggle);
        listFile(new File(HOMEPATH),false);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listview.setMultiChoiceModeListener(choiceMode);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
            {
                File f=list.get(p3);

                if(f.isDirectory()){		//如果是文件夹就进入
                    lvPosition.add(listview.getFirstVisiblePosition());//
                    lvTop=listview.getChildAt(0).getTop();
                    nowPath=f.getAbsolutePath();
                    listFile(f,false);
                }else{		//如果是文件则执行打开操作
                    String fileName=f.getName();
                    String prefix=fileName.substring(fileName.lastIndexOf(".")+1);		//prefix: 文件的后辍名

                    Intent intent = new Intent("android.intent.action.VIEW");
                    Uri uri = Uri.fromFile(f);		//要打开的文件的路径
                    if(cmpprefix(prefix,getResources().getStringArray(R.array.Text))){
                        intent.setDataAndType(uri,"text/plain");
                    }else if(cmpprefix(prefix,getResources().getStringArray(R.array.Package))){
                        intent.setDataAndType(uri,"application/zip");
                    }else if(cmpprefix(prefix,getResources().getStringArray(R.array.Audio))){
                        intent.setDataAndType(uri,"audio/*");
                    }else if(prefix.equals("apk")){
                        intent.setDataAndType(uri,  "application/vnd.android.package-archive");
                    }else if(prefix.equals("pdf")){
                        intent.setDataAndType(uri, "application/pdf");
                    }else if(prefix.equals("html")|prefix.equals("mht")){
                        intent.setDataAndType(uri, "text/html");
                    }else if(cmpprefix(prefix,getResources().getStringArray(R.array.Image))){
                        intent.setDataAndType(uri, "image/*");
                    }

                    startActivity(intent);
                }

            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
            {

                return true;
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){

            @Override
            public void onRefresh()
            {
                flush();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    AbsListView.MultiChoiceModeListener choiceMode = new AbsListView.MultiChoiceModeListener(){
        View actionBarView;

        @Override
        public boolean onCreateActionMode(ActionMode p1, Menu p2)
        {
            //getMenuInflater().inflate(R.menu.actionbar_menu, p2);
            if (actionBarView == null){
                actionBarView = LayoutInflater.from(MainActivity.this).inflate(R.layout.actionbar_view, null);
                //selectedNum = (TextView)actionBarView.findViewById(R.id.selected_num);
            }
            actionBarView.findViewById(R.id.actionbarviewButton_cp).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View p1)
                {
                    shearPlate.clear();
                    shearPlate.add(new File("forCopy"));
                    addFile();
                }
            });
            actionBarView.findViewById(R.id.actionbarviewButton_cut).setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View p1)
                {
                    shearPlate.clear();
                    shearPlate.add(new File("forCut"));
                    addFile();
                }
            });
            actionBarView.findViewById(R.id.actionbarviewButton_del).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View p1)
                {
                    shearPlate.clear();
                    addFile();
                    new CopyFolderDialog(MainActivity.this,1);
                }
            });
            toolbar.setVisibility(View.GONE);
            p1.setCustomView(actionBarView);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode p1, Menu p2)
        {
            // TODO: Implement this method
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode p1, MenuItem p2)
        {
            // TODO: Implement this method
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode p1)
        {
            toolbar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode p1, int p2, long p3, boolean p4)
        {
            // TODO: Implement this method
            adapter.notifyDataSetChanged();
        }

        public void addFile(){
            for(int i=0;i<list.size();i++){
            if(listview.isItemChecked(i)){
                shearPlate.add(list.get(i));
            }
        }
        }
    };

    //如果数组name中有元素和prefix相等，则返回true
    //用于点击文件时判断文件类型
    public boolean cmpprefix(String prefix,String[] name){
        boolean b=false;
        for(String i:name){
        if(prefix.equals(i)){
            b=true;
            break;
        }
    }
        return b;
    }

    public void listFile(File f,boolean isBack){
        if(f.isDirectory()){
            dirList.clear();fileList.clear();list.clear();
            File[] fs=f.listFiles();
            //fs可能为null!
            if (fs==null){
                return;
            }
            for(int i=0;i<fs.length;i++){
                if(fs[i].isDirectory()){
                    dirList.add(fs[i]);
                }else{
                    fileList.add(fs[i]);
                }
            }
            sortByName(dirList);
            sortByName(fileList);
            for(int i=0;i<dirList.size();i++){
                list.add(dirList.get(i));
            }
            for(int i=0;i<fileList.size();i++){
                list.add(fileList.get(i));
            }
            //让listview显示
            adapter=new ListAdapter();
            listview.setAdapter(adapter);
            if (isBack){
                listview.setSelection(lvPosition.get(lvPosition.size()-1));
                lvPosition.remove(lvPosition.size()-1);
            }

        }
    }

    //传入文件集合，对其按文件名排序返回值是处理好后的文件集合
    public List<File> sortByName(List<File> files){
        //要判断集合中是否有元素，对一个空集排序会导致异常进而程序崩溃
        if(files.isEmpty()){
            return files;
        }
        //排序前准备
        List<String> filesName = new ArrayList<>();
        String parent=files.get(0).getParent()+"/";//文件集合中所有元素的父路径
        for(File file:files){
        filesName.add(file.getName());
    }
        //开始排序
        Comparator<Object> com= Collator.getInstance(java.util.Locale.CHINA);
        Collections.sort(filesName, com);
        //排好序后以文件名来恢复原来的文件集合
        File f;
        files.clear();
        for(String name:filesName){
        f=new File(parent+name);
        files.add(f);
    }
        return files;
    }

    //传入文件集合，对其按大小排序
    public List<File> sortBySize(List<File> input){
        int i=0;
        int index=input.size();
        while(true) {
            if(input.get(i).length()>input.get(i+1).length()){
                File tem;
                tem=input.get(i);
                input.set(i,input.get(i+1));
                input.set(i+1,tem);
                i=0;
            }else {
                i++;
                if(i+1==index) {
                    break;
                }
            }
        }
        return input;
    }

    //传入文件集合，对其按日期排序
    public void sortByTime(){

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(nowPath.equals(HOMEPATH)){
                finish();
            }else{

                File f=new File(nowPath);
                listFile(f.getParentFile(),true);
                nowPath=f.getParent();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()){
            case R.id.main_createNewFile:
            createFileDialog();
            break;
            case R.id.main_paste:
            paste();
            break;
            case R.id.main_finish:
            finish();
            break;
            default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void paste(){
        if(shearPlate.isEmpty()){
            showToast(this,"剪切板为空！");
            return;
        }
        if(shearPlate.get(0).getName().equals("forCopy")){
            if(shearPlate.size()==2&&shearPlate.get(1).isFile()){
                new CopyFileDialog(MainActivity.this,shearPlate.get(1));
            }else{
                new CopyFolderDialog(MainActivity.this,0);
            }

        }else if(shearPlate.get(0).getName().equals("forCut")){
            for(int i=1;i<shearPlate.size();i++){
                shearPlate.get(i).renameTo(new File(nowPath+"/"+shearPlate.get(i).getName()));
            }
        }else{
            showToast(this,"错误");
        }
        //shearPlate.clear();
        flush();
    }

    public void showSearchDialog(){
        View view=LayoutInflater.from(MainActivity.this).inflate(R.layout.search_dialog,null);
        EditText editText=view.findViewById(R.id.search_dialog_editText);
        CheckBox checkBox=view.findViewById(R.id.search_dialog_checkBox);
        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
        dialog.setView(view);
        dialog.setPositiveButton("查找", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    //新建按钮弹出对话框
    public void createFileDialog(){
        final EditText editText=new EditText(this);
        AlertDialog.Builder adialog=new AlertDialog.Builder(this);
        adialog.setTitle("输入文件名");
        adialog.setView(editText);
        adialog.setPositiveButton("新建空文件", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                try
                {
                    new File(nowPath + "/" + editText.getText()).createNewFile();
                }
                catch (IOException e)
                {}
                flush();
            }
        });
        adialog.setNegativeButton("新建文件夹", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                new File(nowPath+"/"+editText.getText()).mkdir();
                flush();
            }
        });
        adialog.show();
    }

    public void flush(){
        listFile(new File(nowPath),false);
    }

    public static void showToast(Context context, String str){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }
    //自定义的CopyFileDialog类对复制文件的过程进行了封装
    //要复制文件时，只需在构造方法中传入源文件即可
    class CopyFileDialog extends ProgressDialog {

    private long progress;
    private long total;
    private boolean cancel=true;
    private Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            cancel();
            flush();
            super.handleMessage(msg);
        }
    };
    public CopyFileDialog(Context context,final File source){
    super(context);//不知为何，此句必须放在该方法的第一句
    if(source==null){
        return;
    }
    total=source.length();
    initView();
    new Thread(new Runnable(){

        @Override
        public void run()
        {
            copyFile(source,new File(nowPath+"/"+source.getName()));
            handler.sendEmptyMessage(0);
        }
    }).start();
}

    private void initView(){
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        setMax(1000);
        getButton(1);
        setButton("取消", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                cancel=false;
            }
        });
        show();
    }

    //复制文件，original是源文件，object是目标文件
    private long copyFile(File original,File object) {
    long time=new Date().getTime();
    int bufferSize=1024*1024*2;//2MB
    try{
        FileInputStream in=new FileInputStream(original);
        FileOutputStream out=new FileOutputStream(object);
        byte[] buffer=new byte[bufferSize];
        int ins;
        while((ins=in.read(buffer))!=-1&&cancel){
            out.write(buffer,0,ins);
            progress=progress+bufferSize;
            setProgress((int)((((float)progress)/(float)total)*1000));
        }
        in.close();
        out.flush();
        out.close();
        return new Date().getTime()-time;
    }catch(IOException e){}
    return 0;
}
}

    class CopyFolderDialog extends AlertDialog{

    Context context;
    ProgressBar progressbar;
    TextView text1;
    TextView text2;
    int numFile;
    int numFloder;
    int max;
    int mode;
    Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            text1.setText("文件夹:"+numFloder);
            text2.setText("文件:"+numFile);
            progressbar.setProgress(numFile+numFloder);
            super.handleMessage(msg);
        }
    };

    public CopyFolderDialog(Context context,int mode){
    super(context);
    this.context=context;
    this.mode=mode;
    initView();
}

    private void initView()
    {
        View view=LayoutInflater.from(context).inflate(R.layout.copyfolderdialog_view,null);
        progressbar=(ProgressBar) view.findViewById(R.id.copyfolderdialogviewProgressBar1);
        text1=(TextView) view.findViewById(R.id.copyfolderdialogviewTextView1);
        text2=(TextView) view.findViewById(R.id.copyfolderdialogviewTextView2);
        setView(view);
        getButton(1);
        setButton("取消", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface p1, int p2)
            {

            }
        });
        show();
        new Thread(new Runnable(){

            @Override
            public void run()
            {
                //统计剪切板中包含的文件数
                for(File f:shearPlate){
                    if(f.isDirectory()){
                        countFile(f);
                        numFloder++;
                    }else{
                        numFile++;
                    }
                    handler.sendEmptyMessage(0);
                }
                max=numFile+numFloder;
                progressbar.setMax(max);//为进度条设置最大值，从此进度条开始递减
                if(mode==0){//如果mode等于0，那么是复制模式
                    //统计完成后开始复制，注意这里是从1开始循环的
                    for(int i=1;i<shearPlate.size();i++){
                        if(shearPlate.get(i).isDirectory()){
                            File tem_file=new File(nowPath+"/"+shearPlate.get(i).getName());
                            nowPath=tem_file.getAbsolutePath();
                            tem_file.mkdir();
                            copyFolder(shearPlate.get(i));
                            nowPath=tem_file.getParent();
                            numFloder--;
                        }else{
                            copyFile(shearPlate.get(i),new File(nowPath+"/"+shearPlate.get(i).getName()));
                            numFile--;
                        }
                        handler.sendEmptyMessage(0);
                    }
                }else{//否则是删除模式
                    for(File f:shearPlate){
                        delete(f);
                    }
                }
                cancel();
            }
        }).start();
    }

    //复制文件夹，file是源，nowPath是目标的路径
    public void copyFolder(File file){
        if (file.isDirectory()){
            File[] files=file.listFiles();
            File tem_file;
            for (File f:files){
                if (f.isDirectory()){
                    tem_file=new File(nowPath+"/"+f.getName());
                    tem_file.mkdir();
                    nowPath=tem_file.getAbsolutePath();
                    copyFolder(f);
                    numFloder--;
                    nowPath=new File(nowPath).getParent();
                }else{
                    copyFile(f,new File(nowPath+"/"+f.getName()));
                    numFile--;
                }
                handler.sendEmptyMessage(0);
            }
        }
    }
    //复制文件，original是源文件，object是目标文件
    private long copyFile(File original,File object) {
    long time=new Date().getTime();
    int bufferSize=1024*1024*2;//2MB
    try{
        FileInputStream in=new FileInputStream(original);
        FileOutputStream out=new FileOutputStream(object);
        byte[] buffer=new byte[bufferSize];
        int ins;
        while((ins=in.read(buffer))!=-1){
            out.write(buffer,0,ins);
        }
        in.close();
        out.flush();
        out.close();
        return new Date().getTime()-time;
    }catch(IOException e){}
    return 0;
}

    //删除文件，无论是文件还是文件夹
    public void delete(File file){
        if(file.isDirectory()){
            File[] fs=file.listFiles();
            for(File f:fs){
                if(!f.delete()){
                    delete(f);
                }else{
                    numFile--;
                    handler.sendEmptyMessage(0);
                }
            }
        }
        file.delete();
        numFloder--;
        handler.sendEmptyMessage(0);
    }

    public void countFile(File file){
        File[] fs=file.listFiles();
        for(File f:fs){
        if(f.isDirectory()){
            numFloder++;
            countFile(f);
        }else{
            numFile++;
        }
        handler.sendEmptyMessage(0);
    }
    }
}

    class ListAdapter extends BaseAdapter
    {
        LayoutInflater mInflater=LayoutInflater.from(MainActivity.this);

        @Override
        public int getCount()
        {
            // TODO: Implement this method
            return list.size();
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
            //p2=findViewById(R.layout.file_item);
            p2=mInflater.inflate(R.layout.file_item,null);
            TextView t1 = p2.findViewById(R.id.fileitemTextView1);
            TextView t2 =p2.findViewById(R.id.file_itemTextView2);
            ImageView img=p2.findViewById(R.id.file_itemImageView);
            t1.setTextColor(Color.BLACK);
            File file=list.get(p1);
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

            if(listview.isItemChecked(p1)) {
                t1.setBackgroundColor(Color.RED);
            } else {
                t1.setBackgroundColor(Color.TRANSPARENT);
            }
            return p2;
        }

    }
}

/**
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| >_< |)
 *  O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 *          .............................................
 *           佛曰：bug泛滥，我已瘫痪！
 */