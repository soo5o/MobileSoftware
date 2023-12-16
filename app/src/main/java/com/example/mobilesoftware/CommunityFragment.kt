package com.example.mobilesoftware

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilesoftware.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {
    private val communityPosts = mutableListOf<CommunityPost>()
    private lateinit var adapter: CommunityAdapter
    private val WRITE_ACTIVITY_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCommunityBinding.inflate(inflater, container, false)

        // 리사이클러 뷰 초기화
        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerView)

        // reverseLayout 속성을 true로 설정
        recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        adapter = CommunityAdapter(communityPosts) { clickedPost ->
            // 클릭된 아이템 정보를 다음 화면으로 전달
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("clickedPost", clickedPost)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        binding.extendedFab.setOnClickListener {
            val intent = Intent(requireContext(), WriteActivity::class.java)
            startActivityForResult(intent, WRITE_ACTIVITY_REQUEST_CODE)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WRITE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val newPost = data?.getSerializableExtra("newPost") as? CommunityPost
            newPost?.let {
                // 데이터를 리스트에 추가하고 어댑터에 알리기
                communityPosts.add(it)
                adapter.notifyDataSetChanged()
            }
        }
    }
}