package com.spiritflightapps.memverse.ui


import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.model.Memverse
import com.spiritflightapps.memverse.model.MemverseResponse
import com.spiritflightapps.memverse.network.MemverseApi
import com.spiritflightapps.memverse.network.ServiceGenerator
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.share
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


const val ARG_CURRENT_VERSE = "arg_current_verse_index"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: maybe Save bearer token, but they never expire, so get a strategy, and get encryption
        // TODO: Spinner
        makeGetMemversesNetworkCall()

        setupLiveFeedback()


        button_next.setOnClickListener {
            currentVerseIndex++
            updateUi()
        }

        button_prev.setOnClickListener {
            currentVerseIndex--
            updateUi()
        }

        // TODO: translation; we do have users in other parts of the world.
        button_show.setOnClickListener {
            if (button_show.text == "Show") {
                text_verse_text.text = currentVerse.verse.text
                button_show.text = "Hide"
            } else {
                text_verse_text.text = ""
                button_show.text = "Show"
            }

        }

    }

    override
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate menu resource file.
        menuInflater.inflate(R.menu.menu_main, menu)


        // Return true to display menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.menu_item_share -> {
                share(currentVerse.toDisplayString())
                return true
            }
            R.id.menu_item_logout -> {
                logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val sharedPreferences: SharedPreferences by lazy { defaultSharedPreferences }

    fun logout() {
        // TODO: Delete token from sharedprefs
        sharedPreferences.edit().remove(ServiceGenerator.AUTH_TOKEN_PREFS_KEY).apply()
    }

    fun updateUi() {
        edit_verse_text.setText("")
        edit_verse_text.hint = ""
        text_verse_text.text = ""
        button_show.text = "Show"
        updateVerseUi()
        updateButtonUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_CURRENT_VERSE, currentVerseIndex)
        // TODO: Finish this part, parcelize and restore rotation ability.. but who would use this app in landscape? Maybe no one..
        // still, handling app death would be nice
        super.onSaveInstanceState(outState)
    }

    private fun setupLiveFeedback() {
        // TODO: Refine this; the one online is friendly to semicolons vs periods adn some other stuff
        edit_verse_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val enteredString = edit_verse_text.text.toString()
                if (currentVerse.verse.text.startsWith(enteredString)) {
                    text_verse_text.text = enteredString
                }

                if (currentVerse.verse.text.trim() == enteredString.trim()) {
                    title = "Correct! Good job"
                    Toast.makeText(this@MainActivity, "Correct, good job! ", Toast.LENGTH_LONG).show()

                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }

        })
        // TODO: Kotlin magic to the above
    }

    lateinit var memverses: List<Memverse>
    private var currentVerseIndex = 0
    val currentVerse: Memverse
        get() = memverses[currentVerseIndex]

    private fun updateUi(memverseResponse: MemverseResponse) {
        // TODO: Fix count to say something like _ of _ for the number w/o pending, etc
        // TODO: in March Andy will make ti so you don't have to pull down pending in the network feed which would be fantastic.
        // TODO: Fix the sort date, i don't think it's quite right
        memverses = memverseResponse.verses.sortedWith(compareBy(Memverse::status, Memverse::nextTestDate))
        // TODO: Let them practice...make it a text below they can hide/show when stuck
        updateVerseUi()
        updateButtonUi()


    }

    private fun updateButtonUi() {
        Log.d("NJWMV", "currentIndex=$currentVerseIndex; lastIndex=${memverses.lastIndex}")
        if (currentVerseIndex == memverses.lastIndex - 1) {
            button_next.visibility = View.INVISIBLE
        } else {
            button_next.visibility = View.VISIBLE
        }

        if (currentVerseIndex == 0) {
            button_prev.visibility = View.INVISIBLE
        } else {
            button_prev.visibility = View.VISIBLE
        }
    }

    private fun updateVerseUi() = try {
        with(currentVerse) {
            text_reference.text = ref
            title = "$ref ($status)"
        }
    } catch (e: Exception) {
        // TODO: Log to analytics so we know how often?
        showNoVersesToast()
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
                } else {
                    //TODO: Could check other response codes or if have network connection
                    Toast.makeText(this@MainActivity, "sorry, something went wrong with network call ", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "response code = ${response.code()}")
                    showNetworkErrorToast()
                }
            }

            override fun onFailure(call: Call<MemverseResponse>, t: Throwable) {
                Log.e(TAG, "memversesCall Failure:${call.request()}${t.message}")
                showNetworkErrorToast()

            }
        })

    }

    fun showNetworkErrorToast() =
            Toast.makeText(this, "sorry, something went wrong with network call ", Toast.LENGTH_LONG).show()

    private fun showNoVersesToast() =
            Toast.makeText(this, "Please go to memverse.com and add verses. thanks!", Toast.LENGTH_LONG).show()

    companion object {

        private val TAG = MainActivity::class.java.simpleName
    }
    // TODO: Pick kotlin secure sharedprefs to encrypt token
    // ** it never expires

}

// Note:
// iOS basic getMemverses is https://github.com/avitus/Memverse_iOS/blob/master/Memverse_iOS/MemorizeViewController.swift
// wit realm local db fetch before today's date and pending
