package com.shivam.raymond.di

import com.shivam.raymond.api.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    single { provideRetrofit() }
    factory { provideApiService(get()) }
}


private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

private val client = OkHttpClient.Builder()
    .connectTimeout(2, TimeUnit.MINUTES) // connect timeout
    .writeTimeout(2, TimeUnit.MINUTES) // write timeout
    .readTimeout(2, TimeUnit.MINUTES)
    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    .build()

/**
 * Provides a Retrofit Client to the application
 */
private fun provideRetrofit() = Retrofit.Builder()
    .baseUrl("https://raymondcentral.com/MACYAPI/api/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    //    .addConverterFactory(ScalarsConverterFactory.create())
    .client(client)
    .build()

/**
 * Provides API Service
 */
private fun provideApiService(retrofit: Retrofit) =
    retrofit.create(ApiService::class.java)

