package com.example.movierecommender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.movierecommender.databinding.SwipeScreenBinding
import com.example.movierecommender.databinding.WaitingRoomBinding

class SwipeScreen : AppCompatActivity() {
    private lateinit var binding: SwipeScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SwipeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}