package com.spiritflightapps.memverse.ui


import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.orhanobut.hawk.Hawk
import com.spiritflightapps.memverse.BuildConfig
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.model.Memverse
import com.spiritflightapps.memverse.model.MemverseResponse
import com.spiritflightapps.memverse.model.RatePerformanceResponse
import com.spiritflightapps.memverse.network.MemverseApi
import com.spiritflightapps.memverse.network.ServiceGenerator
import com.spiritflightapps.memverse.utils.Analytics
import com.spiritflightapps.memverse.utils.Prefs
import com.spiritflightapps.memverse.utils.RATINGS_INFO_TEXT
import io.doorbell.android.Doorbell
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


const val ARG_CURRENT_VERSE = "arg_current_verse_index"

class MainActivity : AppCompatActivity() {

    // put in baseactivity or mainapplication
    private val mFirebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }
    private val userAlreadyAcknowledgeRatingsHelp by lazy { Prefs.getFromPrefs(this, Prefs.RATINGS_HELP_DIALOG_ACKNOWLEDGED, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //TODO: Move to mainapplication
        Hawk.init(applicationContext).build()

        Log.e("NJW", "useralreadyack=$userAlreadyAcknowledgeRatingsHelp")

        showRatingButtonsIfNeeded()

        button_next.setOnClickListener {
            showRatingsInfoDialogIfNeeded()
            trackNext()
            gotoNextVerse()
        }

        button_prev.setOnClickListener {
            trackPrevious()
            gotoPreviousVerse()
        }

        button_show.setOnClickListener { onShowClicked() }

        button_hide.setOnClickListener { onHideClicked() }

        button1.setOnClickListener { rate("1") }
        button2.setOnClickListener { rate("2") }
        button3.setOnClickListener { rate("3") }
        button4.setOnClickListener { rate("4") }
        button5.setOnClickListener { rate("5") }


    }

    private fun showRatingButtonsIfNeeded() {
        if (userAlreadyAcknowledgeRatingsHelp) {
            viewGroupRatings.visibility = View.VISIBLE
        } else {
            viewGroupRatings.visibility = View.GONE
        }
    }

    private fun showRatingsInfoDialogIfNeeded() {
        if (userAlreadyAcknowledgeRatingsHelp) {
            return
        }

        trackRatingsHelpDisplayed()
        // TODO: Ths text be fun to do through firebase remote config one day
        alert(RATINGS_INFO_TEXT, "Ratings Help") {
            okButton { onRatingsHelpAcknowledged() }
        }.show()
    }

    fun onRatingsHelpAcknowledged() {
        viewGroupRatings.visibility = View.VISIBLE
        Prefs.saveToPrefs(this, Prefs.RATINGS_HELP_DIALOG_ACKNOWLEDGED, true)
    }

    private fun onShowClicked() {
        trackShowClicked()
        text_verse_hint.text = currentVerse.verse.text
        button_hide.visibility = View.VISIBLE
        button_show.visibility = View.INVISIBLE
    }

    private fun onHideClicked() {
        trackHideClicked()
        text_verse_hint.text = ""
        button_hide.visibility = View.INVISIBLE
        button_show.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        makeGetMemversesNetworkCall()

    }

    fun gotoNextVerse() {
        currentVerseIndex++
        updateUi()
    }

    fun gotoPreviousVerse() {
        trackPrevious()
        currentVerseIndex--
        updateUi()
    }

    //note: must be 1-5
    fun rate(rating: String) {
        Log.d(TAG, "MV-> Rating $rating")
        // TODO: could move rating to success/failure dep network success...
        trackRate(currentVerse.ref, rating)
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
        var returnValue = true
        when (item.itemId) {
            R.id.menu_item_share -> {
                share(currentVerse.toDisplayString())
                trackShare(currentVerse.ref)
            }
            R.id.menu_item_logout -> logout()
            R.id.menu_item_feedback -> {
                trackFeedbackOptionSelected()
                showFeedbackDialog()
            }
            R.id.menu_verse_add -> onAddMenuOptionSelected()
            R.id.menu_verse_delete -> onDeleteMenuOptionSelected()
            else -> returnValue = super.onOptionsItemSelected(item)
        }
        return returnValue
    }

    private fun onDeleteMenuOptionSelected() {
        trackDeleteOptionSelected()
        alert("Are you sure you want to remove ${currentVerse.ref} from your list?", "Remove this verse?") {
            yesButton { onDeleteYesSelected() }
            noButton { onDeleteNoSelected() }
        }.show()
    }

    private fun onAddMenuOptionSelected() = startActivity(intentFor<AddVerseActivity>())

    private fun onDeleteYesSelected() {
        trackDeleteYesSelected()
        makeDeleteVerseNetworkCall()
    }

    private fun onDeleteNoSelected() {
        trackDeleteNoSelected()
    }

    private fun trackRatingsHelpDisplayed() {
        mFirebaseAnalytics.logEvent("rating_help_displayed", Bundle())
    }

    private fun trackDeleteYesSelected() {
        mFirebaseAnalytics.logEvent("delete_verse_yes_selected", Bundle())
    }

    private fun trackDeleteNoSelected() {
        mFirebaseAnalytics.logEvent("delete_verse_no_selected", Bundle())
    }

    private fun trackDeleteOptionSelected() {
        mFirebaseAnalytics.logEvent("delete_option_selected", Bundle())
    }

    private fun trackFeedbackOptionSelected() {
        mFirebaseAnalytics.logEvent("feedback_option_selected", Bundle())
    }

    private fun showFeedbackDialog() {
        val appId = 9502 // Replace with your application's ID
        val apiKey = BuildConfig.DOORBELL_IO_API_KEY // Replace with your application's API key
        val doorbellDialog = Doorbell(this, appId.toLong(), apiKey) // Create the Doorbell object

        val email = Prefs.getFromPrefs(applicationContext, Prefs.EMAIL, "")
        doorbellDialog.setEmail(email)
        doorbellDialog.addProperty("loggedIn", true) // Optionally add some properties


        // Callback for when a message is successfully sent
        doorbellDialog.setOnFeedbackSentCallback { message ->
            // Show the message in a different way, or use your own message!
            Log.d("MV", "Doorbell.io Feedback sent $message")
            trackDoorbellFeedbackSent()
        }

        // Callback for when the dialog is shown
        doorbellDialog.setOnShowCallback {
            Log.d("MV", "Doorbell dialog shown")
            trackDoorbellDialogShown()
        }

        // Capture a screenshot of the current activity, to be sent to Doorbell
        doorbellDialog.captureScreenshot()

        doorbellDialog.show()
    }

    fun logout() {
        Hawk.put(ServiceGenerator.AUTH_TOKEN_PREFS_KEY, "")
        trackLogout()
        startActivity<LoginActivity>()
        //TOOO: organize this a little bit and make sure backstack behaves as expected.
        finish()
    }

    private fun trackShowClicked() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, currentVerse.ref)
        }
        mFirebaseAnalytics.logEvent("show", bundle)
    }

    private fun trackDoorbellDialogShown() {
        mFirebaseAnalytics.logEvent("doorbell_dialog_shown", Bundle())
    }

    private fun trackDeleteNetworkCallSuccess() {
        mFirebaseAnalytics.logEvent("delete_network_call_success", Bundle())
    }

    private fun trackDoorbellFeedbackSent() {
        mFirebaseAnalytics.logEvent("doorbell_feedback_sent", Bundle())
    }

    private fun trackHideClicked() {
        mFirebaseAnalytics.logEvent("hide", Bundle())
    }

    private fun trackLogout() {
        mFirebaseAnalytics.logEvent("logout", Bundle())
    }

    private fun trackNext() {
        mFirebaseAnalytics.logEvent("next", Bundle())
    }

    private fun trackFetchRetry() {
        mFirebaseAnalytics.logEvent("fetch_retry", Bundle())
    }

    private fun trackNoMoreRetries() {
        mFirebaseAnalytics.logEvent("fetch_no_more_retries", Bundle())
    }

    private fun trackPrevious() {
        mFirebaseAnalytics.logEvent("previous", Bundle())
    }

    private fun trackRatedLast() {
        //TODO: Record if they finished a session by rating everything due that day...
        mFirebaseAnalytics.logEvent("rated_last", Bundle())
    }

    // TODO: see if this actually works and use google analytics instead of firebase if needed
    // eventuallt track whay they shared with ...
    private fun trackShare(verseRef: String) {
        val bundle = Bundle()
        // TODO: track share method with intent broadcast receiver.
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, verseRef)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    private fun trackRate(itemName: String, rating: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        bundle.putString(FirebaseAnalytics.Param.LEVEL, rating)
        mFirebaseAnalytics.logEvent("rate", bundle)
    }

    private fun updateUi() {
        edit_verse_text.setText("")
        edit_verse_text.hint = ""
        text_verse_live_feedback.text = ""
        button_show.visibility = View.VISIBLE
        button_hide.visibility = View.INVISIBLE
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

    fun String.approximatelyEquals(string2: String) = this.stripNonAlphaNumeric().toLowerCase() == string2.stripNonAlphaNumeric().toLowerCase()

    private fun String.stripNonAlphaNumeric(): String {
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
                    text_verse_live_feedback.text = enteredString
                    // TOD: Clean up a bit more where it shows the complete text...
                }

                if (currentVerse.verse.text.approximatelyEquals(enteredString)) {
                    onVerseCorrect()
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

    private fun onVerseCorrect() {
        Analytics.trackEvent(Analytics.LEARNING_VERSE_CORRECT, currentVerse.ref)
        title = "Correct! Good job"
        Toast.makeText(this@MainActivity, "Correct, good job! ", Toast.LENGTH_LONG).show()
        viewGroupRatings.visibility = View.VISIBLE
        showRatingsInfoDialogIfNeeded()
    }

    lateinit var memverses: List<Memverse>
    private var currentVerseIndex = 0
    val currentVerse: Memverse
        get() = memverses[currentVerseIndex]

    // TODO: Only show stuff up to this date, dont' show pending, etc.
    private fun updateUi(memverseResponse: MemverseResponse) {
        // TODO: in March Andy will make ti so you don't have to pull down pending in the network feed which would be fantastic.
        // TODO: Fix the sort date, i don't think it's quite right]
        // todo: make it by default notquiz u for future dates
        val versesNotPending = memverseResponse.verses.filterNot { it -> it.status == "Pending" }
        memverses = versesNotPending.sortedWith(compareBy(Memverse::status, Memverse::nextTestDate))

        if (memverses.isNotEmpty()) {
            updateVerseUi()
            updateButtonUi()
            setupLiveFeedback()
        } else {
            Log.d("NJWMV", "no active verses; ask them to add one.")
            alert("You have no active verses; please add a verse") { okButton { startActivity<AddVerseActivity>() } }.show()
        }

    }

    private fun updateButtonUi() {
        Log.d("NJWMV", "currentIndex=$currentVerseIndex; lastIndex=${memverses.lastIndex}")
        if (currentVerseIndex == memverses.lastIndex) {
            button_next.visibility = View.INVISIBLE
        } else {
            button_next.visibility = View.VISIBLE
        }

        if (currentVerseIndex == 0) {
            button_prev.visibility = View.INVISIBLE
        } else {
            button_prev.visibility = View.VISIBLE
        }

        text_verse_hint.text = ""
    }

    private fun updateVerseUi() = try {
        with(currentVerse) {
            val verseNum = currentVerseIndex + 1
            text_reference.text = ref
            title = "Memverse $verseNum/${memverses.size}"
            // TODO: later maybe put the memorized/learning part.
        }
    } catch (e: Exception) {
        // TODO: Log to analytics so we know how often?
        showNoVersesToast()
    }


    // TODO: Change to page number later to support those with > 100 verses?
    private fun makeGetMemversesNetworkCall() {
        Log.d(TAG, "***** makeGetMemversesNetworkCall")
        val dialogFetch = indeterminateProgressDialog(message = "Please wait a bit…", title = "Fetching verses")

        // TODO: Handle auth token in a better way
        val memVersesApi = ServiceGenerator.createService(MemverseApi::class.java)

        val memversesCall = memVersesApi.fetchMemverses()

        memversesCall.enqueue(object : Callback<MemverseResponse> {
            override fun onResponse(call: Call<MemverseResponse>, response: Response<MemverseResponse>) {
                dialogFetch.hide()
                Log.d(TAG, "memversesCall:Response code: " + response.code())
                if (response.isSuccessful) {
                    val myVersesResponse = response.body()

                    if (myVersesResponse == null) {
                        Log.e(TAG, "memverse network response is null")
                        Crashlytics.logException(Exception("Memverse Fetch response is null"))
                    } else {
                        updateUi(myVersesResponse)
                    }
                } else {
                    Crashlytics.logException(Exception("Fetch call network code ${response.code()} error ;url= ${call.request()}; "))
                    Log.e(TAG, "***response code = ${response.code()}")
                    onFetchNetworkError()
                }
            }

            override fun onFailure(call: Call<MemverseResponse>, t: Throwable) {
                dialogFetch.hide()
                // ** TODO Use Timber so that crashes in development don't get sent!!!
                val errorMessage = "***memverses fetch Call  Failure:${call.request()}${t.message}"
                Log.e(TAG, errorMessage)
                Crashlytics.logException(Exception(errorMessage))
                onFetchNetworkError()
            }
        })

    }

    private fun makeRateNetworkCall(verseId: String, rating: String) {
        Log.d(TAG, "MV***** makeGetMemversesNetworkCall")
        val dialog = indeterminateProgressDialog(message = "Please wait a bit…", title = "Rating verse")
        progress_spinner.visibility = View.VISIBLE
        // TODO: Spinner when making rate netowrk call.

        // TODO: Handle auth token in a better way
        // reuse client, use insertKoin...
        val memVersesApi = ServiceGenerator.createService(MemverseApi::class.java)

        val memversesCall = memVersesApi.ratePerformance(verseId, rating)

        memversesCall.enqueue(object : Callback<RatePerformanceResponse> {
            override fun onResponse(call: Call<RatePerformanceResponse>, response: Response<RatePerformanceResponse>) {
                progress_spinner.visibility = View.GONE
                dialog.hide()
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
                    // *** TODO: Report to crashlytics  specifically or something?
                    Log.e(TAG, "******response code = ${response.code()}")
                    Crashlytics.logException(Exception("Rate call error; network code ${response.code()}"))
                    showNetworkErrorToast()
                }
            }

            override fun onFailure(call: Call<RatePerformanceResponse>, t: Throwable) {
                Log.e(TAG, "ratePerformance Failure:${call.request()}${t.message}")
                Crashlytics.logException(Exception("Rate onFailure ${t.message}"))

                dialog.hide()
                progress_spinner.visibility = View.GONE
                onFetchNetworkError()

            }
        })

    }

    private var numFetchErrorsInARow = 0
    private fun onFetchNetworkError() {
        numFetchErrorsInARow++
        showNetworkErrorToast()
        if (numFetchErrorsInARow < 3) {
            trackFetchRetry()
            val handler = Handler()
            // just wait a half a second.
            handler.postDelayed({ makeGetMemversesNetworkCall() }, 500)
        } else {
            trackNoMoreRetries()
        }
    }

    private fun onRatePerformanceNetworkCallSuccess(myRatingResponse: RatePerformanceResponse) {
        //todo: Logcat the nextverse, or even show user "you'll be asked again in x number of days"
        if (currentVerseIndex == memverses.lastIndex) {
            toast("This was your last verse; please tap the left arrow or come back tomorrow :)")
            viewGroupRatings.visibility = View.INVISIBLE
            trackRatedLast()
            Log.d("MV", "Rate Network Call success- last verse")
        } else {
            Log.d("MV", "Rate Network Call success- verse $currentVerseIndex of ${memverses.lastIndex} - goto next")
            gotoNextVerse()
        }
    }

    private fun onDeleteNetworkCallSuccess(verseId: String) {
        toast("This verse has removed from your list.")
        markDeleted(verseId)
        trackDeleteNetworkCallSuccess()
        currentVerseIndex = 0
        makeGetMemversesNetworkCall()
    }

    private fun markDeleted(verseId: String) {
        val previousDeletedVerses: String = Prefs.getFromPrefs(this, Prefs.DELETED_VERSES_CSV, "")
        val deletedVerses = "$previousDeletedVerses,$verseId"
        Log.d("MV", "deletedVerses: $deletedVerses")
        Prefs.saveToPrefs(this, Prefs.DELETED_VERSES_CSV, deletedVerses)
    }

    private fun makeDeleteVerseNetworkCall() {
        Log.d(TAG, "***** makeDeleteVersesNetworkCall")

        val verseId = currentVerse.id
        // TODO: Handle auth token in a better way
        // reuse client, use insertKoin...
        val memVersesApi = ServiceGenerator.createService(MemverseApi::class.java)

        val memversesCall = memVersesApi.deleteVerse(verseId)

        memversesCall.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d(TAG, "memversesCall:Response code: " + response.code())
                if (response.isSuccessful) {
                    // a 204 no content on success is expected
                    if (response.code() == 204) {
                        onDeleteNetworkCallSuccess(verseId)
                    } else {
                        Log.e(TAG, "DeleteVersesNetworkCall response should be 204 for success")
                        Crashlytics.log("Error: Deleting caused a ${response.code()} when 204 no content was expected")
                    }
                } else {
                    //TODO: Could check other response codes or if have network connection
                    // a 404 for already deleted is expected.
                    Toast.makeText(this@MainActivity, "sorry, something went wrong with DeleteVersesNetworkCall  ", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "response code = ${response.code()}")
                    showNetworkErrorToast()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "DeleteVersesNetworkCall Failure:${call.request()}${t.message}")
                showNetworkErrorToast()

            }
        })

    }


    fun showNetworkErrorToast() =
            Toast.makeText(this, "Sorry,  something went wrong with network call; if it keeps happening please logout and log back in and try again ", Toast.LENGTH_LONG).show()

    private fun showNoVersesToast() =
            Toast.makeText(this, "Please go to memverse.com and add verses. thanks!", Toast.LENGTH_LONG).show()

    companion object {

        private val TAG = MainActivity::class.java.simpleName
    }


}

// Note:
// iOS basic getMemverses is https://github.com/avitus/Memverse_iOS/blob/master/Memverse_iOS/MemorizeViewController.swift
// wit realm local db fetch before today's date and pending
