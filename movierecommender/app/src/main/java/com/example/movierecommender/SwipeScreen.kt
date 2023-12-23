package com.example.movierecommender

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.movierecommender.databinding.SwipeScreenBinding
import com.squareup.picasso.Picasso
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

class SwipeScreen : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var binding: SwipeScreenBinding
    private lateinit var roomId:String
    private lateinit var id:String
    private lateinit var mSocket: Socket

    // Declaring gesture detector, swipe threshold, and swipe velocity threshold
    // Values can be adjusted for swipe sensitivity
    private lateinit var gestureDetector: GestureDetector
    private val flingThreshold = 30
    private val flingVelocityThreshold = 50

    // Buffer information for movies
    private val movieBuffer = mutableListOf<Movie>()
    private var currentMovieIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SwipeScreenBinding.inflate(layoutInflater)
        gestureDetector = GestureDetector(this, this);
        setContentView(binding.root)
        mSocket = SocketHandler.getSocket()!!

        val b = intent.extras
        roomId = "404"
        if (b != null) {
            roomId = b.getString("roomcode")!!
        }

        fetchMovies();

        id = (application as UniqueID).uniqueId

        // Binding event listeners to the rating buttons
        binding.likeBtn.setOnClickListener(View.OnClickListener {
           handleLike();
        })
        binding.dislikeBtn.setOnClickListener(View.OnClickListener {
            handleDislike();
        })
    }

    private fun fetchMovies(){
        mSocket.emit("getSuggestions", roomId)
        mSocket.on("getSuggestionsResp") { args ->
            val jsonArray = JSONArray(args[0].toString())
            Log.d("Room suggestions", jsonArray.toString());

            for (i in 0 until jsonArray.length()) {
                val jsonObject = JSONObject(jsonArray.getString(i))
                val index = jsonObject.getString("index").toInt()
                val fullPosterPath = jsonObject.getString("full_poster_path")
                val title = jsonObject.getString("title")
                val overview = jsonObject.getString("overview")
                movieBuffer.add( Movie(index,title, overview, fullPosterPath))
            }
            displayNextMovie()
        }
    }

    private fun displayNextMovie() {
        Log.d("DisplayNextMovie", "$currentMovieIndex,${movieBuffer.size}")
        if (currentMovieIndex < movieBuffer.size) {
            runOnUiThread {
                val movie = movieBuffer[currentMovieIndex++]
                val fullPosterPath = movie.fullPosterPath
                val title = movie.title
                val overview = movie.overview
                /*
                Log.d("index", movie.index)
                Log.d("fullPosterPath", posterPath)
                Log.d("title", title)
                Log.d("overview", overview)
                */

                Picasso.get().load(fullPosterPath).into(binding.imageView2)
                var editable: Editable = Editable.Factory.getInstance().newEditable(title)
                binding.titelText.text = editable
                editable = Editable.Factory.getInstance().newEditable(overview)
                binding.filmDescription.text = editable
            }
        } else {
                showExplanationScreen()
        }
    }

    private fun handleLike(){
        val likedMovie = movieBuffer[currentMovieIndex - 1]
        val jsonString = convertMovieToJsonString(likedMovie)

        val data = "$roomId;;;$id;;;$jsonString;;;${1}"
        mSocket.emit("rateFilm", data)

        Log.d("Rating performed", "liked")
        displayNextMovie()
    }

    // FUTURE: Implement a skip on swiping upward
    private fun handleSkip(){
    }

    private fun handleDislike(){
        val dislikedMovie = movieBuffer[currentMovieIndex - 1]
        val jsonString = convertMovieToJsonString(dislikedMovie)

        val data = "$roomId;;;$id;;;$jsonString;;;${-1}"
        mSocket.emit("rateFilm", data)

        Log.d("Rating performed", "disliked")
        displayNextMovie()
    }


    private fun showExplanationScreen(){
        val intent = Intent(this, ExplanationWaitingRoom::class.java)
        startActivity(intent)
    }

    // Function to convert Movie object to JSON string
    private fun convertMovieToJsonString(movie: Movie): String {
        val json = JSONObject().apply {
            put("index", movie.index)
            put("title", movie.title)
            put("overview", movie.overview)
            put("full_poster_path", movie.fullPosterPath)
        }

        return json.toString()
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

    /**
     * This method recognizes user flings.
     * It uses the flingThresholds to see whether it was a valid movement.
     * It uses begin and end position to differentiate directions
     */
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            val diffY = e2.y - e1!!.y
            val diffX = e2.x - e1.x

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > flingThreshold && abs(velocityX) > flingVelocityThreshold) {
                    if (diffX > 0) {
                        // Toast.makeText(applicationContext, "Left to Right swipe gesture", Toast.LENGTH_SHORT).show()
                        handleLike()
                    }
                    else {
                        // Toast.makeText(applicationContext, "Right to Left swipe gesture", Toast.LENGTH_SHORT).show()
                        handleDislike()
                    }
                }
            }else{
                if (abs(diffY) > flingThreshold && abs(velocityY) > flingVelocityThreshold) {
                    if (diffY > 0) {
                        // Toast.makeText(applicationContext, "Up to Down gesture", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        // Toast.makeText(applicationContext, "Down to Up swipe gesture", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }

    //All next methods need to be overwritten when changing the touch gestures I think, but are not used
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