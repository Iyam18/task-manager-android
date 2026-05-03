package com.example.androidmobileapplicationwithrestapiandwebhosting.model

import com.google.gson.annotations.SerializedName

data class Task(
    @SerializedName("id", alternate = ["taskId", "task_id"])
    val id: Int? = null,
    @SerializedName("userId", alternate = ["user_id"])
    val userId: Int,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("status")
    val status: String?, // "Pending", "In Progress", "Completed"
    @SerializedName("priority")
    val priority: String? = "Medium", // "Low", "Medium", "High"
    @SerializedName("dueDate", alternate = ["due_date"])
    val dueDate: String? = null,
    @SerializedName("createdAt", alternate = ["created_at"])
    val createdAt: String? = null
)

data class TaskResponse(
    val success: Boolean,
    val message: String? = null,
    val tasks: List<Task>? = null,
    val task: Task? = null
)

data class SingleTaskResponse(
    val success: Boolean,
    val message: String? = null,
    val task: Task? = null
)
