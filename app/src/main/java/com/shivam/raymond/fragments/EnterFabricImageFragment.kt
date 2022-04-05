package com.shivam.raymond.fragments

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.shivam.raymond.R
import com.shivam.raymond.ScanQrEnum
import com.shivam.raymond.databinding.FragmentEnterFabricImageBinding
import timber.log.Timber
import java.io.ByteArrayOutputStream


class EnterFabricImageFragment : BaseFragment() {
    private lateinit var enterFabricImageBinding: FragmentEnterFabricImageBinding
    private val args: EnterFabricImageFragmentArgs by navArgs()


    private var fabricDocumentId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterFabricImageBinding = FragmentEnterFabricImageBinding.inflate(layoutInflater)
        return enterFabricImageBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkForExistingFabricCode(args.docId)

        enterFabricImageBinding.btnCaptureImage.setOnClickListener {
            if (hasPermission(Manifest.permission.CAMERA)) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraCaptureIntent.launch(cameraIntent)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            findNavController().navigate(
                EnterFabricCodeFragmentDirections.actionEnterFabricCodeFragmentToScanQrCodeFragment(
                    ScanQrEnum.ADD_IMAGE
                )
            )
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission needed to capture image.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private var cameraCaptureIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {

                val photo = result.data!!.extras!!.get("data") as Bitmap
                enterFabricImageBinding.ivUploadImage.setImageBitmap(photo)
                enterFabricImageBinding.ivUploadImage.visibility = View.VISIBLE
                enterFabricImageBinding.btnCaptureImage.text = getString(R.string.capture_image_again)

                enterFabricImageBinding.btnSaveFabricDetails.visibility = View.VISIBLE
                enterFabricImageBinding.btnSaveFabricDetails.setOnClickListener {
                    it.visibility = View.GONE
                    enterFabricImageBinding.pbImageUploading.visibility = View.VISIBLE
                    uploadImageToFirebase(photo, args.docId)
                }
            } catch (e: Exception) {
                enterFabricImageBinding.ivUploadImage.visibility = View.GONE
                enterFabricImageBinding.btnCaptureImage.text = getString(R.string.capture_image)
                enterFabricImageBinding.btnSaveFabricDetails.visibility = View.GONE

                Timber.e("Error getting image $e")
            }
        }


    private fun uploadImageToFirebase(bitmap: Bitmap, docId: String) {
        val storageRef = storage.reference
        val imageUploadRef = storageRef.child("fabric/$docId.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageUploadRef.putBytes(data)
            .addOnFailureListener {
                enterFabricImageBinding.btnSaveFabricDetails.visibility = View.VISIBLE
                enterFabricImageBinding.pbImageUploading.visibility = View.GONE
                Toast.makeText(requireContext(), "Error saving data!", Toast.LENGTH_SHORT).show()
                Timber.e("Error uploading Image")
            }
            .addOnSuccessListener {
                Timber.d("Successfully uploaded Image")
                imageUploadRef.downloadUrl.addOnSuccessListener {
                    db.collection("fabric")
                        .document(fabricDocumentId)
                        .update("imageUrl", it.toString())
                        .addOnSuccessListener {
                            Timber.d("Successfully updated image URL")
                            Toast.makeText(
                                requireContext(),
                                "Data added with success!",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack(R.id.homeFragment,true)
                            findNavController().navigate(R.id.homeFragment)                     }
                        .addOnFailureListener { Timber.d("Failed to update image URL") }
                }

            }
    }


    private fun checkForExistingFabricCode(docId: String) {
        db.collection("fabric")
            .document(docId)
            .get()
            .addOnSuccessListener {
                enterFabricImageBinding.btnCaptureImage.visibility = View.VISIBLE
                val document = it
                fabricDocumentId = document.id
                enterFabricImageBinding.tvFabricInfo.text = "Fabric Code: ${it["fabricCode"]}\nRack Number: ${document["rackNumber"]}\nBatch: ${document["batch"]}"

                if (document["imageUrl"] != null) {
                    enterFabricImageBinding.ivUploadImage.load(document["imageUrl"])
                    enterFabricImageBinding.ivUploadImage.visibility = View.VISIBLE
                    enterFabricImageBinding.btnCaptureImage.text = getString(R.string.capture_image_again)
                }

            }
            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }

    }
}