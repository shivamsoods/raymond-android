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
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.shivam.raymond.databinding.FragmentEnterFabricCodeBinding


class EnterFabricCodeFragment : BaseFragment() {
    private lateinit var enterFabricCodeBinding: FragmentEnterFabricCodeBinding

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
            findNavController().navigate(EnterFabricCodeFragmentDirections.actionAddFabricImageFragmentToScanQrCodeFragment())
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
                findNavController().navigate(EnterFabricCodeFragmentDirections.actionAddFabricImageFragmentToScanQrCodeFragment())
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

        }

        enterFabricCodeBinding.etFabricCode.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.count() >= 3) {
                        enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.VISIBLE
                        enterFabricCodeBinding.etFabricCode.error = null

                    } else {
                        enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.GONE
                        enterFabricCodeBinding.etFabricCode.error = "Fabric code is more than 3 digits"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        enterFabricCodeBinding.btnSubmitFabricCode.setOnClickListener {
            findNavController().navigate(
                EnterFabricCodeFragmentDirections.actionAddFabricImageFragmentToEnterFabricImageFragment(
                    enterFabricCodeBinding.etFabricCode.editText?.text.toString()
                )
            )
        }
    }


}