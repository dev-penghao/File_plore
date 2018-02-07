package com.penghao.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private int cursorCloumn=0;//光标的位置，列
    private int cursorRow=0;//行
    //Java中的50个关键字
    public String[] javaKeyWords={
      "abstract","assert","boolean","break","byte",
      "case","catch","char","class","const",
      "continue","default","do","double","else",
      "enum","extends","final","finally","float",
      "for","goto","if","implements","import",
      "instanceof","int","interface","long","native",
      "new","package","private","protected","public",
      "return","strictfp","short","static","super",
      "switch","synchronized","this","throw","throws",
      "transient","try","void","volatile","while"
    };

    public MyEditText(Context context, File input) {
        super(context);
        this.input=input;
        this.context=context;
        init();
    }

    public void init(){
        paint= new Paint();
        imm= (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        paint.setColor(Color.BLACK);
        paint.setTextSize(TEXT_SIZE);
        paint.setAntiAlias(true);
        addLine();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (String line:totalLine){
            canvas.drawText(line,0,textY,paint);
            textY+=TEXT_SIZE;
        }
        textY=TEXT_SIZE;
        super.onDraw(canvas);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new MyInputConnection(this,true);
    }

    /**
     * 让View跟着你的手指走吧！
     */
    int x,y,x0,y0,startX,startY;
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
                scrollTo(x,y);
                break;
            case MotionEvent.ACTION_UP:
                x0=x;
                y0=y;
                this.setFocusableInTouchMode(true);
                imm.showSoftInput(this,0);
                break;
        }
        return true;
    }

    private void addLine(){
        int readed;
        String oneLine="";
        try {
            FileReader fis=new FileReader(input);
            while ((readed=fis.read())!=-1){
                if (readed!=0x0a){
                    oneLine=oneLine+(char)readed;
                }else {
                    totalLine.add(oneLine);
                    oneLine="";
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isJavaKeyWord(String world){
        boolean iskeyword=false;
        for(String s:javaKeyWords){
            if (s.equals(world)) {
                iskeyword = true;
                break;
            }
        }
        return iskeyword;
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
            invalidate();
            return true;
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            Log.d("事件","事件!"+cursorRow);
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (cursorRow>0){
                        cursorRow--;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    cursorRow++;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (cursorCloumn>0){
                        cursorCloumn--;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    cursorCloumn++;
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}