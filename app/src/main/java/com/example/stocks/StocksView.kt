package com.example.stocks

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.text.DecimalFormat

/**
 * @author xiejw@133.cn
 * @date 2020/11/11 11:23
 */
class StocksView : View {
    var paint = Paint().apply {
        textSize = 26f
    }
    var stocks = ArrayList<Stock>()
        set(value) {
            field = value
            play()
        }
    var startIndex = 0
    var itemWidth = 10f
    var next = {
        if (!touch) {
            startIndex += 1
            currentIndex = ((width / itemWidth).toInt() + startIndex).coerceAtMost(stocks.size)
            update?.invoke(stocks[currentIndex])
            invalidate()
        }
        play()
    }
    var update: ((Stock) -> Unit)? = null
    var select: ((Stock?) -> Unit)? = null
    var speed = 3000L
    var maxVol = 0f
    var running = false
    var currentIndex = 0
    var path = Path()
    val format = DecimalFormat("0.##")
    var touch = false
    var count = 0f

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        itemWidth = width.toFloat() / ((width / itemWidth).toInt())
        paint.color = Color.RED
    }

    fun play() {
        running = if (width > 0 && width / itemWidth < stocks.size - startIndex) {
            postDelayed(next, speed)
            true
        } else {
            false
        }
    }

    fun stop() {
        if (running) {
            removeCallbacks(next)
            running = false
        }
    }

    fun toggle() {
        if (running) {
            stop()
        } else {
            play()
        }
    }

    fun setOnStockUpdate(run: (Stock) -> Unit) {
        update = run
    }

    fun setOnStockSelect(run: (Stock?) -> Unit) {
        select = run
    }

    fun acceleration(): String {
        speed -= 1000
        speed = speed.coerceAtLeast(0)
        return (speed / 1000).toInt().toString()
    }

    fun deceleration(): String {
        speed += 1000
        return (speed / 1000).toInt().toString()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (stocks.isEmpty()) {
            return
        }
        var volH = 200f
        var x = 0f
        var minA = stocks[startIndex].close
        var maxA = stocks[startIndex].close
        if (maxVol == 0f) {
            maxVol = stocks[startIndex].volume
        }
        if (currentIndex == 0) {
            currentIndex = ((width / itemWidth).toInt() + startIndex).coerceAtMost(stocks.size - 1)
        }
        for (i in startIndex..currentIndex) {
            minA = minA.coerceAtMost(stocks[i].low)
            maxA = maxA.coerceAtLeast(stocks[i].high)
            maxVol = maxVol.coerceAtLeast(stocks[i].volume)
        }

        canvas?.save()
        canvas?.clipRect(0f, 0f, width.toFloat(), height - volH)
        canvas?.scale(1f, (height - volH) / (maxA - minA), 0f, 0f)
        for (i in startIndex..currentIndex) {
            if (stocks[i].close < stocks[i].open) {
                paint.color = Color.GREEN
            } else {
                paint.color = Color.RED
            }
            var min = stocks[i].close.coerceAtMost(stocks[i].open)
            var max = stocks[i].close.coerceAtLeast(stocks[i].open)
            canvas?.drawRect(
                x,
                maxA - max,
                x + itemWidth,
                maxA - min, paint
            )
            canvas?.drawRect(
                x + itemWidth / 2 - 1,
                maxA - min,
                x + itemWidth / 2 + 1,
                maxA - stocks[i].low, paint
            )
            canvas?.drawRect(
                x + itemWidth / 2 - 1,
                maxA - stocks[i].high,
                x + itemWidth / 2 + 1,
                maxA - max, paint
            )
            x += itemWidth
        }
        paint.color = Color.parseColor("#C71585")
        paint.style = Paint.Style.STROKE
        x = 0f
        path.reset()
        var l = 10
        for (i in startIndex..currentIndex) {
            var p = 0f
            var p5 = 0f
            if (i >= l) {
                for (z in i - l..i) {
                    p += stocks[z].close
                }
                p /= l
                for (z in i - 5..i) {
                    p5 += stocks[z].close
                }
                p5 /= 5
                if (i == l) {
                    path.moveTo(x + itemWidth / 2, maxA - p)
                } else {
                    path.lineTo(x + itemWidth / 2, maxA - p)
                }
                if ((p - p5) * count > 0) {
                    count += (p - p5)
                } else {
                    canvas?.drawText(count.toString(), x, height - volH, paint)
                    count = (p - p5)
                }
            }
            x += itemWidth
        }
        canvas?.drawPath(path, paint)
        path.reset()
        x = 0f
        paint.color = Color.parseColor("#333333")
        l = 5
        for (i in startIndex..currentIndex) {
            var p = 0f
            if (i >= l) {
                for (z in i - l..i) {
                    p += stocks[z].close
                }
                p /= l
                if (i == l) {
                    path.moveTo(x + itemWidth / 2, maxA - p)
                } else {
                    path.lineTo(x + itemWidth / 2, maxA - p)
                }
            }
            x += itemWidth
        }
        canvas?.drawPath(path, paint)
        canvas?.restore()

        x = 0f
        paint.style = Paint.Style.FILL
        for (i in startIndex..currentIndex) {
            var h = (volH - 50) / maxVol * stocks[i].volume
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

        paint.color = Color.parseColor("#eeeeee")
        canvas?.drawText(format.format(maxA).toString(), 10f, 20f, paint)
        canvas?.drawText(format.format(minA).toString(), 10f, height - volH, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                touch = true
                selectX(event.x)
            }
            MotionEvent.ACTION_MOVE ->
                selectX(event.x)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touch = false
                select?.invoke(null)
            }
        }
        return true
    }

    fun selectX(x: Float) {
        var ind = ((x / itemWidth).toInt() + startIndex).coerceAtMost(stocks.size - 1)
        select?.invoke(stocks[ind])
    }

}