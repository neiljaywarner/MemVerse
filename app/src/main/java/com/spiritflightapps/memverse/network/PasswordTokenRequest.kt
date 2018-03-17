package com.spiritflightapps.memverse.network


data class PasswordTokenRequest(
        val grant_type: String = "password",
        val username: String = "BuildConfig.NJW_MV_SMALL_ACCOUNT_ID",
        val password: String = "BuildConfig.NJW_MV_SMALL_ACCOUNT_PW",
        val client_id: String = "BuildConfig.NJW_MV_API_KEY"

// *** TODO: REMOVE THE QUOTATION MARKS WHEN SETUP IS COMPLETE
)
