package com.example.taxiapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

const val BASE_URL = "https://api.data.gov.sg"
val coordList: MutableList<List<List<Double>>> = mutableListOf()

var userLat : Double = 0.0
var userLong : Double = 0.0

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest:LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {

        println("in on create")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()
        //checkLocationPermission()
        //checkGPS()

        getFeature()
        generateMapForBtn()
    }
    private fun getCurrentLocation(){
        if(checkPermission()){
            if(isLocationEnabled()){
                //User lat/long can be accessed
                //Permission check for the fusedLocation
                if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)!=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
                    PackageManager.PERMISSION_GRANTED){
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){
                    task-> val location:Location?=task.result
                    if(location == null){
                        Toast.makeText(this,"Null received", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this,"Get success", Toast.LENGTH_SHORT).show()
                        userLat = location.latitude
                        userLong = location.longitude
                        println( "Lat/Long: " + userLat + userLong)

                    }
                }
            }
            else{
                //open settings
                Toast.makeText(this,"Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        else{
            //Request permission
            requestPermission()
        }

    }

    private fun isLocationEnabled(): Boolean {
        val locationManger:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManger.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
        arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION),
        PERMISSION_REQUESTION_ACCESS_LOCATION
        )
    }

    companion object{
        private const val PERMISSION_REQUESTION_ACCESS_LOCATION=100
    }

    private fun checkPermission(): Boolean
    {
        if(ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUESTION_ACCESS_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
                }
            else{
                //Permision not granted, cannot view map
                Toast.makeText(applicationContext,"Denied", Toast.LENGTH_SHORT).show()
                val viewMapBtn = findViewById<Button>(R.id.viewMapBtn)
                viewMapBtn.isEnabled = false

            }
        }
    }

//    //Juleus Stuff
//    private fun checkLocationPermission(){
//
//        println("in check location permission")
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            // permission granted

//            //checkGPS()
//        }
//        else{
//
//            // permission denied
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 100)
//        }
//    }
//
//    private fun checkGPS(){
//
//        println("in check gps")
//
//        locationRequest = LocationRequest.create()
//        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        locationRequest.interval = 5000
//        locationRequest.fastestInterval = 2000
//
//        val builder = LocationSettingsRequest.Builder()
//            .addLocationRequest(locationRequest)
//
//        builder.setAlwaysShow(true)
//
//        val result = LocationServices.getSettingsClient(this.applicationContext)
//            .checkLocationSettings(builder.build())
//
//        result.addOnCompleteListener { task ->
//
//            try {
//
//                println("gps on")
//
//                // when GPS is on
//                val response = task.getResult(
//                    ApiException::class.java
//                )
//
//                println("getting user location")
//                getUserLocation()
//
//
//            } catch( e : ApiException){
//
//                println("gps off")
//                // when GPS is off
//                e.printStackTrace()
//
//                when (e.statusCode){
//
//                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try{
//
//                        // send request to enable GPS
//                        val resolveApiException = e as ResolvableApiException
//                        resolveApiException.startResolutionForResult(this, 200)
//
//                    } catch (sendIntentException : IntentSender.SendIntentException){
//
//                    }
//                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
//
//                        // when setting changes are unavailable
//
//                    }
//                }
//            }
//        }
//    }
//
//    private fun getUserLocation(){
//
//        println("in get user location")
//
//        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task ->
//            val location: Location? = task.result
//
//            println("location " + location)
//
//            if (location != null){
//
//                try {
//
//                    println("in location valid")
//                    //var geocoder = Geocoder(this, Locale.getDefault())
//
//                    userLat = location.latitude
//                    userLong = location.longitude
//
//                    /*val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
//                    val addressLine = address[0].getAddressLine(0)*/
//
//                } catch (e : IOException) {
//
//                    e.printStackTrace()
//                }
//            }
//        }
//    }

    private fun getFeature(){

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        val textView1 = findViewById<TextView>(R.id.txtId)

        //textView1.text = "CALL GET DATA"
        val retrofitData = retrofitBuilder.getData2()
        //textView1.text = "AFTER GET DATA"
        retrofitData.enqueue(object : Callback<TaxiData?> {
            override fun onResponse(call: Call<TaxiData?>, response: Response<TaxiData?>) {
                val responseBody = response.body()!!

                var count = 0
                var retrievedTaxiCount = false
                var taxiCount = "Available Taxis in SG: "

                for(myData in responseBody.features){
                    coordList.add(myData.geometry.coordinates.toList())

                    // if taxi count has not been retrieved
                    // retrieve it
                    if (!retrievedTaxiCount){

                        taxiCount += myData.properties.taxi_count.toString()
                    }
                }

                val myStringBuilder = StringBuilder()
                for(coords in coordList[0]){
                    myStringBuilder.append(coords)
                    myStringBuilder.append("\n")
                }

                //textView1.text = myStringBuilder
                textView1.text = taxiCount
            }

            override fun onFailure(call: Call<TaxiData?>, t: Throwable) {
            }
        })
    }

    private fun generateMapForBtn(){

        val viewMapBtn = findViewById<Button>(R.id.viewMapBtn)

        // on view map button click
        viewMapBtn.setOnClickListener() {

            //checkGPS()
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}