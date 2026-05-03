package com.example.androidmobileapplicationwithrestapiandwebhosting.api

import com.example.androidmobileapplicationwithrestapiandwebhosting.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body user: Map<String, String>): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<AuthResponse>

    @GET("tasks")
    suspend fun getTasks(@Query("userId") userId: Int): Response<TaskResponse>

    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<TaskResponse>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: Int): Response<SingleTaskResponse>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body task: Task): Response<TaskResponse>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<TaskResponse>

    @GET("dashboard/{userId}")
    suspend fun getDashboardSummary(@Path("userId") userId: Int): Response<Map<String, Int>>

    @GET("profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: Int): Response<ProfileResponse>

    @PUT("profile/{userId}")
    suspend fun updateProfile(@Path("userId") userId: Int, @Body profileData: Map<String, String>): Response<AuthResponse>

    @Multipart
    @POST("profile/{userId}/upload")
    suspend fun uploadProfileImage(
        @Path("userId") userId: Int,
        @Part image: okhttp3.MultipartBody.Part
    ): Response<ProfileImageResponse>
}
