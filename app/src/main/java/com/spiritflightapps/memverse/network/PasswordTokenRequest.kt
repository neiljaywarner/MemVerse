package com.spiritflightapps.memverse.network

import com.spiritflightapps.memverse.BuildConfig


data class PasswordTokenRequest(
        val grant_type: String = "password",
        val username: String = BuildConfig.NJW_MV_SMALL_ACCOUNT_ID,
        val password: String = BuildConfig.NJW_MV_SMALL_ACCOUNT_PW,
        val client_id: String = BuildConfig.NJW_MV_API_KEY

//Note: usign sam's password b/c he has only a few verses.
//testname, testpw
)
//TODO: dont' checkin ...