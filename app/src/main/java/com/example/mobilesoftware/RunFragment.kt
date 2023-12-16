package com.example.mobilesoftware

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mobilesoftware.databinding.FragmentRunBinding
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class RunFragment : Fragment() {

    var initTime = 0L
    var pauseTime = 0L
    private lateinit var binding: FragmentRunBinding
    var mediaplayer: MediaPlayer? = null
    private val apiKey = "94ba6a6f362a49bd7a206c07294f576f"
    private val city = "Seoul" // 원하는 도시로 변경

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaplayer = MediaPlayer.create(requireContext(), R.raw.sample)
        WeatherTask().execute()

        binding.buttonPlay.setOnClickListener {
            mediaplayer?.start()
        }

        binding.buttonStop.setOnClickListener {
            mediaplayer?.pause()
            mediaplayer?.seekTo(0)
        }

        binding.chronometer.format = "%s"

        binding.startButton.setOnClickListener {
            binding.chronometer.base = SystemClock.elapsedRealtime() + pauseTime
            binding.chronometer.start()
            binding.stopButton.isEnabled = true
            binding.resetButton.isEnabled = true
            binding.startButton.isEnabled = false
        }

        binding.stopButton.setOnClickListener {
            pauseTime = binding.chronometer.base - SystemClock.elapsedRealtime()
            binding.chronometer.stop()
            binding.stopButton.isEnabled = false
            binding.resetButton.isEnabled = true
            binding.startButton.isEnabled = true
        }

        binding.resetButton.setOnClickListener {
            pauseTime = 0L
            binding.chronometer.base = SystemClock.elapsedRealtime()
            binding.chronometer.stop()
            binding.stopButton.isEnabled = false
            binding.resetButton.isEnabled = false
            binding.startButton.isEnabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaplayer?.release()
    }

    inner class WeatherTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String? {
            val response: String?
            try {
                response =
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey")
                        .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                return null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updatedAt: Long = jsonObj.getLong("dt")
                val updatedAtText =
                    "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                        Date(updatedAt * 1000)
                    )

                // 기존 코드에서 섭씨로 표시되도록 수정
                val tempKelvin = main.getDouble("temp")
                val tempCelsius = tempKelvin - 273.15

                val formattedTemp = String.format("%.2f", tempCelsius) + "°C"
                val weatherDescription = weather.getString("description")

                binding.cityText.text = city
                binding.temperatureText.text = formattedTemp
                binding.statusText.text = weatherDescription.capitalize()
                binding.updatedText.text = updatedAtText

            } catch (e: Exception) {
                binding.cityText.text = "Error fetching data"
            }
        }
    }
}