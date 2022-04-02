package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.shivam.raymond.ScanQrEnum
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
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToEnterFabricCodeFragment(ScanQrEnum.ADD_IMAGE))
        }

        homeBinding.btnGotoAddFabricDetail.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddViewFabricInfoFragment(null,"Add New Fabric"))
        }

        homeBinding.btnGotoViewModifyFabricDetail.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToEnterFabricCodeFragment(ScanQrEnum.VIEW_FABRIC_DETAIL))
        }
    }

}