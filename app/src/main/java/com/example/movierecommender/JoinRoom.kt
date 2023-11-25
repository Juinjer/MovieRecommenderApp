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
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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
            //TODO send join request to some endpoint, for now go to empty lobby
            val roomId = binding.roomidText.text.toString()
            val queue = Volley.newRequestQueue(this)
            val endpoint = UtilsM.getEndPoint(this)
            val uid= application as UniqueID
            val id = uid.uniqueId
            val url = "$endpoint/api/joinRoom?id=$id&roomid=$roomId"
            val stringReq = StringRequest(
                Request.Method.GET, url,
                {response ->
                    val b = Bundle()
                    b.putString("members", response)
                    b.putString("roomcode", roomId)
                    val intent = Intent(this, WaitingRoom::class.java)
                    intent.putExtras(b)
                    startActivity(intent)},
                {er-> Log.e("buh",er.toString())})
            queue.add(stringReq)
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