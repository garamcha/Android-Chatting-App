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
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    // ViewBinding 사용을 위한 변수 선언
    private lateinit var binding : ActivityLoginBinding
    // FirebaseAuth 의 인스턴스를 선언
    private lateinit var auth : FirebaseAuth
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
                    if(it.isSuccessful){
                        Log.d(TAG, "Success Login - LoginActivity" )
                        // 로그인 성공 시 MainActivity로 이동하기
                        moveMainPage(it.result?.user) // 현재 로그인한 유저 정보 가져와서 MainPage로 이동
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
        }
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