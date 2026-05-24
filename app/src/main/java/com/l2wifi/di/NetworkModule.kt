package com.l2wifi.di

import com.l2wifi.data.remote.api.NautaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .followRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideNautaApi(client: OkHttpClient): NautaApiService {
        return Retrofit.Builder()
            .baseUrl("https://secure.etecsa.net:8443/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NautaApiService::class.java)
    }
}
