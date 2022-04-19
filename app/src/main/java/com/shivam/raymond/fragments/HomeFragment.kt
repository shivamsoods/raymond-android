package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.shivam.raymond.ScanQrEnum
import com.shivam.raymond.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber


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

        homeBinding.btnGotoQrCodeScanning.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToEnterFabricCodeFragment(
                    ScanQrEnum.QR_CODE_FLOW
                )
            )
        }

        homeBinding.btnFetchAPI.setOnClickListener {

            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToEnterFabricCodeFragment(
                    ScanQrEnum.API_FLOW
                )
            )


        }

        homeBinding.btnGotoAddFabricImage.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToEnterFabricCodeFragment(
                    ScanQrEnum.ADD_IMAGE
                )
            )
        }

        homeBinding.btnGotoAddFabricDetail.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAddViewFabricInfoFragment(
                    null,
                    "Add New Fabric",
                    null
                )
            )
        }

        homeBinding.btnGotoViewModifyFabricDetail.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToEnterFabricCodeFragment(
                    ScanQrEnum.VIEW_MODIFY_FABRIC_DETAIL
                )
            )
        }
    }



}