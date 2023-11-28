package com.example.mobilesoftware

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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