package com.example.mobilesoftware

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mobilesoftware.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class HomeActivity : AppCompatActivity() {
    lateinit var bottomNav: BottomNavigationView
    lateinit var toggle: ActionBarDrawerToggle
    val userId = MyApplication.auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadFragment(RunFragment())
        val updateNh = {
            val user = Firebase.auth.currentUser
            val imgRef = MyApplication.storage.reference.child("images/${userId}.jpg")
            imgRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(this)
                        .load(task.result)
                        .into(
                            binding.mainDrawerView.getHeaderView(0)
                                .findViewById(R.id.profileImg) as ImageView
                        )
                }
            }.addOnFailureListener {
                Log.d("runTo", "imgref file save error", it)
            }
            MyApplication.db.collection("users")
                .document("${user?.uid}")
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nickname = document.data?.get("nickname").toString()
                        val headerView = binding.mainDrawerView.getHeaderView(0)
                        val userName = headerView.findViewById(R.id.nav_nickname) as TextView
                        userName.text = nickname
                    } else {
                        Log.d("runTo", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("runTo", "get failed with ", exception)
                }
        }
        updateNh()
        bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        bottomNav.setSelectedItemId(R.id.run);
        bottomNav.setOnItemSelectedListener {
            updateNh()
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
                    Toast.makeText(this, "Navigation Error", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }

        // ActionBarDrawerToggle 버튼 적용
        toggle = ActionBarDrawerToggle(this, binding.drawer, R.string.open, R.string.close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState() // 이 부분을 삭제

        //내비게이션 뷰 아이템 선택시 이벤트 처리
        binding.mainDrawerView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_profile -> {
                    loadFragment(EditProfileFragment())
                    updateNh()
                }

                R.id.setting -> {
                    loadFragment(SettingFragment())
                    updateNh()
                }

                R.id.logout -> {
                    MyApplication.auth.signOut()
                    MyApplication.email = null
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            // 내비게이션 드로어 닫음
            binding.drawer.closeDrawer(GravityCompat.START)

            true // 선택한 아이템 상태를 유지하지 않음
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 이벤트가 토글 버튼에서 발생하면
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.commit()
    }
}