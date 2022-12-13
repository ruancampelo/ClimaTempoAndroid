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
    private val metrics="metric"


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

    }


    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {

        ApiUtil.getApiInterface()?.getCurrentWeatherData(latitude,longitude,apiKey,lang,metrics)
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

        binding.apply {

            tvCidade.text = body.name
            tvTemperatura.text = body.main.temp.toString()+"°C"
            tvDescricao.text=body.weather[0].description
            tvMax.text="Max: "+body.main.temp_max.toString()+"°C"
            tvMin.text="Min: "+body.main.temp_min.toString()+"°C"
            tvSensacaoNumero.text= ""+body.main.feels_like.toString()+"°C"
            tvHumidadeNumero.text = body.main.humidity.toString()+"%"
            tvVentoNumero.text = body.wind.speed.toString()+"m/s"
        }
        updateUI(body.weather[0].id)
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
