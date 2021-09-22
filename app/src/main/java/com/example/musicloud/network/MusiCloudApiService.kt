package com.example.musicloud.network

import com.google.gson.GsonBuilder
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import java.io.IOException
import java.lang.ClassCastException
import java.lang.Exception
import java.net.SocketTimeoutException


private const val BASE_URL = "https://www.musicloud-api.site"
//private const val BASE_URL = "http://10.0.2.2:5000"


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

interface MusiCloudApiService {

    /* get search results from YouTube */
    @GET
    suspend fun getYtSearchResults (@Url url: String): List<YoutubeSearchProperty>

    @GET
    suspend fun getYtSearchResultById (@Url url: String): List<YoutubeSearchProperty>

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

object ErrorMessages {
    fun genErrorMessage (e: Exception): String {
        return when (e) {
            is HttpException -> when (e.code()) {
                404 -> "HTTP 404 Error. Resource Not Found!"
                400 -> "HTTP 400 Error. Bad Request!"
                403 -> "HTTP 403 Error. Unauthorized Resource!"
                500 -> "HTTP 500 Error. Internal Server Error!"
                else -> "Unknown Network Error! Please Check your Connection!"
            }
            is IOException -> return "IOException: ${e.message}"
            is SocketTimeoutException -> return "SocketTimeOutException: ${e.message}"
            is ClassCastException -> return "Error -1: Application Error! Please Report to Customer Support!"
            else -> "Unknown Error Occurred! ${e.message}"
        }
    }
}

