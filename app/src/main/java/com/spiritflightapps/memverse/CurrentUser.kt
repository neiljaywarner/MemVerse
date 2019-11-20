package com.spiritflightapps.memverse

import com.spiritflightapps.memverse.model.Memverse
import com.spiritflightapps.memverse.model.MemverseResponse
import com.spiritflightapps.memverse.network.ServiceGenerator

object CurrentUser {
    val isLoggedIn
        get() = ServiceGenerator.hasPasswordAuthToken()

    // TODO: FIXME/use room, etc
    var memverses: MutableList<Memverse> = mutableListOf()
}