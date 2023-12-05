package com.example.mobilesoftware

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date

fun myCheckPermission(activity: AppCompatActivity) {

    val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Log.d("runTo", "권한 승인")
        } else {
            Log.d("runTo", "권한 거부")
        }
    }

    if (ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) !== PackageManager.PERMISSION_GRANTED
    ) {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

fun dateToString(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd")
    return format.format(date)
}