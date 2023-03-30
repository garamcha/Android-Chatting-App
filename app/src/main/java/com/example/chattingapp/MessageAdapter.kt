package com.example.chattingapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


class MessageAdapter(var context: Context,
                     var messageList:ArrayList<MessageData>)
    : RecyclerView.Adapter<MessageAdapter.ViewHolder>(){


    inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        val message_tv = v.findViewById<TextView>(R.id.message_tv)
        val u_name_tv = v.findViewById<TextView>(R.id.chat_name_tv)
        val time_tv = v.findViewById<TextView>(R.id.time_tv)

        fun bind(message : MessageData){
            message_tv.text = message.message
            time_tv.text = message.date

            var firestore = FirebaseFirestore.getInstance()
            // 파이어 스토어에서 사용자 이름 가져오기
            firestore.collection("users").document(message.email).get()
                .addOnSuccessListener {
                    var user = it.toObject(UserData::class.java)
                    u_name_tv.text = user?.userName
                }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

}