package br.infnet.climatempoandroid

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import br.infnet.climatempoandroid.databinding.ActivityMainBinding
import br.infnet.climatempoandroid.model.TempoModel
import br.infnet.climatempoandroid.utils.ApiInteface
import br.infnet.climatempoandroid.utils.ApiUtil
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 101

    private val apiKey="71fe2ae8ebc63936eb0be1d18020853a"
    private val lang="pt_br"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fusedLocationProvider= LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        binding.adView3.loadAd(adRequest)



    }




    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {

        ApiUtil.getApiInterface()?.getCurrentWeatherData(latitude,longitude,apiKey,lang)
            ?.enqueue(object : Callback<TempoModel> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<TempoModel>, response: Response<TempoModel>) {
                    if (response.isSuccessful){
                        response.body()?.let {
                            setData(it)
                        }
                    }
                }
                override fun onFailure(call: Call<TempoModel>, t: Throwable) {
                }
            })
    }


    private fun getCurrentLocation(){

        if (checkPermissions()){
            if (isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProvider.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = location
                            fetchCurrentLocationWeather(
                                location.latitude.toString(),
                                location.longitude.toString()
                            )
                        }
                    }
            }
            else{
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        else{
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_REQUEST_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {

        val locationManager: LocationManager =getSystemService(Context.LOCATION_SERVICE)
                as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
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
        if (requestCode==LOCATION_REQUEST_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
            else{

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData(body:TempoModel){
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        binding.apply {

            tvCidade.text = body.name
            tvTemperatura.text = ""+k2c(body?.main?.temp!!)+"°C"
            tvDescricao.text=body.weather[0].description
            tvMax.text="Max: "+k2c(body?.main?.temp_max!!)+"°C"
            tvMin.text="Min: "+k2c(body?.main?.temp_min!!)+"°C"
            tvFeelslike.text= ""+k2c(body?.main.feels_like)+"°C"
            tvHumidade.text = body.main.humidity.toString()+"%"
            tvPressure.text = body.main.pressure.toString()
            tvVento.text = body.wind.speed.toString()+"m/s"
            tvNascerSol.text = ts2td(body.sys.sunrise.toLong())
            tvPorDoSol.text = ts2td(body.sys.sunset.toLong())
            tvNivelMar.text = body.main.sea_level.toString()

            executor.execute {

                val lat = currentLocation.latitude.toString()
                val lon = currentLocation.longitude.toString()
                val response2:String? = try{
                    URL(
                        "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=71fe2ae8ebc63936eb0be1d18020853a&units=metric")
                        .readText(Charsets.UTF_8)
                } catch (e: Exception){
                    Log.d("TAG", "api not working")
                    null
                }
                handler.post {
                    try{
                        val jsonObject2 = JSONObject(response2)
                        val locale = Locale("pt", "BR")
                        val date1: Long = jsonObject2.getJSONArray("list").getJSONObject(0).getLong("dt")
                        val day1 = jsonObject2.getJSONArray("list").getJSONObject(0).getJSONObject("main")
                        val temp1: String = day1.getLong("temp").toString() + "°C"
                        val date = SimpleDateFormat("EEE", locale).format(Date(date1 * 1000))
                        forecastTemp1.text = temp1
                        forecastDate1.text = date

                        var temps = mutableListOf<String>()
                        var days = mutableListOf<String>()
                        var cur = 1
                        val max = 5
                        var i = 1
                        var prevDate = date

                        while (cur < max) {
                            val dt: Long = jsonObject2.getJSONArray("list").getJSONObject(i).getLong("dt")
                            val currDate = SimpleDateFormat("EEE", locale).format(Date(dt * 1000))
                            if(prevDate != currDate){
                                var currTemp: String = jsonObject2.getJSONArray("list").getJSONObject(i).getJSONObject("main").getLong("temp").toString() + "°C"
                                temps.add(currTemp)
                                days.add(currDate)
                                prevDate = currDate
                                cur++
                            }
                            i++
                        }
                        forecastTemp2.text = temps[0]
                        forecastDate2.text = days[0]
                        forecastTemp3.text = temps[1]
                        forecastDate3.text = days[1]
                        forecastTemp4.text = temps[2]
                        forecastDate4.text = days[2]
                        forecastTemp5.text = temps[3]
                        forecastDate5.text = days[3]

                    }

                    catch (e: Exception){

                    }
                }
            }

        }
        updateUI(body.weather[0].id)
    }

    private fun k2c(t:Double):Double{

        var intTemp=t

        intTemp=intTemp.minus(273)

        return intTemp.toBigDecimal().setScale(1,RoundingMode.UP).toDouble()
    }


    private fun ts2td(ts:Long):String{

        val localTime=ts.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }


    private fun updateUI(id: Int) {

        binding.apply {

            when (id) {
                in 200..232 -> {
                    ivTempo.setImageResource(R.drawable.ic_storm)
                    mainlayout.background= ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.bg_storm)
                }

                in 300..321 -> {
                    ivTempo.setImageResource(R.drawable.ic_sun_cloud)
                    mainlayout.background= ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.bg_ameno)
                }

                in 500..531 -> {
                    ivTempo.setImageResource(R.drawable.ic_rain)
                    mainlayout.background= ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.bg_ameno)
                }

                in 600..622 -> {
                    ivTempo.setImageResource(R.drawable.ic_cloud_ice)
                    mainlayout.background= ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.bg_snow)
                }

                in 701..781 -> {
                    ivTempo.setImageResource(R.drawable.ic_cloud)
                    mainlayout.background= ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.bg_ameno)
                }

                800 -> {
                    ivTempo.setImageResource(R.drawable.ic_sun)
                    mainlayout.background= ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.bg_sum)
                }

                in 801..804 -> {
                    ivTempo.setImageResource(R.drawable.ic_clouds)
                    mainlayout.background= ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.bg_ameno)
                }

                else->{
                    ivTempo.setImageResource(R.drawable.ic_baseline_block_24)
                    mainlayout.background= ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.bg_unknown)
                }
            }
        }
    }
}
