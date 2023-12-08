package com.example.movierecommender

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
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

    private var swipesThreshold = 0;
    //private var numberOfSwipesDone = 0;

    // Buffer information for movies
    private val movieBuffer = mutableListOf<Movie>()
    private var currentMovieIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: buffer more movies to avoid loading
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

        // Receive the swipeThreshold from the backend
        mSocket.emit("getSettings", roomId)
        mSocket.on("getSettingsResp"){ args ->
            // Receive the number of swipes threshold for the room
            this.swipesThreshold = Integer.parseInt(args[0].toString());
            // Fetch an initial batch of movies
            fetchMovies(swipesThreshold);
        }

        id = (application as UniqueID).uniqueId

        // Binding event listeners to the rating buttons
        binding.likeBtn.setOnClickListener(View.OnClickListener {
           handleLike();
        })
        binding.dislikeBtn.setOnClickListener(View.OnClickListener {
            handleDislike();
        })
    }

    private fun fetchMovies(amountMovies: Int){
        mSocket.emit("getMovies", amountMovies)
        mSocket.on("getMoviesResp") { args ->
            val jsonArray = JSONArray(args[0].toString())
            for (i in 0 until jsonArray.length()) {
                val jsonObject = JSONObject(jsonArray.getString(i))
                Log.d("movieResp args", jsonObject.toString())
                val img = jsonObject.getString("img")
                val title = jsonObject.getString("title")
                val desc = jsonObject.getString("desc")
                movieBuffer.add(Movie(img, title, desc))
            }
            displayNextMovie()
        }
    }

    private fun displayNextMovie() {
        Log.d("DisplayNextMovie", "$currentMovieIndex,${movieBuffer.size},$swipesThreshold")
        if (currentMovieIndex < movieBuffer.size) {
            runOnUiThread {
                val movie = movieBuffer[currentMovieIndex++]
                val img = movie.img
                val title = movie.title
                val desc = movie.desc
                Log.d("img", img)
                Log.d("title", title)
                Log.d("desc", desc)
                Picasso.get().load(img).into(binding.imageView2)
                var editable: Editable = Editable.Factory.getInstance().newEditable(title)
                binding.titelText.text = editable
                editable = Editable.Factory.getInstance().newEditable(desc)
                binding.filmDescription.text = editable
            }
        } else {
            if(currentMovieIndex == swipesThreshold) {
                mSocket.emit("getSimilar", id)
                mSocket.on("getSimilarResp") { args ->
                    val jsonMovies = JSONObject(args[0].toString())
                    Log.d("movie_title",jsonMovies.getString("movie_title"))
                    Log.d("recommendations", jsonMovies.getString("recommendations"))
                    showRecommendation(jsonMovies)
                }
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    // This delay serves no function and is here only to keep the app from crashing untill everybody has read the recommendation
                }, 30000) // 30 seconds in milliseconds
                //showExplanationScreen()
            } else {
                fetchMovies(swipesThreshold-currentMovieIndex)
            }
        }
    }

    private fun showRecommendation(jsonMovies: JSONObject) {
        runOnUiThread {
            val movie = movieBuffer[currentMovieIndex++]
            val img = movie.img
            val title = movie.title
            val desc = movie.desc
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

    private fun handleLike(){
        //TODO: pass some filmid
        val data = "$roomId,$id,${movieBuffer[currentMovieIndex-1].title},${1}"
        mSocket.emit("rateFilm", data)
        Log.d("Rating performed", "liked")
        //numberOfSwipesDone++;
        displayNextMovie()
    }

    private fun handleSkip(){
    }

    private fun handleDislike(){
        //TODO: pass some filmid
        val data = "$roomId,$id,${movieBuffer[currentMovieIndex-1].title},${-1}"
        mSocket.emit("rateFilm", data)
        Log.d("Rating performed", "disliked")
        //numberOfSwipesDone++;
        displayNextMovie()
    }


    private fun showExplanationScreen(){
        //if (numberOfSwipesDone == swipesThreshold) {
        val intent = Intent(this, ExplanationWaitingRoom::class.java)
        startActivity(intent)
        //}
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
                        Toast.makeText(applicationContext, "Left to Right swipe gesture", Toast.LENGTH_SHORT).show()
                        handleLike()
                    }
                    else {
                        Toast.makeText(applicationContext, "Right to Left swipe gesture", Toast.LENGTH_SHORT).show()
                        handleDislike()
                    }
                }
            }else{
                if (abs(diffY) > flingThreshold && abs(velocityY) > flingVelocityThreshold) {
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