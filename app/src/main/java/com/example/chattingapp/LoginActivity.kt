package com.example.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chattingapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    // ViewBinding 사용을 위한 변수 선언
    private lateinit var binding : ActivityLoginBinding
    // FirebaseAuth 의 인스턴스를 선언
    private lateinit var auth : FirebaseAuth
    private var userName : String? = null

    private var firestore: FirebaseFirestore? = null
    private var uid : String? = null
    private companion object {
        const val TAG = "로그"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding 객체 선언
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // onCreate() 메서드에서 FirebaseAuth 인스턴스를 초기화
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        /** Login 버튼을 눌렀을 때 **/
        binding.LoginBtn.setOnClickListener {
            Log.d(TAG, "Login Button Click - LoginActivity" )
            // 입력한 이메일 가져오기
            var email = binding.emailEt.text.toString().trim()
            // 입력한 비밀번호 가져오기
            var password = binding.pwdEt.text.toString().trim()

            // EditText의 공백 문자 걸러내기
            if(!isNull(email)){
                Log.d(TAG, "Empty ID - LoginActivity" )
                Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else if(!isNull(password)){
                Log.d(TAG, "Empty Password - LoginActivity" )
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else{
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    uid = FirebaseAuth.getInstance().currentUser?.uid

                    if(it.isSuccessful){
                        Log.d(TAG, "Success Login - LoginActivity" )
                        Log.d(TAG, "currentUser UID ${uid}- LoginActivity" )
                        // 회원가입 시 파이어스토어에 저장된 이름 가져오기
                        firestore!!.collection("users").document(uid!!).get()
                            .addOnSuccessListener {document->
                                userName = document["userName"] as String
                                Log.d("로그", "DocumentSnapshot datas1 : ${document.data}")
                                Log.d("로그", "DocumentSnapshot datas2 : ${document["userName"]}")
                                Log.d("로그", "DocumentSnapshot datas3 : ${userName}")
                                Toast.makeText(this, "${userName}님 환영합니다:)", Toast.LENGTH_SHORT).show()
                                // 로그인 성공 시 MainActivity로 이동하기
                                moveMainPage(it.result?.user) // 현재 로그인한 유저 정보 가져와서 MainPage로 이동
                            }
                    }
                    else{
                        Log.d(TAG, "Failed Login - LoginActivity" )
                        Toast.makeText(this, "존재하지 않는 아이디/비밀번호 입니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } //binding.LoginBtn.setOnClickListener

        /** "아직 회원이 아니신가요?"를 클릭 했을 때*/
        binding.moveSignup.setOnClickListener {
            Log.d(TAG, "SignUp Text(Button) Click - LoginActivity" )
            // SignUp Activity로 이동
            // 명시적 Intent 사용
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            moveMainPage(auth?.currentUser)
        }
    }

    // 로그아웃 하지 않았을 경우. 자동 로그인
    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }
    
    // MainActivity로 이동하는 함수
    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // 문자열 공백 확인하는 함수
    fun isNull(str: String) : Boolean{
        var returnValue = true

        if(str.length == 0){
            returnValue = false
        }
        return returnValue
    }
}