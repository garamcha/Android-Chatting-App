package com.example.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.widget.Toast
import com.example.chattingapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "로그"
    }
    // ViewBinding 사용을 위한 변수 선언
    private lateinit var binding: ActivitySignUpBinding
    // FirebaseAuth 의 인스턴스를 선언
    private lateinit var auth: FirebaseAuth
    // FirebaseFirestore 의 인스턴스 선언
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding 객체 선언
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // onCreate() 메서드에서 FirebaseAuth/FirebaseFirestore 인스턴스를 초기화
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // 전화번호 입력 시 자동 하이픈 추가
        binding.phoneEt.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        /**
         * 회원가입
         * */

        // SIGN UP 버튼을 눌렀을 때
        binding.signUpBtn.setOnClickListener {
            // 사용자의 이름
            val name = binding.nameEt.text.toString().trim()
            // 사용자의 이메일 주소
            val email = binding.emailEt.text.toString().trim()
            // 사용자의 전화번호
            val phone = binding.phoneEt.text.toString().trim()
            // 사용자의 비밀번호
            val password = binding.pwdEt.text.toString().trim()
            // 사용자의 비밀번호 확인
            val checkPwd = binding.pwdCheckEt.text.toString().trim()

            // 입력칸 공백 확인 하기
            if(!LoginActivity().isNull(name)){
                Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else if(!LoginActivity().isNull(email)){
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else if(!LoginActivity().isNull(phone)){
                Toast.makeText(this, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else if(!LoginActivity().isNull(password)){
                Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else if(!LoginActivity().isNull(checkPwd) || password != checkPwd){ // 비밀번호 확인
                Toast.makeText(this, "비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
            }
            else{
                // 사용자 계정 만들기
                createUser(email, password, name, phone)
            }
        }


    }

    // 사용자 계정 만들기
    private fun createUser(email: String, password: String, name : String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if(it.isSuccessful){
                // 회원가입에 성공 했을 때
                val user = auth.currentUser
                Log.d(TAG, "Create Account Success - SignUpActivity" )

                // Firebase에 회원정보 저장
                uploadUserInfo(user, name, email, phone)

                // MainActivity로 넘어가기
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            // 회원가입에 실패 했을 때
            else{
                Toast.makeText(this, "이미 존재하는 계정이거나 이메일 형식에 맞지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /// 사용자 계정 CloudFirestore에 저장하는 함수
    private fun uploadUserInfo(user: FirebaseUser?, name : String, email: String, phone: String){
        // UserData DataClass로 변수 선언
        val newUser = UserData(name, email, user!!.uid, phone)
        /*newUser.userName = name
          newUser.userId = email
          newUser.uid = user!!.uid*/
        firestore.collection("users")?.document(auth.uid.toString())?.set(newUser)
            ?.addOnSuccessListener {
                // Firestore 저장 성공 했을 때
                Toast.makeText(this, "환영합니다 :)", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener {
                // Firestore 저장 실패 했을 때
                Toast.makeText(this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}