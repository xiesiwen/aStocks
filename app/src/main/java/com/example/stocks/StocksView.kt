package com.example.stocks

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * @author xiejw@133.cn
 * @date 2020/11/11 11:23
 */
class StocksView : View {
    var paint = Paint()
    var stocks = ArrayList<Stock>()
        set(value) {
            field = value
            play()
        }
    var showStart = 0
    var itemWidth = 10f
    var next = {
        showStart += 1
        invalidate()
        play()
    }
    var speed = 3000L
    var maxVol = 0f
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        itemWidth = width.toFloat() / ((width / itemWidth).toInt())
        paint.color = Color.RED
    }

    fun play(){
        if (width > 0 && width / itemWidth < stocks.size - showStart) {
            postDelayed(next,speed)
        }
    }

    fun stop(){
        removeCallbacks(next)
    }

    fun acceleration(){
        speed -= 1000
        speed = speed.coerceAtLeast(0)
    }

    fun deceleration(){
        speed += 1000
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d("xsw", "ondraw size  ${stocks.size}")
        if (stocks.isEmpty()) {
            return
        }
        var volH = 200f
        var x = 0f
        var min = stocks[showStart].close
        var max = stocks[showStart].close
        if (maxVol == 0f) {
            maxVol = stocks[showStart].volume
        }
        var maxIndex = ((width/itemWidth).toInt() + showStart).coerceAtMost(stocks.size)
        Log.d("xsw", "$showStart   $maxIndex")
        for (i in showStart..maxIndex) {
            min = min.coerceAtMost(stocks[i].close).coerceAtMost(stocks[i].open)
            max = max.coerceAtLeast(stocks[i].close).coerceAtLeast(stocks[i].open)
            maxVol = maxVol.coerceAtLeast(stocks[i].volume)
        }

        canvas?.save()
        canvas?.clipRect(0f,0f,width.toFloat(),height-volH)
        canvas?.scale(1f,(height-volH)/(max-min),0f,0f)
        for (i in showStart..maxIndex) {
            if (stocks[i].close < stocks[i].open) {
                paint.color = Color.GREEN
            } else {
                paint.color = Color.RED
            }
            min = stocks[i].close.coerceAtMost(stocks[i].open)
            max = stocks[i].close.coerceAtLeast(stocks[i].open)
            canvas?.drawRect(
                x,
                max - max,
                x + itemWidth,
                max - min, paint
            )
            canvas?.drawRect(
                x+itemWidth/2-1,
                max - min,
                x + itemWidth/2+1,
                max - max, paint
            )
            canvas?.drawRect(
                x+itemWidth/2-1,
                max - min,
                x + itemWidth/2+1,
                max - max, paint
            )
            x += itemWidth
            if (x > width) {
                break
            }
        }
        canvas?.restore()

        x = 0f
        for (i in showStart..maxIndex){
            var h = (volH - 50)/maxVol*stocks[i].volume
            if (stocks[i].pct > 0) {
                paint.color = Color.RED
            } else {
                paint.color = Color.GREEN
            }
            canvas?.drawRect(
                x + 2,
                height - h,
                x + itemWidth - 2,
                height.toFloat(), paint
            )
            x += itemWidth
        }
    }

}