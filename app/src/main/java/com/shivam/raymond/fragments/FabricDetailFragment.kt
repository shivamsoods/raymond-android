package com.shivam.raymond.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.shivam.raymond.R
import com.shivam.raymond.databinding.FragmentFabricDetailBinding
import com.shivam.raymond.models.FabricInfoModel
import timber.log.Timber


class FabricDetailFragment : BaseFragment() {
    private lateinit var fabricDetailBinding: FragmentFabricDetailBinding
private val args: FabricDetailFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fabricDetailBinding = FragmentFabricDetailBinding.inflate(layoutInflater)
        return fabricDetailBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
checkForExistingFabricCode(args.docId)

        fabricDetailBinding.btnDone.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment,true)
            findNavController().navigate(R.id.homeFragment)
        }


    }

    @SuppressLint("SetTextI18n")
    private fun checkForExistingFabricCode(docId: String) {
        db.collection("fabric")
            .document(docId)
            .get()
            .addOnSuccessListener {
                val document = it

                val payload = FabricInfoModel(
                    fabricCode = document["fabricCode"].toString(),
                    fabricLength = if (document["fabricLength"] == null) "" else document["fabricLength"].toString(),
                    fabricWidth = if (document["fabricWidth"] == null) "" else document["fabricWidth"].toString(),
                    imageUrl = if (document["imageUrl"] == null) null else document["imageUrl"].toString(),
                    rackNumber = if (document["rackNumber"] == null) "" else document["rackNumber"].toString(),
                    batch = if (document["batch"] == null) "" else document["batch"].toString(),
                    fileNumber = if (document["fileNumber"] == null) "" else document["fileNumber"].toString(),
                    documentId = docId
                )


                fabricDetailBinding.tvFabricInfo.text ="Fabric Code: ${payload.fabricCode}\nFabric Length: ${payload.fabricLength}\nFabric Width: ${payload.fabricWidth}\nRack Number: ${payload.rackNumber}\nBatch: ${payload.batch}\nFile Number: ${payload.fileNumber}\n"

                if (payload.imageUrl != null) {
                    fabricDetailBinding.ivUploadImage.load(payload.imageUrl)
                    fabricDetailBinding.ivUploadImage.visibility = View.VISIBLE
                } else {
                    fabricDetailBinding.ivUploadImage.visibility = View.GONE
                }

            }
            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }

    }


}