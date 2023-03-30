package com.example.chattingapp

import java.io.Serializable


data class MessageData (val message : String ="", // 주고받은 메세지
                        val name : String = "",  // 사용자의 이름
                        val email : String= "",  // 사용자 이메일
                        val date : String=""// 시간 정보
                        ) : Serializable {
   var id: String = ""
}