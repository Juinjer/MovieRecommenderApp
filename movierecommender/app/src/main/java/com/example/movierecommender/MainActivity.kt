package com.example.movierecommender

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.movierecommender.databinding.ActivityMainBinding

import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding;
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val endpoint = UtilsM.getEndPoint(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar:Toolbar = binding.toolbar.root
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.create.setOnClickListener(View.OnClickListener() {
            var crIdReceived = false
            SocketHandler.setSocket("http://$endpoint")
            SocketHandler.establishConnection()
            val mSocket = SocketHandler.getSocket()
            mSocket?.emit("createRoom", generateID())
            mSocket?.on("crId") { args ->
                crIdReceived = true
                val id = args[0].toString()
                val b = Bundle()
                b.putString("rId", id)
                val intent = Intent(this, CreateRoom::class.java)
                intent.putExtras(b)
                startActivity(intent)
            }
            if(!crIdReceived)
                Toast.makeText(applicationContext, "Unable to connect to server", Toast.LENGTH_SHORT).show()
        })

        binding.join.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, JoinRoom::class.java)
            startActivity(intent)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketHandler.closeConnection()
    }

    fun generateID(): String {
        val uid = application as UniqueID
        if (!uid.initialized) {
            uid.setUniqueId(UUID.randomUUID().toString())
        }
        return uid.uniqueId
    }
}