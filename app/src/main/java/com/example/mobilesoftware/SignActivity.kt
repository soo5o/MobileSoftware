package com.example.mobilesoftware

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mobilesoftware.databinding.ActivitySignBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class SignActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignBinding
    private lateinit var auth: FirebaseAuth
    lateinit var filePath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        //gallery request launcher..................
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
            Glide
                .with(this)
                .load(it.data?.data)
                .apply(RequestOptions().override(150, 150))
                .centerCrop()
                .error(R.drawable.account_circle)
                .into(binding.addProfile)
            val cursor = contentResolver.query(
                it.data?.data as Uri,
                arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null
            );
            cursor?.moveToFirst().let {
                filePath = cursor?.getString(0) as String
            }
            Log.d("runTo", "filePath : $filePath")
        }
        binding.addProfileText.setOnClickListener {
            //gallery app........................
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }
        binding.signBtn.setOnClickListener {
            //이메일,비밀번호 회원가입........................
            val nick = binding.inputNick.text.toString()
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            val confirm = binding.inputConfirm.text.toString()
            if (nick.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "빈칸을 채워주세요", Toast.LENGTH_SHORT).show()
            } else if (password != confirm) {  //비밀번호와 비밀번호 확인이 다를 경우
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            } else {
                val intent = intent
                intent.putExtra("sign complete", binding.signBtn.text.toString()) //login page로 이동
                MyApplication.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding.inputNick.text.clear()
                        binding.inputEmail.text.clear()
                        binding.inputPassword.text.clear()
                        binding.inputConfirm.text.clear()
                        if (task.isSuccessful) {
                            // 회원가입 성공 시 유저 정보를 Database에 저장
                            val userId = MyApplication.auth.currentUser?.uid
                            saveUserInfoToDatabase(userId, nick)
                            MyApplication.auth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener { sendTask ->
                                    if (sendTask.isSuccessful) {
                                        Toast.makeText(
                                            baseContext, "전송된 메일을 통해 인증해주세요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        setResult(RESULT_OK, intent)
                                        finish()
                                    } else {
                                        Toast.makeText(baseContext, "메일 발송 실패", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                        } else {
                            Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        binding.signBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun saveUserInfoToDatabase(userId: String?, nickname: String) {
        val user = mapOf(
            "uid" to userId,
            "nickname" to nickname
        )
        Log.d("runTo", "save 함수로 넘어옴")
        MyApplication.db.collection("users")
            .document("$userId")
            .set(user)
            .addOnSuccessListener {
                uploadImage(userId)
            }
            .addOnFailureListener { e ->
                Log.d("runTo", "data save error", e)
            }
    }

    private fun uploadImage(docId: String?) {
        //add............................
        val storage = MyApplication.storage
        val storageRef = storage.reference
        val imgRef = storageRef.child("images/${docId}.jpg")
        val file = Uri.fromFile(File(filePath))
        imgRef.putFile(file)
            .addOnSuccessListener {
                finish()
            }
            .addOnFailureListener {
                Log.d("runTo", "file save error", it)
            }
    }
}