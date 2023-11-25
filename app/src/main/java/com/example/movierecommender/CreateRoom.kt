package com.example.movierecommender

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
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
    private lateinit var qrgEncoder: QRGEncoder
    private lateinit var bitmap: Bitmap

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val b = intent.extras
        Log.d("rid", b.toString())
        if (b != null) {
            val s = b.getString("roomcode")
            val editable: Editable = Editable.Factory.getInstance().newEditable(s)
            binding.roomidnumber.text = editable
        }

        binding.cancel.setOnClickListener(View.OnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
        binding.start.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SwipeScreen::class.java)
            startActivity(intent)
        })
        binding.settings.setOnClickListener(View.OnClickListener {
            val intent = Intent(this,SettingsScreen::class.java)
            startActivity(intent)
        })

        binding.copyBtn.setOnClickListener(View.OnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val id = binding.roomidnumber.text.toString()
            val clip = ClipData.newPlainText("test", id)
            clipboard.setPrimaryClip(clip)
        })

        displayQR()
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun displayQR() {
        val dim = min(binding.idIVQrcode.layoutParams.width, binding.idIVQrcode.layoutParams.height)
        val uri = "http://example.com/${binding.roomidnumber}"
        qrgEncoder = QRGEncoder(uri, null, QRGContents.Type.TEXT, dim)
        //TODO: image always has padding, dunno why
        // --> Zwarte rand door generator, aanpassing padding geen verschil
        // binding.idIVQrcode.setPadding(0, 0, 0, 0)
        try {
            bitmap = qrgEncoder.bitmap
            binding.idIVQrcode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}