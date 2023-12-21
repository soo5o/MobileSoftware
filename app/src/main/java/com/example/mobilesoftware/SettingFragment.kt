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
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Timer
import java.util.TimerTask
import android.content.res.Configuration
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SettingFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var timer: Timer
    private val ALARM_INTERVAL = 30 * 60 * 1000 // 30분을 밀리초로 변환
    private val PREF_SELECTED_LANGUAGE = "selectedLanguage"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_setting, container, false)

        val switchSetting = rootView.findViewById<Switch>(R.id.alarmSwitch)

        switchSetting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showToast("Alarm is ON")
                setRepeatingAlarm(requireContext())
            } else {
                showToast("Alarm is OFF")
                cancelRepeatingAlarm()
            }
        }

        // 언어 변환 버튼
        val languageButton = rootView.findViewById<Button>(R.id.language_eng_btn)
        languageButton.setOnClickListener {
            changeLanguage(it)
        }

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

    // 언어 저장하는 함수
    private fun saveSelectedLanguage(selectedLanguage: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.edit().putString(PREF_SELECTED_LANGUAGE, selectedLanguage).apply()
    }

    private fun getSelectedLanguage(): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val loadedLanguage = preferences.getString(PREF_SELECTED_LANGUAGE, "") ?: ""
        return loadedLanguage
    }

    private fun setLanguage(selectedLanguage: String) {
        // 언어 저장
        saveSelectedLanguage(selectedLanguage)

        // 선택된 언어에 따라 언어 변경
        setLocale(selectedLanguage)

        // 액티비티에 언어 변경을 알림
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.nav2) // 액티비티의 타이틀을 갱신
        activity?.recreate() // 액티비티 다시 생성하여 변경된 언어 반영
    }

    fun changeLanguage(view: View) {
        val currentLanguage = getSelectedLanguage()
        val targetLanguage = if (currentLanguage == "ko") "en" else "ko"
        setLanguage(targetLanguage)

        // 화면에 표시된 내용 다시 로드
        reloadFragment()
    }

    private fun reloadFragment() {
        val ft = requireFragmentManager().beginTransaction()
        ft.detach(this).attach(this).commit()
    }

    // 언어 변경 함수
    private fun setLocale(languageCode: String) {
        if (languageCode.isNotEmpty()) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources = requireContext().resources
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)

            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
}