package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shivam.raymond.databinding.FragmentAddFabricImageBinding


class AddFabricImageFragment : BaseFragment() {
    private lateinit var addFabricImageBinding: FragmentAddFabricImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addFabricImageBinding = FragmentAddFabricImageBinding.inflate(layoutInflater)
        return addFabricImageBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}