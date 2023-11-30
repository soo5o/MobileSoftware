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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.FileOutputStream


class CalendarFragment : Fragment() {
    var userID: String = "userID"
    lateinit var fname: String
    lateinit var fabMain: FloatingActionButton   //floating action button
    lateinit var fabEdit: FloatingActionButton
    lateinit var fabRemove: FloatingActionButton
    lateinit var recordImage: ImageView
    lateinit var calendarView: CalendarView
    private var isFabOpen = false
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                //비율 맞추기
                if (selectedImageUri != null) {
                    val bitmap: Bitmap? = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                    } else {
                        val source = ImageDecoder.createSource(requireContext().contentResolver, selectedImageUri)
                        ImageDecoder.decodeBitmap(source)
                    }
                    // 로드한 이미지를 ImageView에 설정
                    // 이미지를 꽉 채우도록 설정
                    bitmap?.let {
                        recordImage.setImageBitmap(bitmap)
                        recordImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    // 이미지를 저장
                    saveDiary(fname)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
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
        //캘린더 클릭
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            fabMain.visibility = View.VISIBLE
            fabEdit.visibility = View.VISIBLE
            fabRemove.visibility = View.VISIBLE
            checkDay(year, month, dayOfMonth, userID)
        }
        fabMain.setOnClickListener {
            toggleFab()
        }
        fabEdit.setOnClickListener {
            openGallery()
        }
    }
    // 달력 내용 조회, 수정
    private fun checkDay(cYear: Int, cMonth: Int, cDay: Int, userID: String) {
        // 파일 이름 설정
        fname = "$userID$cYear-${cMonth + 1}$cDay.txt"

        try {
            val fileInputStream = requireContext().openFileInput(fname)
            // 이미지가 있는 경우 이미지를 로드
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close()
            // 로드된 이미지를 표시
            bitmap?.let {
                recordImage.setImageBitmap(bitmap)
                recordImage.scaleType = ImageView.ScaleType.CENTER_CROP
            }
            // 편집을 위한 클릭 리스너 설정
            fabEdit.setOnClickListener {
                openGallery()
                // 참고: 여기서 다이어리를 저장하지 않으므로 이미지가 사라지지 않습니다.
            }

            // 제거를 위한 클릭 리스너 설정
            fabRemove.setOnClickListener {/*
                recordImage.setImageResource(R.drawable.record_default)
                recordImage.scaleType = ImageView.ScaleType.CENTER_INSIDE*/
                removeDiary(fname)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 이미지 파일이 없는 경우를 처리
            recordImage.setImageResource(R.drawable.record_default)
            recordImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
            // 편집을 위한 클릭 리스너 설정
            fabEdit.setOnClickListener {
                openGallery()
                // 갤러리를 열고 나서 다이어리를 저장하도록 수정
                saveDiary(fname)
            }

            // 제거를 위한 클릭 리스너 설정
            fabRemove.setOnClickListener {
                removeDiary(fname)
            }
        }
    }


    // 달력 내용 제거
    @SuppressLint("WrongConstant")
    private fun removeDiary(readDay: String?) {
        try {
            requireContext().deleteFile(readDay)
            recordImage.setImageResource(R.drawable.record_default) //record_default
            recordImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // 달력 내용 추가
    @SuppressLint("WrongConstant")
    private fun saveDiary(readDay: String?) {
        val bitmap = (recordImage.drawable as? BitmapDrawable)?.bitmap
        if (bitmap != null) {
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = requireContext().openFileOutput(readDay, MODE_NO_LOCALIZED_COLLATORS)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                fileOutputStream?.close()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
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