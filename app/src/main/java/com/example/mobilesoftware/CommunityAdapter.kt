package com.example.mobilesoftware

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Locale

class CommunityAdapter(
    private val posts: List<CommunityPost>,
    private val onItemClick: (CommunityPost) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_community_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(post: CommunityPost) {
            val titleTextView = itemView.findViewById<TextView>(R.id.titleTextView)
            titleTextView.text = post.title

            val timestampTextView = itemView.findViewById<TextView>(R.id.timestampTextView)
            timestampTextView.text = getFormattedTime(post.timestamp)

            val nicknameTextView = itemView.findViewById<TextView>(R.id.nicknameTextView)
            nicknameTextView.text = post.nickname

            // 아이템 클릭 시
            itemView.setOnClickListener {
                // 클릭된 아이템 정보를 다음 화면으로 전달
                onItemClick.invoke(post)
            }
        }
    }

    // 시간을 형식화하는 함수
    private fun getFormattedTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return sdf.format(calendar.time)
    }
}
