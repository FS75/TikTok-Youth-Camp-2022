package com.example.taxiapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://api.data.gov.sg"
val coordList: MutableList<List<List<Double>>> = mutableListOf()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getFeature()
        generateMapForBtn()
    }

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

            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}