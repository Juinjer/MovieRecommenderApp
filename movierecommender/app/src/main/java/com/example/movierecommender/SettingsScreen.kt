package com.example.movierecommender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.movierecommender.databinding.SettingsScreenBinding

class SettingsScreen : AppCompatActivity() {

    private lateinit var binding: SettingsScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar:Toolbar = binding.settingsToolbar.root
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Settings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}