package com.example.mobilesoftware

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.os.Build.VERSION_CODES.O
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeActivity : AppCompatActivity() {
    lateinit var bottomNav : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        loadFragment(RunFragment())
        bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        bottomNav.setSelectedItemId(R.id.run);
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.map -> {
                    loadFragment(MapFragment())
                    true
                }
                R.id.calendar -> {
                    loadFragment(CalendarFragment())
                    true
                }
                R.id.run -> {
                    loadFragment(RunFragment())
                    true
                }
                R.id.ranking -> {
                    loadFragment(RankFragment())
                    true
                }
                R.id.community -> {
                    loadFragment(CommunityFragment())
                    true
                }
                else -> {
                    Toast.makeText(this, "BNav Error", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
    }
    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout,fragment)
        transaction.commit()
    }
}