package com.example.stocks

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_stock.*
import java.text.DecimalFormat

/**
 * @author xiejw@133.cn
 * @date 2020/11/11 9:56
 */
class StockActivity : Activity() {
    var ss = arrayListOf<Stock>()
    var fs = arrayOf("")
    var curStock:Stock? = null
    var b = 0f
    var format = DecimalFormat("0.##")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)
        fs = assets.list("stocks") as Array<String>
        listView.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fs)
        stop.setOnClickListener { stocksView.toggle() }
        acc.setOnClickListener { speed.text = stocksView.acceleration() }
        dec.setOnClickListener { speed.text = stocksView.deceleration() }
        buy.setOnClickListener { b = curStock?.close ?: 0f }
        sale.setOnClickListener {
            var o = curStock?.close ?: 0f
            if (o > b) {
                AlertDialog.Builder(this).setMessage("WIN: ${(o/b-1)*100}").show()
                val sharedPreferences = getSharedPreferences("stock", Context.MODE_PRIVATE)
                val int = sharedPreferences.getInt("win", 0)
                sharedPreferences.edit().putInt("win", int+1).apply()
            } else {
                AlertDialog.Builder(this).setMessage("LOSS: ${(o/b-1)*100}").show()
                val sharedPreferences = getSharedPreferences("stock", Context.MODE_PRIVATE)
                val int = sharedPreferences.getInt("loss", 0)
                sharedPreferences.edit().putInt("loss", int+1).apply()
            }
            stocksView.stop()
        }
        stocksView.setOnStockUpdate { stock ->
            curStock = stock
            if (stock.pct > 0) {
                info.setTextColor(Color.RED)
            } else {
                info.setTextColor(Color.GREEN)
            }
            val format = DecimalFormat("0.##")
            info.text = "${stock.date}  ${format.format(stock.close)}  ${if (stock.pct>0) "+" else ""}${format.format(stock.pct)}%"
        }
        stocksView.setOnStockSelect {
            if (it == null){
                infos.visibility = View.GONE
            } else {
                infos.visibility = View.INVISIBLE
                date.text = "日期：${it?.date}"
                open.text = "开盘：${format.format(it?.open)}"
                close.text = "收盘：${format.format(it?.close)}"
                high.text = "最高：${format.format(it?.high)}"
                low.text = "最低：${format.format(it?.low)}"
                pct.text = "比例：${format.format(it?.pct)}"
            }
        }
        list.setOnClickListener {
            if (listView.visibility == View.GONE){
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

    fun select(file:String){
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
            ExecUtils.runOnMainThread {
                stocksView.stocks = ss
                stocksView.startIndex = (0..ss.size/2).random()
            }
        }
    }
}