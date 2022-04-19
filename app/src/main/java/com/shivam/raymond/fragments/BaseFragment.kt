package com.shivam.raymond.fragments

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.shivam.raymond.api.ApiService
import org.koin.android.ext.android.inject

open class BaseFragment : Fragment() {

    val db = Firebase.firestore
    val storage = Firebase.storage

    fun hasPermission(permission: String): Boolean {
        val result = ActivityCompat.checkSelfPermission(requireContext(), permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    val apiService: ApiService by inject()


}