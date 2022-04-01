package com.shivam.raymond.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterFabricCodeBinding.btnGotoScanQrCode.setOnClickListener {
            findNavController().navigate(EnterFabricCodeFragmentDirections.actionAddFabricImageFragmentToScanQrCodeFragment())
        }

        enterFabricCodeBinding.etFabricCode.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.count() >= 3) {
                        enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.VISIBLE
                    } else {
                        enterFabricCodeBinding.btnSubmitFabricCode.visibility = View.GONE
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