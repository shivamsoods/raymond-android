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
import coil.load
import com.shivam.raymond.R
import com.shivam.raymond.databinding.FragmentAddViewFabricInfoBinding
import com.shivam.raymond.models.FabricInfoModel
import timber.log.Timber
import java.io.ByteArrayOutputStream

class AddViewFabricInfoFragment : BaseFragment() {
    private lateinit var addViewFabricInfoBinding: FragmentAddViewFabricInfoBinding
    private val args: AddViewFabricInfoFragmentArgs by navArgs()
    private var fabricDocumentId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addViewFabricInfoBinding = FragmentAddViewFabricInfoBinding.inflate(layoutInflater)
        return addViewFabricInfoBinding.root
    }


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraCaptureIntent.launch(cameraIntent)
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


        addViewFabricInfoBinding.btnCaptureImage.setOnClickListener {
            if (hasPermission(Manifest.permission.CAMERA)) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraCaptureIntent.launch(cameraIntent)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        addViewFabricInfoBinding.btnSaveFabricDetails.setOnClickListener {
            if (args.fabricCode != null) {

                if (validateInputData()) {
                    addViewFabricInfoBinding.btnSaveFabricDetails.visibility = View.GONE
                    addViewFabricInfoBinding.pbImageUploading.visibility = View.VISIBLE

                    uploadImageToFirebase(true)
                }
            } else {
                if (validateInputData()) {
                    addViewFabricInfoBinding.btnSaveFabricDetails.visibility = View.GONE
                    addViewFabricInfoBinding.pbImageUploading.visibility = View.VISIBLE

                    uploadImageToFirebase(false)
                }
            }

        }

        if (args.fabricCode != null) {
            checkForExistingFabricCode(args.fabricCode!!)
        }
    }

    private fun uploadImageToFirebase(isUpdate: Boolean) {

        val storageRef = storage.reference
        val imageUploadRef = storageRef.child("fabric/${addViewFabricInfoBinding.etFabricCode.editText?.text.toString()}.jpg")
        try {
            val bitmap = (addViewFabricInfoBinding.ivUploadImage.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            imageUploadRef.putBytes(data)
                .addOnFailureListener {
                    addViewFabricInfoBinding.btnSaveFabricDetails.visibility = View.VISIBLE
                    addViewFabricInfoBinding.pbImageUploading.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Error saving data!",
                        Toast.LENGTH_SHORT
                    ).show()
                    Timber.e("Error uploading Image")
                }
                .addOnSuccessListener {
                    Timber.d("Successfully uploaded Image")
                    imageUploadRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val payload = FabricInfoModel(
                            fabricCode = addViewFabricInfoBinding.etFabricCode.editText?.text.toString(),
                            fabricLength = addViewFabricInfoBinding.etFabricLength.editText?.text.toString(),
                            fabricWidth = addViewFabricInfoBinding.etFabricWidth.editText?.text.toString(),
                            imageUrl = downloadUrl.toString(),
                            rackNumber = addViewFabricInfoBinding.etRackNumber.editText?.text.toString(),
                            batch = addViewFabricInfoBinding.etBatch.editText?.text.toString(),
                            fileNumber = addViewFabricInfoBinding.etFileNumber.editText?.text.toString()
                        )

                        if (isUpdate) {
                            db.collection("fabric").document(fabricDocumentId)
                                .set(payload)
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
                                        "Data updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Timber.d("Data updated successfully")
                                    findNavController().navigateUp()
                                }
                        } else {
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
        } catch (e: Exception) {
            Timber.e("Error uploading image")
        }

    }

    private var cameraCaptureIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {

                val photo = result.data!!.extras!!.get("data") as Bitmap
                addViewFabricInfoBinding.ivUploadImage.setImageBitmap(photo)
                addViewFabricInfoBinding.ivUploadImage.visibility = View.VISIBLE
                addViewFabricInfoBinding.btnCaptureImage.text = getString(R.string.capture_image_again)


            } catch (e: Exception) {
                addViewFabricInfoBinding.ivUploadImage.visibility = View.GONE
                addViewFabricInfoBinding.btnCaptureImage.text = getString(R.string.capture_image)

                Timber.e("Error getting image $e")
            }
        }


    fun validateInputData(): Boolean {
        var allCorrect = true

        if (addViewFabricInfoBinding.etFabricCode.editText?.text.isNullOrEmpty()) {
            addViewFabricInfoBinding.etFabricCode.error = "Enter fabric code"
            allCorrect = false
        } else {
            addViewFabricInfoBinding.etFabricCode.error = null
        }

        if (addViewFabricInfoBinding.etFabricCodeAgain.editText?.text.isNullOrEmpty()) {
            addViewFabricInfoBinding.etFabricCodeAgain.error = "Enter fabric code"
            allCorrect = false
        } else {
            if (addViewFabricInfoBinding.etFabricCodeAgain.editText?.text.toString()!=addViewFabricInfoBinding.etFabricCode.editText?.text.toString()) {
                addViewFabricInfoBinding.etFabricCodeAgain.error = "Fabric code doesn't match!"
                allCorrect = false
            } else{
                addViewFabricInfoBinding.etFabricCodeAgain.error = null
            }
        }

        if (addViewFabricInfoBinding.etRackNumber.editText?.text.isNullOrEmpty()) {
            addViewFabricInfoBinding.etRackNumber.error = "Enter rack number"
            allCorrect = false
        } else {
            addViewFabricInfoBinding.etRackNumber.error = null
        }
        if (addViewFabricInfoBinding.etFabricLength.editText?.text.isNullOrEmpty()) {
            addViewFabricInfoBinding.etFabricLength.error = "Enter fabric length"
            allCorrect = false
        } else {
            addViewFabricInfoBinding.etFabricLength.error = null
        }
        if (addViewFabricInfoBinding.etFabricWidth.editText?.text.isNullOrEmpty()) {
            addViewFabricInfoBinding.etFabricWidth.error = "Enter fabric width"
            allCorrect = false
        } else {
            addViewFabricInfoBinding.etFabricWidth.error = null
        }
        if (addViewFabricInfoBinding.etBatch.editText?.text.isNullOrEmpty()) {
            addViewFabricInfoBinding.etBatch.error = "Enter batch"
            allCorrect = false
        } else {
            addViewFabricInfoBinding.etBatch.error = null
        }

        if (addViewFabricInfoBinding.etFileNumber.editText?.text.isNullOrEmpty()) {
            addViewFabricInfoBinding.etFileNumber.error = "Enter file number"
            allCorrect = false
        } else {
            addViewFabricInfoBinding.etFileNumber.error = null
        }
        if (addViewFabricInfoBinding.ivUploadImage.visibility == View.GONE) {
            Toast.makeText(requireContext(), "Please capture an image", Toast.LENGTH_SHORT).show()
            allCorrect = false
        }

        return allCorrect
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

                    val payload = FabricInfoModel(
                        fabricCode = document["fabricCode"].toString(),
                        fabricLength = if(document["fabricLength"]==null) "" else document["fabricLength"].toString(),
                        fabricWidth = if(document["fabricWidth"]==null) "" else document["fabricWidth"].toString(),
                        imageUrl = if(document["imageUrl"]==null) null else document["imageUrl"].toString(),
                        rackNumber =if(document["rackNumber"]==null) "" else document["rackNumber"].toString(),
                        batch = if(document["batch"]==null) "" else document["batch"].toString(),
                        fileNumber = if(document["fileNumber"]==null) "" else document["fileNumber"].toString()
                    )
                    setUiWithData(payload)
                    fabricDocumentId = document.id

                }
            }
            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }

    }

    private fun setUiWithData(fabricInfoItem: FabricInfoModel) {
        addViewFabricInfoBinding.etFabricCode.editText?.setText(fabricInfoItem.fabricCode)
        addViewFabricInfoBinding.etFabricCodeAgain.editText?.setText(fabricInfoItem.fabricCode)
        addViewFabricInfoBinding.etFabricLength.editText?.setText(fabricInfoItem.fabricLength)
        addViewFabricInfoBinding.etFabricWidth.editText?.setText(fabricInfoItem.fabricWidth)
        addViewFabricInfoBinding.etRackNumber.editText?.setText(fabricInfoItem.rackNumber)
        addViewFabricInfoBinding.etBatch.editText?.setText(fabricInfoItem.batch)
        addViewFabricInfoBinding.etFileNumber.editText?.setText(fabricInfoItem.fileNumber)
        if(fabricInfoItem.imageUrl==null){
            addViewFabricInfoBinding.ivUploadImage.visibility=View.GONE
        }else{
            addViewFabricInfoBinding.ivUploadImage.visibility = View.VISIBLE
            addViewFabricInfoBinding.ivUploadImage.load(fabricInfoItem.imageUrl)
            addViewFabricInfoBinding.btnCaptureImage.text = getString(R.string.capture_image_again)
        }

    }

}