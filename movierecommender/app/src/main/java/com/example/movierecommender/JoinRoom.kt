package com.example.movierecommender

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.movierecommender.databinding.ActivityJoinRoomBinding

class JoinRoom : AppCompatActivity() {
    private lateinit var binding: ActivityJoinRoomBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val handler = Handler(Looper.getMainLooper())

        binding.cancel.setOnClickListener(View.OnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
        binding.joinBtn.setOnClickListener(View.OnClickListener {
            val roomId = binding.roomidText.text.toString()
            var mSocket = SocketHandler.getSocket()
            if (mSocket == null || !mSocket.connected()) {
                SocketHandler.setSocket("http://${UtilsM.getEndPoint(this)}")
                SocketHandler.establishConnection()
                mSocket = SocketHandler.getSocket()
            }
            val id = (application as UniqueID).uniqueId
            val data = listOf(roomId, id)
            mSocket?.emit("joinRoom", data.joinToString(","))

            mSocket?.on("jrRes") { args ->
                if (args[1].toString() != "404"){
                    val b = Bundle()
                    b.putString("members", args[0].toString())
                    b.putString("roomcode", args[1].toString())
                    val intent = Intent(this, WaitingRoom::class.java)
                    intent.putExtras(b)
                    startActivity(intent)
                }
                else {
                    handler.postDelayed({
                        Toast.makeText(applicationContext, "A room with this roomcode ${roomId} does not exist",
                            Toast.LENGTH_SHORT).show()
                    }, 200L)
                }
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