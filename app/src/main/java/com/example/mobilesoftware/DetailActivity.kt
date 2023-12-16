package com.example.mobilesoftware

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilesoftware.databinding.ActivityDetailBinding
import java.util.Calendar
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바에 뒤로가기 버튼 추가
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 클릭된 아이템 정보 받기
        val clickedPost = intent.getSerializableExtra("clickedPost") as? CommunityPost

        // 받은 정보를 화면에 출력
        clickedPost?.let {
            binding.titleTextView.text = it.title
            binding.timestampTextView.text = getFormattedTime(it.timestamp)
            binding.editContentTextView.text = it.content
        }
    }

    // 뒤로가기 버튼을 눌렀을 때의 동작 설정
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getFormattedTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return sdf.format(calendar.time)
    }
}
