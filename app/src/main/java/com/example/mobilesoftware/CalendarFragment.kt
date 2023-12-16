package com.example.mobilesoftware

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.MODE_NO_LOCALIZED_COLLATORS
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mobilesoftware.MyApplication.Companion.storage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

class CalendarFragment : Fragment() {
    val userId = MyApplication.auth.currentUser?.uid
    lateinit var filePath: String
    lateinit var fname: String
    lateinit var fabMain: FloatingActionButton
    lateinit var fabEdit: FloatingActionButton
    lateinit var fabRemove: FloatingActionButton
    lateinit var recordImage: ImageView
    lateinit var calendarView: CalendarView
    private var isFabOpen = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabMain = view.findViewById(R.id.fabMain)
        fabEdit = view.findViewById(R.id.fabEdit)
        fabRemove = view.findViewById(R.id.fabRemove)
        recordImage = view.findViewById(R.id.recordImage)
        calendarView = view.findViewById(R.id.calendarView)
        val requestGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // 이미지 URI를 Glide를 통해 설정
                    Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .error(R.drawable.record_default)
                        .into(recordImage)

                    // 이미지 URI를 사용하여 파일 경로를 얻기
                    val cursor = requireContext().contentResolver.query(
                        uri,
                        arrayOf(MediaStore.Images.Media.DATA),
                        null,
                        null,
                        null
                    )
                    cursor?.moveToFirst().let {
                        filePath=cursor?.getString(0) as String
                        Log.d("runTo", "cursor filePath : $filePath")
                    }
                    saveRecord(fname)
                }
            }
        }
        //캘린더 클릭
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            fabMain.visibility = View.VISIBLE
            fabEdit.visibility = View.VISIBLE
            fabRemove.visibility = View.VISIBLE
            //checkDay(year, month, dayOfMonth)
            fname = "$userId$year-${month + 1}$dayOfMonth"
            val storage = MyApplication.storage
            val storageRef = storage.reference
            val imgRef = storageRef.child("recordImages/${fname}.jpg")
            imgRef.downloadUrl.addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    Glide.with(this)
                        .load(task.result)
                        .centerCrop()
                        .error(R.drawable.record_default)
                        .into(recordImage)
                }
                // 편집을 위한 클릭 리스너 설정
                fabEdit.setOnClickListener {
                    requestGalleryLauncher.launch(Intent(Intent.ACTION_PICK).apply {
                        type = MediaStore.Images.Media.CONTENT_TYPE
                    })
                }
                // 제거를 위한 클릭 리스너 설정
                fabRemove.setOnClickListener {
                    removeRecord(fname)
                }
                recordImage.setOnClickListener{
                    //dialog open
                    val RecordDialogFragment = RecordDialogFragment.newInstance(fname)
                    RecordDialogFragment.show(requireActivity().supportFragmentManager, "RecordDialogFragment")
                }
            } .addOnFailureListener{
                // 이미지가 없을 경우 기본 이미지 설정
                Glide.with(this)
                    .load(R.drawable.record_default)
                    .error(R.drawable.record_default)
                    .into(recordImage)
                // 편집을 위한 클릭 리스너 설정
                fabEdit.setOnClickListener {
                    requestGalleryLauncher.launch(Intent(Intent.ACTION_PICK).apply {
                        type = MediaStore.Images.Media.CONTENT_TYPE
                    })
                }
                // 제거를 위한 클릭 리스너 설정
                fabRemove.setOnClickListener {
                    removeRecord(fname)
                }
            }
        }
        fabMain.setOnClickListener {
            toggleFab()
        }
    }
    // 달력 내용 제거
    private fun removeRecord(fname: String?) {
        val storage = MyApplication.storage
        val storageRef = storage.reference
        val imgRef = storageRef.child("recordImages/${fname}.jpg")
        imgRef.delete()
            .addOnSuccessListener {
                Log.d("runTo", "file remove success")
                Glide.with(this)
                    .load(R.drawable.record_default)
                    .error(R.drawable.record_default)
                    .into(recordImage)
            }
            .addOnFailureListener{
                Log.d("runTo", "file remove error")
            }
    }
    // 달력 내용 추가
    private fun saveRecord(fname: String?) {
        val storage = MyApplication.storage
        val storageRef = storage.reference
        val imgRef = storageRef.child("recordImages/${fname}.jpg")
        val file = Uri.fromFile(File(filePath)) //filePth 오류
        Log.d("runTo", "fname : $fname, filePath : $filePath")
        imgRef.putFile(file)
            .addOnSuccessListener {
                Log.d("runTo", "file save success")
            }
            .addOnFailureListener{
                Log.d("runTo", "file save error", it)
            }
    }
    private fun toggleFab() {
        //FAD 버튼 닫기
        if (isFabOpen) {
            ObjectAnimator.ofFloat(fabEdit, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(fabRemove, "translationY", 0f).apply { start() }
            fabMain.setImageResource(R.drawable.ic_add)
            isFabOpen = false
        }
        //FAD 버튼 열기
        else {
            ObjectAnimator.ofFloat(fabEdit, "translationY", -160f).apply { start() }
            ObjectAnimator.ofFloat(fabRemove, "translationY", -320f).apply { start() }
            fabMain.setImageResource(R.drawable.ic_clear)
            isFabOpen = true
        }
    }
}