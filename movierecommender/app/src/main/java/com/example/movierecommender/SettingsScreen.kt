package com.example.movierecommender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.widget.Toolbar
import com.example.movierecommender.databinding.SettingsScreenBinding
import io.socket.client.Socket
import java.lang.Integer.parseInt

class SettingsScreen : AppCompatActivity() {

    private lateinit var binding: SettingsScreenBinding

    private lateinit var mSocket: Socket
    private lateinit var roomId: String
    private var nSwipes: Int = 5

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
            println(roomId);

        }
        getSettings();

        binding.nswipes.progress = nSwipes;

        binding.nswipes.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                setSettings()
                Log.d("Swipes", nSwipes.toString());
            }
        })

        supportActionBar?.apply {
            title = "Settings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setSettings(){
        nSwipes = binding.nswipes.progress + 1;
        mSocket.emit("setSettings", roomId + "," + nSwipes);
    }

    private fun getSettings(){
        mSocket.emit("getSettings", roomId)
        mSocket.on("getSettingsResp"){args ->
            nSwipes =  parseInt(args[0].toString());
            binding.nswipes.progress = nSwipes - 1;
        }
    }

}

