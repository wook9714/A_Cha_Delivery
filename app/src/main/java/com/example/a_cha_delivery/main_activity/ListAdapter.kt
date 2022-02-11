package com.example.a_cha_delivery.main_activity

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_cha_delivery.MainActivity
import com.example.a_cha_delivery.data_classes.OrderInfo
import com.example.a_cha_delivery.data_classes.OrderItemsID
import com.example.a_cha_delivery.databinding.ListPageViewBinding
import com.example.a_cha_delivery.databinding.NeedItemInfoBinding
import com.example.a_cha_delivery.databinding.OrderListInfoBinding
import com.example.a_cha_delivery.function.DataFunction
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class ListAdapter():RecyclerView.Adapter<Holder>() {


    var menuList:MutableList<String> = mutableListOf()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ListPageViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if(position!=3){
            holder.setInfo(MainActivity.orderInfos.values.filter{it.deliveryState==position}.toMutableList())
        }
        else{
            holder.setInfo(MainActivity.orderInfos.values.toMutableList())
        }

    }


    override fun getItemCount(): Int {
        return menuList.size
    }

}


class Holder(val binding:ListPageViewBinding):RecyclerView.ViewHolder(binding.root){

    var enabledDeliveryTime = mutableListOf<String>()
    val needInfoBindings = mutableListOf<NeedItemInfoBinding>()

    fun setInfo(info:MutableList<OrderInfo>){


        val times = info.groupingBy { it.deliveryTime }.eachCount().toList().toMutableList()
        for(i in times){

            val date = i.first.toDate()
            val format = SimpleDateFormat("HH:mm")
            val startTString = format.format(date)
            enabledDeliveryTime.add(startTString)

        }


        if(adapterPosition==0){
            for(i in enabledDeliveryTime){
                val needItemInfoBinding = NeedItemInfoBinding.inflate(LayoutInflater.from(binding.root.context),binding.root,false)

                val hour = i.split(":").get(0)
                val minute = i.split(":").get(1)



                val format = SimpleDateFormat("HH:mm")
                val date = format.parse(i)
                val startT = date.time - (30 * 60 * 1000)

                val date1 = Date(startT)
                val format1 = SimpleDateFormat("HH:mm")
                val startTString = format1.format(date1)

                needItemInfoBinding.time.text =startTString+" ~ "+ hour.toString()+":"+minute.toString()






                needInfoBindings.add(needItemInfoBinding)

                binding.scrollItems.addView(needItemInfoBinding.parentLayout)

                var thisTimeOrderList = mutableListOf<String>()


                for(j in info){
                    if(hour.toInt()==j.deliveryTime.toDate().hours&&minute.toInt()==j.deliveryTime.toDate().minutes){

                        thisTimeOrderList.addAll(j.orderItems)



                        val orderListInfoBinding = OrderListInfoBinding.inflate(
                            LayoutInflater.from(binding.root.context),
                            binding.root,
                            false
                        )


                        val sdf = SimpleDateFormat("HH:mm")
                        val sT = j.deliveryTime.toDate()
                        val timeEnd = sdf.format(sT).toString()
                        val subtractedTime = Date(sT.time - (30 * 60 * 1000))
                        val timeStart = sdf.format(subtractedTime).toString()

                        orderListInfoBinding.userInfo.text = j.userID
                        orderListInfoBinding.userAddress.text = j.detailedLocation



                        var it = j.orderItems.groupingBy { it }.eachCount().toMutableMap()

                        var orderText = ""
                        for (j in it) {
                            orderText += j.key.toString() + " "+j.value.toString()+"개\n"
                        }
                        orderListInfoBinding.orderItems.text = orderText



                        //timeStart+" ~ "+timeEnd

                        if(adapterPosition==0){

                            orderListInfoBinding.btnOrderState.visibility = View.VISIBLE
                            orderListInfoBinding.btnOrderState.setOnClickListener {

                                MainActivity.db.collection("orderInfo").document(DataFunction.findDataId(j)).update("deliveryState",1).addOnSuccessListener {
                                    Log.d("firebase","데이터 업데이트 성공")
                                    //TODO("여기서부터")
                                    MainActivity.loadDataFromFirestore(MainActivity.d1,MainActivity.d2,true)

                               }

                            }
                            binding.scrollItems.addView(orderListInfoBinding.parentLayout,binding.scrollItems.indexOfChild(needItemInfoBinding.root)+1)
                        }
                        else{
                            binding.scrollItems.addView(orderListInfoBinding.parentLayout)
                        }
                    }
                }

                var timePartitionResult = thisTimeOrderList.groupingBy { it }.eachCount()

                var thisTimeOrder = ""

                for(k in timePartitionResult){
                    thisTimeOrder+= k.key + " " + k.value + "개\n"
                }

                needItemInfoBinding.needItems.text = thisTimeOrder


           }



        }
        else{
            for(j in info){

                val orderListInfoBinding = OrderListInfoBinding.inflate(
                    LayoutInflater.from(binding.root.context),
                    binding.root,
                    false
                )


                val sdf = SimpleDateFormat("HH:mm")
                val sT = j.deliveryTime.toDate()
                val timeEnd = sdf.format(sT).toString()
                val subtractedTime = Date(sT.time - (30 * 60 * 1000))
                val timeStart = sdf.format(subtractedTime).toString()

                orderListInfoBinding.userInfo.text = j.userID
                orderListInfoBinding.userAddress.text = j.detailedLocation



                var it = j.orderItems.groupingBy { it }.eachCount().toMutableMap()

                var orderText = ""
                for (j in it) {
                    orderText += j.key + " "+j.value.toString()+"개\n"
                }
                orderListInfoBinding.orderItems.text = orderText



                //timeStart+" ~ "+timeEnd


                binding.scrollItems.addView(orderListInfoBinding.parentLayout)


            }
        }



    }
}
