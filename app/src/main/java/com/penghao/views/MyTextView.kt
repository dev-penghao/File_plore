package com.penghao.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.ArrayList

/**
 * Created by penghao on 18-1-22.
 */
class MyTextView(context: Context, private val input: File) : View(context) {
    private val totalLine = ArrayList<String>()
    private val paint: Paint
    private var y: Int = 0

    init {
        paint = Paint()
        paint.color = Color.RED
        paint.textSize = 40f
        addLine()
    }

    override fun onDraw(canvas: Canvas) {
        for (s in totalLine) {
            canvas.drawText(s, 0f, y.toFloat(), paint)
            y += 40
        }
        super.onDraw(canvas)
    }

    private fun addLine() {
        var readed: Int
        var oneLine = ""
        try {
            val fis = FileInputStream(input)
            while (true) {
                readed=fis.read();
                if (readed!=-1){
                    if (readed != 0x0a) {
                        oneLine += readed.toChar()
                    } else {
                        totalLine.add(oneLine)
                        oneLine = ""
                    }
                }else{
                    break
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
