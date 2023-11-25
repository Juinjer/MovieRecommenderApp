package com.example.movierecommender

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movierecommender.databinding.WaitingRoomBinding

class WaitingRoom : AppCompatActivity() {
    private lateinit var binding: WaitingRoomBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var nameList: ArrayList<Name>
    private lateinit var nameAdapter: NameAdapter

    @SuppressLint("ClickableViewAccessibility") //TODO look for a better way
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = WaitingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val b = intent.extras
        if (b != null) {
            binding.roomId.setText(b.getString("roomcode"))
        }
        binding.cancel.setOnClickListener(View.OnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        nameList = ArrayList()
        nameList.add(Name(R.mipmap.camera_logo_foreground, "Testnaam"))
        nameList.add(Name(R.mipmap.camera_logo_foreground, "Testnaam1"))
        nameList.add(Name(R.mipmap.camera_logo_foreground, "Testnaam2"))
        nameList.add(Name(R.mipmap.camera_logo_foreground, "Testnaam3"))
        nameList.add(Name(R.mipmap.camera_logo_foreground, "Testnaam4"))

        nameAdapter = NameAdapter(nameList)
        recyclerView.adapter = nameAdapter
    }
}