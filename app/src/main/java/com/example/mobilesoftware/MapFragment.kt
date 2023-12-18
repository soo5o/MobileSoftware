package com.example.mobilesoftware

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null
    private var polyline: Polyline? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 500
    private var currentMarker: Marker? = null
    private var polylineOptions: PolylineOptions? = null
    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var isFirstLocationUpdate = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // 초기화 및 권한 체크
        initLocation()

        // 위치 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 이미 부여된 경우
            startLocationUpdates()
        } else {
            // 권한이 부여되지 않은 경우 권한 요청
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        // 맵 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 내 위치 버튼 비활성화
        mMap.uiSettings.isMyLocationButtonEnabled = false

        // 위치 업데이트 요청
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 이미 부여된 경우 위치 업데이트 요청
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // 위치 권한이 없는 경우 권한 요청
            requestLocationPermission()
        }

        // 초기 위치를 표시하기 위해 맵이 준비될 때까지 대기하지 않도록 변경
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(location.latitude, location.longitude)
                // 처음 위치 업데이트가 발생한 경우에만 현재 위치로 이동
                if (isFirstLocationUpdate) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    isFirstLocationUpdate = false
                }

                // 초기 마커 추가
                currentMarker = mMap.addMarker(MarkerOptions().position(latLng).title("현재 위치"))

                // 이전 위치와 현재 위치를 선으로 연결
                lastLocation?.let {
                    drawPolyline(it, location)
                }

                lastLocation = location
            }
        }

        // 지도 클릭 이벤트 리스너 등록
        mMap.setOnMapClickListener { latLng ->
            // 사용자가 지도를 클릭할 때마다 호출되는 콜백
            currentMarker?.position = latLng
        }
    }

    private fun initLocation() {
        fusedLocationClient = FusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    onLocationChanged(location)
                }
            }
        }

        // 권한 체크 및 위치 업데이트 요청
        checkPermissions()
    }

    private fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)

        // 이전 마커가 있다면 제거
        currentMarker?.remove()

        // 처음 위치 업데이트가 발생한 경우에만 현재 위치로 이동
        if (isFirstLocationUpdate) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            isFirstLocationUpdate = false
        }

        // 새로운 마커 추가
        currentMarker = mMap.addMarker(MarkerOptions().position(latLng).title("현재 위치"))

        // 이전 위치와 현재 위치를 선으로 연결
        lastLocation?.let {
            drawPolyline(it, location)
        }

        lastLocation = location
    }

    private fun drawPolyline(startLocation: Location, endLocation: Location) {
        // PolylineOptions 초기화
        if (polylineOptions == null) {
            polylineOptions = PolylineOptions().width(5f).color(Color.RED)
        }

        // 이전 Polyline이 있다면 제거
        polyline?.remove()

        // Polyline 업데이트
        polylineOptions?.add(LatLng(endLocation.latitude, endLocation.longitude))
        polyline = mMap.addPolyline(polylineOptions!!)
    }

    private fun checkPermissions() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            // 권한이 없으면 권한 요청
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onDestroyView() {
        // 위치 업데이트 중지 또는 필요한 정리 작업 수행
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroyView()
    }

    private fun showPermissionAlertDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("권한 필요")
            .setMessage("현재 위치를 사용하려면 위치 권한이 필요합니다. 설정에서 권한을 부여해 주세요.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여되면 위치 업데이트 시작
                startLocationUpdates()
            } else {
                // 권한이 거부된 경우 처리
                showPermissionAlertDialog()
            }
        }
    }

    private fun startLocationUpdates() {
        // 위치 권한 확인
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 이미 부여된 경우 위치 업데이트 요청
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // 위치 권한이 없는 경우 권한 요청
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // 권한이 이전에 거부되었지만 사용자에게 설명이 필요한 경우
            showPermissionAlertDialog()
        } else {
            // 권한을 요청
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }
}