package com.l2wifi.data.remote.api

import retrofit2.http.*
import com.l2wifi.data.remote.model.*

interface NautaApiService {
    @FormUrlEncoded
    @POST("Login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("wlanuserip") wlanUserIp: String? = null
    ): retrofit2.Response<LoginResponse>

    @GET("Logout")
    suspend fun logout(): retrofit2.Response<LogoutResponse>

    @GET("GetBalance")
    suspend fun getBalance(): retrofit2.Response<BalanceResponse>

    @GET("GetSessionInfo")
    suspend fun getSessionInfo(): retrofit2.Response<SessionInfoResponse>
}
