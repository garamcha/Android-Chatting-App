package com.example.chattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import com.example.chattingapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(){
    private companion object {
        const val TAG = "로그"
    }
    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var mBinding: ActivityMainBinding? = null  // ViewBinding 변수 선언
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var friendFragment: FriendFragment
    private lateinit var listFragment: ListFragment
    private lateinit var settingFragment: SettingFragment

    //메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater) // ViewBinding 초기화
        //레이아웃과 연결
        setContentView(binding.root)
        Log.d(TAG, "MainActivity - onCreate() called")

        val toolbar = binding.mainToolbar
        setSupportActionBar(toolbar)

        var mainFrame = R.id.fragment_frame //MainActivity FrameLayout ID
        friendFragment = FriendFragment.newInstance()
        // 처음 실행하였을 때 비어있는 MainActivity에 Fragment추가하기
        supportFragmentManager.beginTransaction().add(mainFrame, friendFragment).commit()

        bottomNavigationView = binding.bottomNav
        bottomNavigationView.setOnItemSelectedListener { menuItem->
            when(menuItem.itemId){
                R.id.menu_friend -> {
                    Log.d(TAG, "MainActivity - 친구 Fragment")
                    // 친구 버튼 클릭 시 FriendFragment 보여주기
                    friendFragment = FriendFragment.newInstance()
                    supportFragmentManager.beginTransaction().replace(mainFrame, friendFragment).commit()
                                                                    // 해당 위치에, 다음 프레그먼트를 넣어라
                    true
                }
                R.id.menu_list -> {
                    Log.d(TAG, "MainActivity - 채팅 목록 Fragment")
                    setTitle("채팅방")
                    // 채팅목록 버튼 클릭 시 ListFragment 보여주기
                    listFragment = ListFragment.newInstance()
                    supportFragmentManager.beginTransaction().replace(mainFrame, listFragment).commit()
                                                                    // 해당 위치에, 다음 프레그먼트를 넣어라
                    true
                }
                R.id.menu_setting -> {
                    Log.d(TAG, "MainActivity - 설정 Fragment")
                    // 설정 버튼 클릭 시 SettingFragment 보여주기
                    settingFragment = SettingFragment.newInstance()
                    supportFragmentManager.beginTransaction().replace(mainFrame, settingFragment).commit()
                                                                     // 해당 위치에, 다음 프레그먼트를 넣어라
                    true
                }
                else -> false
            } // when(menuItem.itemId)
        } // bottomNavigationView.setOnItemSelectedListener
    } // override fun onCreate
} // class MainActivity : AppCompatActivity()