package com.example.a_cha_delivery

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.iterator
import com.example.a_cha_delivery.data_classes.OrderInfo
import com.example.a_cha_delivery.databinding.FragmentHomeBinding
import com.example.a_cha_delivery.databinding.NeedItemInfoBinding
import com.example.a_cha_delivery.databinding.OrderListInfoBinding
import com.example.a_cha_delivery.databinding.OrderListOnMapBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    lateinit var binding:FragmentHomeBinding

    private lateinit var mapView: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    val orderDataByTime = mutableListOf<OrderInfo>()

    fun loadDataByTime(){
        orderDataByTime.clear()

        for(i in MainActivity.orderInfos){

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        binding.btnOrderInfoClose.setOnClickListener {
            binding.orderItemList.removeAllViews()
            binding.layoutOrderInfos.visibility = View.GONE
        }

        var enabledDeliveryTime = mutableListOf<String>()
        val info = MainActivity.orderInfos

        val times = info.groupingBy { it.deliveryTime }.eachCount().toList().toMutableList()
        for(i in times){

            val date = i.first.toDate()
            val format = SimpleDateFormat("HH:mm")
            val startTString = format.format(date)
            enabledDeliveryTime.add(startTString)

        }

        for(i in enabledDeliveryTime) {
            val needItemInfoBinding = NeedItemInfoBinding.inflate(
                LayoutInflater.from(binding.root.context),
                binding.root,
                false
            )

            val hour = i.split(":").get(0)
            val minute = i.split(":").get(1)


            val format = SimpleDateFormat("HH:mm")
            val date = format.parse(i)
            val startT = date.time - (30 * 60 * 1000)

            val date1 = Date(startT)
            val format1 = SimpleDateFormat("HH:mm")
            val startTString = format1.format(date1)

            needItemInfoBinding.time.text =
                startTString + " ~ " + hour.toString() + ":" + minute.toString()


            binding.linearLayoutInScroll.addView(needItemInfoBinding.parentLayout)

            var thisTimeOrderList = mutableListOf<String>()
            for(j in info){
                if(hour.toInt()==j.deliveryTime.toDate().hours&&minute.toInt()==j.deliveryTime.toDate().minutes&&j.deliveryState==0){

                    thisTimeOrderList.addAll(j.orderItems)





                    val sdf = SimpleDateFormat("HH:mm")
                    val sT = j.deliveryTime.toDate()
                    val timeEnd = sdf.format(sT).toString()
                    val subtractedTime = Date(sT.time - (30 * 60 * 1000))
                    val timeStart = sdf.format(subtractedTime).toString()



                    var it = j.orderItems.groupingBy { it }.eachCount().toMutableMap()

                    var orderText = ""
                    for (j in it) {
                        orderText += j.key.toString() + " "+j.value.toString()+"개\n"
                    }
                    var timePartitionResult = thisTimeOrderList.groupingBy { it }.eachCount()

                    var thisTimeOrder = ""

                    for(k in timePartitionResult){
                        thisTimeOrder+= k.key + " " + k.value + "개\n"
                    }

                    needItemInfoBinding.needItems.text = thisTimeOrder


                }
            }





        }



        //시간 선택 버튼 만들기
        val list = MainActivity.orderInfos.groupingBy { it.deliveryTime }.eachCount()
        Log.d("myTag",list.toString())
        for(i in list){
            val sdf = SimpleDateFormat("HH:mm")

            Log.d("myTag",i.key.toDate().toString())
            val selectiveTime = sdf.format(i.key.toDate()).toString()

            val btnInstance = Button(this.context).apply{
                TODO("여기부터 하자")
                //resources.getColor(R.color.black).toDrawable()
                text = selectiveTime
            }
            binding.btnGroupLayout.addView(btnInstance,ViewGroup.LayoutParams(100,100))

        }

        val mF:SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mF.getMapAsync(this)
        return binding.root
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapView = googleMap
        var markers = mutableListOf<Pair<MarkerOptions,Int>>()
        if(MainActivity.location=="은마"){
            val startLocation = LatLng(37.497576, 127.065434)
            val db = Firebase.firestore
            db.collection("pinGeo").document("은마아파트").get().addOnSuccessListener { document->
                for(i in 1..31){
                    if(i !=4&&i!=14&&i!=24){

                        for(j in MainActivity.orderInfos){
                            if(j.deliveryState==0){
                                var string = j.detailedLocation.split("동")[0]
                                var res = string.replace("[^0-9]".toRegex(), "").toInt()

                                if(res == i){
                                    val pos = document.get("${i}") as GeoPoint
                                    val latLng = LatLng(pos.latitude,pos.longitude)

                                    val marker = MarkerOptions()
                                        .position(latLng)
                                        .title(i.toString()+"동")


                                    mapView.addMarker(marker)
                                }




                            }
                        }



                    }
                }




            }


            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation,16.2f))


            mapView.setOnMarkerClickListener(object :GoogleMap.OnMarkerClickListener{
                override fun onMarkerClick(marker: Marker): Boolean {
                    binding.orderItemList.removeAllViews()

                    binding.layoutOrderInfos.visibility = View.VISIBLE
                    val dongOrderInfos = MainActivity.orderInfos.groupingBy { it.detailedLocation.split("동")[0]}.eachCount().toList()




                    for(i in MainActivity.orderInfos){
                        if(i.detailedLocation.split("동")[0].replace("[^0-9]".toRegex(), "").toInt() ==marker.title!!.replace("동","").toInt()
                            &&i.deliveryState==0){
                            val elementBinding:OrderListOnMapBinding = OrderListOnMapBinding.inflate(LayoutInflater.from(binding.root.context),binding.root,false)
                            elementBinding.textUserID.text = i.userID
                            elementBinding.userAddress.text = i.detailedLocation



                            //
                            var it = i.orderItems.groupingBy { it }.eachCount().toMutableMap()

                            var orderText = ""
                            for (j in it) {
                                orderText += j.key.toString() + " "+j.value.toString()+"개\n"
                            }




                            elementBinding.textOrderElements.text = orderText


                            binding.orderItemList.addView(elementBinding.root)
                        }
                    }
                    return false
                }
            })


        }









    }

}