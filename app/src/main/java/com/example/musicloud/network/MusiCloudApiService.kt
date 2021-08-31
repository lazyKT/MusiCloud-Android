package com.example.musicloud.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url


private const val BASE_URL = "http:10.0.2.2:5000"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory (MoshiConverterFactory.create(moshi))
    .baseUrl (BASE_URL)
    .build()


interface YoutubeSearchApiService {
    @GET ("/")
    fun testService(): Call<String>

    @GET
    suspend fun getSearchResults (@Url url: String): List<YoutubeSearchProperty>
}

object YoutubeSearchApi {
    /*
    * create a service object.
    * Retrofit service creation is expensive.
    * Since we need only one instance throughout the app life time,
    * we lazily initialize the service.
    * Now the app can use the singleton Retrofit object via YoutubeSearchApi.retrofitService
    * */
    val retrofitService: YoutubeSearchApiService by lazy {
        retrofit.create (YoutubeSearchApiService::class.java)
    }
}
