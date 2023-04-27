package com.example.chattingapp

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isInvisible
import com.bumptech.glide.Glide
import com.example.chattingapp.databinding.ActivityProfileDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.storage.FirebaseStorage

class ProfileDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileDetailBinding
    private var result : Boolean = true
    private var storage : FirebaseStorage? = FirebaseStorage.getInstance() //FirebaseStorage 인스턴스 생성
    private var storageRef = storage?.reference
    private var auth = Firebase.auth
    private var currentUserEmail = auth.currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FriendFragment에서 Intent한 정보 받아서 출력하기
        // val datas = intent.extras?.getSerializable("data", ProfileData::class.java)
        //val datas = intent.getSerializableExtra("data") as ProfileData // 사용 되는 거였음;;
        val datas = getSerializable<ProfileData>(this, "data",ProfileData::class.java)
        result = intent.getBooleanExtra("result", true)
        if(result){ // result가 true면 톱니바퀴가 보임
            // 액션바 추가하여 프로필 변경할 수 있도록 설정
            binding.profileModify.visibility = View.VISIBLE
            binding.chatbtn.visibility = View.INVISIBLE
        }
        else{ // 톱니바퀴 안보임
            binding.profileModify.visibility =  View.INVISIBLE
        }
        if(datas != null){
            Log.d("로그", "프로필을 클릭 했을 때 intent로 넘어오는 데이터 = $datas - ProfileDetailActivity")
            if(datas.img.equals(null) || datas.img == ""){
                // 설정된 프로필 없을 경우 기본 이미지로 설정
                binding.profileImg.setImageResource(R.drawable.ic_thumnail)
            }else{
                // 파이어베이스 스토리지에서 이미지 가져오기
                storageRef?.child("profileImages/${datas.email}/${datas.img}")?.downloadUrl
                    ?.addOnSuccessListener {uri ->
                        Log.d("로그", "파이어 스토어에서 이미지 파일 가져오기 성공 - ProfileDetailActivity")
                        showImage(uri)
                    }?.addOnFailureListener { 
                        Log.d("로그", "파이어 스토어에서 이미지 파일 가져오기 실패 - ProfileDetailActivity") 
                    }
            }
            binding.profileName.text = datas.name
        }

        // ImageView(설정) 클릭
        binding.profileModify.setOnClickListener {
            Intent(this, ProfileModifyActivity::class.java).apply {
                putExtra("name", binding.profileName.text)
            }.run { startActivity(this) }
            finish()
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

    fun <t : java.io.Serializable?>getSerializable(activity : Activity, name : String, clazz: Class<ProfileData>) : ProfileData{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as ProfileData

    }

    fun showImage(uri : Uri){
        Glide.with(this)
            .load(uri) // 이미지
            .into(binding.profileImg) // 보여줄 위치
    }
}


