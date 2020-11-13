package com.example.stocks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat

/**
 * @author xiejw@133.cn
 * @date 2020/11/11 9:55
 */
class MainActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnC.setOnClickListener { startActivity(Intent(this, StockActivity::class.java)) }
        val sharedPreferences = getSharedPreferences("stock", Context.MODE_PRIVATE)
        val int = sharedPreferences.getInt("win", 0)
        val loss = sharedPreferences.getInt("loss", 0)
        var format = DecimalFormat("0.##%")
        if (int + loss > 0){
            rate.text = format.format(int.toFloat()/(int+loss))
        }
    }

}