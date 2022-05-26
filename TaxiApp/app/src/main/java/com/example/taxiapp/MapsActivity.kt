package com.example.taxiapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taxiapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

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
        drawMarkerOnMap(gMap)
        var currentLocation = LatLng(userLat, userLong) // current location
        var staticLocation = LatLng(1.3040612421100153, 103.83146976185485)
        gMap.addMarker(MarkerOptions().position(staticLocation).title("UserCurrentLocation")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        // zoom onto user's current location
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(staticLocation, 17F))
    }

    private fun drawMarkerOnMap(googleMap: GoogleMap){
        var count = 0
        var lat = 0.0
        var long = 0.0
        var convertedCoord = LatLng(0.0, 0.0)
        for (coords in coordList[0]){

            for (coord in coords) {

                count += 1

                // if count is odd
                if (count % 2 != 0)
                    long = coord

                // if count is even
                else {
                    //var coordCount = count / 2
                    lat = coord

                    //println("lat $lat")
                    //println("long $long")

                    // lat long pair retrieved
                    // can convert
                    convertedCoord = LatLng(lat, long)
                    //println(convertedCoord)
                    //gMap.addMarker(MarkerOptions().position(convertedCoord).title("Marker").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_local_taxi_24)))
                    gMap.addMarker(
                        MarkerOptions().position(convertedCoord).title("Taxi")
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
                    //gMap.addMarker(MarkerOptions().position(convertedCoord).title("Taxi"))
                }
            }
        }
    }

}