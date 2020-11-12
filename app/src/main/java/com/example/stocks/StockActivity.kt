package com.example.stocks

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_stock.*
import java.text.DecimalFormat

/**
 * @author xiejw@133.cn
 * @date 2020/11/11 9:56
 */
class StockActivity : Activity() {
    var ss: ArrayList<Stock> = ArrayList()
    var curStock:Stock? = null
    var b = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)
        ExecUtils.runOnIOThread {
            var inp = assets.open("stocks/sh.600048.csv")
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
            ExecUtils.runOnMainThread { stocksView.stocks = ss }
        }
        stop.setOnClickListener { stocksView.toggle() }
        acc.setOnClickListener { speed.text = stocksView.acceleration() }
        dec.setOnClickListener { speed.text = stocksView.deceleration() }
        buy.setOnClickListener { b = curStock?.close ?: 0f }
        sale.setOnClickListener {
            var o = curStock?.close ?: 0f
            if (o > b) {

            } else {

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
            info.text = "${stock.data}  ${format.format(stock.close)}  ${if (stock.pct>0) "+" else ""}${format.format(stock.pct)}%"
        }
    }

}