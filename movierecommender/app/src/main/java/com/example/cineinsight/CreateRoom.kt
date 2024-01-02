package com.example.cineinsight

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.View
import com.example.cineinsight.databinding.ActivityCreateRoomBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class CreateRoom : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRoomBinding
//    private lateinit var qrgEncoder: QRGEncoder
//    private lateinit var bitmap: Bitmap
    private lateinit var recyclerView: RecyclerView
    private lateinit var nameList: ArrayList<Name>
    private lateinit var nameAdapter: NameAdapter

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val handler = Handler(Looper.getMainLooper())

        val mSocket = SocketHandler.getSocket()!!

        val b = intent.extras
        Log.d("rid", b.toString())
        if (b != null) {
            val s = b.getString("rId")
            val editable: Editable = Editable.Factory.getInstance().newEditable(s)
            binding.roomidnumber.text = editable
        }
        mSocket.off("disbandgroup") // to avoid weird behaviour

        val roomId = binding.roomidnumber.text.toString()
        val id = (application as UniqueID).uniqueId
        val data = listOf(roomId, id)
        b?.putString("roomcode", roomId)

        binding.cancel.setOnClickListener(View.OnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            mSocket.emit("leaveRoom", data.joinToString(","))
            startActivity(intent)
        })
        binding.start.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SwipeScreen::class.java)
            intent.putExtra("roomcode", roomId)
            startActivity(intent)
            mSocket.emit("startLobby", data.joinToString(","))
        })
        binding.settings.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SettingsScreen::class.java)
            intent.putExtra("roomcode", roomId)
            startActivity(intent)
        })
        binding.copyBtn.setOnClickListener(View.OnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val id = binding.roomidnumber.text.toString()
            val clip = ClipData.newPlainText("test", id)
            clipboard.setPrimaryClip(clip)
        })

        //TODO: should make some class to handle this member logic

        val imageResources = arrayOf(
            R.drawable.alligator,
            R.drawable.bat,
            R.drawable.cormorant,
            R.drawable.coyote,
            R.drawable.dinosaur,
            R.drawable.elephant,
            R.drawable.giraffe,
            R.drawable.kangaroo,
            R.drawable.llama,
            R.drawable.penguin,
            R.drawable.raccoon
        )
        mSocket.off("disbandgroup") // to avoid weird behaviour

        mSocket.on("joinNotif") { args ->
            nameList.add(Name(imageResources[Random.nextInt(imageResources.size)], args[0].toString()))
            handler.post { updateList() }
        }

        mSocket.on("leaveNotif") { args ->
            for (i in 0..<nameList.size) {
                if (nameList[i].name == args[0].toString()){
                    nameList.removeAt(i)
                }
            }
            handler.post { updateList() }
        }
        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        nameList = ArrayList()

        if (b != null) {
            Log.d("test", b.getString("members")!!)
            val s = b.getString("members")!!
            val splitted = s.split(",")
            imageResources.shuffle()
            for ((index, st) in splitted.withIndex()) {
                nameList.add(Name(imageResources[index], st))
            }
            updateList()
        }
    }

    fun updateList() {
        this.nameAdapter = NameAdapter(this.nameList)
        recyclerView.adapter = this.nameAdapter
    }

}