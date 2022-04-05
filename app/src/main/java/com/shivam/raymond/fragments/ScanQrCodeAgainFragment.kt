package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.shivam.raymond.databinding.FragmentScanQrCodeAgainBinding
import timber.log.Timber


class ScanQrCodeAgainFragment : BaseFragment() {
    private lateinit var scanQrCodeAgainBinding: FragmentScanQrCodeAgainBinding
    private lateinit var codeScanner: CodeScanner
    private val args: ScanQrCodeAgainFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        scanQrCodeAgainBinding = FragmentScanQrCodeAgainBinding.inflate(layoutInflater)
        return scanQrCodeAgainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeScanner = CodeScanner(requireActivity(), scanQrCodeAgainBinding.qrScannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                checkForSameQrScannedAgain(it.text)
            }
        }

        scanQrCodeAgainBinding.qrScannerView.setOnClickListener {
            codeScanner.startPreview()
        }

    }

    private fun checkForSameQrScannedAgain(fabricCode: String) {
        db.collection("fabric")
            .whereEqualTo("fabricCode", fabricCode)
            .get()
            .addOnSuccessListener {

                var isDocumentFound = false
                it.documents.forEach { document ->
                    if (document.id == args.docId) {
                        isDocumentFound = true
                    }
                }

                if (isDocumentFound) {
                    scanQrCodeAgainBinding.tvWrongQr.visibility = View.GONE
                    findNavController().navigate(
                        ScanQrCodeAgainFragmentDirections.actionScanQrCodeAgainFragmentToFabricDetailFragment(
                            args.docId
                        )
                    )

                } else {
                    scanQrCodeAgainBinding.tvWrongQr.visibility = View.VISIBLE
                    codeScanner.startPreview()
                }
            }
            .addOnFailureListener { Timber.d("Failed to fetch Fabric Code") }


    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

}