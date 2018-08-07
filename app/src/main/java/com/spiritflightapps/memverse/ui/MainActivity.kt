package com.spiritflightapps.memverse.ui


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.orhanobut.hawk.Hawk
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.model.Memverse
import com.spiritflightapps.memverse.model.MemverseResponse
import com.spiritflightapps.memverse.model.RatePerformanceResponse
import com.spiritflightapps.memverse.network.MemverseApi
import com.spiritflightapps.memverse.network.ServiceGenerator
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.share
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


const val ARG_CURRENT_VERSE = "arg_current_verse_index"

class MainActivity : AppCompatActivity() {

    // put in baseactivity or mainapplication
    private val mFirebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //TODO: Move to mainapplication
        Hawk.init(applicationContext).build()


        // TODO: maybe Save bearer token, but they never expire, so get a strategy, and get encryption
        // TODO: Spinner
        makeGetMemversesNetworkCall()



        button_next.setOnClickListener { gotoNextVerse() }

        button_prev.setOnClickListener { gotoPreviousVerse() }

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

        button1.setOnClickListener { rate("1") }
        button2.setOnClickListener { rate("2") }
        button3.setOnClickListener { rate("3") }
        button4.setOnClickListener { rate("4") }
        button5.setOnClickListener { rate("5") }


    }

    fun gotoNextVerse() {
        currentVerseIndex++
        updateUi()
    }

    fun gotoPreviousVerse() {
        currentVerseIndex--
        updateUi()
    }

    //note: must be 1-5
    fun rate(rating: String) {
        makeRateNetworkCall(currentVerse.id, rating)
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
                trackShare(currentVerse.ref)
                return true
            }
            R.id.menu_item_logout -> {
                logout()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun logout() {
        Hawk.put(ServiceGenerator.AUTH_TOKEN_PREFS_KEY, "")
        trackLogout()
        startActivity<LoginActivity>()
        //TOOO: organize this a little bit and make sure backstack behaves as expected.
        finish()
    }

    private fun trackLogout() {
        mFirebaseAnalytics.logEvent("logout", Bundle())
    }

    // TODO: see if this actually works and use google analytics instead of firebase if needed
    private fun trackShare(itemName: String) {
        val bundle = Bundle()
        // TODO: track share method with intent broadcast receiver.
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    private fun updateUi() {
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

    // TODO: Unit test to handle psalms 27:1 and some others...
    // TODO 2. work with this over time since this is part of the core experience
    // e.g put back the "word1 ...word2 "
    fun String.approximatelyStartsWith(subString: String): Boolean {
        val s1 = this.stripNonAlphaNumeric()
        val s2 = subString.stripNonAlphaNumeric()


        return s1.startsWith(s2, true)

    }

    fun String.approximatelyEquals(string2: String) = this.stripNonAlphaNumeric() == string2.stripNonAlphaNumeric()

    fun String.stripNonAlphaNumeric(): String {
        return this.filter { it.isLetterOrDigit() }
    }


    // TODO: Later maybe do hte "rigthword1 ... rightword2" that the website does
    // could even look at iOS code to determine the regex or the logic...
    private fun setupLiveFeedback() {
        // TODO: Refine this; the one online is friendly to semicolons vs periods adn some other stuff
        //make it output according to the verse but count correct/not acording to friendly part...
        //TODO: Unit test, for example with psalms 21
        edit_verse_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val enteredString = edit_verse_text.text.toString()
                if (currentVerse.verse.text.approximatelyStartsWith(enteredString)) {
                    // maybe have it find the last character
                    text_verse_text.text = enteredString
                    // TOD: Clean up a bit more where it shows the complete text...
                }

                if (currentVerse.verse.text.approximatelyEquals(enteredString)) {
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

    // TODO: Only show stuff up to this date, dont' show pending, etc.
    private fun updateUi(memverseResponse: MemverseResponse) {
        // TODO: Fix count to say something like _ of _ for the number w/o pending, etc
        // TODO: in March Andy will make ti so you don't have to pull down pending in the network feed which would be fantastic.
        // TODO: Fix the sort date, i don't think it's quite right
        memverses = memverseResponse.verses.sortedWith(compareBy(Memverse::status, Memverse::nextTestDate))
        // TODO: Let them practice...make it a text below they can hide/show when stuck
        if (memverses.isNotEmpty()) {
            updateVerseUi()
            updateButtonUi()
            setupLiveFeedback()
        } else {
            text_reference.text = "No verses found; please make sure you have a valid network connection and verses added from www.memverse.com"
        }

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

    private fun makeRateNetworkCall(verseId: String, rating: String) {
        Log.d(TAG, "***** makeGetMemversesNetworkCall")

        // TODO: Handle auth token in a better way
        // reuse client, use insertKoin...
        val memVersesApi = ServiceGenerator.createPasswordAuthService(MemverseApi::class.java)

        val memversesCall = memVersesApi.ratePerformance(verseId, rating)

        memversesCall.enqueue(object : Callback<RatePerformanceResponse> {
            override fun onResponse(call: Call<RatePerformanceResponse>, response: Response<RatePerformanceResponse>) {
                Log.d(TAG, "memversesCall:Response code: " + response.code())
                if (response.isSuccessful) {
                    val myRatingResponse = response.body()

                    if (myRatingResponse == null) {
                        Log.e(TAG, "Rate performance response is null, which probably tells us nothing.")
                    } else {
                        Log.d("MV-RatePerf", "myRatingResponse=${myRatingResponse.status};nextText=${myRatingResponse.next_test}")
                        onRatePerformanceNetworkCallSuccess(myRatingResponse)
                        // next button
                    }
                } else {
                    //TODO: Could check other response codes or if have network connection
                    Toast.makeText(this@MainActivity, "sorry, something went wrong with rating network call ", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "response code = ${response.code()}")
                    showNetworkErrorToast()
                }
            }

            override fun onFailure(call: Call<RatePerformanceResponse>, t: Throwable) {
                Log.e(TAG, "ratePerormance Failure:${call.request()}${t.message}")
                showNetworkErrorToast()

            }
        })

    }

    private fun onRatePerformanceNetworkCallSuccess(myRatingResponse: RatePerformanceResponse) {
        //todo: Logcat the nextverse, or even show user "you'll be asked again in x number of days"
        gotoNextVerse()
    }

    private fun makeDeleteVerseNetworkCall(verseId: String) {
        Log.d(TAG, "***** makeDeleteVersesNetworkCall")

        // TODO: Handle auth token in a better way
        // reuse client, use insertKoin...
        val memVersesApi = ServiceGenerator.createPasswordAuthService(MemverseApi::class.java)

        val memversesCall = memVersesApi.deleteVerse(verseId)

        memversesCall.enqueue(object : Callback<RatePerformanceResponse> {
            override fun onResponse(call: Call<RatePerformanceResponse>, response: Response<RatePerformanceResponse>) {
                Log.d(TAG, "memversesCall:Response code: " + response.code())
                if (response.isSuccessful) {
                    val myRatingResponse = response.body()

                    if (myRatingResponse == null) {
                        Log.e(TAG, "Rate performance response is null, which probably tells us nothing.")
                    } else {
                        Log.d("MV-RatePerf", "myRatingResponse=${myRatingResponse.status};nextText=${myRatingResponse.next_test}")
                        onRatePerformanceNetworkCallSuccess(myRatingResponse)
                        // next button
                    }
                } else {
                    //TODO: Could check other response codes or if have network connection
                    Toast.makeText(this@MainActivity, "sorry, something went wrong with rating network call ", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "response code = ${response.code()}")
                    showNetworkErrorToast()
                }
            }

            override fun onFailure(call: Call<RatePerformanceResponse>, t: Throwable) {
                Log.e(TAG, "ratePerormance Failure:${call.request()}${t.message}")
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


}

// Note:
// iOS basic getMemverses is https://github.com/avitus/Memverse_iOS/blob/master/Memverse_iOS/MemorizeViewController.swift
// wit realm local db fetch before today's date and pending
