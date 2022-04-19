package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.shivam.raymond.ScanQrEnum
import com.shivam.raymond.databinding.FragmentScanQrCodeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

                if (args.viewType == ScanQrEnum.API_FLOW) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        getFabricApiInfo(it.text)
                    }
                } else {

                    checkForExistingFabricCode(it.text)
                }
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
                    findNavController().navigate(ScanQrCodeFragmentDirections.actionScanQrCodeFragmentToListFabricFragment(fabricCode,args.viewType))

                }


            }

            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }

    }

    private suspend fun getFabricApiInfo(fabricCode: String) {
        val fabricResponse = apiService.getFabricInfo(fabricCode)
        if (fabricResponse.isSuccessful && fabricResponse.body() != null) {

            lifecycleScope.launch(Dispatchers.Main) {
                if (fabricResponse.body()!!.isEmpty()) {
                    Toast.makeText(requireContext(), "No such fabric code", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Found fabric code", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(ScanQrCodeFragmentDirections.actionScanQrCodeFragmentToListFabricFragment(fabricCode, args.viewType))
                }
            }
        }

    }


}