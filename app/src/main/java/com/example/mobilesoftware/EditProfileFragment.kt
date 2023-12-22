package com.example.mobilesoftware

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File

class EditProfileFragment : Fragment() {
    val userId = MyApplication.auth.currentUser?.uid
    lateinit var editNick: Button
    lateinit var editPass: Button
    lateinit var changeNick: EditText
    lateinit var password: EditText
    lateinit var confirm: EditText
    lateinit var profileImg: ImageView
    lateinit var changeProfileText: TextView
    lateinit var changeProfileBtn: Button
    lateinit var filePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileImg = view.findViewById(R.id.addProfile)
        editNick = view.findViewById(R.id.editNick)
        editPass = view.findViewById(R.id.editPass)
        changeNick = view.findViewById(R.id.inputNick)
        password = view.findViewById(R.id.inputPassword)
        confirm = view.findViewById(R.id.inputConfirm)
        changeProfileText = view.findViewById(R.id.changeProfile)
        changeProfileBtn = view.findViewById(R.id.changeProfileBtn)
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // 이미지 URI를 Glide를 통해 설정
                    Glide.with(this)
                        .load(uri)
                        .apply(RequestOptions().override(150, 150))
                        .centerCrop()
                        .error(R.drawable.account_circle)
                        .into(profileImg)

                    // 이미지 URI를 사용하여 파일 경로를 얻기
                    val cursor = requireContext().contentResolver.query(
                        uri,
                        arrayOf(MediaStore.Images.Media.DATA),
                        null,
                        null,
                        null
                    )
                    cursor?.moveToFirst().let {
                        filePath = cursor?.getString(0) as String
                        Log.d("runTo", "cursor filePath : $filePath")
                    }
                }
            }
        }
        val user = Firebase.auth.currentUser
        changeProfileText.setOnClickListener {
            Log.d("runTo", "프로필 사진 변경")
            requestGalleryLauncher.launch(Intent(Intent.ACTION_PICK).apply {
                type = MediaStore.Images.Media.CONTENT_TYPE
            })
        }
        changeProfileBtn.setOnClickListener {
            uploadImage(userId)
        }
        editNick.setOnClickListener {
            if (changeNick.text.toString().isEmpty()) {
                //닉네임이 입력되지 않은 경우
                Toast.makeText(requireContext(), "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                //닉네임 변경
                val userId = MyApplication.auth.currentUser?.uid
                MyApplication.db.collection("users")
                    .document("$userId")
                    .update("nickname", changeNick.text.toString())
                changeNick.text.clear()
                Toast.makeText(requireContext(), "닉네임이 변경되었습니다", Toast.LENGTH_SHORT).show()
                Log.d("runTo", "닉네임 변경")
            }
        }
        editPass.setOnClickListener {
            if (password.text.toString().isEmpty() || confirm.text.toString().isEmpty()) {
                //비밀번호가 입력되지 않은 경우
                Toast.makeText(requireContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (password.text.toString() != confirm.text.toString()) {
                Toast.makeText(requireContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            } else {
                //비밀번호 변경
                val newPassword = password.text.toString()
                password.text.clear()
                confirm.text.clear()
                user!!.updatePassword(newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("runTo", "User password updated.")
                            Toast.makeText(requireContext(), "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.d("runTo", "User password updated failed.")
                        }
                    }
            }
        }
        //이미지를 불러오는 부분
        val imgRef = MyApplication.storage.reference.child("images/${userId}.jpg")

        imgRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
                    .into(profileImg)
            }
        }.addOnFailureListener {
            Log.d("runTo", "imgref file save error", it)
        }
        //얘는 우선 db 데이터 값을 가져오기 위해 있는 것
        MyApplication.db.collection("users")
            .document("${user?.uid}")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("runTo", "DocumentSnapshot data: ${document.data}")
                    val nickname = document.data?.get("nickname").toString()
                    changeNick.setText(nickname)
                } else {
                    Log.d("runTo", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("runTo", "get failed with ", exception)
            }
    }

    private fun uploadImage(docId: String?) {
        //add............................
        val storage = MyApplication.storage
        val storageRef = storage.reference
        val imgRef = storageRef.child("images/${userId}.jpg")
        val file = Uri.fromFile(File(filePath))
        imgRef.putFile(file)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "프로필 사진이 변경되었습니다", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.d("runTo", "file save error", it)
            }
    }
}