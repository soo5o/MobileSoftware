package com.example.mobilesoftware

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.ActivityRunBinding

class RunActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRunBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_run)
        /*        binding.btnSet.setOnClickListener {
                    val intent = intent
                    intent.putExtra("rs_set", binding.editSet.text.toString())
                    setResult(RESULT_OK, intent)
                    finish()
                }*/
    }
}