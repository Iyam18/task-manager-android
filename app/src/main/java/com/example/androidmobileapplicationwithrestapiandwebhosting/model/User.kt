package com.example.androidmobileapplicationwithrestapiandwebhosting.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val profileImage: String? = null,
    val bio: String? = null,
    val token: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User?
)

data class ProfileResponse(
    val success: Boolean,
    val user: User? = null
)

data class ProfileImageResponse(
    val success: Boolean,
    val message: String? = null,
    val profileImage: String? = null
)
