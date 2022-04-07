package com.shivam.raymond.fragments

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.shivam.raymond.R
import com.shivam.raymond.databinding.FragmentEnterFabricImageBinding
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat


class EnterFabricImageFragment : BaseFragment() {
    private lateinit var enterFabricImageBinding: FragmentEnterFabricImageBinding
    private val args: EnterFabricImageFragmentArgs by navArgs()
    private lateinit var imageDownloadUrl: String
    var isImageChanged = false

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
                launchCameraIntent()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

        }
    }

    lateinit var photoUri: Uri
    private fun launchCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { cameraIntent ->
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: java.lang.Exception) {
                Timber.e("poiu Error creating file $ex")
                null
            }

            photoFile?.also {
                val photoURI = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.shivam.raymond.fileProvider",
                    it
                )
                photoUri=photoURI
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                Timber.d("poiu PhotoUri-> $photoURI")
                cameraCaptureIntent.launch(cameraIntent)
            }
        }
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis())
        val storageDir = requireActivity().cacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {

            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Timber.d("poiu File created in cache-> $currentPhotoPath")
        }
    }


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
       launchCameraIntent()
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

                val takenImage = BitmapFactory.decodeFile(currentPhotoPath)
                enterFabricImageBinding.ivUploadImage.load(takenImage)
                enterFabricImageBinding.ivUploadImage.visibility = View.VISIBLE
                enterFabricImageBinding.btnCaptureImage.text = getString(R.string.capture_image_again)

                enterFabricImageBinding.btnSaveFabricDetails.visibility = View.VISIBLE
                enterFabricImageBinding.btnSaveFabricDetails.setOnClickListener {
                    it.visibility = View.GONE
                    enterFabricImageBinding.pbImageUploading.visibility = View.VISIBLE
                    uploadImageToFirebase(args.docId)
                }
            } catch (e: Exception) {
                enterFabricImageBinding.ivUploadImage.visibility = View.GONE
                enterFabricImageBinding.btnCaptureImage.text = getString(R.string.capture_image)
                enterFabricImageBinding.btnSaveFabricDetails.visibility = View.GONE

                Timber.e("Error getting image $e")
            }
        }


    private fun uploadImageToFirebase(docId: String) {
        val storageRef = storage.reference
        val imageUploadRef = storageRef.child("fabric/$docId.jpg")

        imageUploadRef.putFile(photoUri)
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
                        .document(docId)
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
                enterFabricImageBinding.tvFabricInfo.text = "Fabric Code: ${document["fabricCode"]}\nRack Number: ${document["rackNumber"]}\nBatch: ${document["batch"]}"

                if (document["imageUrl"] != null) {
                    enterFabricImageBinding.ivUploadImage.load(document["imageUrl"])
                    enterFabricImageBinding.ivUploadImage.visibility = View.VISIBLE
                    enterFabricImageBinding.btnCaptureImage.text = getString(R.string.capture_image_again)
                }


            }
            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }

    }
}