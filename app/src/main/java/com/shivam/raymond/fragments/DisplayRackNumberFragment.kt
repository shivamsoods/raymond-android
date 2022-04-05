package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.shivam.raymond.databinding.FragmentDisplayRackNumberBinding
import timber.log.Timber


class DisplayRackNumberFragment : BaseFragment() {
    private lateinit var displayRackNumberBinding: FragmentDisplayRackNumberBinding
    private val args: DisplayRackNumberFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        displayRackNumberBinding = FragmentDisplayRackNumberBinding.inflate(layoutInflater)
        return displayRackNumberBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForExistingFabricCode(args.docId)

        displayRackNumberBinding.btnNext.setOnClickListener {
            findNavController().navigate(
                DisplayRackNumberFragmentDirections.actionDisplayRackNumberFragmentToScanQrCodeAgainFragment(
                    args.docId
                )
            )
        }
    }

    private fun checkForExistingFabricCode(docId: String) {
        db.collection("fabric")
            .document(docId)
            .get()
            .addOnSuccessListener {
                displayRackNumberBinding.btnNext.visibility = View.VISIBLE
                val document = it
                displayRackNumberBinding.tvFabricInfo.text = "Fabric Code: ${document["fabricCode"]}\nRack Number: ${document["rackNumber"]}\nBatch: ${document["batch"]}"

                if (document["imageUrl"] != null) {
                    displayRackNumberBinding.ivUploadImage.load(document["imageUrl"])
                    displayRackNumberBinding.ivUploadImage.visibility = View.VISIBLE
                } else {
                    displayRackNumberBinding.ivUploadImage.visibility = View.GONE
                }

            }
            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }

    }
}