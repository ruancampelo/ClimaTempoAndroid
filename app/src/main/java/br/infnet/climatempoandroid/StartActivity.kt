package br.infnet.climatempoandroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.infnet.climatempoandroid.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setup()

        val firstboot: Boolean =
            getSharedPreferences("BOOT_PREF", MODE_PRIVATE).getBoolean("firstboot", false)

        if(firstboot==true){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            getSharedPreferences("BOOT_PREF", MODE_PRIVATE)
                .edit()
                .putBoolean("firstboot", true)
                .commit();
        }
    }


    private fun setup() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnInicio.setOnClickListener {
                startMainActivity()

            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}