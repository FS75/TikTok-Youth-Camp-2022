package com.example.taxiapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://api.data.gov.sg"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getFeature()
    }
    private fun getFeature(){

        val textId = findViewById<TextView>(R.id.txtId)

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        textId.text = "CALL GET DATA"
        val retrofitData = retrofitBuilder.getData2()
        textId.text = "AFTER GET DATA"
        retrofitData.enqueue(object : Callback<TaxiData?> {
            override fun onResponse(call: Call<TaxiData?>, response: Response<TaxiData?>) {
                val responseBody = response.body()!!

                val list: MutableList<List<List<Double>>> = mutableListOf()
                var count =0
                for(myData in responseBody.features){
                    list.add(myData.geometry.coordinates.toList())
                }

                val myStringBuilder = StringBuilder()
                for(coords in list[0]){
                    myStringBuilder.append(coords)
                    myStringBuilder.append("\n")

                }
                textId.text = myStringBuilder
            }

            override fun onFailure(call: Call<TaxiData?>, t: Throwable) {
            }
        })
    /*
        retrofitData.enqueue(object : Callback<List<TaxiData>?> {
            override fun onResponse(
                call: Call<List<TaxiData>?>,
                response: Response<List<TaxiData>?>
            ) {
                val responseBody = response.body()!!

                val myStringBuilder = StringBuilder()
                for(myData in responseBody){
                    myStringBuilder.append(myData)
                    myStringBuilder.append("\n")
                }
                textId.text = "SUCESS"
            }
            override fun onFailure(call: Call<List<TaxiData>?>, t: Throwable) {
                Log.d("Map","onFailure:"+t.message)
                //textId.text = t.message
            }
        })

    */
    }
}