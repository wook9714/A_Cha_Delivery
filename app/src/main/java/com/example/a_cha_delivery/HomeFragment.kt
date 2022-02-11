package com.example.a_cha_delivery

import android.location.Geocoder
import android.os.Bundle
import android.os.Debug
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
import com.example.a_cha_delivery.databinding.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
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


    var markers = mutableMapOf<Int,Marker>()
    var wantShowMarkerSet = mutableSetOf<Int>()
    var selectedTime:Timestamp? = null







    private lateinit var mapView: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    fun initialData(){
        //뷰 제거 후 데이터 초기화
        binding.linearLayoutInScroll.removeAllViews()
        for(i in markers){
            i.value.isVisible = false
        }






    }

    fun loadAllData(){
        initialData()
        for(i in MainActivity.processedData_undelivered){
            loadDataByTime(i.first)
        }
        selectedTime = null

    }


    fun loadDataByTime(time:Timestamp){
        //선택한 시간대에 있는 데이터 로드
        selectedTime = time
        for(i in MainActivity.processedData_undelivered){
            if(i.first==time){
                val needItemInfoBinding = NeedItemInfoBinding.inflate(LayoutInflater.from(binding.root.context),binding.root,false)

                //시간 처리
                val wantDeliverTime = SimpleDateFormat("HH:mm").format(i.first.toDate())
                val startT = i.first.toDate().time - (30 * 60 * 1000)
                val date1 = Date(startT)
                val format1 = SimpleDateFormat("HH:mm")
                val startTString = format1.format(date1)
                needItemInfoBinding.time.text =
                    startTString + " ~ " + wantDeliverTime

                //시간대에 맞는 아이템 출력
                var thisTimeOrderList = mutableListOf<String>()
                for(j in i.second){
                    thisTimeOrderList.addAll(j.orderItems)
                }
                var orderText = ""
                for(j in thisTimeOrderList.groupingBy { it }.eachCount()){
                    orderText += j.key +" "+ j.value.toString()+"개\n"
                }
                needItemInfoBinding.needItems.text= orderText
                binding.linearLayoutInScroll.addView(needItemInfoBinding.parentLayout)


                //마커 관련
                for(j in i.second){
                    wantShowMarkerSet.add(j.detailedLocation.split("동")[0].replace("[^0-9]".toRegex(), "").toInt())
                }
            }
        }
    }

    //마커 보여주기
    fun loadMarker(){
        for(i in wantShowMarkerSet){
            Log.d("marker",markers[i].toString())
            markers[i]!!.isVisible = true

        }
    }
    fun init(isFirst:Boolean){
        //binding.btnGroupLayout.removeAllViews()
        //binding.linearLayoutInScroll.removeAllViews()
        loadAllData()

        binding.btnOrderInfoClose.setOnClickListener {
            binding.orderItemList.removeAllViews()
            binding.layoutOrderInfos.visibility = View.GONE
        }


        //전체 데이터 로드
        binding.buttonAllView.setOnClickListener {
            wantShowMarkerSet.clear()
            loadAllData()
            loadMarker()
        }

        //시간 선택 버튼 만들기
        for(i in MainActivity.processedData_undelivered){
            val sdf = SimpleDateFormat("HH:mm")

            val selectiveTime = sdf.format(i.first.toDate()).toString()

            val bind = SelectiveBtnBinding.inflate(LayoutInflater.from(this.context),binding.root,false)
            bind.selectiveBtn.text = selectiveTime
            bind.selectiveBtn.setOnClickListener {
                wantShowMarkerSet.clear()
                initialData()
                loadDataByTime(i.first)
                loadMarker()
            }

            binding.btnGroupLayout.addView(bind.root)



        }
        if(!isFirst){
            initialData()
            loadMarker()
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        init(true)








        val mF:SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mF.getMapAsync(this)
        return binding.root
    }



    companion object {
        var instance:HomeFragment? = null
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
        if(MainActivity.location=="은마"){
            val startLocation = LatLng(37.497576, 127.065434)
            val db = Firebase.firestore
            db.collection("pinGeo").document("은마아파트").get().addOnSuccessListener { document->
                for(i in 1..31){
                    if(i!=4&&i!=14&&i!=24){
                        val loadedPos = document.get(i.toString()) as GeoPoint
                        val latlang = LatLng(loadedPos.latitude,loadedPos.longitude)
                        val marker = MarkerOptions().position(latlang).title(i.toString()+"동")
                        marker.visible(false)
                        markers.set(i,mapView.addMarker(marker)!!)
                    }
                }
            }.addOnSuccessListener {
                loadMarker()
            }


            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation,16.2f))


            mapView.setOnMarkerClickListener(object :GoogleMap.OnMarkerClickListener{
                override fun onMarkerClick(marker: Marker): Boolean {
                    binding.orderItemList.removeAllViews()
                    binding.layoutOrderInfos.visibility = View.VISIBLE

                    //val dongOrderInfos = MainActivity.orderInfos.values.groupingBy { it.detailedLocation.split("동")[0]}.eachCount().toList()

                    if(selectedTime == null){
                        for(i in MainActivity.orderInfos.values){
                            if(i.detailedLocation.split("동")[0].replace("[^0-9]".toRegex(), "").toInt() ==marker.title!!.replace("동","").toInt()
                                &&i.deliveryState==0){
                                val elementBinding:OrderListOnMapBinding = OrderListOnMapBinding.inflate(LayoutInflater.from(binding.root.context),binding.root,false)
                                elementBinding.textUserID.text = i.userID
                                elementBinding.userAddress.text = i.detailedLocation

                                var it = i.orderItems.groupingBy { it }.eachCount().toMutableMap()

                                var orderText = ""
                                for (j in it) {
                                    orderText += j.key.toString() + " "+j.value.toString()+"개\n"
                                }
                                elementBinding.textOrderElements.text = orderText
                                binding.orderItemList.addView(elementBinding.root)
                            }
                        }
                    }else{
                        for(i in MainActivity.processedData_undelivered){
                            if(i.first==selectedTime){
                                for(j in i.second){
                                    if(j.detailedLocation.split("동")[0].replace("[^0-9]".toRegex(), "").toInt() ==marker.title!!.replace("동","").toInt()
                                        &&j.deliveryState==0){
                                        val elementBinding:OrderListOnMapBinding = OrderListOnMapBinding.inflate(LayoutInflater.from(binding.root.context),binding.root,false)
                                        elementBinding.textUserID.text = j.userID
                                        elementBinding.userAddress.text = j.detailedLocation

                                        var it = j.orderItems.groupingBy { it }.eachCount().toMutableMap()

                                        var orderText = ""
                                        for (j in it) {
                                            orderText += j.key.toString() + " "+j.value.toString()+"개\n"
                                        }
                                        elementBinding.textOrderElements.text = orderText
                                        binding.orderItemList.addView(elementBinding.root)
                                    }
                                }
                                break
                            }
                        }
                    }

                    return false
                }
            })




        }


    }

}