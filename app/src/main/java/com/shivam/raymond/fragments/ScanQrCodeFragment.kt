package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.shivam.raymond.databinding.FragmentScanQrCodeBinding


class ScanQrCodeFragment : BaseFragment() {
    private lateinit var scanQrCodeBinding: FragmentScanQrCodeBinding
    private lateinit var codeScanner: CodeScanner

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
               findNavController().navigate(ScanQrCodeFragmentDirections.actionScanQrCodeFragmentToEnterFabricImageFragment(it.text))
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
}