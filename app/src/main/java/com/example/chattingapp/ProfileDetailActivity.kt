package com.example.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chattingapp.databinding.ActivityProfileDetailBinding

class ProfileDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FriendFragment에서 Intent한 정보 받아서 출력하기
        val datas = intent.getParcelableExtra("data", ProfileData::class.java)
        binding.profileName.text = datas?.name
        binding.profileImg.setImageResource(datas!!.img)

        //ImageButton(채팅) 클릭
        binding.chatbtn.setOnClickListener {
            // ChattingActivity로 이동
            val intent = Intent(this, ChattingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}


