package com.example.mobilesoftware

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mobilesoftware.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCommunityBinding.inflate(layoutInflater, container, false)

        binding.extendedFab.setOnClickListener {
            when (binding.extendedFab.isExtended) { // 글씨가 출력 여부
                true -> binding.extendedFab.shrink() // 글씨 출력 X
                false -> binding.extendedFab.extend() // 글씨 출력 O
            }
        }

        return binding.root
    }
}