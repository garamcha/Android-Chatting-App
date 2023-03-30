package com.example.chattingapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

// 프로필 정보를 저장하는 데이터 클래스

data class ProfileData(//var img : Int,  // 프로필 이미지
                       var name : String? = null,
                       var email : String? = null
) : Serializable // 사용자 이름

