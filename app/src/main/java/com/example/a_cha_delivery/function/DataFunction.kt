package com.example.a_cha_delivery.function

import android.util.Log
import com.example.a_cha_delivery.MainActivity
import com.example.a_cha_delivery.data_classes.OrderInfo
import java.text.SimpleDateFormat

object DataFunction {
    fun convertDateToTimestamp(date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        Log.d("TTTT Time -> ", sdf.parse(date).time.toString())
        Log.d("TTT Unix -> ", (System.currentTimeMillis()).toString())
        return sdf.parse(date).time
    }

    fun convertTimestampToDate(timestamp: Long) {
        val sdf = SimpleDateFormat("yyyy-MM-dd-hh-mm")
        val date = sdf.format(timestamp)
        Log.d("TTT UNix Date -> ", sdf.format((System.currentTimeMillis())).toString())
        Log.d("TTTT date -> ", date.toString())
    }

    fun findDataId(x:OrderInfo):String{
        for(i in MainActivity.orderInfos){
            if(i.value.equals(x)){
                return i.key
            }
        }
        return ""
    }
}