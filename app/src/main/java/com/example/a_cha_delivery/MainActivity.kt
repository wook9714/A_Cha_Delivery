package com.example.a_cha_delivery


import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.a_cha_delivery.data_classes.OrderInfo
import com.example.a_cha_delivery.databinding.ActivityMainBinding
import com.example.a_cha_delivery.function.DataFunction.convertDateToTimestamp
import com.example.a_cha_delivery.main_activity.*

import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.sql.Date
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(){

    companion object{

        val location:String = "은마"
        val orderInfos:MutableMap<String,OrderInfo> = mutableMapOf()
        val processedData = mutableListOf<Pair<Timestamp,MutableList<OrderInfo>>>()
        val processedData_undelivered = mutableListOf<Pair<Timestamp,MutableList<OrderInfo>>>()
        val db = Firebase.firestore


        //원하는 데이터
        val d1 = Date(convertDateToTimestamp("2022-1-20"))
        val d2 = Date(convertDateToTimestamp("2022-1-23"))

        fun loadDataFromFirestore(wantLoadDateStart:Date,wantLoadDateEnd:Date,isNeedScrollChange:Boolean){
            processedData_undelivered.clear()
            processedData.clear()
            orderInfos.clear()
            db.collection("orderInfo").
            whereGreaterThan("deliveryTime",wantLoadDateStart).whereLessThan("deliveryTime",wantLoadDateEnd).get().addOnSuccessListener { document ->
                if (document != null) {
                    for(i in document){
                        orderInfos.set(i.id,i.toObject<OrderInfo>())
                    }

                    for(i in orderInfos.values.groupingBy { it.deliveryTime }.eachCount()){
                        processedData.add(Pair(i.key, mutableListOf<OrderInfo>()))
                    }
                    for(i in processedData){
                        for(j in orderInfos.values){
                            if(i.first == j.deliveryTime){
                                i.second.add(j)
                            }
                        }
                    }

                    val temp = mutableListOf<OrderInfo>()
                    temp.addAll(orderInfos.values)

                    temp.removeAll{ it.deliveryState !=0 }

                    for(i in temp.groupingBy { it.deliveryTime }.eachCount()){
                        processedData_undelivered.add(Pair(i.key, mutableListOf<OrderInfo>()))
                    }
                    for(i in processedData_undelivered){
                        for(j in orderInfos.values){
                            if(i.first == j.deliveryTime){
                                if(j.deliveryState==0){
                                    i.second.add(j)
                                }
                            }
                        }
                    }
                    Log.d("TAG", processedData_undelivered.toString())
                    if(isNeedScrollChange==true){
                        ListFragment.initalizeAdapter()
                        //HomeFragment.instance!!.init(false)
                    }

                } else {
                    Log.d("TAG", "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
            }
        }
    }

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    val TAG = "myTag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        //Log.d(TAG,list.groupingBy { it }.eachCount().filter { it.value > 1 }.toString())
        //Log.d(TAG,list.groupingBy { it }.eachCount().toString())

        loadDataFromFirestore(d1,d2,false)

        val fragmentList = listOf(SiginalFragment(),SuggestionFragment(),HomeFragment(),ListFragment())
        val adapter = FragmentAdapter(this)
        adapter.fragmentList = fragmentList

        binding.viewPager.adapter = adapter
        val tabTitles = listOf<String>("신호","건의사항","홈","리스트")
        TabLayoutMediator(binding.tab,binding.viewPager){tab,position->
            tab.text = tabTitles[position]

        }.attach()
        binding.viewPager.isUserInputEnabled = false







    }



    fun addTestData(){
        //val a = orderInfos.get(0).copy()
        //a.deliveryState = 2
        //Log.d(TAG, orderInfos.get(0).deliveryState.toString()+"dd")
        //db.collection("orderInfo").document("testdata3").set(a)
    }





}
