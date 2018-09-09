package com.spiritflightapps.memverse.network


data class RegisterUserRequest(val user: RegisterUser)

data class RegisterUser(
        val name: String = "",
        val email: String = "",
        val password: String = ""
)

// tell him later that the swagger doc needs updated