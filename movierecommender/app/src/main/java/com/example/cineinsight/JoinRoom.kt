package com.example.cineinsight

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.cineinsight.databinding.ActivityJoinRoomBinding

class JoinRoom : AppCompatActivity() {
    private lateinit var binding: ActivityJoinRoomBinding
    private val errorMessages: Map<String,String> = mapOf(
        "404" to "A room with this roomcode does not exist",
        "999" to "The provided room has already started")

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
                if (args[1].toString() !in errorMessages.keys){
                    val b = Bundle()
                    b.putString("members", args[0].toString())
                    b.putString("roomcode", args[1].toString())
                    val intent = Intent(this, WaitingRoom::class.java)
                    intent.putExtras(b)
                    startActivity(intent)
                }
                else {
                    handler.postDelayed({
                        Toast.makeText(applicationContext, errorMessages[args[1].toString()],
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