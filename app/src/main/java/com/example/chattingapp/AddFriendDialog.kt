package com.example.chattingapp

import android.app.Dialog
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.example.chattingapp.databinding.AddFriendDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AddFriendDialog(private val context : AppCompatActivity) {

    private lateinit var binding: AddFriendDialogBinding
    private val dlg = Dialog(context)

    fun show() {
        binding = AddFriendDialogBinding.inflate(context.layoutInflater)

        // EditText 초기화
        binding.addFriendEt.text = null

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 바 제거
        dlg.setContentView(binding.root) // 다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setCanceledOnTouchOutside(false)// 다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않음
        dlg.setCancelable(false)    // 다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않음

        // 확인 버튼 동작
        binding.okBtn.setOnClickListener {
            Log.d("로그", "이메일: ${binding.addFriendEt.text.toString().trim()} - Dialog 확인 버튼 동작 시 입력한 이메일 가져오기")
            // 이메일 입력 칸이 공백인 경우
            if(binding.addFriendEt.text == null || binding.addFriendEt.text.length == 0){
                Toast.makeText(context, "이메일을 입력하세요", Toast.LENGTH_SHORT).show()
            }
            else{
                // 이메일을 입력하고 interface를 이용하여 값을 넘긴다.
                onClickedListener.onClicked(binding.addFriendEt.text.toString().trim())
                dlg.dismiss() // 다이얼로그 종료
            }
        }

        // 취소 버튼 동작
        binding.cancleBtn.setOnClickListener {
            // 아무것도 하지 않고 다이얼로그 종료
            dlg.dismiss()
        }

        dlg.show()
    }

    interface ButtonClickListener {
        fun onClicked(friendEmail : String)
    }

    private lateinit var onClickedListener: ButtonClickListener

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickedListener = listener
    }
}
