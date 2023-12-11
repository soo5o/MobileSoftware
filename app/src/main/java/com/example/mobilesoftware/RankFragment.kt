package com.example.mobilesoftware

import ProfileAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RankFragment : Fragment() {
    val datas = mutableListOf<ProfileData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rank, container, false)

        initRecycler(view)

        return view
    }

    private fun initRecycler(view: View) {
        datas.apply {
            add(ProfileData(name = "mary", age = 1))
            add(ProfileData(name = "jenny", age = 2))
            add(ProfileData(name = "jhon", age = 3))
            add(ProfileData(name = "ruby", age = 4))
            add(ProfileData(name = "yuna", age = 5))
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_profile)
        val adapter = ProfileAdapter(requireContext(), datas)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
}
