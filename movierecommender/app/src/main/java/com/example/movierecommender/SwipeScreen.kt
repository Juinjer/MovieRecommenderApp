package com.example.movierecommender

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import com.example.movierecommender.databinding.SwipeScreenBinding
import com.example.movierecommender.databinding.WaitingRoomBinding
import com.squareup.picasso.Picasso
import io.socket.client.Socket
import kotlin.math.abs

class SwipeScreen : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var binding: SwipeScreenBinding
    private lateinit var roomId:String
    private lateinit var id:String
    private lateinit var mSocket: Socket

    // Declaring gesture detector, swipe threshold, and swipe velocity threshold
    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 30
    private val swipeVelocityThreshold = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: buffer more movies to avoid loading
        super.onCreate(savedInstanceState)
        binding = SwipeScreenBinding.inflate(layoutInflater)

        // Initializing the gesture detector
        gestureDetector = GestureDetector(this, this);

        setContentView(binding.root)
        mSocket = SocketHandler.getSocket()!!
        val b = intent.extras
        roomId = "404"
        if (b != null) {
            roomId = b.getString("roomcode")!!
        }

        id = (application as UniqueID).uniqueId
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
           handleLike();
        })

        binding.dislikeBtn.setOnClickListener(View.OnClickListener { ;
            handleDislike();
        })
    }

    private fun handleLike(){
        //TODO: pass some filmid
        val data = listOf(roomId, id,1)
        System.out.println("Disliked")
        mSocket.emit("rateFilm", data)
        val intent = Intent(this, SwipeScreen::class.java)
        startActivity(intent)
    }

    private fun handleDislike(){
        //TODO: pass some filmid
        val data = listOf(roomId, id,-1)
        System.out.println("Liked")
        mSocket.emit("rateFilm", data)
        val intent = Intent(this, SwipeScreen::class.java)
        startActivity(intent)
    }

    // Override this method to recognize touch event
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        }
        else {
            super.onTouchEvent(event)
        }
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            val diffY = e2.y - e1!!.y
            val diffX = e2.x - e1.x

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        Toast.makeText(applicationContext, "Left to Right swipe gesture", Toast.LENGTH_SHORT).show()
                        handleLike()
                    }
                    else {
                        Toast.makeText(applicationContext, "Right to Left swipe gesture", Toast.LENGTH_SHORT).show()
                        handleDislike()
                    }
                }
            }else{
                if (abs(diffY) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {
                    if (diffY > 0) {
                        Toast.makeText(applicationContext, "Up to Down gesture", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(applicationContext, "Down to Up swipe gesture", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }


    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        return
    }

}