package com.example.stocks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author xiejw@133.cn
 * @date 2020/11/11 9:55
 */
class MainActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnC.setOnClickListener { startActivity(Intent(this, StockActivity::class.java)) }
    }

}