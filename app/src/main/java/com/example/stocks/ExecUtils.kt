package com.example.stocks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author xiejw@133.cn
 * @date 2020/11/3 16:45
 */
class ExecUtils {
    companion object{
        fun runOnIOThread(r:()->Unit) {
            GlobalScope.launch(Dispatchers.IO)  {
                r.invoke()
            }
        }

        fun runOnMainThread(r:()->Unit) {
            GlobalScope.launch(Dispatchers.Main)  {
                r.invoke()
            }
        }
    }

}