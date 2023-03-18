package com.example.chattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chattingapp.databinding.ActivityProfileDetailBinding

class ProfileDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val datas = intent.getParcelableExtra("data", ProfileData::class.java)
        binding.profileName.text = datas?.name
        datas!!.img?.let { binding.profileImg.setImageResource(it) }
    }
}


