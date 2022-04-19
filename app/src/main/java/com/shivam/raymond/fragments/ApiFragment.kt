package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.shivam.raymond.R
import com.shivam.raymond.databinding.FragmentApiBinding


class ApiFragment : BaseFragment() {
    private lateinit var apiBinding: FragmentApiBinding
    private val args: ApiFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        apiBinding = FragmentApiBinding.inflate(layoutInflater)
        return apiBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiBinding.btnDone.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, true)
            findNavController().navigate(R.id.homeFragment)
        }

        setUpUiData()
    }

    private fun setUpUiData() {
        val payload =args.payload

            apiBinding.tvFabricInfo.text = "Fabric Code: ${payload.fabricCode}\nRack Number: ${payload.rackNumber}\nBatch: ${payload.batch}\nFile Number: ${payload.fileNumber}\nQuantity: ${payload.quantity}\n"

        if (payload.imageUrl != null) {
            apiBinding.ivUploadImage.load(payload.imageUrl)
            apiBinding.ivUploadImage.visibility = View.VISIBLE
            apiBinding.ivUploadImage.setOnClickListener {
                findNavController().navigate(
                    ApiFragmentDirections.actionApiFragmentToFullScreenImageFragment(
                        payload.imageUrl
                    )
                )
            }
        } else {
            apiBinding.ivUploadImage.visibility = View.GONE
        }
    }
}