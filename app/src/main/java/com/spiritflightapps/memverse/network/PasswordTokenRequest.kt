package com.spiritflightapps.memverse.network

import com.spiritflightapps.memverse.BuildConfig


data class PasswordTokenRequest(
        val grant_type: String = "password",
        val username: String = "",
        val password: String = "",
        val client_id: String = BuildConfig.NJW_MV_API_KEY

)

fun getSmallAccountPasswordTokenRequest() =
        PasswordTokenRequest(username = BuildConfig.NJW_MV_SMALL_ACCOUNT_ID,
                password = BuildConfig.NJW_MV_SMALL_ACCOUNT_PW)
