package com.penghao.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by penghao on 18-1-22.
 */

public class MyEditText extends View {

    private File input;
    private List<String> totalLine=new ArrayList<>();
    private Paint paint;
    private Context context;
    private final int TEXT_SIZE=40;
    private int textY=TEXT_SIZE;
    private InputMethodManager imm;
    private int cursorRow=0;//光标的位置，行号。从0开始索引
    private int cursorCloumn=0;//列号
    private boolean showRowNum=false;//是否显示行号，false为不显示
    //Java中的50个关键字
    public String[] javaKeyWords={
            "abstract",  "assert",      "boolean",  "break",     "byte",
            "case",      "catch",       "char",     "class",     "const",
            "continue",  "default",     "do",       "double",    "else",
            "enum",      "extends",     "final",    "finally",   "float",
            "for",       "goto",        "if",       "implements","import",
            "instanceof","int",         "interface","long",      "native",
            "new",       "package",     "private",  "protected", "public",
            "return",    "strictfp",    "short",    "static",    "super",
            "switch",    "synchronized","this",     "throw",     "throws",
            "transient", "try",         "void",     "volatile",  "while"
    };

    public MyEditText(Context context) {
        super(context);
        this.context=context;
        init();
    }

    public MyEditText(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context=context;
        init();
    }

    public void init(){
        //初始化输入法管理器
        imm= (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //初始化画笔
        paint= new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(TEXT_SIZE);
        paint.setAntiAlias(true);//去锯齿，如果不设置画出的文字会有很强的锯齿现象
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (totalLine.isEmpty())return;//如果文本是空的就不画了
        for (int i=0;i<totalLine.size();i++){
            if (showRowNum){
                canvas.drawText(i+".",0,textY,paint);
                canvas.drawText(totalLine.get(i),TEXT_SIZE*4,textY,paint);
            }else {
                canvas.drawText(totalLine.get(i),0,textY,paint);
            }
            textY+=TEXT_SIZE;
        }
        textY=TEXT_SIZE;
        float cursorX=paint.measureText(totalLine.get(cursorRow).substring(0,cursorCloumn));
        float cursorY=cursorRow*TEXT_SIZE;
        paint.setColor(Color.RED);
        canvas.drawLine(cursorX,cursorY,cursorX,cursorY+TEXT_SIZE,paint);
        paint.setColor(Color.BLACK);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=getMaxWidght();
        int height=getMaxHeight();
        setMeasuredDimension(width,height);
        Log.d("寬高",getWidth()+"  "+getHeight());
    }

    //这个方法没有实际作用，因为无论如何都是返回size。但还是先留着吧
    private int getSize(int measureSpace,int type){
//        final int deafualSize=100;
        int mode=MeasureSpec.getMode(measureSpace);
        int size=MeasureSpec.getSize(measureSpace);
        Log.d("模式",mode+"");
        if (mode==MeasureSpec.UNSPECIFIED){//如果是match_parent
            if (type==0){

            }
        }else if (mode==MeasureSpec.AT_MOST){//如果是wrap_content
            return size;
        }else if (mode==MeasureSpec.EXACTLY){//如果是指定大小，比如100dp
            return size;
        }
        return size;
    }

    //返回一个MyInputConnection对象，使view与该对象关联起来。InputConnection是view与输入法之间的桥梁
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new MyInputConnection(this,true);
    }

    //返回全文的宽，实际上是返回文中最长的一行的宽
    private int getMaxWidght(){
        if (totalLine.isEmpty())return 0;//对Empty的集合遍历好像会导致下越界异常，为了保险还是加上这一行吧
        int widght=0;
        int tem=0;
        for (String s:totalLine){
            if ((tem= (int) paint.measureText(s))>=widght)
                widght=tem;
        }
        return widght;
    }

    //返回全文的宽
    private int getMaxHeight(){
        return totalLine.size()*TEXT_SIZE;
    }

    int x,y,x0,y0,startX,startY,dx,dy,offsetX,offsetY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startX= (int) event.getX();
                startY= (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                x= (int) (x0+startX-event.getX());
                y= (int) (y0+startY-event.getY());
                break;
            case MotionEvent.ACTION_UP:
                dx=x-x0;
                dy=y-y0;
                x0=x;
                y0=y;
                offsetX=x;
                offsetY=y;
                //如果手指移动的距离很短的话，那么认为是点击事件
                if((Math.abs(dx)+Math.abs(dy))<20){
                    //是点击事件，先弹出输入法
                    this.setFocusableInTouchMode(true);
                    imm.showSoftInput(this,0);
                    //再移动光标
                    setCursor(event);
                }
                break;
            default:
                break;
        }
        return true;
    }

    //为光标设置位置
    public void setCursor(MotionEvent event){
        List<Integer> weight=new ArrayList<>();
        //设置光标的行
        int tem;
        if ((tem=((int)event.getY()+offsetY)/TEXT_SIZE)<=totalLine.size()){
            cursorRow=tem;
        }else {
            cursorRow=totalLine.size()-1;
            cursorCloumn=0;
            invalidate();
            return;
        }
        //设置光标的列
        if(paint.measureText(totalLine.get(cursorRow))<event.getX()){
            cursorCloumn=totalLine.get(cursorRow).length();
        }else{
            char[] cha=totalLine.get(cursorRow).toCharArray();
            String str="";
            for(int i=0;i<cha.length;i++){
                str+=cha[i];
                weight.add((int)paint.measureText(str));
            }
            int temCloumn = 0;
            for(int i=0;i<weight.size();i++){
                if(event.getX()+offsetX<=weight.get(i)){
                    temCloumn=i;
                    break;
                }
            }
            cursorCloumn=temCloumn;
        }
        invalidate();
    }

    public void setFile(File file){
        input=file;
        totalLine.clear();
        addLine();
        invalidate();
    }

    //将文件input中的内容填充到totalLine中
    private void addLine(){
        String oneLine="";
        int readedint;
        char readedchar;
        try {
            FileReader fis=new FileReader(input);
            while ((readedint=fis.read())!=-1){
                if (readedint!=0x0a){
                    readedchar=(char) readedint;
                    //由于画出的Tab与空格键长度不同，所以为了保持文章整齐，如果遇到Tab就将Tab转换为四个空格
                    if(readedchar=='	'){
                        oneLine=oneLine+"    ";
                    }else{
                        oneLine=oneLine+readedchar;
                    }
                }else {
                    totalLine.add(oneLine);
                    oneLine="";
                }
            }
            totalLine.add(oneLine);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //保存文件
    public void saveFile(){
        try {
            FileWriter fos=new FileWriter(input.getAbsolutePath()+"[副本]");
            for (String s:totalLine){
                fos.write(s+(char)0x0a);
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isJavaKeyWord(String word){
        boolean iskeyword=false;
        for(String s:javaKeyWords){
            if (s.equals(word)) {
                iskeyword = true;
                break;
            }
        }
        return iskeyword;
    }

    public boolean isCKeyWord(String word){

        return true;
    }

    class MyInputConnection extends BaseInputConnection{

        public MyInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            if (cursorCloumn==0){
                totalLine.set(cursorRow,text+totalLine.get(cursorRow));
            }else {
                char[] c=totalLine.get(cursorRow).toCharArray();
                String str1="";
                for (int i=0;i<cursorCloumn;i++)
                    str1+=c[i];
                String str2="";
                for (int i=cursorCloumn;i<c.length;i++)
                    str2+=c[i];
                totalLine.set(cursorRow,str1+text+str2);
            }
            cursorCloumn+=text.length();
            invalidate();
            return true;
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if(event.getAction()==KeyEvent.ACTION_DOWN)//只在按下时触发
                switch (event.getKeyCode()){
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (cursorRow>0){
                            cursorRow--;
                        }
                        if (cursorCloumn>totalLine.get(cursorRow).length()){
                            cursorCloumn=totalLine.get(cursorRow).length();
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        cursorRow++;
                        if (cursorCloumn>totalLine.get(cursorRow).length()){
                            cursorCloumn=totalLine.get(cursorRow).length();
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (cursorCloumn>0){
                            cursorCloumn--;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        cursorCloumn++;
                        break;
                    case KeyEvent.KEYCODE_DEL:
                        if (cursorRow==0&&cursorCloumn==0){//如果光标在0行0列，那就不能再删了
                            break;
                        }else if (cursorCloumn==0){//如果光标在一行的开始，那么
                            cursorCloumn=totalLine.get(cursorRow-1).length();//将光标移到上一行尾
                            totalLine.set(cursorRow-1,totalLine.get(cursorRow-1)+totalLine.get(cursorRow));//把该行加到上一行尾
                            totalLine.remove(cursorRow);//删掉该行
                            cursorRow--;
                        }else {//如果光标在正文中，那就删除一个字符
                            String str1=totalLine.get(cursorRow).substring(0,cursorCloumn);
                            String str2=totalLine.get(cursorRow).substring(cursorCloumn);
                            str1=str1.substring(0,str1.length()-1);
                            totalLine.set(cursorRow,str1+str2);
                            cursorCloumn--;
                        }
                        break;
                    default:
                        break;
                }
            invalidate();
            return true;
        }
    }
}