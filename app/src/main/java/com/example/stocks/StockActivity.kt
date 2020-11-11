package com.example.stocks

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_stock.*

/**
 * @author xiejw@133.cn
 * @date 2020/11/11 9:56
 */
class StockActivity : Activity() {
    var ss: ArrayList<Stock> = ArrayList()

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
                                ds[5].toFloat(),
                                ds[6].toFloat()
                            )
                        )
                    }
                }
            }
            ExecUtils.runOnMainThread { stocksView.stocks = ss }
        }
    }

}