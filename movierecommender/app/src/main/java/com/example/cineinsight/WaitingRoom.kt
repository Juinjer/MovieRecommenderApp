package com.example.cineinsight

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cineinsight.databinding.WaitingRoomBinding
import kotlin.random.Random

class WaitingRoom : AppCompatActivity() {
    private lateinit var binding: WaitingRoomBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var nameList: ArrayList<Name>
    private lateinit var nameAdapter: NameAdapter

    @SuppressLint("ClickableViewAccessibility") //TODO look for a better way
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mSocket = SocketHandler.getSocket()

        val handler = Handler(Looper.getMainLooper())

        binding = WaitingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val b = intent.extras
        if (b != null) {
            val s = b.getString("roomcode")
            val editable: Editable = Editable.Factory.getInstance().newEditable(s)
            binding.roomId.text = editable
        }
        mSocket?.on("hostStart") { _ ->
            val b = Bundle()
            b.putString("roomcode", binding.roomId.text.toString())
            val intent = Intent(this, SwipeScreen::class.java)
            intent.putExtras(b)
            startActivity(intent)
        }
        mSocket?.on("disbandgroup") { _ ->
            handler.post {
                Toast.makeText(
                    applicationContext, "Group host disbanded the group",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val intent = Intent(this, JoinRoom::class.java)
            mSocket.disconnect()
            startActivity(intent)
        }
        binding.cancel.setOnClickListener(View.OnClickListener() {
            val id = (application as UniqueID).uniqueId
            val roomID = binding.roomId.text.toString()
            val data = listOf(roomID, id)
            mSocket?.emit("leaveRoom", data.joinToString(","))
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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
        mSocket?.on("joinNotif") { args ->
            nameList.add(Name(imageResources[Random.nextInt(imageResources.size-1)], args[0].toString()))
            handler.post { updateList() }
        }

        mSocket?.on("leaveNotif") { args ->
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