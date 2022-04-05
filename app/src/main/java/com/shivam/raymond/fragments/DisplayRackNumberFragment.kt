package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.shivam.raymond.databinding.FragmentDisplayRackNumberBinding
import timber.log.Timber


class DisplayRackNumberFragment : BaseFragment() {
    private lateinit var displayRackNumberBinding: FragmentDisplayRackNumberBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        displayRackNumberBinding = FragmentDisplayRackNumberBinding.inflate(layoutInflater)
        return displayRackNumberBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        checkForExistingFabricCode(args.fabricCode)

        displayRackNumberBinding.btnNext.setOnClickListener {

        }
    }

    private fun checkForExistingFabricCode(fabricCode: String) {
        db.collection("fabric")
            .whereEqualTo("fabricCode", fabricCode)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    Toast.makeText(
                        requireContext(),
                        "No such fabric code",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                } else {
                    val document = it.documents[0]
                    displayRackNumberBinding.tvFabricInfo.text = "Fabric Code: $fabricCode\nRack Number: ${document["rackNumber"]}"

                    if (document["imageUrl"] != null) {
                        displayRackNumberBinding.ivUploadImage.load(document["imageUrl"])
                        displayRackNumberBinding.ivUploadImage.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }

    }
}