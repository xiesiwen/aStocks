package com.example.stocks

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.lang.Exception
import java.text.DecimalFormat
import kotlin.math.abs

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
    var startMaxIndex = 0
        set(value) {
            field = value
            startIndex = value - (width / itemWidth).toInt()
        }
    var itemWidth = 10f
    var next = {
            if (!touch) {
                startIndex += 1
                currentIndex = ((width / itemWidth).toInt() + startIndex).coerceAtMost(stocks.size - 1)
                update?.invoke(stocks[currentIndex-1])
                invalidate()
            }
        if (running) {
            play()
        }
    }
    var dif = mapOf<Int,Float>()
    var dea = mapOf<Int, Float>()
    var macd = mapOf<Int, Float>()
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
    var t = 1
    var drawMacd = false

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        itemWidth = width.toFloat() / ((width / itemWidth).toInt())
        paint.color = Color.RED
    }

    fun nextPos(){
        startIndex += 1
        currentIndex = ((width / itemWidth).toInt() + startIndex).coerceAtMost(stocks.size)
        update?.invoke(stocks[currentIndex - 1])
        invalidate()
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
            var maxMacd = abs(macd[startIndex] ?: error(""))
            if (maxVol == 0f) {
                maxVol = stocks[startIndex].volume
            }
            if (currentIndex == 0) {
                currentIndex =
                    ((width / itemWidth).toInt() + startIndex).coerceAtMost(stocks.size - 1)
            }
            for (i in startIndex..currentIndex) {
                minA = minA.coerceAtMost(stocks[i].low)
                maxA = maxA.coerceAtLeast(stocks[i].high)
                maxVol = maxVol.coerceAtLeast(stocks[i].volume)
                maxMacd = maxMacd.coerceAtLeast(abs(macd[i] ?: error("")))
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
//        paint.strokeWidth = 2f
            x = 0f
            path.reset()
            var l = 10
            for (i in startIndex..currentIndex) {
                var p = 0f
                var p5 = 0f
                if (i >= l) {
                    for (z in i - l + 1..i) {
                        p += stocks[z].close
                    }
                    p /= l
                    for (z in i - 4..i) {
                        p5 += stocks[z].close
                    }
                    p5 /= 5
                    if (i == l) {
                        path.moveTo(x + itemWidth / 2, maxA - p)
                    } else {
                        path.lineTo(x + itemWidth / 2, maxA - p)
                    }
                    var gap = p5 - p
                    if (gap * count >= 0) {
                        count += gap
                        t += 1
                    } else {
                        Log.d("xsw", "${format.format(count)}   ${t}  ${format.format(count / t)}")
                        canvas?.drawText(
                            format.format(count).toString(),
                            x,
                            height - volH - 20,
                            paint
                        )
                        canvas?.drawText(
                            format.format(count / t).toString(),
                            x,
                            height - volH - 40,
                            paint
                        )
                        count = gap
                        t = 1
                    }
                }
                x += itemWidth
            }
            canvas?.drawPath(path, paint)
            path.reset()
            x = 0f
            paint.color = Color.parseColor("#EEEE00")
            l = 5
            for (i in startIndex..currentIndex) {
                var p = 0f
                if (i >= l) {
                    for (z in i - l + 1..i) {
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

            path.reset()
            x = 0f
            paint.color = Color.parseColor("#ffffff")
            l = 30
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
            if (drawMacd) {
                for (i in startIndex..currentIndex) {
                    var vh = (volH - 50) / 2
                    var h = vh / maxMacd * macd[i]!!
                    if (macd[i]!! > 0) {
                        paint.color = Color.RED
                        canvas?.drawRect(
                            x + 2,
                            height - vh - h,
                            x + itemWidth - 2,
                            height - vh, paint
                        )
                    } else {
                        paint.color = Color.GREEN
                        canvas?.drawRect(
                            x + 2,
                            height - vh,
                            x + itemWidth - 2,
                            height - vh - h, paint
                        )
                    }
                    x += itemWidth
                }
            } else {
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