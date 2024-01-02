package com.example.cineinsight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NameAdapter(private val nameList: ArrayList<Name>) :
    RecyclerView.Adapter<NameAdapter.NameViewHolder>() {

    class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem, parent, false)
        return NameViewHolder(view)
    }

    override fun getItemCount(): Int {
        return nameList.size
    }

    override fun onBindViewHolder(holder: NameViewHolder, position: Int) {
        val name = nameList[position]
        holder.imageView.setImageResource(name.image)
        holder.textView.text = name.name
    }
}