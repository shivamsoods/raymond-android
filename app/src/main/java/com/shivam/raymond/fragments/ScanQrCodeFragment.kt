package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.shivam.raymond.ScanQrEnum
import com.shivam.raymond.databinding.FragmentScanQrCodeBinding
import timber.log.Timber


class ScanQrCodeFragment : BaseFragment() {
    private lateinit var scanQrCodeBinding: FragmentScanQrCodeBinding
    private lateinit var codeScanner: CodeScanner
    private val args: ScanQrCodeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        scanQrCodeBinding = FragmentScanQrCodeBinding.inflate(layoutInflater)
        return scanQrCodeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeScanner = CodeScanner(requireActivity(), scanQrCodeBinding.qrScannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
 checkForExistingFabricCode(it.text)
            }
        }

        scanQrCodeBinding.qrScannerView.setOnClickListener {
            codeScanner.startPreview()
        }

    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
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
                    Toast.makeText(requireContext(), "Found fabric code", Toast.LENGTH_SHORT).show()
                    when (args.viewType) {
                        ScanQrEnum.ADD_IMAGE -> {
                            findNavController().navigate(
                                ScanQrCodeFragmentDirections.actionScanQrCodeFragmentToEnterFabricImageFragment(
                                    fabricCode
                                )
                            )
                        }
                        ScanQrEnum.VIEW_FABRIC_DETAIL -> {
                            findNavController().navigate(
                                ScanQrCodeFragmentDirections.actionScanQrCodeFragmentToAddViewFabricInfoFragment(
                                    fabricCode, "View/Modify Fabric"
                                )
                            )

                        }
                    }

                }


            }

            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }

    }

}