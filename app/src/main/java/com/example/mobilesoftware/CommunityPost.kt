package com.example.mobilesoftware

import java.io.Serializable

data class CommunityPost(
    val title: String,
    val content: String,
    val timestamp: Long, // 글 작성 시간 나타내는 타임스탬프
    val nickname: String
) : Serializable
