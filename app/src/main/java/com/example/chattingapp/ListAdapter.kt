package com.example.chattingapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ListAdapter (var context: Context, var chatList:ArrayList<ListData>)
    : RecyclerView.Adapter<ListAdapter.ViewHolder>()  {

    private var listener: OnClickListener? = null

    interface OnClickListener{
        fun onClick(v : View, data : ListData, position : Int)
    }

    fun setOnClickListener(listener: OnClickListener){
        this.listener = listener
    }


    inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        val other_person_name = v.findViewById<TextView>(R.id.list_name)
        val last_chat = v.findViewById<TextView>(R.id.list_message)
        val other_person_img = v.findViewById<ImageView>(R.id.list_img)
        val last_time = v.findViewById<TextView>(R.id.list_time_tv)

        fun bind(list : ListData){
            val formatter2 = DateTimeFormatter.ofPattern("HH:mm")
            var time = LocalDateTime.parse(list.last_time, DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
            other_person_name.text = list.name
            last_chat.text = list.last_chat
            last_time.text = time.format(formatter2)

            // 상대 프로필 이미지 가져오기
            var storage : FirebaseStorage? = FirebaseStorage.getInstance() //FirebaseStorage 인스턴스 생성
            var storageRef = storage?.reference

            if(list.img.length == 0 && list.img == ""){
                other_person_img.setImageResource(R.drawable.ic_thumnail)
            }else{
                storageRef?.child("profileImages/${list.email}/${list.img}")?.downloadUrl
                    ?.addOnSuccessListener { uri ->
                        Log.d("로그", "상대 프로필 이미지 가져오기 성공 - ListAdapter")
                        Glide.with(context).load(uri).into(other_person_img)
                    }?.addOnFailureListener {
                        Log.d("로그", "상대 프로필 이미지 가져오기 실패 - ListAdapter")
                    }
            }


            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION){
                itemView.setOnClickListener {
                    listener?.onClick(itemView, list, pos)
                }
            }
        }
    }

    var datas = mutableListOf<ListData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        datas = chatList.toMutableList()
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}