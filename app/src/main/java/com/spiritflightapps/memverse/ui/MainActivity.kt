package com.spiritflightapps.memverse.ui


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.model.Memverse
import com.spiritflightapps.memverse.model.MemverseResponse
import com.spiritflightapps.memverse.network.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // TODO: Save bearer token, but they never expire, so get a strategy, and get encryption
        retrieveBearerToken()
        // TODO: Spinner

    }

    //private void updateUi(
    private fun updateUi(memverseResponse: MemverseResponse) {
        title = "${memverseResponse.count} verses"
        val memVerse: Memverse = memverseResponse.verses.first { it.status != "Pending" }

        // TODO: Let them practice...make it a text below they can hide/show when stuck
        with(memVerse) {
            edit_verse_text.setText(memVerse.verse.text)
            text_reference.text = ref
        }


    }

    private fun makeGetMemversesNetworkCall(authToken: String) {
        Log.d(TAG, "***** makeGetMemversesNetworkCall")

        // TODO: Handle auth token in a better way
        val memVersesApi = ServiceGenerator.createService(MemverseApi::class.java, authToken)

        val memversesCall = memVersesApi.fetchMemverses()

        memversesCall.enqueue(object : Callback<MemverseResponse> {
            override fun onResponse(call: Call<MemverseResponse>, response: Response<MemverseResponse>) {
                Log.d(TAG, "memversesCall:Response code: " + response.code())
                if (response.isSuccessful) {
                    val myVersesResponse = response.body()

                    if (myVersesResponse == null) {
                        Log.e(TAG, "memverse network response is null")
                    } else {
                        updateUi(myVersesResponse)
                    }
                    //updateList(tweets.getTweetList());
                } else {
                    //TODO: Could check other response codes or if have network connection
                    Toast.makeText(this@MainActivity, "sorry, something went wrong with network call ", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "response code = ${response.code()}")
                    showNetworkErrorToast()
                }
            }

            override fun onFailure(call: Call<MemverseResponse>, t: Throwable) {
                Log.e(TAG, "tweetsCall Failure:${call.request()}${t.message}")
                showNetworkErrorToast()

            }
        })

    }

    fun showNetworkErrorToast() =
            Toast.makeText(this, "sorry, something went wrong with network call ", Toast.LENGTH_LONG).show()

    private fun retrieveBearerToken() {
        Log.d(TAG, "*** Retrieving bearer token.")
        val twitterApi = ServiceGenerator.createBearerKeyService(
                TwitterAuthUtils.generateEncodedBearerTokenCredentials())

        val passwordTokenRequest = PasswordTokenRequest()
        val bearerTokenCall = twitterApi.getBearerToken(passwordTokenRequest)
        // TODO: Consider https://auth0.com/docs/api-auth/grant/authorization-code-pkce

        // also consider https://github.com/openid/AppAuth-Android
        // also consider https://github.com/auth0/Auth0.Android


        bearerTokenCall.enqueue(object : Callback<BearerTokenResponse> {
            override fun onResponse(call: Call<BearerTokenResponse>, response: Response<BearerTokenResponse>) {
                Log.d(TAG, "bearerTokenCall:Response code: ${response.code()}")
                if (response.code() == 200) {
                    val bearerTokenResponse = response.body()

                    Log.d(TAG, "bearerTokenCall:token_type=" + bearerTokenResponse!!.tokenType)
                    makeGetMemversesNetworkCall(bearerTokenResponse.accessToken)


                } else {
                    Log.e(TAG, "Response invalid, check consumer key/secret combination if 403")
                }


            }

            override fun onFailure(call: Call<BearerTokenResponse>, t: Throwable) {
                Log.e(TAG, "bearerTokenCall Failure:${call.request()} ${t.message}")
            }
        })
    }

    companion object {

        private val TAG = MainActivity::class.java.simpleName
    }
    // TODO: Pick kotlin secure sharedprefs to encrypt token
    // ** it never expires

}

// Note:
// iOS basic getMemverses is https://github.com/avitus/Memverse_iOS/blob/master/Memverse_iOS/MemorizeViewController.swift
// wit realm local db fetch before today's date and pending
