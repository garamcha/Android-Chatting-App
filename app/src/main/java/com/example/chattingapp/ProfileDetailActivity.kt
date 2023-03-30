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

        val datas = intent.extras
            //intent.getParcelableExtra("data", ProfileData::class.java)
        if(datas != null){
            binding.profileName.text = datas.getString("name")
            binding.profileImg.setImageResource(datas.getInt("img"))

        }

        //ImageButton(채팅) 클릭
        binding.chatbtn.setOnClickListener {
            // ChattingActivity로 사용자의 정보를 담아서 이동
            Intent(this, ChattingActivity::class.java).apply {
                putExtra("data", datas)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //액티비티가 다른 애플리케이션과 독립적으로 실행되도록 보장
            }.run { startActivity(this) } //현재 액티비티의 컨텍스트에서 다른 액티비티를 시작
            finish()
        }
    }
}


