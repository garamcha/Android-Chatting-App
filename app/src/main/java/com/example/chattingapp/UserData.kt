package com.example.chattingapp

// 사용자의 정보를 저장하기 위한 데이터 클래스
data class UserData(var userName : String? = null,  // 사용자 이름
                    var userId : String? = null,    // 사용자 로그인 ID
                    var uid : String? = null,       // 사용자 UID
                    var phoneNo : String? = null )  // 사용자 핸드폰 번호
