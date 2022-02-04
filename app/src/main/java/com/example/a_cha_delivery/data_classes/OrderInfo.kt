package com.example.a_cha_delivery.data_classes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class OrderInfo(
    var deliveryState:Int =0,
    var deliveryTime:Timestamp=Timestamp(0,0),
    var detailedLocation:String="",
    var geoLocation:GeoPoint=GeoPoint(0.0,0.0),
    var orderItems:MutableList<String> = mutableListOf(),
    var userID:String = "",
    var userNeed:String = ""
)


data class ItemInfo(
    var ID:Int
)

object OrderItemsID{
    val itemID = hashMapOf(0 to "클럽샌드위치",1 to "데리야키 샌드위치")
}




