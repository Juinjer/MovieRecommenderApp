package com.example.movierecommender

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.example.movierecommender.databinding.ActivityCreateRoomBinding
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import java.lang.Exception
import java.lang.Integer.min

class CreateRoom : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRoomBinding
    lateinit var qrgEncoder:QRGEncoder
    lateinit var bitmap: Bitmap

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancel.setOnClickListener(View.OnClickListener() {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        })
        displayQR()
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun displayQR(){
        val manager = getSystemService(WINDOW_SERVICE) as WindowManager
//        @Suppress("DEPRECATION")
        val dim = min(binding.idIVQrcode.layoutParams.height,binding.idIVQrcode.layoutParams.width)
        val uri = "http://example.com/${binding.roomidnumber}"
        qrgEncoder = QRGEncoder(uri, null, QRGContents.Type.TEXT,dim) //TODO: image always has padding, dunno why
        try {
            bitmap = qrgEncoder.bitmap
            binding.idIVQrcode.setImageBitmap(bitmap)
        } catch (e:Exception){
            e.printStackTrace()
        }
    }
}