package com.example.mobilesoftware

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilesoftware.databinding.ActivityDetailBinding
import java.util.Calendar
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val commentsList = mutableListOf<Comment>()
    private lateinit var commentAdapter: CommentAdapter

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
            binding.nicknameTextView.text = it.nickname
        }

        // 댓글 리사이클러 뷰
        commentAdapter = CommentAdapter(commentsList)
        binding.commentRecyclerView.adapter = commentAdapter
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // 댓글 전송 버튼을 클릭했을 때 호출되는 메서드
    fun onSendCommentClick(view: View) {
        val commentEditText: EditText = findViewById(R.id.commentEditText)
        val commentContent = commentEditText.text.toString().trim()

        if (commentContent.isNotEmpty()) {
            // 댓글 내용이 비어 있지 않은 경우에만 추가
            val newComment = Comment("작성자", commentContent, System.currentTimeMillis())
            commentsList.add(newComment)
            commentAdapter.notifyDataSetChanged()

            // 댓글을 추가한 후에 EditText를 초기화
            commentEditText.text.clear()
        }
    }

    private fun getFormattedTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return sdf.format(calendar.time)
    }
}
