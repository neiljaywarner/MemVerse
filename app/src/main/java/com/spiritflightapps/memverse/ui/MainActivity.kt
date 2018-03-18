package com.spiritflightapps.memverse.ui


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.model.Memverse
import com.spiritflightapps.memverse.model.MemverseResponse
import com.spiritflightapps.memverse.network.MemverseApi
import com.spiritflightapps.memverse.network.ServiceGenerator
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: maybe Save bearer token, but they never expire, so get a strategy, and get encryption
        // TODO: Spinner
        makeGetMemversesNetworkCall()
    }

    lateinit var memverses: List<Memverse>
    var currentVerseIndex = 0

    private fun updateUi(memverseResponse: MemverseResponse) {
        title = "${memverseResponse.count} verses"
        // TODO: in March Andy will make ti so you don't have to pull down pending in the network feed which would be fantastic.
        // TODO: Fix the sort date, i don't think it's quite right
        memverses = memverseResponse.verses.sortedWith(compareBy(Memverse::status, Memverse::nextTestDate))
        // TODO: Let them practice...make it a text below they can hide/show when stuck
        updateVerseUi(memverses.first())


    }

    fun updateVerseUi(memverse: Memverse) {
        with(memverse) {
            edit_verse_text.setText(verse.text)
            text_reference.text = ref
        }
    }

    // TODO: Change to page number later to support those with > 100 verses?
    private fun makeGetMemversesNetworkCall() {
        Log.d(TAG, "***** makeGetMemversesNetworkCall")

        // TODO: Handle auth token in a better way
        val memVersesApi = ServiceGenerator.createPasswordAuthService(MemverseApi::class.java)

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



    companion object {

        private val TAG = MainActivity::class.java.simpleName
    }
    // TODO: Pick kotlin secure sharedprefs to encrypt token
    // ** it never expires

}

// Note:
// iOS basic getMemverses is https://github.com/avitus/Memverse_iOS/blob/master/Memverse_iOS/MemorizeViewController.swift
// wit realm local db fetch before today's date and pending
