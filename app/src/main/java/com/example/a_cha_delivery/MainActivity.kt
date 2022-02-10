package com.example.a_cha_delivery


import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.a_cha_delivery.data_classes.OrderInfo
import com.example.a_cha_delivery.databinding.ActivityMainBinding
import com.example.a_cha_delivery.function.DataFunction.convertDateToTimestamp
import com.example.a_cha_delivery.main_activity.FragmentAdapter
import com.example.a_cha_delivery.main_activity.ListFragment
import com.example.a_cha_delivery.main_activity.SiginalFragment
import com.example.a_cha_delivery.main_activity.SuggestionFragment

import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.sql.Date
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(){
    val db = Firebase.firestore
    companion object{
        val location:String = "은마"
        val orderInfos:MutableList<OrderInfo> = mutableListOf()
    }

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    val TAG = "myTag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val d1 = Date(convertDateToTimestamp("2022-1-20"))
        val d2 = Date(convertDateToTimestamp("2022-1-23"))
        val list = listOf("orange", "apple", "apple", "banana", "water", "bread", "banana")

        //Log.d(TAG,list.groupingBy { it }.eachCount().filter { it.value > 1 }.toString())
        //Log.d(TAG,list.groupingBy { it }.eachCount().toString())

        loadDataFromFirestore(d1,d2)

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

    fun loadDataFromFirestore(wantLoadDateStart:Date,wantLoadDateEnd:Date){
        db.collection("orderInfo").
        whereGreaterThan("deliveryTime",wantLoadDateStart).whereLessThan("deliveryTime",wantLoadDateEnd).get().addOnSuccessListener { document ->
            if (document != null) {
                for(i in document){
                    orderInfos.add(i.toObject<OrderInfo>())
                }
                //addTestData()
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    fun addTestData(){
        val a = orderInfos.get(0).copy()
        a.deliveryState = 2
        Log.d(TAG, orderInfos.get(0).deliveryState.toString()+"dd")
        db.collection("orderInfo").document("testdata3").set(a)
    }





}
