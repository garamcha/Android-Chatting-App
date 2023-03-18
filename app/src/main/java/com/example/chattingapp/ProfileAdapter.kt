package com.example.chattingapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private var items: ArrayList<ProfileData>) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(v: View, data: ProfileData, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImg = itemView.findViewById<ImageView>(R.id.profile_image)
        val userName = itemView.findViewById<TextView>(R.id.friend_name)

        fun bind(item: ProfileData) {
            userName.text = item.name
            userImg.setImageResource(item.img)

            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                itemView.setOnClickListener {
                    listener?.onClick(itemView, item, pos)
                }
            }
        }
    }

    var datas = mutableListOf<ProfileData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        datas = items.toMutableList() // items로부터 datas 초기화
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_recy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
