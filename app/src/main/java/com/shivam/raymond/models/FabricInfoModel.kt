package com.shivam.raymond.models

data class FabricInfoModel(
    val fabricCode: String,
    val fabricLength: String,
    val fabricWidth: String,
    val imageUrl: String?,
    val rackNumber: String,
    val batch:String,
    val fileNumber:String,
    val documentId:String?=null
)

