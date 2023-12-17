package com.example.mobilesoftware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 알람이 울릴 때 수행할 동작(진동, 소리 알림)
        startVibration(context)
        playSound(context)
    }

    private fun startVibration(context: Context?) {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android Oreo (API 레벨 26) 이상인 경우
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else { // Android Oreo 미만인 경우
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000)
        }
    }

    private fun playSound(context: Context?) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        mediaPlayer.start()
    }
}
