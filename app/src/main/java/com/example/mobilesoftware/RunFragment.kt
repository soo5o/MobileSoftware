package com.example.mobilesoftware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.FragmentRunBinding
import android.media.MediaPlayer
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class RunFragment : Fragment() {

    private val MY_PERMISSIONS_REQUEST_LOCATION = 123

    var initTime = 0L
    var pauseTime = 0L
    private lateinit var binding: FragmentRunBinding
    var mediaPlayer: MediaPlayer? = null
    private val apiKey = "94ba6a6f362a49bd7a206c07294f576f"
    private val city = "Seoul" // Replace with your desired city

    private lateinit var locationManager: LocationManager
    private var distance: Float = 0f
    private var lastLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.sample)
        WeatherTask().execute()

        binding.buttonPlay.setOnClickListener {
            mediaPlayer?.start()
        }

        binding.buttonStop.setOnClickListener {
            mediaPlayer?.pause()
            mediaPlayer?.seekTo(0)
        }

        binding.chronometer.format = "%s"

        // Initialize the LocationManager
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if the app has location permissions
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If permissions are granted, start location updates
            requestLocationUpdates()
        } else {
            // If permissions are not granted, request them
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }

        binding.startButton.setOnClickListener {
            binding.chronometer.base = SystemClock.elapsedRealtime() + pauseTime
            binding.chronometer.start()
            binding.stopButton.isEnabled = true
            binding.resetButton.isEnabled = true
            binding.startButton.isEnabled = false

            // Request location updates when the start button is pressed
            requestLocationUpdates()
        }

        binding.stopButton.setOnClickListener {
            pauseTime = binding.chronometer.base - SystemClock.elapsedRealtime()
            binding.chronometer.stop()
            binding.stopButton.isEnabled = false
            binding.resetButton.isEnabled = true
            binding.startButton.isEnabled = true

            // Stop location updates when the stop button is pressed
            locationManager.removeUpdates(locationListener)

            // Display the measured distance
            updateDistanceTextView()
        }

        binding.resetButton.setOnClickListener {
            // Reset distance and other values
            distance = 0f
            lastLocation = null
            pauseTime = 0L
            binding.chronometer.base = SystemClock.elapsedRealtime()
            binding.chronometer.stop()
            binding.stopButton.isEnabled = false
            binding.resetButton.isEnabled = false
            binding.startButton.isEnabled = true

            // Update the distance TextView to show the reset value
            updateDistanceTextView()
        }
    }

    private fun requestLocationUpdates() {
        try {
            // Request location updates with a listener
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, // 5000 milliseconds (5 seconds) between updates
                5f,   // 5 meters between updates
                locationListener
            )
        } catch (e: SecurityException) {
            // Handle the exception
            Log.e("RunFragment", "SecurityException: ${e.message}")
        }
    }

    private fun updateDistanceTextView(realTimeDistance: Float = distance) {

        // Update real-time distance TextView
        val formattedRealTimeDistance = String.format("%.2f", realTimeDistance)
        binding.realTimeDistanceText.text = "Real-time Distance: $formattedRealTimeDistance m"
    }


    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Calculate distance between current and last location
            if (lastLocation != null) {
                distance += lastLocation!!.distanceTo(location)
                updateDistanceTextView(distance)
            }
            lastLocation = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle status changes if needed
        }

        override fun onProviderEnabled(provider: String) {
            // Handle provider enabled
        }

        override fun onProviderDisabled(provider: String) {
            // Handle provider disabled
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissions granted, start location updates
                    requestLocationUpdates()
                } else {
                    // Permissions denied, handle accordingly
                    Toast.makeText(
                        requireContext(),
                        "Location permissions are required for distance tracking.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
            // Handle other permission requests if needed
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        // Stop location updates when the fragment is destroyed
        locationManager.removeUpdates(locationListener)
    }

    inner class WeatherTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String? {
            val response: String?
            try {
                response =
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey")
                        .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                Log.e("RunFragment", "WeatherTask Exception: ${e.message}")
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

                // Convert temperature to Celsius
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
                Log.e("RunFragment", "WeatherTask onPostExecute Exception: ${e.message}")
            }
        }
    }
}
