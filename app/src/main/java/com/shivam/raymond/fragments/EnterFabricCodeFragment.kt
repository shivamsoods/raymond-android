package com.shivam.raymond.fragments

import android.Manifest
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.shivam.raymond.ScanQrEnum
import com.shivam.raymond.databinding.FragmentEnterFabricCodeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class EnterFabricCodeFragment : BaseFragment() {
    private lateinit var enterFabricCodeBinding: FragmentEnterFabricCodeBinding
    private val args: EnterFabricCodeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterFabricCodeBinding = FragmentEnterFabricCodeBinding.inflate(layoutInflater)
        return enterFabricCodeBinding.root
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            findNavController().navigate(
                EnterFabricCodeFragmentDirections.actionEnterFabricCodeFragmentToScanQrCodeFragment(
                    args.viewType
                )
            )
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission needed to scan QR code",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterFabricCodeBinding.btnGotoScanQrCode.setOnClickListener {

            if (hasPermission(Manifest.permission.CAMERA)) {
                findNavController().navigate(
                    EnterFabricCodeFragmentDirections.actionEnterFabricCodeFragmentToScanQrCodeFragment(
                        args.viewType
                    )
                )
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

        }

        enterFabricCodeBinding.etFabricCode.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {

                    if (s.toString() == enterFabricCodeBinding.etFabricCodeAgain.editText?.text.toString()) {
                        enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.VISIBLE
                        enterFabricCodeBinding.etFabricCodeAgain.error = null

                    } else {
                        enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.GONE
                        enterFabricCodeBinding.etFabricCodeAgain.error = "Fabric codes doesn't match"
                    }

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        enterFabricCodeBinding.etFabricCodeAgain.editText?.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.toString() == enterFabricCodeBinding.etFabricCode.editText?.text.toString()) {
                        enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.VISIBLE
                        enterFabricCodeBinding.etFabricCodeAgain.error = null

                    } else {
                        enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.GONE
                        enterFabricCodeBinding.etFabricCodeAgain.error = "Fabric codes doesn't match"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        enterFabricCodeBinding.btnSubmitFabricCode.setOnClickListener {
            val fabricCode = enterFabricCodeBinding.etFabricCode.editText?.text.toString()
            enterFabricCodeBinding.pbFabricSearching.visibility = View.VISIBLE
            enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.GONE
            if (args.viewType == ScanQrEnum.API_FLOW) {
                lifecycleScope.launch(Dispatchers.IO) {
                    getFabricApiInfo(fabricCode)
                }
            } else {

                checkForExistingFabricCode(fabricCode)
            }
        }
    }

    private fun checkForExistingFabricCode(fabricCode: String) {
        db.collection("fabric")
            .whereEqualTo("fabricCode", fabricCode)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    Toast.makeText(
                        requireContext(),
                        "No such fabric code",
                        Toast.LENGTH_SHORT
                    ).show()

                    enterFabricCodeBinding.pbFabricSearching.visibility = View.GONE
                    enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "Found fabric code", Toast.LENGTH_SHORT).show()

                    findNavController().navigate(
                        EnterFabricCodeFragmentDirections.actionEnterFabricCodeFragmentToListFabricFragment(
                            fabricCode,
                            args.viewType
                        )
                    )

                }


            }

            .addOnFailureListener { Timber.e("Failed to fetch Fabric Code") }

    }

    private suspend fun getFabricApiInfo(fabricCode: String) {
        val fabricResponse = apiService.getFabricInfo(fabricCode)
        if (fabricResponse.isSuccessful && fabricResponse.body() != null) {
            lifecycleScope.launch(Dispatchers.Main) {
                Timber.d("Fabric Info=> ${fabricResponse.body()!!}")
                if (fabricResponse.body()!!.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No such fabric code",
                        Toast.LENGTH_SHORT
                    ).show()
                    enterFabricCodeBinding.pbFabricSearching.visibility = View.GONE
                    enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "Found fabric code", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(
                        EnterFabricCodeFragmentDirections.actionEnterFabricCodeFragmentToListFabricFragment(
                            fabricCode,
                            args.viewType
                        )
                    )
                }
            }
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Error occurred!", Toast.LENGTH_SHORT).show()
                enterFabricCodeBinding.pbFabricSearching.visibility = View.GONE
                enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.VISIBLE
            }
        }
    }
}