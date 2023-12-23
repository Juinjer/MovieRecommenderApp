package com.example.movierecommender

import androidx.appcompat.app.AppCompatActivity
import com.example.movierecommender.databinding.ExplanationWaitingRoomBinding
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.TextView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.example.movierecommender.databinding.SwipeScreenBinding
import com.squareup.picasso.Picasso
import io.socket.client.Socket
import org.json.JSONObject
import android.widget.Toast
import org.json.JSONArray
import kotlin.math.abs


class ExplanationWaitingRoom : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var binding: ExplanationWaitingRoomBinding
    private lateinit var roomId:String
    private lateinit var id:String
    private lateinit var mSocket: Socket

    private lateinit var gestureDetector: GestureDetector
    private val flingThreshold = 30
    private val flingVelocityThreshold = 50

    // Buffer information for recommendations
    private val recommendationBuffer = mutableListOf<Movie>()
    private var currentRecommendationIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ExplanationWaitingRoomBinding.inflate(layoutInflater)
        gestureDetector = GestureDetector(this, this);
        setContentView(binding.root)
        mSocket= SocketHandler.getSocket()!!
        updateLoadingState(true);

        val b = intent.extras
        roomId = "404"
        if (b != null) {
            roomId = b.getString("roomcode")!!
            Log.d("ExplanationWaitingRoom", "Room ID: $roomId");
        }

        id = (application as UniqueID).uniqueId

        mSocket.on("processingDone") { args ->
            Log.d("ExplanationWaitingRoom", "Processing done");
            val jsonArray = JSONArray(args[0].toString())

            for (i in 0 until jsonArray.length()) {
                val jsonObject = JSONObject(jsonArray.getString(i))
                val index = jsonObject.getString("index").toInt()
                val title = jsonObject.getString("title")
                val overview = jsonObject.getString("overview")
                val fullPosterPath = jsonObject.getString("full_poster_path")
                val explanation = jsonObject.getString("explanation")
                recommendationBuffer.add( Movie(index,title, overview, fullPosterPath, explanation))
            }
            updateLoadingState(false)
            displayRecommendation(recommendationBuffer[0]);
        }

    }

    private fun displayRecommendation(recommendation: Movie) {
        Log.d("DisplayRecommendation", "$currentRecommendationIndex,${recommendationBuffer.size}")
        if (currentRecommendationIndex < recommendationBuffer.size) {
            runOnUiThread {
                val title = recommendation.title
                val fullPosterPath = recommendation.fullPosterPath
                val recommendationFactors = recommendation.explanation

                // Update UI components
                Picasso.get().load(fullPosterPath).into(binding.ivRecommendedMovie)
                var editable: Editable = Editable.Factory.getInstance().newEditable(title)
                binding.tvRecommendationFactors.text = editable
                editable = Editable.Factory.getInstance().newEditable(recommendationFactors)
                binding.tvRecommendationExplained.text = editable
            }
        }
    }

    private fun updateLoadingState(loadingRecommendations: Boolean) {
        runOnUiThread {
            if (loadingRecommendations) {
                // Show loading state
                binding.tvLoadingRecommendations.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                // Hide other UI elements
                binding.ivRecommendedMovie.visibility = View.GONE
                binding.tvRecommendedMovie.visibility = View.GONE
                binding.tvRecommendationFactors.visibility = View.GONE
                binding.tvRecommendationExplained.visibility = View.GONE
            } else {
                // Hide loading state
                binding.tvLoadingRecommendations.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                // Show other UI elements
                binding.ivRecommendedMovie.visibility = View.VISIBLE
                binding.tvRecommendedMovie.visibility = View.VISIBLE
                binding.tvRecommendationFactors.visibility = View.VISIBLE
                binding.tvRecommendationExplained.visibility = View.VISIBLE
            }
        }
    }

    private fun handleNext(){
        if( currentRecommendationIndex<recommendationBuffer.size-1) {
            // Display the next recommendation
            val nextRecommendation = recommendationBuffer[++currentRecommendationIndex]
            displayRecommendation(nextRecommendation)
        } else {
            // No more recommendations, show a toast
            Toast.makeText(this, "Last recommendation reached", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePrevious(){
        if( currentRecommendationIndex>0) {
            // Display the next recommendation
            val nextRecommendation = recommendationBuffer[--currentRecommendationIndex]
            displayRecommendation(nextRecommendation)
        } else {
            // No more recommendations, show a toast
            Toast.makeText(this, "First recommendation reached", Toast.LENGTH_SHORT).show()
        }
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
                        handleNext()
                    }
                    else {
                        // Toast.makeText(applicationContext, "Right to Left swipe gesture", Toast.LENGTH_SHORT).show()
                        handlePrevious()
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