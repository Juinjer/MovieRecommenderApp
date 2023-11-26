package com.example.movierecommender

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
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
            val s = b.getString("roomcode")
            val editable: Editable = Editable.Factory.getInstance().newEditable(s)
            binding.roomId.text = editable
        }
        binding.cancel.setOnClickListener(View.OnClickListener() {
            var mSocket = SocketHandler.getSocket()
            val id= (application as UniqueID).uniqueId
            val roomID = binding.roomId.text.toString()
            val data = listOf(roomID, id)
            mSocket?.emit("leaveRoom", data.joinToString(","))
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        nameList = ArrayList()
        if (b != null) {
            val s = b.getString("members")!!
            val splitted = s.split(",")
            for(st in splitted) {
                nameList.add(Name(R.mipmap.camera_logo_foreground,st))
            }
        }

        nameAdapter = NameAdapter(nameList)
        recyclerView.adapter = nameAdapter
    }
}