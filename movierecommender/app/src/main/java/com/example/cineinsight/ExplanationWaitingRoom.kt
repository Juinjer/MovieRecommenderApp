package com.example.cineinsight

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import com.example.cineinsight.databinding.ExplanationWaitingRoomBinding
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.squareup.picasso.Picasso
import io.socket.client.Socket
import org.json.JSONObject
import android.widget.Toast
import androidx.compose.ui.text.toLowerCase
import org.json.JSONArray
import org.json.JSONException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.TreeSet
import kotlin.math.abs


class ExplanationWaitingRoom : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var binding: ExplanationWaitingRoomBinding
    private lateinit var roomId: String
    private lateinit var id: String
    private lateinit var mSocket: Socket

    private lateinit var gestureDetector: GestureDetector
    private val flingThreshold = 30
    private val flingVelocityThreshold = 50

    // Buffer information for recommendations
    private val recommendationBuffer = mutableListOf<Movie>()
    private var currentRecommendationIndex = 0
    var scrollingText: TextView? = null

    var currentMovie: Movie? = null
    var infoMode = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ExplanationWaitingRoomBinding.inflate(layoutInflater)
        gestureDetector = GestureDetector(this, this)
        setContentView(binding.root)
        val scrollingText: TextView = findViewById(R.id.titleText)
        scrollingText.isSelected = true
        mSocket = SocketHandler.getSocket()!!
        updateLoadingState(true)
        var loaded = false

        val b = intent.extras
        roomId = "404"
        if (b != null) {
            roomId = b.getString("roomcode")!!
            Log.d("ExplanationWaitingRoom", "Room ID: $roomId")
        }

        id = (application as UniqueID).uniqueId

        mSocket.on("processingDone") { args ->
            Log.d("ExplanationWaitingRoom", "Processing done")
            val jsonArray = JSONArray(args[0].toString())

            for (i in 0 until jsonArray.length()) {
                val jsonObject = JSONObject(jsonArray.getString(i))
                val index = jsonObject.getString("index").toInt()
                val title = jsonObject.getString("title")
                val overview = jsonObject.getString("overview")
                val fullPosterPath = jsonObject.getString("full_poster_path")
                val explanation = jsonObject.getString("explanation")
                recommendationBuffer.add(Movie(index, title, overview, fullPosterPath, explanation))
            }
            updateLoadingState(false)
            displayRecommendation(recommendationBuffer[0])
            loaded = true
        }
        binding.swLeftBtn.setOnClickListener(View.OnClickListener {
            if (loaded)
                handlePrevious()
        })
        binding.swRightBtn.setOnClickListener(View.OnClickListener {
            if (loaded)
                handleNext()
        })
        binding.exit.setOnClickListener(View.OnClickListener {
            val data = listOf(roomId, id)
            mSocket.emit("leaveRoom", data.joinToString(","))
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
        binding.info.setOnClickListener(View.OnClickListener {
            infoMode = !infoMode;
            displayRecommendation(currentMovie!!);
        })
    }

    private fun displayRecommendation(recommendation: Movie) {

        if (infoMode) {
            binding.info.text= "Factors";
            binding.tvRecommendationExplained.text = recommendation.overview.toString();

        }
        else {
            binding.info.text= "Info";

            Log.d("DisplayRecommendation", "$currentRecommendationIndex,${recommendationBuffer.size}")
            if (currentRecommendationIndex < recommendationBuffer.size) {
                runOnUiThread {

                    currentMovie = recommendation;

                    val title = recommendation.title
                    val fullPosterPath = recommendation.fullPosterPath
                    val recommendationFactors = recommendation.explanation
                    Picasso.get().load(fullPosterPath).into(binding.ivRecommendedMovie)
                    var editable: Editable = Editable.Factory.getInstance().newEditable(title)
                    binding.titleText.text = editable
                    editable = Editable.Factory.getInstance().newEditable(recommendationFactors)
                    println("CONTENT = $editable")
                    if (editable.startsWith("{")) {
                        try {
                            val jsonObject = JSONObject(editable.toString())
                            val keysList = jsonObject.keys().asSequence().map { it.toString() }.toList()
                            val caseInsensitiveComparator = Comparator<String> { str1, str2 ->
                                str1.compareTo(
                                        str2,
                                        ignoreCase = true
                                )
                            }
                            val sortedKeys = TreeSet(caseInsensitiveComparator)
                            sortedKeys.addAll(keysList)
                            val spannableStringBuilder = SpannableStringBuilder()
                            for (key in sortedKeys) {
                                val lowerKey = key.toLowerCase()
                                val importance = jsonObject.optDouble(lowerKey, 0.00)
                                val isBold = importance >= 0.45
                                println("Importance value for key '$key': $importance")
                                val fontSize = if (importance in 0.0..0.99) {
                                    val importanceToFontSize = mapOf(
                                            0.99 to 24f,
                                            0.50 to 23f,
                                            0.45 to 22f,
                                            0.40 to 20f,
                                            0.35 to 18f,
                                            0.30 to 16f,
                                            0.25 to 14f,
                                            0.20 to 12f,
                                            0.10 to 11f
                                    )
                                    importanceToFontSize.entries.firstOrNull { it.key <= importance }?.value
                                            ?: importanceToFontSize.values.lastOrNull()
                                } else {
                                    10f
                                }
                                println("Importance value for key '$key': $fontSize")
                                val spannableString = SpannableString("$lowerKey, ")
                                if (fontSize != null) {
                                    spannableString.setSpan(
                                            AbsoluteSizeSpan(fontSize.toInt(), true),
                                            0,
                                            spannableString.length,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                                if (isBold) {
                                    spannableString.setSpan(
                                            StyleSpan(Typeface.BOLD),
                                            0,
                                            spannableString.length,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                                spannableStringBuilder.append(spannableString)
                            }
                            if (spannableStringBuilder.length >= 2) {
                                spannableStringBuilder.delete(
                                        spannableStringBuilder.length - 2,
                                        spannableStringBuilder.length
                                )
                            }
                            binding.tvRecommendationExplained.text = spannableStringBuilder
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        binding.tvRecommendationExplained.text = editable
                    }
                    val recommendationTitle = "Recommendation #" + (currentRecommendationIndex + 1)
                    binding.tvRecommendedMovie.text = recommendationTitle
                }
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
                binding.titleText.visibility = View.GONE
                binding.tvRecommendationExplained.visibility = View.GONE
                binding.swLeftBtn.visibility = View.GONE
                binding.swRightBtn.visibility = View.GONE
                binding.info.visibility = View.GONE
            } else {
                // Hide loading state
                binding.tvLoadingRecommendations.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                // Show other UI elements
                binding.ivRecommendedMovie.visibility = View.VISIBLE
                binding.tvRecommendedMovie.visibility = View.VISIBLE
                binding.tvRecommendationFactors.visibility = View.VISIBLE
                binding.titleText.visibility = View.VISIBLE
                binding.tvRecommendationExplained.visibility = View.VISIBLE
                // Update swipe button visibility
                binding.swLeftBtn.visibility = View.GONE
                binding.swRightBtn.visibility = View.VISIBLE
                binding.info.visibility = View.VISIBLE
            }
        }
    }

    private fun updateSwipeButtonVisibility() {
        binding.swLeftBtn.visibility =
            if (currentRecommendationIndex > 0) View.VISIBLE else View.INVISIBLE
        binding.swRightBtn.visibility =
            if (currentRecommendationIndex < recommendationBuffer.size - 1) View.VISIBLE else View.INVISIBLE
    }

    private fun handleNext() {
        if (currentRecommendationIndex < recommendationBuffer.size - 1) {
            // Display the next recommendation
            val nextRecommendation = recommendationBuffer[++currentRecommendationIndex]
            displayRecommendation(nextRecommendation)
        } else {
            // No more recommendations, show a toast
            Toast.makeText(this, "Last recommendation reached", Toast.LENGTH_SHORT).show()
        }
        updateSwipeButtonVisibility()
    }

    private fun handlePrevious() {
        if (currentRecommendationIndex > 0) {
            // Display the next recommendation
            val nextRecommendation = recommendationBuffer[--currentRecommendationIndex]
            displayRecommendation(nextRecommendation)
        } else {
            // No more recommendations, show a toast
            Toast.makeText(this, "First recommendation reached", Toast.LENGTH_SHORT).show()
        }
        updateSwipeButtonVisibility()
    }

    // Override this method to recognize touch event
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    /**
     * This method recognizes user flings.
     * It uses the flingThresholds to see whether it was a valid movement.
     * It uses begin and end position to differentiate directions
     */
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        try {
            val diffY = e2.y - e1!!.y
            val diffX = e2.x - e1.x

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > flingThreshold && abs(velocityX) > flingVelocityThreshold) {
                    if (diffX > 0) {
                        // Toast.makeText(applicationContext, "Left to Right swipe gesture", Toast.LENGTH_SHORT).show()
                        handlePrevious()
                    } else {
                        // Toast.makeText(applicationContext, "Right to Left swipe gesture", Toast.LENGTH_SHORT).show()
                        handleNext()
                    }
                }
            } else {
                if (abs(diffY) > flingThreshold && abs(velocityY) > flingVelocityThreshold) {
                    if (diffY > 0) {
                        // Toast.makeText(applicationContext, "Up to Down gesture", Toast.LENGTH_SHORT).show()
                    } else {
                        // Toast.makeText(applicationContext, "Down to Up swipe gesture", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (exception: Exception) {
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

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        return
    }
}