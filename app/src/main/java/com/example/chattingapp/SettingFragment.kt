package com.example.chattingapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.chattingapp.databinding.FragmentSettingBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingFragment:Fragment() {
    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var mBinding: FragmentSettingBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!
    // FirebaseAuth 의 인스턴스를 선언
    private lateinit var auth: FirebaseAuth

    companion object{ // 정적으로 사용되는 부분이 object로 들어감
        const val TAG : String = "로그"

        // 자기 자신의 instance 가져오기
        fun newInstance() : SettingFragment{
            return SettingFragment()
        }
    }

    // 메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "SettingFragment - onCreate() called")
        setHasOptionsMenu(true)
    }

    //Fragment를 안고 있는 액티비티에 붙었을 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG,"SettingFragment - onAttach() called")
    }

    // View가 생성 되었을 때 - 화면(Fragment)과 레이아웃을 연결
   override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 액티비티 와는 다르게 layoutInflater 를 쓰지 않고 inflater 인자를 가져와 뷰와 연결한다.
        Log.d(TAG, "SettingFragment - onCreateView() called")
        mBinding = FragmentSettingBinding.inflate(inflater, container, false)
        auth = Firebase.auth

        //모바일 광고 SDK 초기화
        MobileAds.initialize(requireContext()){}

        // 광고 띄우기
        val adRequest = AdRequest.Builder().build()
        binding.adsBanner.loadAd(adRequest)


        // SharedPreference 객체 선언
        val auto : SharedPreferences = requireContext().getSharedPreferences("autoLogin", Context.MODE_PRIVATE)

        //1. 툴바 사용 설정
        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar) // 내가 만든 툴바 사용
        toolbar.title = "설정"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.logouBtn.setOnClickListener {
            // 사용자 계정 로그아웃
            auth.signOut()
            googleSignInClient.signOut()
            Log.d(TAG, "Logout Button Click. - SettingFragment")
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            // 로그아웃 시 sharedPreference에 있는 데이터 삭제
            val autoLogoutEdit = auto.edit()
            autoLogoutEdit.clear()
            autoLogoutEdit.commit()
            // 로그인 화면으로 이동
            startActivity(Intent(requireContext(), LoginActivity::class.java))

        }

        return binding.root
    }
    // 3. 툴바 메뉴 버튼을 설정
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_setting_menu, menu)
    }
/*
    // 4. 툴바 메뉴 버튼이 클릭 되었을 때 콜백
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭된 메뉴 아이템의 아이디 마다 when 구절로 클릭 시 동작 설정
        when(item!!.itemId){
            R.id.menu_search -> { // 검색 버튼
                Toast.makeText(activity, "Search menu pressed.", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_upsort -> {// 오름차순 버튼
                Toast.makeText(activity, "Up Sort pressed.", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_downsort -> {// 내림차순 버튼
                Toast.makeText(activity, "Down Sort pressed.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    /* Fragment에서 View Binding을 사용할 경우 Fragment는 View보다 오래 지속되어,
    Fragment의 Lifecycle로 인해 메모리 누수가 발생할 수 있기 때문*/
    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}