package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val TAG = "NetworkClient"

/**
 * Centralized Retrofit + Moshi network factory for CricAPI and The Odds API v4.
 */
object NetworkClient {
    const val CRIC_API_BASE_URL = "https://api.cricapi.com/v1/"
    const val ODDS_API_BASE_URL = "https://api.the-odds-api.com/v4/"

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    val moshiConverterFactory: MoshiConverterFactory by lazy {
        MoshiConverterFactory.create(moshi)
    }

    val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            // Redact API key query parameters before writing to Logcat
            val sanitized = message.replace(
                Regex("(?i)(apikey|apiKey)=([^&\\s]+)"),
                "$1=***"
            )
            Log.d(TAG, sanitized)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val cricApiRetrofit: Retrofit by lazy {
        createRetrofit(CRIC_API_BASE_URL, okHttpClient)
    }

    val oddsApiRetrofit: Retrofit by lazy {
        createRetrofit(ODDS_API_BASE_URL, okHttpClient)
    }

    val cricApiService: CricApiService by lazy {
        cricApiRetrofit.create(CricApiService::class.java)
    }

    val oddsApiService: OddsApiService by lazy {
        oddsApiRetrofit.create(OddsApiService::class.java)
    }

    fun createRetrofit(
        baseUrl: String,
        client: OkHttpClient = okHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(moshiConverterFactory)
        .build()

    fun createCricApiService(
        baseUrl: String = CRIC_API_BASE_URL,
        client: OkHttpClient = okHttpClient
    ): CricApiService = createRetrofit(baseUrl, client).create(CricApiService::class.java)

    fun createOddsApiService(
        baseUrl: String = ODDS_API_BASE_URL,
        client: OkHttpClient = okHttpClient
    ): OddsApiService = createRetrofit(baseUrl, client).create(OddsApiService::class.java)
}
