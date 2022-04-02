package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.shivam.raymond.databinding.FragmentHomeBinding


class HomeFragment : BaseFragment() {

    private lateinit var homeBinding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(layoutInflater)
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeBinding.btnGotoAddFabricImage.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddFabricImageFragment())
        }

        homeBinding.btnGotoAddFabricInformation.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddFabricInfoFragment(null))
        }
    }

}