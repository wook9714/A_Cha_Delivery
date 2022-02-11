package com.example.a_cha_delivery.main_activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.a_cha_delivery.R
import com.example.a_cha_delivery.databinding.FragmentListBinding
import com.example.a_cha_delivery.databinding.ListPageViewBinding
import com.google.android.material.tabs.TabLayoutMediator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var binding:FragmentListBinding

class ListFragment : Fragment() {




    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater,container,false)


        initalizeAdapter()
        return binding.root
    }



    companion object {
        @JvmStatic
        val listAdapter = ListAdapter()
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun initalizeAdapter(){
            val menuList = mutableListOf<String>("배송중","배송완료","미결제","전체")
            listAdapter.menuList = menuList
            binding.listViewPager.adapter = listAdapter




            TabLayoutMediator(binding.tabLayout, binding.listViewPager){ tab, position ->
                tab.text = menuList[position]
            }.attach()
        }
    }
}