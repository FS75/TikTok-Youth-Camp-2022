package com.example.taxiapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taxiapp.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.internal.ConnectionCallbacks
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import org.w3c.dom.Text
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    //private lateinit var currentLocation : LatLng
    //private lateinit var staticLocation : LatLng
    internal lateinit var mLastLocation: Location
    private lateinit var mFusedLocationProviderClient:FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {

        gMap = googleMap
        // map setup, will do user location and Taxi markers inside
        setUpMap()
        // just for testing radius working or not
        // drawCircleOnMap(LatLng(1.3040612421100153, 103.83146976185485))

    }

    @SuppressLint("MissingPermission")  //If don't have this, somehow the permissions below cannot work
    private fun setUpMap() {
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)   {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        gMap.isMyLocationEnabled = true
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location->
            if (location != null){
                mLastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                drawMarkersOnMap(gMap, currentLatLong)
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            }
        }
    }

    private fun drawMarkersOnMap(googleMap: GoogleMap, currentLatLng: LatLng){
        var count = 0
        var lat = 0.0
        var long = 0.0
        var convertedTaxiCoord = LatLng(0.0, 0.0)
        /* TBD if still need a user marker
        gMap.addMarker(MarkerOptions().position(currentLatLng).title("UserCurrentLocation")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
        */
        for (taxiCoords in coordList[0]){

            for (taxiCoord in taxiCoords) {

                count += 1

                // if count is odd
                if (count % 2 != 0)
                    long = taxiCoord

                // if count is even
                else {
                    lat = taxiCoord

                    // lat long pair retrieved, convert
                    convertedTaxiCoord = LatLng(lat, long)
                    android.location.Location.distanceBetween(currentLatLng.latitude,currentLatLng.longitude , lat, long, results)

                    if (results[0] < DISTANCE_AWAY_FROM_USER){

                        gMap.addMarker(
                            MarkerOptions().position(convertedTaxiCoord).title("Taxi")
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        resizeMapIcons(
                                            "taxi_icon",
                                            150,
                                            150
                                        )
                                    )
                                )
                        )
                    }

                    //println(convertedTaxiCoord)
                    //gMap.addMarker(MarkerOptions().position(convertedTaxiCoord).title("Marker").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_local_taxi_24)))
                    //gMap.addMarker(MarkerOptions().position(convertedTaxiCoord).title("Taxi"))
                }
            }
        }
    }

    // this function is just for testing whether the radius accurate or not
    private fun drawCircleOnMap(point : LatLng){

        var circleOptions = CircleOptions()

        // set center coordinates of circle
        circleOptions.center(point)
        circleOptions.radius(500.0) // radius is in meters

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);

        // Border width of the circle
        circleOptions.strokeWidth(2F);

        // Adding the circle to the GoogleMap
        gMap.addCircle(circleOptions);
    }

    // Trying to draw sum markers
    open fun resizeMapIcons(
        iconName: String?,
        width: Int,
        height: Int
        ): Bitmap? {
            val imageBitmap = BitmapFactory.decodeResource(
                resources, resources.getIdentifier(
                    iconName, "drawable",
                    packageName
                )
            )
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }
}