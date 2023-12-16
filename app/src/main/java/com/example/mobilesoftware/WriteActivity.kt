package com.example.mobilesoftware

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilesoftware.databinding.ActivityWriteBinding

class WriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postButton.setOnClickListener {
            val newPost = CommunityPost(
                title = binding.editTitle.text.toString(),
                content = binding.editContent.text.toString(),
                timestamp = System.currentTimeMillis(),
                nickname = "작성자"
            )

            // 결과 설정
            val resultIntent = Intent()
            resultIntent.putExtra("newPost", newPost)
            setResult(RESULT_OK, resultIntent)

            // 액티비티 종료
            finish()
        }

    }


    private fun savePostAndFinish() {
        val newPost = CommunityPost(
            title = binding.editTitle.text.toString(),
            content = binding.editContent.text.toString(),
            timestamp = System.currentTimeMillis(),
            nickname = "작성자"
        )

        // 결과 설정
        val resultIntent = Intent()
        resultIntent.putExtra("newPost", newPost)
        setResult(RESULT_OK, resultIntent)
        // 액티비티 종료
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        savePostAndFinish()
    }
}