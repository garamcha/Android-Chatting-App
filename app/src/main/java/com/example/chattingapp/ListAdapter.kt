package com.example.chattingapp

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ListAdapter (var chatList:ArrayList<ListData>)
    : RecyclerView.Adapter<ListAdapter.ViewHolder>()  {

    class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        val other_person_name = v.findViewById<TextView>(R.id.list_name)
        val last_chat = v.findViewById<TextView>(R.id.list_message)
        val other_person_img = v.findViewById<ImageView>(R.id.profile_image)

        fun bind(list : ListData){
            other_person_name.text = list.name
            last_chat.text = list.last_chat

            var firestore = FirebaseFirestore.getInstance()
            // 파이어 스토어에서 마지막 대화목록 가져오기
            firestore.collection("message")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}