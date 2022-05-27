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
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

const val BASE_URL = "https://api.data.gov.sg"
val coordList: MutableList<List<List<Double>>> = mutableListOf()

// set your distance here (in meters)
var DISTANCE_AWAY_FROM_USER = 3000

// in km
var SEEKBAR_DEFAULT_DISTANCE = 3
var SEEKBAR_MAX_DISTANCE = 10

var userLat : Double = 0.0
var userLong : Double = 0.0

val results = FloatArray(1)

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

        getJson()
        generateMapForBtn()

        var textForSeekBar = "Taxi Visibility Range: " + SEEKBAR_DEFAULT_DISTANCE.toString() + " km"
        var seekBarText = findViewById<TextView>(R.id.seekBarText)
        seekBarText.text = textForSeekBar

        val seekBar = findViewById<SeekBar>(R.id.seekBar1)

        // i set default max to 10km only (-1 is because the stupid bar must start at 0)
        seekBar.setMax(SEEKBAR_MAX_DISTANCE - 1)
        // i set default to 3km (-1 is because the stupid bar must start at 0)
        seekBar.setProgress(SEEKBAR_DEFAULT_DISTANCE - 1)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{

            var seekBarProgress = 3

            // when there is a change in value
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {

                seekBarProgress = progress + 1
            }

            // when finger is released from seek bar
            override fun onStopTrackingTouch(p0: SeekBar?) {

                //Toast.makeText(applicationContext, seekBarProgress.toString() + " km", Toast.LENGTH_SHORT).show()
                DISTANCE_AWAY_FROM_USER = seekBarProgress * 1000

                textForSeekBar = "Taxi Visibility Range: " + (DISTANCE_AWAY_FROM_USER/1000).toString() + " km"
                seekBarText.text = textForSeekBar

                // pull JSON again after distance changed
                // more intuitive
                // refresh button should still be kept in the scenario where
                // the user dosent want to change the range but wants to refresh to
                // see updated num. of taxis near him/her
                refreshData()

                println("DISTANCE AWAY FROM USER: " + DISTANCE_AWAY_FROM_USER)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }
        })

        refreshData()
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

    private fun getJson(){

        var coordCount = 0
        var long = 0.0
        var lat = 0.0
        var numTaxisNearUser = 0

        var taxiCount2 = "Available Taxis Near You: "


        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        val textView1 = findViewById<TextView>(R.id.txtId)
        val textView2 = findViewById<TextView>(R.id.txtId2)

        //textView1.text = "CALL GET DATA"
        val retrofitData = retrofitBuilder.getData()
        //textView1.text = "AFTER GET DATA"
        retrofitData.enqueue(object : Callback<TaxiData?> {
            override fun onResponse(call: Call<TaxiData?>, response: Response<TaxiData?>) {
                val responseBody = response.body()!!

                var count = 0
                var retrievedTaxiCount = false
                var taxiCount = "Available Taxis in SG: "
                var timeStamp = ""

                for(myData in responseBody.features){
                    coordList.add(myData.geometry.coordinates.toList())

                    // if taxi count has not been retrieved
                    // retrieve it
                    if (!retrievedTaxiCount){

                        taxiCount += myData.properties.taxi_count.toString()
                        timeStamp = myData.properties.timestamp
                    }
                }

                val myStringBuilder = StringBuilder()

                for(coords in coordList[0]){
                    myStringBuilder.append(coords)
                    myStringBuilder.append("\n")

                    for (taxiCoord in coords) {

                        coordCount += 1

                        // if count is odd
                        if (coordCount % 2 != 0)
                            long = taxiCoord

                        // if count is even
                        else {
                            lat = taxiCoord

                            android.location.Location.distanceBetween(1.3040612421100153, 103.83146976185485, lat, long, results)

                            if (results[0] < DISTANCE_AWAY_FROM_USER) {

                                numTaxisNearUser += 1
                                println("distance: " + results[0])
                            }
                        }
                    }
                }

                textView1.text = taxiCount + "\nLast Updated: "+timeStamp

                taxiCount2 += numTaxisNearUser.toString()
                textView2.text = taxiCount2
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

    private fun refreshData(){

        val refreshBtn = findViewById<Button>(R.id.refresh)

        // on refresh button click
        refreshBtn.setOnClickListener() {
            println("Refresh button clicked")
            coordList.clear()
            getJson()
        }
    }
}