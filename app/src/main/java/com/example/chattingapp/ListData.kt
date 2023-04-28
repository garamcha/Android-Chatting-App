package com.example.chattingapp

import java.io.Serializable
// 채팅 목록 데이터 클래스
data class ListData(var name : String? = "",
                    var last_chat : String? = "",
                    var last_time : String? = "",
                    var img : String? = "",
                    var email : String? = "") : Serializable