package com.shivam.raymond.fragments

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.shivam.raymond.R
import com.shivam.raymond.databinding.FragmentAddFabricInfoBinding
import com.shivam.raymond.models.FabricInfoModel
import timber.log.Timber
import java.io.ByteArrayOutputStream

class AddFabricInfoFragment : BaseFragment() {
    private lateinit var addFabricInfoBinding: FragmentAddFabricInfoBinding
    private val args: AddFabricInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addFabricInfoBinding = FragmentAddFabricInfoBinding.inflate(layoutInflater)
        return addFabricInfoBinding.root
    }


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            findNavController().navigate(EnterFabricCodeFragmentDirections.actionAddFabricImageFragmentToScanQrCodeFragment())
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission needed to capture image.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.fabricCode == null) {
            addFabricInfoBinding.btnCaptureImage.setOnClickListener {
                if (hasPermission(Manifest.permission.CAMERA)) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraCaptureIntent.launch(cameraIntent)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            addFabricInfoBinding.btnSaveFabricDetails.setOnClickListener {
                if (validateInputData()) {
                    addFabricInfoBinding.btnSaveFabricDetails.visibility = View.GONE
                    addFabricInfoBinding.pbImageUploading.visibility = View.VISIBLE

                    uploadImageToFirebase()
                }
            }

        } else {
            //todo this is modify old fabric
        }

    }

    private fun uploadImageToFirebase() {

        val storageRef = storage.reference
        val imageUploadRef = storageRef.child("fabric/${addFabricInfoBinding.etFabricCode.editText?.text.toString()}.jpg")

        val bitmap = (addFabricInfoBinding.ivUploadImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageUploadRef.putBytes(data)
            .addOnFailureListener {
                addFabricInfoBinding.btnSaveFabricDetails.visibility = View.VISIBLE
                addFabricInfoBinding.pbImageUploading.visibility = View.GONE
                Toast.makeText(requireContext(), "Error saving data!", Toast.LENGTH_SHORT).show()
                Timber.e("Error uploading Image")
            }
            .addOnSuccessListener {
                Timber.d("Successfully uploaded Image")
                imageUploadRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val payload = FabricInfoModel(
                        fabricCode = addFabricInfoBinding.etFabricCode.editText?.text.toString(),
                        fabricLength = addFabricInfoBinding.etFabricLength.editText?.text.toString().toFloat(),
                        fabricWidth = addFabricInfoBinding.etFabricWidth.editText?.text.toString().toFloat(),
                        imageUrl = downloadUrl.toString(),
                        rackNumber = addFabricInfoBinding.etRackNumber.editText?.text.toString(),
                        batch = addFabricInfoBinding.etBatch.editText?.text.toString(),
                        fileNumber = addFabricInfoBinding.etFileNumber.editText?.text.toString()
                    )

                    db.collection("fabric")
                        .add(payload)
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Failed to add data",
                                Toast.LENGTH_SHORT
                            ).show()
                            Timber.e("Failed to add data")
                        }
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Data added successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            Timber.d("Data added successfully")
                            findNavController().navigateUp()
                        }
                }

            }
    }

    private var cameraCaptureIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {

                val photo = result.data!!.extras!!.get("data") as Bitmap
                addFabricInfoBinding.ivUploadImage.setImageBitmap(photo)
                addFabricInfoBinding.ivUploadImage.visibility = View.VISIBLE
                addFabricInfoBinding.btnCaptureImage.text = getString(R.string.capture_image_again)


            } catch (e: Exception) {
                addFabricInfoBinding.ivUploadImage.visibility = View.GONE
                addFabricInfoBinding.btnCaptureImage.text = getString(R.string.capture_image)

                Timber.e("Error getting image $e")
            }
        }


    fun validateInputData(): Boolean {
        var allCorrect = true

        if (addFabricInfoBinding.etFabricCode.editText?.text.isNullOrEmpty()) {
            addFabricInfoBinding.etFabricCode.error = "Enter fabric code"
            allCorrect = false
        } else {
            addFabricInfoBinding.etFabricCode.error = null
        }

        if (addFabricInfoBinding.etRackNumber.editText?.text.isNullOrEmpty()) {
            addFabricInfoBinding.etRackNumber.error = "Enter rack number"
            allCorrect = false
        } else {
            addFabricInfoBinding.etRackNumber.error = null
        }
        if (addFabricInfoBinding.etFabricLength.editText?.text.isNullOrEmpty()) {
            addFabricInfoBinding.etFabricLength.error = "Enter fabric length"
            allCorrect = false
        } else {
            addFabricInfoBinding.etFabricLength.error = null
        }
        if (addFabricInfoBinding.etFabricWidth.editText?.text.isNullOrEmpty()) {
            addFabricInfoBinding.etFabricWidth.error = "Enter fabric width"
            allCorrect = false
        } else {
            addFabricInfoBinding.etFabricWidth.error = null
        }
        if (addFabricInfoBinding.etBatch.editText?.text.isNullOrEmpty()) {
            addFabricInfoBinding.etBatch.error = "Enter batch"
            allCorrect = false
        } else {
            addFabricInfoBinding.etBatch.error = null
        }

        if (addFabricInfoBinding.etFileNumber.editText?.text.isNullOrEmpty()) {
            addFabricInfoBinding.etFileNumber.error = "Enter file number"
            allCorrect = false
        } else {
            addFabricInfoBinding.etFileNumber.error = null
        }
        if (addFabricInfoBinding.ivUploadImage.visibility == View.GONE) {
            Toast.makeText(requireContext(), "Please capture an image", Toast.LENGTH_SHORT).show()
            allCorrect = false
        }

        return allCorrect
    }
}