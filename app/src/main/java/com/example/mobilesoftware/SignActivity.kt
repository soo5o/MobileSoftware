package com.example.mobilesoftware

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.ActivitySignBinding

class SignActivity : AppCompatActivity(){
    lateinit var binding: ActivitySignBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //gallery request launcher..................
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            try {
                val calRatio = calculateInSampleSize(
                    it.data!!.data!!,
                    resources.getDimensionPixelSize(R.dimen.imgSize),
                    resources.getDimensionPixelSize(R.dimen.imgSize)
                )
                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio

                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                inputStream = null

                bitmap?.let {
                    binding.addProfile.setImageBitmap(bitmap)
                } ?: let{
                    Log.d("runTo", "bitmap null")
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        binding.addProfile.setOnClickListener {
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

            if(nick.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()){
                Toast.makeText(this, "빈칸을 채워주세요", Toast.LENGTH_SHORT).show()
            }else if(password != confirm){  //비밀번호와 비밀번호 확인이 다를 경우
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            }else{
                val intent = intent
                intent.putExtra("sign complete", binding.signBtn.text.toString()) //login page로 이동
                MyApplication.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this){task ->
                        binding.inputNick.text.clear()
                        binding.inputEmail.text.clear()
                        binding.inputPassword.text.clear()
                        binding.inputConfirm.text.clear()
                        if(task.isSuccessful){
                            MyApplication.auth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener{ sendTask ->
                                    if(sendTask.isSuccessful){
                                        Toast.makeText(baseContext, "전송된 메일을 통해 인증해주세요",
                                            Toast.LENGTH_SHORT).show()
                                        setResult(RESULT_OK , intent)
                                        finish()
                                    }else {
                                        Toast.makeText(baseContext, "메일 발송 실패", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }else {
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
    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 options 에 설정 된다.
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //비율 계산........................
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        //inSampleSize 비율 계산
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}