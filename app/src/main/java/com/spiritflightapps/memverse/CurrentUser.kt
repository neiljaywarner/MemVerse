package com.spiritflightapps.memverse

import com.spiritflightapps.memverse.network.ServiceGenerator

object CurrentUser {
    val isLoggedIn
        get() = ServiceGenerator.hasPasswordAuthToken()
}