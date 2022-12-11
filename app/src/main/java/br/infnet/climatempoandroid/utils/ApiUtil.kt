package br.infnet.climatempoandroid.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtil {

    private var retrofit:Retrofit?=null

    var BASE_URL = "https://api.openweathermap.org/data/2.5/"

    fun getApiInterface(): ApiInteface?{

        if (retrofit ==null){

            retrofit =Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()


        }

        return retrofit?.create(ApiInteface::class.java)


    }
}