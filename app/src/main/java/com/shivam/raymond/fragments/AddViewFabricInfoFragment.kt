package com.shivam.raymond.fragments

import android.Manifest
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.shivam.raymond.R
import com.shivam.raymond.databinding.FragmentAddViewFabricInfoBinding
import com.shivam.raymond.models.FabricInfoModel
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat


class AddViewFabricInfoFragment : BaseFragment() {
    private lateinit var addViewFabricInfoBinding: FragmentAddViewFabricInfoBinding
    private val args: AddViewFabricInfoFragmentArgs by navArgs()
    private lateinit var imageDownloadUrl: String
    var isImageChanged = false
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
            launchCameraIntent()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission needed to capture image.",
                Toast.LENGTH_SHORT
            ).show()
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
                photoUri = photoURI
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        addViewFabricInfoBinding.btnCaptureImage.setOnClickListener {
            if (hasPermission(Manifest.permission.CAMERA)) {
                launchCameraIntent()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        addViewFabricInfoBinding.btnSaveFabricDetails.setOnClickListener {

            if (args.docId != null) {
                /**
                 * Updating fabric details
                 */
                if (validateInputData()) {
                    addViewFabricInfoBinding.btnSaveFabricDetails.visibility = View.GONE
                    addViewFabricInfoBinding.pbImageUploading.visibility = View.VISIBLE
                    if (isImageChanged) {
                        uploadImageToFirebase(true)
                    } else {
                        updateDataOnlyToFirebase(imageDownloadUrl)
                    }
                }
            } else {
                /**
                 * Saving new fabric details
                 */
                if (validateInputData()) {
                    addViewFabricInfoBinding.btnSaveFabricDetails.visibility = View.GONE
                    addViewFabricInfoBinding.pbImageUploading.visibility = View.VISIBLE
                    uploadImageToFirebase(false)
                }
            }

        }

        if (args.docId != null) {
            checkForExistingFabricCode(args.docId!!)
        }
    }

    private fun updateDataOnlyToFirebase(downloadUrl: String) {
        val payload = FabricInfoModel(
            fabricCode = addViewFabricInfoBinding.etFabricCode.editText?.text.toString(),
            fabricLength = addViewFabricInfoBinding.etFabricLength.editText?.text.toString(),
            fabricWidth = addViewFabricInfoBinding.etFabricWidth.editText?.text.toString(),
            imageUrl = downloadUrl,
            rackNumber = addViewFabricInfoBinding.etRackNumber.editText?.text.toString(),
            batch = addViewFabricInfoBinding.etBatch.editText?.text.toString(),
            fileNumber = addViewFabricInfoBinding.etFileNumber.editText?.text.toString(),
            documentId = args.docId
        )

        db.collection("fabric")
            .document(payload.documentId!!)
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
                findNavController().popBackStack(R.id.homeFragment, true)
                findNavController().navigate(R.id.homeFragment)
            }
    }

    private fun uploadImageToFirebase(isUpdate: Boolean) {

        val storageRef = storage.reference

        val imageName=if(args.fabricCode==null){
            addViewFabricInfoBinding.etFabricCode.editText?.text.toString()
        }else{
            args.fabricCode
        }
        val imageUploadRef=storageRef.child("fabric/$imageName.jpg")

        try {

            imageUploadRef.putFile(photoUri)
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
                            fileNumber = addViewFabricInfoBinding.etFileNumber.editText?.text.toString(),
                            documentId = null
                        )

                        if (isUpdate) {
                            updateDataOnlyToFirebase(payload.imageUrl!!)
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
                                    findNavController().popBackStack(R.id.homeFragment, true)
                                    findNavController().navigate(R.id.homeFragment)
                                }
                        }

                    }

                }
        } catch (e: Exception) {
            Timber.e("Error uploading image")
        }

    }

    private var cameraCaptureIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {

                lifecycleScope.launch(Dispatchers.IO) {
                    Compressor.compress(requireContext(), File(currentPhotoPath)).also {
                        photoUri = FileProvider.getUriForFile(
                            requireActivity(),
                            "com.shivam.raymond.fileProvider",
                            it
                        )
                    }

                }

                addViewFabricInfoBinding.ivUploadImage.load(photoUri)
                addViewFabricInfoBinding.ivUploadImage.visibility = View.VISIBLE
                addViewFabricInfoBinding.btnCaptureImage.text = getString(R.string.capture_image_again)
                isImageChanged = true

            } catch (e: Exception) {
                addViewFabricInfoBinding.ivUploadImage.visibility = View.GONE
                addViewFabricInfoBinding.btnCaptureImage.text = getString(R.string.capture_image)

                Timber.e("Error getting image $e")
            }
        }


    private fun validateInputData(): Boolean {
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
            if (addViewFabricInfoBinding.etFabricCodeAgain.editText?.text.toString() != addViewFabricInfoBinding.etFabricCode.editText?.text.toString()) {
                addViewFabricInfoBinding.etFabricCodeAgain.error = "Fabric code doesn't match!"
                allCorrect = false
            } else {
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
                setUiWithData(payload)
                imageDownloadUrl = payload.imageUrl.toString()
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
        if (fabricInfoItem.imageUrl == null) {
            addViewFabricInfoBinding.ivUploadImage.visibility = View.GONE
        } else {
            addViewFabricInfoBinding.ivUploadImage.visibility = View.VISIBLE
            addViewFabricInfoBinding.ivUploadImage.load(fabricInfoItem.imageUrl)
            addViewFabricInfoBinding.btnCaptureImage.text = getString(R.string.capture_image_again)
        }

    }

}