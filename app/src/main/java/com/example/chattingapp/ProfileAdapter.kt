package com.example.chattingapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

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
            val fileName = item.img
            val email = item.email
            var storage : FirebaseStorage? = FirebaseStorage.getInstance() //FirebaseStorage 인스턴스 생성
            var storageRef = storage?.reference

            if(fileName.equals(null) || fileName?.length == 0){
                userImg.setImageResource(R.drawable.ic_thumnail)
                Log.d("로그", "친구 프로필 이미지 기본으로 설정 - ProfileAdapter")
            }else{
                storageRef?.child("profileImages/${email}/$fileName")?.downloadUrl
                    ?.addOnSuccessListener {uri ->
                        Log.d("로그", "친구 파이어 스토어에서 이미지 파일 가져오기 성공 - ProfileAdapter")
                        Glide.with(itemView).load(uri).into(userImg)

                    }?.addOnFailureListener {
                        Log.d("로그", "친구 파이어 스토어에서 이미지 파일 가져오기 실패 - ProfileAdapter")
                    }
            }

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
