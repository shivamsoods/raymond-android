package com.shivam.raymond.api

import com.shivam.raymond.models.FabricInfoItemResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("Material/Fabric")
    suspend fun getFabricInfo(@Query("fabricCode") fabricCode: String): Response<List<FabricInfoItemResponseModel>>

}
