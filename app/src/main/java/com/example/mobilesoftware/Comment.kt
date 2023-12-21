package com.example.mobilesoftware

import java.io.Serializable

data class Comment(
    val nickname: String,
    val content: String,
    val timestamp: Long
): Serializable