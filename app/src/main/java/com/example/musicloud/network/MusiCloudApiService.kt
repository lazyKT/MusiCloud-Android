package com.example.musicloud.network

import com.google.gson.GsonBuilder
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url


private const val BASE_URL = "http:10.0.2.2:5000"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory (MoshiConverterFactory.create(moshi))
    .baseUrl (BASE_URL)
    .build()

private val gson = GsonBuilder().setLenient().create()
private val okHttpClient = OkHttpClient.Builder().build()

private val retrofitDL = Retrofit.Builder()
    .baseUrl (BASE_URL)
    .client (okHttpClient)
    .addConverterFactory (GsonConverterFactory.create(gson))
    .build()

data class SongProcessStatus (
    @Json (name = "status") val status: String
)

data class SongRequestBody (
    val url: String = ""
)

data class ProcessTaskResponse (
    @Json (name = "id") val taskID: String
)

/* Customer Error Response Class */
data class ErrorResponseBody (
    val message: String
        )


/*
* Wrapper Class for the network response of SongDLApiService.
* This class is to overcome the exception handling problem within `async` call.
* */
sealed class ResultWrapper<out T> {

    data class Success<out T> (val value: T): ResultWrapper<T> ()

    data class Failure (val statusCode: Int? = null, val errorResponseBody: ErrorResponseBody? = null): ResultWrapper<Nothing> ()
}

interface MusiCloudApiService {

    /* get search results from YouTube */
    @GET
    suspend fun getYtSearchResults (@Url url: String): List<YoutubeSearchProperty>

    /* Request Song Processing */
    @POST ("/process")
    suspend fun checkSong (@Body requestBody: SongRequestBody): SongProcessStatus

    /* Conversion from Video to Audio file in Server */
    @POST ("/convert")
    suspend fun doConversion (@Body requestBody: SongRequestBody): ProcessTaskResponse
}

interface SongDLApiService {
    @GET
    suspend fun downloadSong (
        @Url url: String
    ): Response <ResponseBody>
}

object MusiCloudApi {
    /*
    * create a service object.
    * Retrofit service creation is expensive.
    * Since we need only one instance throughout the app life time,
    * we lazily initialize the service.
    * Now the app can use the singleton Retrofit object via YoutubeSearchApi.retrofitService
    * */
    val retrofitService: MusiCloudApiService by lazy {
        retrofit.create (MusiCloudApiService::class.java)
    }

    val retrofitDLApiService: SongDLApiService by lazy {
        retrofitDL.create (SongDLApiService::class.java)
    }
}
