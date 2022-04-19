package com.shivam.raymond.models

import com.squareup.moshi.Json

data class FabricInfoItemResponseModel(
    @Json(name = "Batch")
    val batch: String,
    @Json(name = "Fabric_Code")
    val fabricCode: String,
    @Json(name = "FileNo")
    val fileNo: String,
    @Json(name = "Image")
    val imageUrl: String,
    @Json(name = "Quantity")
    val quantity: Float,
    @Json(name = "RackNo")
    val rackNo: String
)

