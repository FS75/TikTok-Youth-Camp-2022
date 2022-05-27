package com.example.taxiapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taxiapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.w3c.dom.Text


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var currentLocation : LatLng
    private lateinit var staticLocation : LatLng

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
        drawMarkersOnMap(gMap)
        currentLocation = LatLng(userLat, userLong) // current location
        staticLocation = LatLng(1.3040612421100153, 103.83146976185485)
        gMap.addMarker(MarkerOptions().position(staticLocation).title("UserCurrentLocation")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        // zoom onto user's current location
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(staticLocation, 17F))

        // just for testing radius working or not
        // drawCircleOnMap(LatLng(1.3040612421100153, 103.83146976185485))

    }

    private fun drawMarkersOnMap(googleMap: GoogleMap){
        var count = 0
        var lat = 0.0
        var long = 0.0
        var convertedTaxiCoord = LatLng(0.0, 0.0)

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
                    android.location.Location.distanceBetween(1.3040612421100153, 103.83146976185485, lat, long, results)

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

}