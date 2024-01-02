package com.example.cineinsight

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.cineinsight.databinding.ActivityMainBinding

import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding;
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val endpoint = UtilsM.getEndPoint(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar:Toolbar = binding.toolbarMain.root
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(true)
        }
        val uuid = generateID()

        binding.create.setOnClickListener(View.OnClickListener() {
            var crIdReceived = false
            val handler = Handler(Looper.getMainLooper())
            SocketHandler.setSocket("http://$endpoint")
            SocketHandler.establishConnection()
            val mSocket = SocketHandler.getSocket()
            mSocket?.emit("createRoom", uuid)

            handler.postDelayed({
                if (!crIdReceived) {
                    Toast.makeText(applicationContext, "Unable to connect to server", Toast.LENGTH_SHORT).show()
                }
            }, 2000L) // 2 sec delay if server doesn't respond
            mSocket?.on("crId") { args ->
                crIdReceived = true
                val rid = args[1].toString()
                val b = Bundle()
                b.putString("members", args[0].toString())
                b.putString("rId", rid)
                val intent = Intent(this, CreateRoom::class.java)
                intent.putExtras(b)
                startActivity(intent)
            }
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