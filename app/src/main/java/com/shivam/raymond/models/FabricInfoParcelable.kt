package com.shivam.raymond.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FabricInfoParcelable(
    val fabricCode: String,
    val imageUrl: String?,
    val rackNumber: String,
    val batch: String,
    val fileNumber: String,
    val quantity: Float = 0f
) : Parcelable