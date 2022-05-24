package com.example.taxiapp
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {
    @GET("/v1/transport/taxi-availability")
    fun getData(): Call<List<TaxiData>>

    @GET("/v1/transport/taxi-availability")
    fun getData2(): Call<TaxiData>
}