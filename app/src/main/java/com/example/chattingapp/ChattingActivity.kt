package com.example.chattingapp

import android.app.Activity
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.databinding.ActivityChattingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldPath.documentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChattingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChattingBinding // ViewBinding
    private lateinit var firestore : FirebaseFirestore
    private var auth : FirebaseAuth = FirebaseAuth.getInstance()  // Firebase 계정 정보
    private var currentUser = auth.currentUser!!.email

    private lateinit var messageList : ArrayList<MessageData> // 메시지 정보를 담을 리스트

    // Recycler View 선언하기
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var youName : String
    private lateinit var youEmail : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // ProfileDetailActivity에서 Intent한 상대 정보 받아오기
        val datas = getSerializable<ProfileData>(this, "data", ProfileData::class.java)
        if(datas != null){
            // Intent한 정보 변수로 저장하기
            youName = datas.name.toString()
            youEmail = datas.email.toString()
            Log.d("로그", "상대 사용자 정보 이름 가져오기 성공 - Chatting Activity")
        }
        else{
            Log.d("로그", "상대 사용자 정보 없음 - Chatting Activity")
            finish()
        }

        // 툴바 설정
        val toolbar = binding.chatToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 왼쪽 버튼 사용 여부 true
        // 이미지를 추가하지 않으면 자동으로 '<-'가 추가된다.
        setTitle(youName) // 툴바 타이틀 사용자 이름으로 설정

        // 채팅 메세지 데이터를 관리하는 리스트 초기화
        messageList = ArrayList()

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Adpater 초기화
        messageAdapter = MessageAdapter(this, currentUser!!, messageList)
        binding.chatRecy.adapter = messageAdapter
        binding.chatRecy.layoutManager = LinearLayoutManager(this)

        Toast.makeText(this, "현재 사용자 : $currentUser", Toast.LENGTH_SHORT).show()

        // Message Collection의 변경사항 리스너 등록
        firestore?.collection("users")?.document(currentUser!!)
            ?.collection("friendList")?.document(youEmail)?.collection("message")?.orderBy("date", Query.Direction.ASCENDING)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot != null) {
                    for (dc in querySnapshot.documentChanges) {
                        // firebase에 추가된 메세지를 messagmeList에 추가
                        if (dc.type == DocumentChange.Type.ADDED) {
                            var firebaseMessage = dc.document.toObject(MessageData::class.java)
                            firebaseMessage.id = dc.document.id
                            Log.d("로그", "${firebaseMessage.id} - 도큐멘트 자동 id")
                            messageList.add(firebaseMessage)
                            messageAdapter.notifyDataSetChanged()
                            binding.chatRecy.scrollToPosition(messageAdapter.itemCount-1) // 메세지를 입력하면 제일 아래로 이동

                        }
                    }
                }
                else{
                    Log.d("로그", "파이어스토어에 대화 내용 없음 - Chatting Activity")
                }
            }

        binding.sendBtn.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                onClickSendBtn(datas)
            }, 1000) // 1초 이하보다 많이 보내는 경우를 방지하기 위하여 추가
        }
    }

    private fun onClickSendBtn(datas : ProfileData) {
        var msg = binding.chatEt.text.toString()
        // 작성한 메세지가 공백이라면
        if(!(LoginActivity().isNull(msg))){
            return
        }

        // 로컬 시간 가져오기
        val timestamp = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")
        var time = timestamp.format(formatter)



        var myName : String = ""
        // 사용자가 입력한 메세지로 MEssage 인스턴스 생성
        // 현재 사용자 이름 가져오기
        firestore.collection("users").document(currentUser!!).get().addOnSuccessListener {
            //Toast.makeText(this, "$timestampServer", Toast.LENGTH_SHORT).show()
            Log.d("로그", "현재 사용자 이름 가져오기 성공 - Chatting Activity")
            var user = it.toObject(UserData::class.java)
            myName = user?.userName as String
            var message = MessageData(msg, myName, auth.currentUser?.email!!,time)
            // 친구추가 되어있는 상태인지 확인하기


            //파이어스토어에 메세지 저장
            //본인 데이터 베이스에 저장
            val myFirestoreRef = firestore.collection("users").document(currentUser!!)
                .collection("friendList").document(youEmail)
            val youFirestoreRef = firestore.collection("users").document(youEmail)
                .collection("friendList").document(currentUser!!)

            myFirestoreRef.collection("message").document().set(message)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.d("로그", "본인 데이터베이스에 메세지 저장 성공 - Chatting Activity")
                        // 상대방 데이터베이스에 저장
                        youFirestoreRef.collection("message").document().set(message)
                            .addOnSuccessListener {

                                Log.d("로그", "상대 데이터베이스에 메세지 저장 성공 - Chatting Activity")
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "상대방이 친구추가가 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    }
                    else{
                        Toast.makeText(this, "네트워크가 원활하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.chatEt.setText("") // 메세지 입력창 초기화


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun <t : java.io.Serializable?>getSerializable(activity : Activity, name : String, clazz: Class<ProfileData>) : ProfileData{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as ProfileData

    }

}