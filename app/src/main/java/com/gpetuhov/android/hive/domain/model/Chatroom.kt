package com.gpetuhov.android.hive.domain.model

import com.gpetuhov.android.hive.util.getDateTimeFromTimestamp

data class Chatroom (
    var chatroomUid: String,
    var userUid1: String,
    var userUid2: String,
    var userName1: String,
    var userName2: String,
    var secondUserUid: String,
    var secondUserName: String,
    var lastMessageText: String,
    var lastMessageTimestamp: Long
) {
    fun getLastMessageTime() = getDateTimeFromTimestamp(lastMessageTimestamp)
}