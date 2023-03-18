package com.example.chattingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.databinding.FragmentFriendBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FriendFragment:Fragment() {
    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var mBinding: FragmentFriendBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!
    // FirebaseAuth 의 인스턴스를 선언
    private lateinit var auth: FirebaseAuth
    // Recycler View 선언하기
    private lateinit var recyclerView: RecyclerView
    private lateinit var profileAdapter: ProfileAdapter

    companion object{ // 정적으로 사용되는 부분이 object로 들어감
        const val TAG : String = "로그"

        // 자기 자신의 instance 가져오기
        fun newInstance() : FriendFragment{
            return FriendFragment()
        }
    }

    // 메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "FriendFragment - onCreate() called")
    }

    //Fragment를 안고 있는 액티비티에 붙었을 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG,"FriendFragment - onAttach() called")
    }

    // View가 생성되었을 때 - 화면(Fragment)과 레이아웃을 연결
   override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 액티비티 와는 다르게 layoutInflater 를 쓰지 않고 inflater 인자를 가져와 뷰와 연결한다.
        Log.d(TAG, "FriendFragment - onCreateView() called")
        mBinding = FragmentFriendBinding.inflate(inflater, container, false)
        auth = Firebase.auth

       /* mBinding!!.testLogoutBtn.setOnClickListener {
            // 사용자 계정 로그아웃
            auth.signOut()
            Log.d(TAG, "Logout Button Click. - FriendFragment")
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

            // 로그인 화면으로 이동
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
*/
        recyclerView = binding.friendRecy
        val dataList = ArrayList<Profile>()
        profileAdapter = ProfileAdapter(dataList)
        recyclerView.adapter = profileAdapter

        dataList.add(Profile(R.drawable.ic_friend, "Ramy"))
        dataList.add(Profile(R.drawable.ic_friend, "Choi"))
        dataList.add(Profile(R.drawable.ic_friend, "James"))
        dataList.add(Profile(R.drawable.ic_friend, "David"))
        dataList.add(Profile(R.drawable.ic_friend, "Ramy"))
        dataList.add(Profile(R.drawable.ic_friend, "Choi"))
        dataList.add(Profile(R.drawable.ic_friend, "James"))
        dataList.add(Profile(R.drawable.ic_friend, "David"))
        dataList.add(Profile(R.drawable.ic_friend, "Ramy"))
        dataList.add(Profile(R.drawable.ic_friend, "Choi"))
        dataList.add(Profile(R.drawable.ic_friend, "James"))
        dataList.add(Profile(R.drawable.ic_friend, "David"))
        dataList.add(Profile(R.drawable.ic_friend, "Ramy"))
        dataList.add(Profile(R.drawable.ic_friend, "Choi"))
        dataList.add(Profile(R.drawable.ic_friend, "James"))
        dataList.add(Profile(R.drawable.ic_friend, "David"))


        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        profileAdapter.notifyDataSetChanged()
        return binding.root
    }

    /* Fragment에서 View Binding을 사용할 경우 Fragment는 View보다 오래 지속되어,
    Fragment의 Lifecycle로 인해 메모리 누수가 발생할 수 있기 때문*/
    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}