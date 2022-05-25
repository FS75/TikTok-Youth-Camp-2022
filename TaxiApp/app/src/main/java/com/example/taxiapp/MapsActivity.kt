package com.example.taxiapp

import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taxiapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        gMap = googleMap

        // list[0] is one big chunk of coords

        var count = 0
        var lat = 0.0
        var long = 0.0
        var convertedCoord = LatLng(0.0, 0.0)

        for (coords in coordList[0]){

            for (coord in coords){

                count += 1

                // if count is odd
                if (count % 2 != 0)
                    long = coord

                // if count is even
                else{
                    //var coordCount = count / 2
                    lat = coord

                    //println("lat $lat")
                    //println("long $long")

                    // lat long pair retrieved
                    // can convert
                    convertedCoord = LatLng(lat, long)
                    //println(convertedCoord)
                    gMap.addMarker(MarkerOptions().position(convertedCoord).title("Marker"))
                }
            }

            //float   HUE_AZURE
            //float   HUE_BLUE
            //float   HUE_CYAN
            //float   HUE_GREEN
            //float   HUE_MAGENTA
            //float   HUE_ORANGE
            //float   HUE_RED
            //float   HUE_ROSE
            //float   HUE_VIOLET
            //float   HUE_YELLOW

            // currentLocation should change to user's current location when code is up

            //var currentLocation = LatLng(1.3732, 103.9493) // Pasir Ris MRT for testing

            var currentLocation = LatLng(userLat, userLong) // current location
            println("current loc " + currentLocation)
            gMap.addMarker(MarkerOptions().position(currentLocation).title("UserCurrentLocation")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

            // zoom onto user's current location
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17F))
        }
    }

}