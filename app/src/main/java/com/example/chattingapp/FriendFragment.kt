package com.example.chattingapp

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.databinding.FragmentFriendBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class FriendFragment:Fragment() {
    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var mBinding: FragmentFriendBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!
    // FirebaseAuth 의 인스턴스를 선언
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    // Recycler View 선언하기
    private lateinit var recyclerView: RecyclerView
    private lateinit var profileAdapter: ProfileAdapter

    // 프로필 이미지 리스트
    private  var imgList : ArrayList<Int> = arrayListOf(R.drawable.ic_friend)
    // 프로필 정보 리스트
    val dataList = ArrayList<ProfileData>()
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
        // 앱바 프래그먼트가 옵션 메뉴를 채우는데 참여하고 있다고 시스템에 알림
        // 프래그먼트가 메뉴 관련 콜백을 수신하려 한다고 시스템에 알립
        setHasOptionsMenu(true)
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
        firestore = FirebaseFirestore.getInstance()

        //1. 툴바 사용 설정
        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar) // 내가 만든 툴바 사용
        toolbar.title = "친구 목록"
        //(activity as AppCompatActivity).supportActionBar?.title = "새로운 타이틀"


        /*// 2. 툴바 왼쪽 버튼 설정
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)  // 왼쪽 버튼 사용 여부 true
        supportActionBar!!.setHomeAsUpIndicator()  // 왼쪽 버튼 아이콘 설정
        supportActionBar!!.setDisplayShowTitleEnabled(false)*/

        recyclerView = binding.friendRecy
        profileAdapter = ProfileAdapter(dataList)
        recyclerView.adapter = profileAdapter

        profileAdapter.setOnItemClickListener(object : ProfileAdapter.OnItemClickListener{
            override fun onClick(v: View, data: ProfileData, position: Int) {
                // 클릭 시 실행할 행동 입력
                Intent(requireContext(), ProfileDetailActivity::class.java).apply {
                    putExtra("data", data)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //액티비티가 다른 애플리케이션과 독립적으로 실행되도록 보장
                }.run { startActivity(this) } //현재 액티비티의 컨텍스트에서 다른 액티비티를 시작
            }

        })

        // 파이어스토어에서 친구목록 불러오기
        friendList()

        //dataList.add(ProfileData(imgList[0], "Ramy"))
        //dataList.add(ProfileData(imgList[0], "Choi"))


        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        profileAdapter.notifyDataSetChanged()
        return binding.root
    }

    // 3. 툴바 메뉴 버튼을 설정
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_friend_menu, menu)
        // IconColor 변경 함수
        changeIconColor(menu)
    }

    // 4. 툴바 메뉴 버튼이 클릭 되었을 때 콜백
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭된 메뉴 아이템의 아이디 마다 when 구절로 클릭 시 동작 설정
        when(item!!.itemId){
            R.id.menu_search -> { // 검색 버튼
                Toast.makeText(activity, "Search menu pressed.", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_add -> {// 친구 추가 버튼
                // 친구 추가 버튼(+) 클릭 시
                // 팝업 창에서 이메일로로 친구 추가 가능
                val dialog = AddFriendDialog(requireContext() as AppCompatActivity)
                dialog.show()

                // 다이얼로그에서 정의한 interface를 통해 데이터를 받아온다.
                dialog.setOnClickedListener(object : AddFriendDialog.ButtonClickListener{
                    override fun onClicked(friendEmail: String) {
                        // 다이얼로그에서 가져온 이메일 값으로 친구 추가하기
                        addFriendList(friendEmail)
                    }
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Toolbar Drawble로 설정한 IconColor 변경 함수
    fun changeIconColor(menu: Menu){
        for(m in 0 until  menu.size()){
            val drawable : Drawable? = menu.getItem(m).icon
            if(drawable != null){
                drawable.mutate() // rawable 객체를 수정 가능하게 만듬
                drawable.setTint(ContextCompat.getColor(requireActivity(), R.color.dark_purple)) // Drawable에 색상 필터를 적용
                drawable.setTintMode(PorterDuff.Mode.SRC_ATOP) //  Drawable에 적용되는 색상 필터의 블렌딩 모드를 설정
                //PorterDuff.Mode.SRC_ATOP은 기존 색상과 새로운 색상을 합칠 때, 새로운 색상이 기존 색상을 덮어쓰는 모드
            }
        }
    }

    // 친구 목록을 불러오는 함수
    private fun friendList(){
        // 친구 목록 파이어스토어에서 불러오기
        firestore.collection("users").document(auth.currentUser?.email!!)
            .collection("friendList").get()
            .addOnSuccessListener {result ->
                for(document in result){
                    dataList.add(ProfileData(document.data["name"] as String?, document.id))
                    Log.d(TAG,"파이어스토어에서 친구목록 가져오기 ${document.id} => ${document.data}" )
                    profileAdapter.notifyDataSetChanged()
                }
            }.addOnFailureListener {exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    // 친구 추가 함수
    private fun addFriendList(friendEmail : String){
        var result : Int = 0

        for (data in dataList){ // 이미 친구추가가 되어있는 친구는 추가 불가능
            if(friendEmail.equals(data.email)){
                result = 1
            }
        }
        // 본인은 추가 불가
        if(friendEmail == auth.currentUser!!.email){ // 자기 자신은 친구추가 불가능
            result = 2
        }
        when(result){
            1 -> { // 이미 친구추가가 되어있는 친구는 추가 불가능
                Toast.makeText(requireContext(), "이미 친구입니다.", Toast.LENGTH_SHORT).show()
            }
            2-> { // 본인은 추가 불가
                Toast.makeText(requireContext(), "본인은 친구 추가가 불가능합니다.", Toast.LENGTH_SHORT).show()
            }
            else -> { // 친구추가
                firestore?.collection("users")?.document(friendEmail)?.get()
                    ?.addOnSuccessListener {document ->
                        Log.d(TAG,"document - $document")
                        Log.d(TAG,"이름 ${document["userName"]}, ${document["userEmail"]}")
                        val name = document["userName"] as String?
                        val email = document["userEmail"] as String?
                        if(name != null && email != null){
                            dataList.add(ProfileData(name, email))
                            profileAdapter.notifyDataSetChanged()
                            // 친구추가 목록 파이어스토어에 저장하기
                            firestore?.collection("users")!!.document(auth.currentUser!!.email!!)
                                .collection("friendList")!!.document(friendEmail).set(ProfileData(name, email))
                            Toast.makeText(requireContext(), "친구추가 성공!!", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(requireContext(), "존재하지 않는 친구입니다.", Toast.LENGTH_SHORT).show()
                        }

                    }?.addOnFailureListener {
                        Toast.makeText(requireContext(), "존재하지 않는 친구입니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    /* Fragment에서 View Binding을 사용할 경우 Fragment는 View보다 오래 지속되어,
        Fragment의 Lifecycle로 인해 메모리 누수가 발생할 수 있기 때문*/
    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}