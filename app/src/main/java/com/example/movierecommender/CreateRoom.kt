package com.example.movierecommender

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.movierecommender.databinding.ActivityCreateRoomBinding

class CreateRoom : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancel.setOnClickListener(View.OnClickListener() {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        })
    }
}