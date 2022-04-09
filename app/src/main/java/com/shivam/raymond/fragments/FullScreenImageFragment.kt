package com.shivam.raymond.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import coil.load
import com.shivam.raymond.databinding.FragmentFullScreenImageBinding


class FullScreenImageFragment : BaseFragment() {
    lateinit var fullScreenImageBinding: FragmentFullScreenImageBinding
    val args: FullScreenImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fullScreenImageBinding = FragmentFullScreenImageBinding.inflate(layoutInflater)
        return fullScreenImageBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullScreenImageBinding.ivFabricFull.load(args.imageUri){
            crossfade(true)
        }
    }


}