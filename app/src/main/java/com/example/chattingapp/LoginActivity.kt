package com.example.chattingapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chattingapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sign

class LoginActivity : AppCompatActivity() {
    // ViewBinding 사용을 위한 변수 선언
    private lateinit var binding : ActivityLoginBinding
    // FirebaseAuth 의 인스턴스를 선언
    private lateinit var auth : FirebaseAuth
    private var userName : String? = null

    // Firestore 의 객체 선언
    private var firestore: FirebaseFirestore? = null
    private var uid : String? = null

    // 구글 로그인 연동에 필요한 변수
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private companion object {
        const val TAG = "로그"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() - LoginActivity" )
        super.onCreate(savedInstanceState)
        //DefaultHandler 지정
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
        // binding 객체 선언
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreference 객체 선언
        val auto : SharedPreferences = getSharedPreferences("autoLogin", Context.MODE_PRIVATE)

        // onCreate() 메서드에서 FirebaseAuth 인스턴스를 초기화
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // 구글 로그인
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
        , ActivityResultCallback {result ->
                Log.d(TAG, "resultCode : ${result.resultCode}")
                Log.d(TAG, "result : $result")

                if(result.resultCode == RESULT_OK){
                    Log.d(TAG, "resultCode : ${result.resultCode}")
                    Log.d(TAG, "result : $result")
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try{
                        task.getResult(ApiException::class.java)?.let{account->
                            var tokenId = account.idToken
                            if(tokenId != null && tokenId != ""){
                                val credential : AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                                auth.signInWithCredential(credential).addOnCompleteListener {
                                    if(auth.currentUser != null){
                                        val user : FirebaseUser = auth.currentUser!!
                                        var email = user.email.toString()
                                        Log.d("로그", "email : $email")
                                        val googleSignInToken = account.idToken ?: ""
                                        if(googleSignInToken != ""){
                                            Log.d(TAG, "googleSignInToken : $googleSignInToken")
                                        } else {
                                            Log.d(TAG, "googleSignInToken이 null")
                                        } // else
                                        if(it.isSuccessful){
                                            user?.let {
                                                for(profile in it.providerData){
                                                    Log.d("로그", "$profile : providerData - LoginActivty")
                                                    Log.d("로그", "${profile.uid} : profile.uid - LoginActivty")
                                                    Log.d("로그", "${profile.displayName} : profile.displayName - LoginActivty")
                                                    Log.d("로그", "${profile.photoUrl} : profile.photoUrl - LoginActivty")
                                                    Log.d("로그", "${profile.phoneNumber} :profile.phoneNumber - LoginActivty")
                                                    val name = profile.displayName as String
                                                    val phone = profile.phoneNumber as? String
                                                    // 파이어스토어에 저장. 근데 위치가 잘못되었으나 어디에다가 작성해야할지 고민이다.
                                                    firestore!!.collection("users").document(email).set(UserData(name, null, email, user.uid, phone))
                                                        .addOnSuccessListener { 
                                                            Log.d("로그", "구글 로그인 파이어스토어 저장 성공 - LoginActivty")
                                                        }.addOnFailureListener {
                                                            Log.d("로그", "구글 로그인 파이어스토어 저장 실패 - LoginActivty")
                                                        }
                                                }
                                            }
                                            moveMainPage(it.result?.user)
                                        }
                                    } // if(auth.currentUser != null)
                                } // .addOnCompleteListener
                            }// if(tokenId != null && tokenId != "")
                        } ?: throw Exception() // let
                    } catch (e : Exception){
                        e.printStackTrace()
                    }
                } // if(result.resultCode == RESULT_OK)
            })

        // 구글 로그인 버튼 클릭 시
        binding.run {
            googleSignInBtn.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
                    val signInIntent : Intent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            }
        }

        /**자동로그인**/
        val autoEmail = auto.getString("userEmail", null)
        val autoPassword = auto.getString("password", null)
        //1번째는 데이터 키 값이고 2번째는 키 값에 데이터가 존재하지 않을 때 대체 값
        Log.d(TAG, "SharedPreferences - email : $autoEmail, password : $autoPassword")
        if(autoEmail != null && autoPassword != null){
            auth.signInWithEmailAndPassword(autoEmail!!, autoPassword!!).addOnCompleteListener {
                if(it.isSuccessful){
                    // 로그인 성공 시 MainActivity로 이동하기
                    moveMainPage(it.result?.user) // 현재 로그인한 유저 정보 가져와서 MainPage로 이동
                }
            }.addOnFailureListener {
                Toast.makeText(this, "자동 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }else{
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
                            firestore!!.collection("users").document(email!!).get()
                                .addOnSuccessListener {document->
                                    userName = document["userName"] as String
                                    Log.d("로그", "DocumentSnapshot datas1 : ${document.data}")
                                    Log.d("로그", "DocumentSnapshot datas2 : ${document["userName"]}")
                                    Log.d("로그", "DocumentSnapshot datas3 : ${userName}")
                                    Toast.makeText(this, "${userName}님 환영합니다:)", Toast.LENGTH_SHORT).show()
                                    /**Check Box가 체크 되었을 때**/
                                    if(binding.autoLogin.isChecked){
                                        // 자동로그인에 필요한 정보 저장
                                        val autoLoginEdit = auto.edit()
                                        autoLoginEdit.putString("userEmail", email)
                                        autoLoginEdit.putString("password", password)
                                        autoLoginEdit.putString("name", userName)
                                        autoLoginEdit.commit()
                                    }
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
        }

        /** "아직 회원이 아니신가요?"를 클릭 했을 때*/
        binding.moveSignup.setOnClickListener {
            Log.d(TAG, "SignUp Text(Button) Click - LoginActivity" )
            // SignUp Activity로 이동
            // 명시적 Intent 사용
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // Handler 구현
    inner class ExceptionHandler : Thread.UncaughtExceptionHandler{
        override fun uncaughtException(p0: Thread, p1: Throwable) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
            // 앱이 비정상적으로 종료 되었을 경우 로그아웃
            auth?.signOut()
            googleSignInClient.signOut()
            // SharedPreference 객체 선언
            val auto : SharedPreferences = getSharedPreferences("autoLogin", Context.MODE_PRIVATE)
            val logoutbtn : SharedPreferences.Editor = auto.edit()
            // SharedPreference 데이터 삭제
            logoutbtn.clear()
            logoutbtn.commit()
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
