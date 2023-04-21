package com.example.chattingapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MessageAdapter(var context: Context, var currentUser : String,
                     var messageList:ArrayList<MessageData>)
    : RecyclerView.Adapter<MessageAdapter.ViewHolder>(){


    inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        val message_tv = v.findViewById<TextView>(R.id.message_tv)
        val u_name_tv = v.findViewById<TextView>(R.id.chat_name_tv)
        val time_tv = v.findViewById<TextView>(R.id.time_tv)
        val profile_img = v.findViewById<ImageView>(R.id.chatting_profileImage)

        fun bind(message : MessageData){
            message_tv.text = message.message
            val formatter2 = DateTimeFormatter.ofPattern("HH:mm")
            var time = LocalDateTime.parse(message.date, DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
            Log.d("로그", "$time - time 변수 - MassageAdapter") // 공백에 T 들어감
            Log.d("로그", "${message.date} - message.date 변수 - MassageAdapter")
            time_tv.text = time.format(formatter2)

            var firestore = FirebaseFirestore.getInstance()
            // 파이어 스토어에서 사용자 이름 가져오기
            firestore.collection("users").document(message.email).get()
                .addOnSuccessListener {
                    var user = it.toObject(UserData::class.java)
                    var storage : FirebaseStorage? = FirebaseStorage.getInstance() //FirebaseStorage 인스턴스 생성
                    var storageRef = storage?.reference

                    storageRef?.child("profileImages/${message.email}/${user?.uri}")?.downloadUrl
                        ?.addOnSuccessListener {uri ->
                            Log.d("로그", "내프로필 파이어 스토어에서 이미지 파일 가져오기 성공 - FriendFragment")
                            Glide.with(context).load(uri).into(profile_img)

                        }?.addOnFailureListener {
                            Log.d("로그", "내프로필 파이어 스토어에서 이미지 파일 가져오기 실패 - FriendFragment")
                        }
                    u_name_tv.text = user?.userName
                    if(currentUser != message.email){
                        message_tv.background.setTint(ContextCompat.getColor(context, R.color.white))
                    }else{
                        message_tv.background.setTint(ContextCompat.getColor(context, R.color.yellow))
                    }

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