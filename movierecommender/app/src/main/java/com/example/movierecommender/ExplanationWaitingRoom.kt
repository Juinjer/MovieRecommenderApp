package com.example.movierecommender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.example.movierecommender.databinding.SettingsScreenBinding
import io.socket.client.Socket
import org.json.JSONObject

private lateinit var roomId:String
private lateinit var id:String
private lateinit var mSocket: Socket

class ExplanationWaitingRoom : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explanation_waiting_room)

        mSocket= SocketHandler.getSocket()!!

        val b = intent.extras
        roomId = "404"
        if (b != null) {
            roomId = b.getString("roomcode")!!
            Log.d("ExplanationWaitingRoom", "Room ID: $roomId");
        }

        mSocket.emit("getSimilar", (application as UniqueID).uniqueId)
        mSocket.on("getSimilarResp") { args ->
            val jsonMovies = JSONObject(args[0].toString())
            Log.d("movie_title",jsonMovies.getString("movie_title"))
            Log.d("recommendations", jsonMovies.getString("recommendations"))
        }
    }
}