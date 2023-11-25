package com.example.movierecommender

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.movierecommender.databinding.ActivityMainBinding
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = generateID()
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData.getString("webServerUrl")
        val key = value.toString()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.create.setOnClickListener(View.OnClickListener() {
            val queue = Volley.newRequestQueue(this)
//            var code = ""

            val url = "$key/api/createRoom?id=$id"
            val stringReq = StringRequest(Request.Method.GET, url,
                {response -> val b = Bundle()
                    b.putString("roomcode",response)
                    val intent = Intent(this,CreateRoom::class.java)
                    intent.putExtras(b)
                    startActivity(intent) },
                {er->Log.e("buh",er.toString())})
            queue.add(stringReq)
        })
        binding.join.setOnClickListener(View.OnClickListener {
            val intent = Intent(this,JoinRoom::class.java)
            startActivity(intent)
        })
    }

    fun generateID(): String {
        val preferences = getPreferences(Context.MODE_PRIVATE)
        val editor = preferences.edit()
        if (preferences.getString("unique_id", "")==null) {
            editor.putString("unique_id", UUID.randomUUID().toString())
            editor.apply()
        }
        return preferences.getString("unique_id", "")!!
    }
}