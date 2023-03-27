package com.example.chattingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Window
import com.example.chattingapp.databinding.AddFriendDialogBinding

class AddFriendDialog(private val context : AppCompatActivity) {

    private lateinit var binding: AddFriendDialogBinding
    private val dlg = Dialog(context)

    fun show(){
        binding = AddFriendDialogBinding.inflate(context.layoutInflater)

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 바 제거
        dlg.setContentView(binding.root) // 다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setCancelable(false)    // 다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않음

        // 확인 버튼 동작
        binding.okBtn.setOnClickListener {

            dlg.dismiss()
        }

        // 취소 버튼 동작
        binding.cancleBtn.setOnClickListener {

            dlg.dismiss()
        }

        dlg.show()
    }

    fun setOnOKClickedListener(listener : (String) -> Unit){
    }

    interface  AddFriendDialogOKClickedListener{
        fun onOKClicked(content : String)
    }
}