package com.example.movierecommender

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.movierecommender.databinding.ActivityJoinRoomBinding

class JoinRoom : AppCompatActivity() {
    private lateinit var binding: ActivityJoinRoomBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cancel.setOnClickListener(View.OnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
        binding.joinBtn.setOnClickListener(View.OnClickListener {
            val roomId = binding.roomidText.text.toString()
            var mSocket = SocketHandler.getSocket()
            if (mSocket==null || !mSocket.connected()){
                SocketHandler.setSocket("http://${UtilsM.getEndPoint(this)}")
                SocketHandler.establishConnection()
                mSocket = SocketHandler.getSocket()
            }
            val id= (application as UniqueID).uniqueId
            val data = listOf(roomId, id)
            mSocket?.emit("joinRoom", data.joinToString(","))

            mSocket?.on("jrRes"){args ->
                val b = Bundle()
                b.putString("members", args[0].toString())
                b.putString("roomcode", args[1].toString())
                val intent = Intent(this, WaitingRoom::class.java)
                intent.putExtras(b)
                startActivity(intent)
            }
        })
        binding.mainid.setOnTouchListener(View.OnTouchListener { view, _ ->
            hideKeyboard(view)
            return@OnTouchListener true
        })
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}