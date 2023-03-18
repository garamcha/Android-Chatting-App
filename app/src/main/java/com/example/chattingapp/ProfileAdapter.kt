package com.example.chattingapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private val item: ArrayList<Profile>) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>(){

    class ViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val userImg = itemView.findViewById<ImageView>(R.id.profile_image)
        val userName = itemView.findViewById<TextView>(R.id.friend_name)
    }

    // item과 연결
   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAdapter.ViewHolder {
        Log.d("TAG", "ProfileAdapter - onCreateViewHolder() called.")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_recy, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ProfileAdapter.ViewHolder, position: Int) {
        Log.d("TAG", "ProfileAdapter - onBindViewHolder() called.")
        val data = item[position]
        holder.userImg.setImageResource(data.img)
        holder.userName.text = data.name

    }

    override fun getItemCount(): Int {
        Log.d("TAG", "ProfileAdapter - getItemCount() called.")
        return item.size
    }


}