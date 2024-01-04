package com.example.movierecommender

import android.content.Intent
import android.graphics.Color
import android.graphics.Shader
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import com.example.movierecommender.databinding.ExplanationWaitingRoomBinding
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.squareup.picasso.Picasso
import io.socket.client.Socket
import org.json.JSONObject
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
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
        mSocket.off("disbandgroup") // to avoid weird behaviour
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

    fun insertSpaceBetweenCapitalAndNonCapital(input: String): String {
        val result = StringBuilder()

        for (i in input.indices) {
            if (i > 0 && input[i - 1].isLowerCase() && input[i].isUpperCase()) {
                result.append(' ') // Insert space between capital and non-capital letters
            }
            result.append(input[i])
        }

        return result.toString()
    }

    private fun showSliders() {
        binding.tvRecommendationFactors.visibility = View.VISIBLE
        binding.tvRecommendationImportance.visibility = View.VISIBLE
        binding.expl.visibility = View.VISIBLE
        binding.explText.visibility = View.VISIBLE
        binding.tvExplText.visibility = View.GONE
    }

    private fun hideSliders(infoText : String) {
        binding.tvRecommendationFactors.visibility = View.GONE
        binding.tvRecommendationImportance.visibility = View.GONE
        binding.expl.visibility = View.GONE
        binding.explText.visibility = View.GONE
        binding.tvExplText.visibility = View.VISIBLE
        binding.tvExplText.text = infoText

        if (infoMode) {
            binding.info.text= "Factors";
        }
        else {
            binding.info.text= "Info";
        }
    }

    private fun displayRecommendation(recommendation: Movie) {
        Log.d("DisplayRecommendation", "$currentRecommendationIndex,${recommendationBuffer.size}")
        if (currentRecommendationIndex < recommendationBuffer.size) {
            runOnUiThread {

                currentMovie = recommendation

                val title = recommendation.title
                val fullPosterPath = recommendation.fullPosterPath
                val recommendationFactors = recommendation.explanation

                // Update UI components
                Picasso.get().load(fullPosterPath).into(binding.ivRecommendedMovie)
                var editable: Editable = Editable.Factory.getInstance().newEditable(title)
                binding.titleText.text = editable
                editable = Editable.Factory.getInstance().newEditable(recommendationFactors)
                println("CONTENT = $editable")


                if (editable.startsWith("{")) {

                    try {
                        val jsonObject = JSONObject(editable.toString())
                        val keysList = jsonObject.keys().asSequence().map { it.toString() }.toList()

                        var factors = keysList.map { k ->
                            k to Pair(jsonObject.optDouble(k, 0.0), abs(jsonObject.optDouble(k, 0.0)))
                        }

                        // normalize
                        val maxFactor = factors.maxBy { it.second.second }?.second?.second ?: 1.0
                        factors = keysList.map { k ->
                            k to Pair(jsonObject.optDouble(k, 0.0), abs(jsonObject.optDouble(k, 0.0) / maxFactor) * 100)
                        }

                        Log.d("FACTORS", factors.toString());

                        val linearLayoutSlider = findViewById<LinearLayout>(binding.expl.id)
                        val linearLayoutText = findViewById<LinearLayout>(binding.explText.id);

                        linearLayoutSlider.removeAllViews();
                        linearLayoutText.removeAllViews();

                        for ((k, v) in factors) {

                            val textView = TextView(this)
                            textView.text = insertSpaceBetweenCapitalAndNonCapital(k.toString());

                            val seekBar = SeekBar(this)
                            seekBar.max = 100 // Assuming a range from 0 to 1, you can adjust as needed

                            seekBar.progress = v.second.toInt()
                            seekBar.isEnabled = false

                            linearLayoutText.addView(textView)
                            linearLayoutSlider.addView(seekBar)

                            if (infoMode) {
                                hideSliders(currentMovie!!.overview);
                            }
                            else {
                                showSliders();
                            }
                        }
                    }
                    catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                else {
                    if (infoMode) {
                        hideSliders(currentMovie!!.overview);
                    }
                    else {
                        hideSliders(editable.toString());
                    }
                }
/*

                if (editable.startsWith("{")) {
                    try {
                        val jsonObject = JSONObject(editable.toString())
                        val keysList = jsonObject.keys().asSequence().map { it.toString() }.toList()
                        val importanceToFontSize = mapOf(
                            -0.04 to 26f,
                            -0.03 to 24f,
                            -0.02 to 22f,
                            -0.01 to 20f,
                            0.0 to 18f,
                            0.01 to 16f,
                            0.02 to 14f
                        )

                        fun capitalizeFirstLetter(str: String): String {
                            return if (str.isNotEmpty()) {
                                str.substring(0, 1).toUpperCase() + str.substring(1)
                            } else {
                                str
                            }
                        }

                        val capitalizedKeysList = keysList.map { capitalizeFirstLetter(it) }
                        val caseInsensitiveComparator = Comparator<String> { str1, str2 ->
                            str1.compareTo(
                                str2,
                                ignoreCase = true
                            )
                        }
                        val sortedKeys = TreeSet(caseInsensitiveComparator)
                        sortedKeys.addAll(capitalizedKeysList)
                        val spannableStringBuilder = SpannableStringBuilder()
                        for (key in sortedKeys) {
                            val importance = jsonObject.optDouble(key, 0.0)
                            val fontSize = importanceToFontSize[importance] ?: 20f
                            val isBold = importance <= -0.0125
                            val spannableString = SpannableString("$key, ")
                            spannableString.setSpan(
                                AbsoluteSizeSpan(fontSize.toInt(), true),
                                0,
                                spannableString.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
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
                        spannableStringBuilder.delete(
                            spannableStringBuilder.length - 2,
                            spannableStringBuilder.length
                        )
                        binding.tvRecommendationExplained.text = spannableStringBuilder
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    binding.tvRecommendationExplained.text = editable
                }
*/
//                if (editable.startsWith("{")) {
//                    try {
//                        val jsonObject = JSONObject(editable.toString())
//                        val keysList = jsonObject.keys().asSequence().map { it.toString() }.toList()
//                        fun capitalizeFirstLetter(str: String): String {
//                            return if (str.isNotEmpty()) {
//                                str.substring(0, 1).toUpperCase() + str.substring(1)
//                            } else {
//                                str
//                            }
//                        }
//
//                        val capitalizedKeysList = keysList.map { capitalizeFirstLetter(it) }
//                        val caseInsensitiveComparator = Comparator<String> { str1, str2 ->
//                            str1.compareTo(
//                                str2,
//                                ignoreCase = true
//                            )
//                        }
//                        val sortedKeys = TreeSet(caseInsensitiveComparator)
//                        sortedKeys.addAll(capitalizedKeysList)
//                        val keysString = sortedKeys.joinToString(", ")
//                        binding.tvRecommendationExplained.text = keysString
//                        println(keysString)
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                    }
//                } else {
//                    binding.tvRecommendationExplained.text = editable
//                }
                val recommendationTitle = "Recommendation #" + (currentRecommendationIndex + 1)
                binding.tvRecommendedMovie.text = recommendationTitle
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
                binding.tvRecommendationImportance.visibility = View.GONE
                binding.tvExplText.visibility = View.GONE

                binding.titleText.visibility = View.GONE
                ///binding.tvRecommendationExplained.visibility = View.GONE
                binding.swLeftBtn.visibility = View.GONE
                binding.swRightBtn.visibility = View.GONE
            } else {
                // Hide loading state
                binding.tvLoadingRecommendations.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                // Show other UI elements
                binding.ivRecommendedMovie.visibility = View.VISIBLE
                binding.tvRecommendedMovie.visibility = View.VISIBLE
                binding.tvRecommendationFactors.visibility = View.VISIBLE
                binding.tvRecommendationImportance.visibility = View.VISIBLE
                binding.titleText.visibility = View.VISIBLE
                binding.tvExplText.visibility = View.VISIBLE

                /// binding.tvRecommendationExplained.visibility = View.VISIBLE
                // Update swipe button visibility
                binding.swLeftBtn.visibility = View.GONE
                binding.swRightBtn.visibility = View.VISIBLE
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