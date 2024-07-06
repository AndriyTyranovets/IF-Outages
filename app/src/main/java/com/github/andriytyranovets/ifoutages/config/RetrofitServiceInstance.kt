package com.github.andriytyranovets.ifoutages.config

import android.util.Log
import com.github.andriytyranovets.ifoutages.gson.typeadapters.DateDeserializer
import com.github.andriytyranovets.ifoutages.gson.typeadapters.DateTimeDeserializer
import com.github.andriytyranovets.ifoutages.services.OutagesApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime

object RetrofitServiceInstance {
    private const val BASE_URL = "https://svitlo.oe.if.ua/GAVTurnOff/"

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, DateDeserializer())
            .registerTypeAdapter(LocalDateTime::class.java, DateTimeDeserializer())
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
//            .addConverterFactory(JacksonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(RequestInterceptor)
            .build()
    }

    val outagesApi: OutagesApiService by lazy {
        retrofit.create(OutagesApiService::class.java)
    }

    private object RequestInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            Log.d("Retrofit", "Outgoing request to ${request.url()}")
            return chain.proceed(request)
        }
    }
}