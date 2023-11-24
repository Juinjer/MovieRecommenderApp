package com.example.movierecommender

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.movierecommender.UtilsM
import com.example.movierecommender.databinding.WaitingRoomBinding

class WaitingRoom : AppCompatActivity() {
    private lateinit var binding: WaitingRoomBinding

    @SuppressLint("ClickableViewAccessibility") //TODO look for a better way
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = WaitingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val b = intent.extras
        if (b != null) {
            binding.roomId.setText(b.getString("roomcode"))
        }
        binding.cancel.setOnClickListener(View.OnClickListener(){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        })

        binding.mainid.setOnTouchListener(View.OnTouchListener { view, _->
            val cont = view.context
            hideKeyboard(view)
            return@OnTouchListener true
        })
    }
    fun Context.hideKeyboard(view:View) { // TODO extract to UtilsM
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}