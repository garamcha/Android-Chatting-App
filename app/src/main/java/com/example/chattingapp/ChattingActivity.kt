package com.example.chattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.databinding.ActivityChattingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChattingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChattingBinding // ViewBinding
    private lateinit var firestore : FirebaseFirestore
    private lateinit var auth : FirebaseAuth    // Firebase 계정 정보

    private lateinit var messageList : ArrayList<MessageData> // 메시지 정보를 담을 리스트

    // Recycler View 선언하기
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    lateinit var name : String
    var img = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ProfileDetailActivity에서 Intent한 정보 받아오기
        val datas =  intent.extras
            //intent.getParcelableExtra("data", ProfileData::class.java)
        if(datas != null){
            // Intent한 정보 변수로 저장하기
            name = datas.getString("name").toString()
            //img = datas?.img.toString()
        }



        // 툴바 설정
        val toolbar = binding.chatToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 왼쪽 버튼 사용 여부 true
        // 이미지를 추가하지 않으면 자동으로 '<-'가 추가된다.
        setTitle(name) // 툴바 타이틀 사용자 이름으로 설정

        // 채팅 메세지 데이터를 관리하는 리스트 초기화
        messageList = ArrayList()

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Adpater 초기화
        messageAdapter = MessageAdapter(this, messageList)
        binding.chatRecy.adapter = messageAdapter
        binding.chatRecy.layoutManager = LinearLayoutManager(this)

        // Message Collection의 변경사항 리스너 등록
        firestore?.collection("message")?.orderBy("date", Query.Direction.ASCENDING)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot != null) {
                    for (dc in querySnapshot.documentChanges) {
                        // firebase에 추가된 메세지를 messagmeList에 추가
                        if (dc.type == DocumentChange.Type.ADDED) {
                            var firebaseMessage = dc.document.toObject(MessageData::class.java)
                           //firebaseMessage.id = dc.document.id
                            messageList.add(firebaseMessage)
                            messageAdapter.notifyDataSetChanged()
                            binding.chatRecy.scrollToPosition(messageAdapter.itemCount-1)

                        }
                    }
                }
            }

        binding.sendBtn.setOnClickListener {
            onClickSendBtn()
        }
    }

    private fun onClickSendBtn() {
        var msg = binding.chatEt.text.toString()
        // 작성한 메세지가 공백이라면
        if(!(LoginActivity().isNull(msg))){
            return
        }

        val timestamp = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        var time = timestamp.format(formatter)

        // 사용자가 입력한 메세지로 MEssage 인스턴스 생성
        var message = MessageData(msg, name, auth.currentUser?.email!!,time)
        binding.chatEt.setText("") // 메세지 입력창 초기화

        //파이어스토어에 메세지 저장
        firestore.collection("message").document().set(message)
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    Toast.makeText(this, "네트워크가 원활하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
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

}