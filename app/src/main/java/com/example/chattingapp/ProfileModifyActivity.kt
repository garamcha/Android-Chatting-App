package com.example.chattingapp


import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chattingapp.databinding.ActivityProfileModifyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ProfileModifyActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProfileModifyBinding
    private var auth: FirebaseAuth = Firebase.auth
    private lateinit var firestore : FirebaseFirestore
    private var storage : FirebaseStorage? = FirebaseStorage.getInstance()
    private var uri : Uri? = null
    var currentUserEmail = auth.currentUser?.email

    // 권한 요청
    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ results ->
            results.forEach{
                if(!it.value){
                    Toast.makeText(this, "${it.key} 권한 허용 필요", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    // 추가할 권한들, 매니페스트 파일서도 추가 필요
    private val permissionList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firestore = FirebaseFirestore.getInstance()

        // 권한 받기
        requestMultiplePermission.launch(permissionList)

        auth = Firebase.auth

        var originalName = intent.getStringExtra("name")
        // 기존 이름 가져오기
        binding.myNameModify.setText(originalName)

        // 이메일 가져오기
        binding.myEmail.setText(auth.currentUser!!.email)

        // 내 프로필 이미지 띄우기
        getMyProfileImage()

        // 저장버튼 클릭 했을 때
        binding.savebtn.setOnClickListener {
            // 기본 형태 다이얼로그
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Profile")
                .setMessage("변경사항을 저장하시겠습니까?")
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        // 확인 클릭 시 할 행동
                        // 파이어베이스 스토리지에 이미지 업로드
                        if(uri != null){
                            deleteImageToFirebase()
                            uploadImageToFirebase(uri!!)
                        }
                        // 이름 변경 확인 후 적용하기
                        var modifyName = binding.myNameModify.text.toString()

                        // 파이어베이스 스토어에 변경한 이름 적용하기
                        firestore.collection("users").document(auth.currentUser!!.email!!).update("userName", modifyName)
                            .addOnSuccessListener {
                                Log.d("로그", "이름 변경 성공 $modifyName - ProfileModifyActivity" )
                            }.addOnFailureListener {
                                Log.d("로그", "이름 변경 실패 $modifyName - ProfileModifyActivity")
                            }
                        Toast.makeText(this, "확인 클릭", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 1500)

                })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                        // 취소 틀릭시 할 행동
                        Toast.makeText(this, "취소 클릭", Toast.LENGTH_SHORT).show()
                })
            builder.show()
        }
        // 프로필 이미지를 클릭 했을 때
        binding.myProfileImg.setOnClickListener {
            // 갤러리 호출
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            activityResult.launch(intent)

        }

    }

    // 결과 가져오기
    private val activityResult : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        // 결과 코드 OK. 결과 값이 null 이 아니면
        if(it.resultCode == RESULT_OK && it.data != null){
            // 값 담기
            uri = it.data!!.data
            Log.d("로그", "URI : $uri - ProfileModifyActivity")
            // 화면에 보여주기
            showImage(uri!!)
        }
    }

    // 이미지 삭제하는 함수
    private fun deleteImageToFirebase(){
        // 기존 이미지 명 가져오기
        firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(currentUserEmail!!).get()
            .addOnSuccessListener {
                var info = it.toObject(UserData::class.java)
                var img = info?.uri
                Log.d("로그", "기존 프로필 이미지 주소 $img - ProfileModifyActivity")
                if(img != null && img.length > 0){
                    storage?.reference?.child("profileImages/${currentUserEmail}/$img")?.delete()
                        ?.addOnSuccessListener {
                            Log.d("로그", "기존 프로필 이미지 삭제 완료 - ProfileModifyActivity")
                        }
                        ?.addOnFailureListener {
                            Log.d("로그", "기존 프로필 이미지 삭제 실패 - ProfileModifyActivity")
                        }
                }else{
                    Log.d("로그", "삭제할 이미지 없음 - ProfileModifyActivity")
                }
            }
    }

    fun uploadImageToFirebase(uri : Uri){
        // 스토리지에 저장될 파일 명
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imgFileName = "IMAGE_" + timeStamp + "_.jpg"
        //파일 업로드, 다운로드, 삭제, 메타데이터 가져오기 또는 업데이트를 하기 위해 참조를 생성.
        //참조는 클라우드 파일을 가리키는 포인터라고 할 수 있음.
        var storagrRef = storage?.reference?.child("profileImages")?.child(auth.currentUser?.email!!)?.child(imgFileName)
        storagrRef?.putFile(uri!!)?.addOnSuccessListener {
            // 파이어스토어에 uri 저장하기
            val firestoreRef = firestore.collection("users").document(auth.currentUser!!.email!!)
            firestoreRef.update("uri", imgFileName)
                .addOnSuccessListener {
                    Log.d("로그", "Uri 업데이트 성공 - ProfileModifyActivity")

                }.addOnFailureListener {
                        e -> Log.w("로그", "Error updating document - ProfileModifyActivity", e)
                }
            Toast.makeText(this, "ImageUploaded Success.", Toast.LENGTH_SHORT).show()
        }?.addOnFailureListener{
            Toast.makeText(this, "ImageUploaded Failed.", Toast.LENGTH_SHORT).show()
        }
    }

    fun getMyProfileImage(){
        var storageRef = storage?.reference

        firestore.collection("users").document(currentUserEmail!!).get()
            .addOnSuccessListener {document ->
                Log.d("로그", "${document["uri"]} - ProfileModigyActivty")
                storageRef?.child("profileImages/${currentUserEmail}/${document["uri"]}")?.downloadUrl
                    ?.addOnSuccessListener {uri ->
                        Log.d("로그", "파이어 스토어에서 이미지 파일 가져오기 성공 - ProfileModigyActivty")
                        showImage(uri)
                    }
            }.addOnFailureListener {
                Log.d("로그", "파이어 스토어에서 이미지 파일 가져오기 실패 - ProfileModigyActivty")
            }
    }

    fun showImage(uri : Uri){
        Glide.with(this)
            .load(uri) // 이미지
            .into(binding.myProfileImg) // 보여줄 위치
    }
}