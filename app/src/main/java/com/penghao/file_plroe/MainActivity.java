package com.penghao.file_plroe;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.penghao.activitys.AboutActivity;
import com.penghao.activitys.SettingsActivity;
import com.penghao.adapters.MyListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static ListView listview;
    public List<File> dirList=new ArrayList<>();
    public List<File> fileList=new ArrayList<>();
    public static List<File> list=new ArrayList<>();//list用于dir与file之并
    public List<File> shearPlate=new ArrayList<>();//剪切板;修饰为static，要为其他类共享剪切板
    public List<Integer> lvPosition=new ArrayList<>();//listview的第一个可见项的位置
    public MyListAdapter adapter;
    public SwipeRefreshLayout swipeRefresh;
    public Toolbar toolbar;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle drawerToggle;
    int lvTop=0;//listview第一个可见项距顶部的距离
    static final String HOMEPATH="/sdcard";
    static String nowPath=HOMEPATH;//当前所处的路径
    static boolean isZipMode=false;//是否以打开压缩文件的模式工作
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();
    }

    public void init(){
        //绑定控件
        listview=findViewById(R.id.mainListView);
        swipeRefresh=findViewById(R.id.main_srl);
        toolbar=findViewById(R.id.main_toolbar);
        drawerLayout=findViewById(R.id.dl_left);
        //设置状态栏
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
                    case R.id.main_about:
                        Intent intent1=new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(intent1);
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
        //判断是否以浏览zip模式工作！
        if (getIntent().getType().equals("application/zip"))
            isZipMode=true;
        //处理权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            //如果有权限的话就可以列出文件了
            if (!isZipMode)//不是zip模式才可以列出文件
                listFile(new File(HOMEPATH),false);
        }
        //listview相关
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

    //Create by Penghao on 2018-2-17
    //权限申请后具体的处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){//第一次申请后处理的逻辑
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                listFile(new File(HOMEPATH),false);//设置权限处理主要是为了防止没有权限时读不到文件，listFile方法会因为空指针而崩溃，为此，特地在listFile中加入了判空机制
            }else {//如果第一次拒绝，那么弹出对话框说明权限的重要性，然后再申请一次
                AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("权限申请");
                dialog.setMessage("文件管理需要读写SD卡权限，这是文件管理工作的基本，请您务必同意！");
                dialog.setPositiveButton("知道了", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //再次申请
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
                    }
                });
                dialog.show();
            }
        }else if (requestCode==2){//这是2次申请后处理的逻辑
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                listFile(new File(HOMEPATH),false);
            }else {//如果第二次申请仍然拒绝的话，那就结束程序
                finish();
            }
        }
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
            adapter=new MyListAdapter(MainActivity.this);
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
                showToast(MainActivity.this,"功能待开发！");
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

    //更新listview
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

    class CopyFolderDialog extends AlertDialog {

        Context context;
        ProgressBar progressbar;
        TextView text1;
        TextView text2;
        int numFile;
        int numFloder;
        int max;
        int mode;
        Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                text1.setText("文件夹:" + numFloder);
                text2.setText("文件:" + numFile);
                progressbar.setProgress(numFile + numFloder);
                super.handleMessage(msg);
            }
        };

        public CopyFolderDialog(Context context, int mode) {
            super(context);
            this.context = context;
            this.mode = mode;
            initView();
        }

        private void initView() {
            View view = LayoutInflater.from(context).inflate(R.layout.copyfolderdialog_view, null);
            progressbar = (ProgressBar) view.findViewById(R.id.copyfolderdialogviewProgressBar1);
            text1 = (TextView) view.findViewById(R.id.copyfolderdialogviewTextView1);
            text2 = (TextView) view.findViewById(R.id.copyfolderdialogviewTextView2);
            setView(view);
            getButton(1);
            setButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface p1, int p2) {

                }
            });
            show();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    //统计剪切板中包含的文件数
                    for (File f : shearPlate) {
                        if (f.isDirectory()) {
                            countFile(f);
                            numFloder++;
                        } else {
                            numFile++;
                        }
                        handler.sendEmptyMessage(0);
                    }
                    max = numFile + numFloder;
                    progressbar.setMax(max);//为进度条设置最大值，从此进度条开始递减
                    if (mode == 0) {//如果mode等于0，那么是复制模式
                        //统计完成后开始复制，注意这里是从1开始循环的
                        for (int i = 1; i < shearPlate.size(); i++) {
                            if (shearPlate.get(i).isDirectory()) {
                                File tem_file = new File(nowPath + "/" + shearPlate.get(i).getName());
                                nowPath = tem_file.getAbsolutePath();
                                tem_file.mkdir();
                                copyFolder(shearPlate.get(i));
                                nowPath = tem_file.getParent();
                                numFloder--;
                            } else {
                                copyFile(shearPlate.get(i), new File(nowPath + "/" + shearPlate.get(i).getName()));
                                numFile--;
                            }
                            handler.sendEmptyMessage(0);
                        }
                    } else {//否则是删除模式
                        for (File f : shearPlate) {
                            delete(f);
                        }
                    }
                    cancel();
                }
            }).start();
        }

        //复制文件夹，file是源，nowPath是目标的路径
        public void copyFolder(File file) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                File tem_file;
                for (File f : files) {
                    if (f.isDirectory()) {
                        tem_file = new File(nowPath + "/" + f.getName());
                        tem_file.mkdir();
                        nowPath = tem_file.getAbsolutePath();
                        copyFolder(f);
                        numFloder--;
                        nowPath = new File(nowPath).getParent();
                    } else {
                        copyFile(f, new File(nowPath + "/" + f.getName()));
                        numFile--;
                    }
                    handler.sendEmptyMessage(0);
                }
            }
        }

        //复制文件，original是源文件，object是目标文件
        private long copyFile(File original, File object) {
            long time = new Date().getTime();
            int bufferSize = 1024 * 1024 * 2;//2MB
            try {
                FileInputStream in = new FileInputStream(original);
                FileOutputStream out = new FileOutputStream(object);
                byte[] buffer = new byte[bufferSize];
                int ins;
                while ((ins = in.read(buffer)) != -1) {
                    out.write(buffer, 0, ins);
                }
                in.close();
                out.flush();
                out.close();
                return new Date().getTime() - time;
            } catch (IOException e) {
            }
            return 0;
        }

        //删除文件，无论是文件还是文件夹
        public void delete(File file) {
            if (file.isDirectory()) {
                File[] fs = file.listFiles();
                for (File f : fs) {
                    if (!f.delete()) {
                        delete(f);
                    } else {
                        numFile--;
                        handler.sendEmptyMessage(0);
                    }
                }
            }
            file.delete();
            numFloder--;
            handler.sendEmptyMessage(0);
        }

        public void countFile(File file) {
            File[] fs = file.listFiles();
            for (File f : fs) {
                if (f.isDirectory()) {
                    numFloder++;
                    countFile(f);
                } else {
                    numFile++;
                }
                handler.sendEmptyMessage(0);
            }
        }
    }
}