package com.example.movierecommender

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.movierecommender.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.create.setOnClickListener(View.OnClickListener() {
            val intent = Intent(this,CreateRoom::class.java)
            startActivity(intent)
        })
        binding.join.setOnClickListener(View.OnClickListener {
            val intent = Intent(this,JoinRoom::class.java)
            startActivity(intent)
        })
    }
}