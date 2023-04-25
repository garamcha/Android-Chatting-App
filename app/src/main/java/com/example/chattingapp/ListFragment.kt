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
import com.example.chattingapp.databinding.FragmentListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ListFragment:Fragment() {
    // 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
    private var mBinding: FragmentListBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!
    // RecyclerView 객체 선언
    private lateinit var recyclerView : RecyclerView
    private lateinit var listAdapter: ListAdapter
    // 파이어베이스
    var firestore = FirebaseFirestore.getInstance()
    var currentUsers = FirebaseAuth.getInstance().currentUser?.email

    // 채팅 목록 배열 선언
    private var chatList = ArrayList<ListData>()

    companion object{ // 정적으로 사용되는 부분이 object로 들어감
        const val TAG : String = "로그"

        // 자기 자신의 instance 가져오기
        fun newInstance() : ListFragment{
            return ListFragment()
        }
    }

    // 메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ListFragment - onCreate() called")
        // 프래그먼트가 메뉴 관련 콜백을 수신하려 한다고 시스템에 알립
        setHasOptionsMenu(true)
    }

    //Fragment를 안고 있는 액티비티에 붙었을 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG,"ListFragment - onAttach() called")
    }

    // View가 생성되었을 때 - 화면(Fragment)과 레이아웃을 연결
   override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 액티비티 와는 다르게 layoutInflater 를 쓰지 않고 inflater 인자를 가져와 뷰와 연결한다.
        Log.d(TAG, "ListFragment - onCreateView() called")
        mBinding = FragmentListBinding.inflate(inflater, container, false)

        //1. 툴바 사용 설정
        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar) // 내가 만든 툴바 사용
        toolbar.title = "채팅방"


        // 파이어 스토어에서 마지막 대화목록 가져오기
        firestore.collection("users").document(currentUsers.toString())
            .collection("friendList").get()
            .addOnSuccessListener {result ->
                for(document in result){
                    var youName = document["name"] as String
                    var youImg : String? = null
                    if(document["img"] == null){
                        youImg = ""
                    }else{
                        youImg = document["img"] as String
                    }

                    var youEmail = document.id

                    var firestoreRef = firestore.collection("users").document(currentUsers.toString())
                        .collection("friendList").document(youEmail).collection("message")

                    firestoreRef.get().addOnSuccessListener {
                        firestoreRef.orderBy("date", Query.Direction.DESCENDING).limit(1)
                            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                                if(querySnapshot == null){
                                    Log.d("로그", "대화 내용 없음 - ListFragment")
                                }else{
                                    for (dc in querySnapshot!!.documentChanges) {
                                        // firebase에 추가된 메세지 마지막 메세지를 가져오기
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            var firebaseMessage =
                                                dc.document.toObject(MessageData::class.java)
                                            chatList.add(
                                                ListData(youName, firebaseMessage.message, firebaseMessage.date, youImg, youEmail)
                                            )
                                            listAdapter.notifyDataSetChanged()

                                        }
                                    }
                                }
                            }
                    }.addOnFailureListener {
                        Log.d("로그", "대화 내용 없음 - ListFragment")
                    }


                    Log.d("로그", "${document.id} - ListFragment")
                }

            }.addOnFailureListener {
                Log.d("로그", "친구목록 접근 실패 - ListFragment")
            }


        // recyclerView 연결하기
        recyclerView = binding.listRecy
        listAdapter = ListAdapter(requireContext(), chatList)
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        listAdapter.notifyDataSetChanged()

        // 리사이클러뷰 클릭 했을 때 채팅 창으로 이동
        listAdapter.setOnClickListener(object : ListAdapter.OnClickListener{
            override fun onClick(v: View, data: ListData, position: Int) {
                Intent(requireContext(), ChattingActivity::class.java).apply {
                    putExtra("data", ProfileData(data.img, data.name, data.email))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        })

        return binding.root

    }

    // 3. 툴바 메뉴 버튼을 설정
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_list_menu, menu)
        changeIconColor(menu)
    }

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
    }

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

    /* Fragment에서 View Binding을 사용할 경우 Fragment는 View보다 오래 지속되어,
    Fragment의 Lifecycle로 인해 메모리 누수가 발생할 수 있기 때문*/
    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}