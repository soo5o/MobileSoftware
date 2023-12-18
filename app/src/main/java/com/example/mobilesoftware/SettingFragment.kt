package com.example.mobilesoftware

import android.app.AlarmManager
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Timer
import java.util.TimerTask
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

class SettingFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var timer: Timer
    private val ALARM_INTERVAL = 30 * 60 * 1000 // 30분을 밀리초로 변환
    //private val ALARM_INTERVAL = 5 * 1000 // 5분을 밀리초로 변환 (테스트용)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_setting, container, false)

        val switchSetting = rootView.findViewById<Switch>(R.id.alarmSwitch)
        val languageSpinner = rootView.findViewById<Spinner>(R.id.languageSpinner)

        switchSetting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) { // Switch ON
                showToast("Alarm is ON")
                setRepeatingAlarm(requireContext()) // 30분 간격으로 반복 알람 설정
            } else { // Switch OFF
                showToast("Alarm is OFF")
                cancelRepeatingAlarm() // 반복 알람 취소
            }
        }

        val languages = arrayOf(
            getString(R.string.lang1),
            getString(R.string.lang2)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        timer = Timer()

        return rootView
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setRepeatingAlarm(context: Context) {
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")
        val vibrationPattern = longArrayOf(0, 1000, 1000) // 1초 간격으로 진동

        // 30분 간격으로 토스트 메시지 및 소리, 진동 출력
        timer.scheduleAtFixedRate(object : TimerTask() {
            var minutesPassed = 0

            override fun run() {
                activity?.runOnUiThread {
                    if (minutesPassed > 0) {
                        startVibration(context, vibrationPattern)
                        playSound(context, soundUri)
                        showToast("$minutesPassed minutes later")
                    }
                    minutesPassed += 30
                }
            }
        }, 0, ALARM_INTERVAL.toLong())
    }

    private fun playSound(context: Context, soundUri: Uri) {
        val mediaPlayer = MediaPlayer.create(context, soundUri)
        mediaPlayer.start()
    }


    private fun startVibration(context: Context, vibrationPattern: LongArray) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationPattern, -1)
        }
    }

    private fun cancelRepeatingAlarm() {
        // 반복 알람 및 타이머 취소
        timer.cancel()
        timer.purge()
    }
}