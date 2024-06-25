package com.example.ittalk.model

data class User(
    val name: String = "",
    val profilePic: String = "",
    val online: Boolean = false,
    val currentTopic: String = "",
    val callStatus: String = "",
    val incomingCall: IncomingCall? = IncomingCall()
)

data class IncomingCall(
    val isIncomingCall: Boolean = false,
    val callerId: String = "",
    val callerName: String = "",
    val callerProfilePic: String = ""
)

data class Users(
    val users: Map<String, User> = emptyMap()
)
