package com.example.movierecommender

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.example.movierecommender.databinding.SettingsScreenBinding
import io.socket.client.Socket
import java.lang.Integer.parseInt

class SettingsScreen : AppCompatActivity() {

    private lateinit var binding: SettingsScreenBinding

    private lateinit var mSocket: Socket
    private lateinit var roomId: String
    private var nSwipes: Int = 5

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsScreenBinding.inflate(layoutInflater)
        mSocket= SocketHandler.getSocket()!!
        setContentView(binding.root)
        val toolbar:Toolbar = binding.settingsToolbar.root
        setSupportActionBar(toolbar)

        val b = intent.extras
        roomId = "404"
        if (b != null) {
            roomId = b.getString("roomcode")!!
            Log.d("SettingsScreen", "Room ID: $roomId");

        }
        getSettings();
        binding.sbSwipesThreshold.min = 3;
        binding.sbSwipesThreshold.max = 10;
        binding.sbSwipesThreshold.progress = nSwipes;

        binding.sbSwipesThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update TextView content as the SeekBar progresses
                binding.tvSwipeThreshold.text = "$progress"
                nSwipes = binding.sbSwipesThreshold.progress;
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("SwipeThreshold", "${binding.sbSwipesThreshold.progress}");
            }
        })

        supportActionBar?.apply {
            title = "App Settings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        setSettings()
        super.onBackPressed()
    }

    private fun setSettings(){
        Log.d("SettingsScreen", "SetSettings")
        mSocket.emit("setSettings", "$roomId,$nSwipes");
    }

    private fun getSettings(){
        Log.d("SettingsScreen", "GetSettings")
        mSocket.emit("getSettings", roomId)
        mSocket.on("getSettingsResp"){args ->
            nSwipes =  parseInt(args[0].toString());
            binding.sbSwipesThreshold.progress = nSwipes;
        }
    }

}

