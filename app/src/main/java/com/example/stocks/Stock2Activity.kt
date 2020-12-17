package com.example.stocks

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_stock2.*
import java.text.DecimalFormat

/**
 * @author xiejw@133.cn
 * @date 2020/11/11 9:56
 */
class Stock2Activity : Activity() {
    var ss = arrayListOf<Stock>()
    var fs = arrayOf("")
    var curStock: Stock? = null
    var b = 0f
    var format = DecimalFormat("0.##")
    var ema12 = mutableMapOf<Int, Float>()
    var ema26 = mutableMapOf<Int, Float>()
    var dif = mutableMapOf<Int, Float>()
    var dea = mutableMapOf<Int, Float>()
    var macd = mutableMapOf<Int, Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock2)
        fs = assets.list("stocks/practice") as Array<String>
        listView.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fs)
        next.setOnClickListener { stocksView.nextPos() }
        buy.setOnClickListener { b = curStock?.close ?: 0f }
        sale.setOnClickListener {
            var o = curStock?.close ?: 0f
            if (o > b) {
                AlertDialog.Builder(this).setMessage("WIN: ${(o / b - 1) * 100}").show()
                val sharedPreferences = getSharedPreferences("stock", Context.MODE_PRIVATE)
                val int = sharedPreferences.getInt("win", 0)
                sharedPreferences.edit().putInt("win", int + 1).apply()
            } else {
                AlertDialog.Builder(this).setMessage("LOSS: ${(o / b - 1) * 100}").show()
                val sharedPreferences = getSharedPreferences("stock", Context.MODE_PRIVATE)
                val int = sharedPreferences.getInt("loss", 0)
                sharedPreferences.edit().putInt("loss", int + 1).apply()
            }
        }
        stocksView.drawMacd = true
        stocksView.setOnStockUpdate { stock ->
            curStock = stock
            if (stock.pct > 0) {
                info.setTextColor(Color.RED)
            } else {
                info.setTextColor(Color.GREEN)
            }
            val format = DecimalFormat("0.##")
            info.text =
                "${stock.date}  ${format.format(stock.close)}  ${if (stock.pct > 0) "+" else ""}${format.format(
                    stock.pct
                )}%"
            if (b != 0f) {
                if (stock.close > b) {
                    tv_res.setTextColor(Color.RED)
                } else {
                    tv_res.setTextColor(Color.GREEN)
                }
                tv_res.text = ((stock.close  / b - 1) * 100).toString()
            }
        }
        stocksView.setOnStockSelect {
            if (it == null){
                infos.visibility = View.VISIBLE
                dayInfo.visibility = View.GONE
            } else {
                infos.visibility = View.INVISIBLE
                dayInfo.visibility = View.VISIBLE
                date.text = "日期：${it?.date}"
                open.text = "开盘：${format.format(it?.open)}"
                close.text = "收盘：${format.format(it?.close)}"
                high.text = "最高：${format.format(it?.high)}"
                low.text = "最低：${format.format(it?.low)}"
                pct.text = "比例：${format.format(it?.pct)}"
            }
        }
        list.setOnClickListener {
            if (listView.visibility == View.GONE) {
                listView.visibility = View.VISIBLE
            } else {
                listView.visibility = View.GONE
            }
        }
        listView.setOnItemClickListener { parent, view, position, id ->
            select(fs[position])
        }
        select(fs[0])
    }

    fun select(file: String) {
        ExecUtils.runOnIOThread {
            var inp = assets.open("stocks/$file")
            var bs = ByteArray(inp.available())
            inp.read(bs)
            var data = String(bs)
            data.split("\n").forEach {
                if (!it.startsWith("date")) {
                    var ds = it.split(",")
                    if (ds.size > 6) {
                        ss.add(
                            Stock(
                                ds[0],
                                ds[1].toFloat(),
                                ds[2].toFloat(),
                                ds[3].toFloat(),
                                ds[4].toFloat(),
                                ds[5].toFloat(),
                                ds[6].toFloat()
                            )
                        )
                    }
                }
            }
            computeEma(12, ss.size - 1)
            computeEma(26, ss.size - 1)
            for (i in 0 until ss.size) {
                if (ema12[i] == null) {
                    ema12[i] = ss[i].close
                }
                if (ema26[i] == null) {
                    ema26[i] = ss[i].close
                }
                dif[i] = ema12[i]!! - ema26[i]!!
            }
            computeEma(9, ss.size -1)
            for (i in 0 until ss.size) {
                if (dea[i] == null) {
                    dea[i] = dif[i]!!
                }
                macd[i] = 2 * (dif[i]!! - dea[i]!!)
            }
            ExecUtils.runOnMainThread {
                stocksView.stocks = ss
                stocksView.dif = dif
                stocksView.dea = dea
                stocksView.macd = macd
                val split = file.split(".")
                stocksView.startIndex = 0.coerceAtLeast(split[1].toInt() - 60)
            }
        }
    }

    fun computeEma(days: Int, index: Int): Float {
        if (days == 12) {
            var res = when {
                index == 11 -> {
                    ss[11].close
                }
                ema12.containsKey(index - 1) -> {
                    (ema12[index - 1] ?: error("")) * 11 / 13 + ss[index].close / 13 * 2
                }
                else -> {
                    computeEma(days, index - 1) * 11 / 13 + ss[index].close / 13 * 2
                }
            }
            ema12[index] = res
            return res
        } else if (days == 25){
            var res = when {
                index == 25 -> {
                    ss[25].close
                }
                ema26.containsKey(index - 1) -> {
                    (ema26[index - 1] ?: error("")) * 25 / 27 + ss[index].close / 27 * 2
                }
                else -> {
                    computeEma(days, index - 1) * 25 / 27 + ss[index].close / 27 * 2
                }
            }
            ema26[index] = res
            return res
        } else if (days == 9) {
            var res = when {
                index == 9 -> {
                    dif[9]!!
                }
                dea.containsKey(index - 1) -> {
                    (dif[index - 1] ?: error("")) * 8 / 10 + dif[index]!! / 10 * 2
                }
                else -> {
                    computeEma(days, index - 1) * 8 / 10 + dif[index]!! / 10 * 2
                }
            }
            dea[index] = res
            return res
        }
        return 0f;
    }
}