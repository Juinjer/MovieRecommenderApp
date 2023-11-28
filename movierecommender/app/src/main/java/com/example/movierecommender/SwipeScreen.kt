package com.example.movierecommender

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import com.example.movierecommender.databinding.SwipeScreenBinding
import com.example.movierecommender.databinding.WaitingRoomBinding
import com.squareup.picasso.Picasso

class SwipeScreen : AppCompatActivity() {
    private lateinit var binding: SwipeScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: buffer more movies to avoid loading
        super.onCreate(savedInstanceState)
        binding = SwipeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mSocket = SocketHandler.getSocket()!!
        val b = intent.extras
        var roomId = "404"
        if (b != null) {
            roomId = b.getString("roomcode")!!
        }
        val id = (application as UniqueID).uniqueId
        mSocket.emit("getMovie")
        mSocket.on("getMovieResp"){args ->
            runOnUiThread {
                val img = args[0].toString()
                val title = args[1].toString()
                val desc = args[2].toString()
                Log.d("img", img)
                Log.d("title", title)
                Log.d("desc", desc)
                Picasso.get().load(img).into(binding.imageView2)
                var editable: Editable = Editable.Factory.getInstance().newEditable(title)
                binding.titelText.text = editable
                editable = Editable.Factory.getInstance().newEditable(desc)
                binding.filmDescription.text = editable
            }
        }
        binding.likeBtn.setOnClickListener(View.OnClickListener {
            //TODO: pass some filmid
            val data = listOf(roomId, id,1)

            mSocket.emit("rateFilm", data)
            val intent = Intent(this, SwipeScreen::class.java)
            startActivity(intent)
        })

        binding.dislikeBtn.setOnClickListener(View.OnClickListener {
            //TODO: pass some filmid
            val data = listOf(roomId, id,-1)

            mSocket.emit("rateFilm", data)
            val intent = Intent(this, SwipeScreen::class.java)
            startActivity(intent)
        })
    }
}