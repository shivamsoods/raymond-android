package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.shivam.raymond.databinding.FragmentEnterFabricImageBinding

class EnterFabricImageFragment : BaseFragment() {
    private lateinit var enterFabricImageBinding: FragmentEnterFabricImageBinding
    private val args: EnterFabricImageFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterFabricImageBinding = FragmentEnterFabricImageBinding.inflate(layoutInflater)
        return enterFabricImageBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(requireContext(), "The code is: ${args.fabricCode}", Toast.LENGTH_LONG).show()

    }
}