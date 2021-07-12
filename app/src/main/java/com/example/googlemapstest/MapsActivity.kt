package com.example.googlemapstest

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fuseLocationProviderClient:FusedLocationProviderClient
    private var locationRequest = LocationRequest()
    private var locationCallback = MyLocationCallBack()
    private val REQUEST_ACCESS_FINE_LOCATION = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // SupportMapFragment를 가져와서 지도가 준비되면 알림을 받음
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationInit()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun locationInit() {
        fuseLocationProviderClient = FusedLocationProviderClient(this)

        locationCallback = MyLocationCallBack()
        locationRequest = LocationRequest()

        //GPS 우선
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 10000
        //정확함. 이것보다 짧은 업데이트는 하지 않음
        locationRequest.fastestInterval = 5000
    }

    override fun onResume(){
        super.onResume()

        //권한 요청
        permissionCheck(cancel = {
            showPermissionInfoDialog()
        }, ok = {
            addLocationListener()
        })
    }
    private fun showPermissionInfoDialog() {
        alert("현재 위치 정보를 얻으려면 위치 권한이 필요합니다", "권한이 필요한 이유"){
            yesButton {
                //권한요청
                ActivityCompat.requestPermissions(this@MapsActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_ACCESS_FINE_LOCATION)
            }
            noButton {  }
        }.show()
    }

    private fun permissionCheck(cancel:() -> Unit, ok:() -> Unit){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            //권한이 허용되지 x
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                //이전에 궎한을 한번 거부한 적이 있는 경우에 실행 함수
                cancel()
            } else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
            }
        } else {
            //권한을 수락했을 때 실행할 함수
            ok()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_ACCESS_FINE_LOCATION->{
                if ((grantResults.isNotEmpty()
                                &&grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    //권한 허용됨
                    addLocationListener()
                } else {
                    //권한 거부됨
                    toast("권한 거부")
                }
                return
            }
        }
    }

    //현재 위치를 업데이트 시키는 함수
    @SuppressLint("MissingPermission")
    private fun addLocationListener() {
        fuseLocationProviderClient.requestLocationUpdates(locationRequest,
        locationCallback, null)
    }

    inner class MyLocationCallBack:LocationCallback() {
        override fun onLocationResult(locationRequest: LocationResult?) {
            super.onLocationResult(locationRequest)

            // 마지막으로 알려진 위치를 가져옴
            val location = locationRequest?.lastLocation

            location?.run {
                //14레벨로 확대하고 현재 위치로 카메라 이동
                val latLng = LatLng(latitude, longitude)
                //mMap.animateCamera 함수를 통해 지도의 중심점을 해당 위치로 옮김
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                // 정확한 위경도 확인
                Log.d("MapsActivity", "위도: $latitude, 경도: $longitude")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        removeLocationListener()
    }

    //액티비티가 가려질 시 LocationListener 삭제
    private fun removeLocationListener(){
        fuseLocationProviderClient.removeLocationUpdates(locationCallback)
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

}