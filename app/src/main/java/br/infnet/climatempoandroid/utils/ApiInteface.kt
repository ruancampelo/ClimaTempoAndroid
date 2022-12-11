package br.infnet.climatempoandroid.utils

import br.infnet.climatempoandroid.model.TempoModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInteface {

    @GET("weather")
    fun getCurrentWeatherData(
        @Query("lat") lat:String,
        @Query("lon") lon:String,
        @Query("APPID") appid:String,
        @Query("lang") lang:String
    ): Call<TempoModel>

    @GET("weather")
    fun getCityWeatherData(
        @Query("q") q:String,
        @Query("APPID") appid:String,
        @Query("lang") lang:String
    ):Call<TempoModel>
}