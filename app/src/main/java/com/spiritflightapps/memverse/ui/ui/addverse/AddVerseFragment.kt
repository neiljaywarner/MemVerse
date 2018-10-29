package com.spiritflightapps.memverse.ui.ui.addverse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.crashlytics.android.Crashlytics
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.model.Verse
import com.spiritflightapps.memverse.network.MemverseAddRequest
import com.spiritflightapps.memverse.network.MemverseApi
import com.spiritflightapps.memverse.network.ServiceGenerator
import com.spiritflightapps.memverse.ui.MainActivity
import com.spiritflightapps.memverse.utils.*
import kotlinx.android.synthetic.main.add_verse_fragment.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import retrofit2.HttpException


class AddVerseFragment : Fragment() {

    companion object {
        fun newInstance() = AddVerseFragment()
        const val TAG = "MV_AddVerseFragment"
    }

    val memVersesApi by lazy { ServiceGenerator.createDeferredService(MemverseApi::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.add_verse_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.title = getString(R.string.add_verse)
        handleIntent()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autocomplete_book.apply {
            val array = BOOKS_OF_BIBLE.split(",").toTypedArray()
            val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, array)
            setAdapter(adapter)
            setOnItemClickListener { _, _, _, _ -> edit_chapter.requestFocus() }
        }

        text_translation_abbreviation.setOnClickListener {
            val translations = TRANSLATIONS_ABBREVIATIONS_TEXT.split(",").toTypedArray().toList()

            it.context.selector("Please select a translation", translations) { _, i ->
                onTranslationSelected(translations[i])
            }
        }
        text_translation_abbreviation.text = Prefs.getFromPrefs(context, Prefs.LAST_TRANSLATION_SELECTED, "NIV")

        button_add.setOnClickListener { addVerse() }


    }

    private fun onTranslationSelected(translationDescription: String) {
        Analytics.trackEvent(Analytics.TRANSLATION_SELECTED, translationDescription)
        val abbreviation = getTranslationAbbreviation(translationDescription)
        text_translation_abbreviation.text = abbreviation
        Prefs.saveToPrefs(context, Prefs.LAST_TRANSLATION_SELECTED, abbreviation)
    }

    private fun getTranslationAbbreviation(translationDescription: String): String {
        return translationDescription.split("-")[1].trim()
    }

    private fun addVerse() {
        //TODO: validate first based on a) is it too many chap/verse, more than in the book James 20:200
        // b) does the user already have it in their list?
        val book = autocomplete_book.text.toString().trim()
        val chapter = edit_chapter.text.toString().trim()
        val verse = edit_verse.text.toString().trim()
        Log.d("MV-NJW-AdddVerse", "$book $chapter:$verse ${text_translation_abbreviation.text}")
        val ref = "$book $chapter:$verse"
        val translation = text_translation_abbreviation.text.toString()
        Analytics.trackEvent(Analytics.ADD_VERSE_CLICK, "$ref $translation")
        // *** TODO* Check to see if

        // TODO: Dn't let the user add a verse if they already have it in their list
        // they shludn't have to have a network call for that.
        // check via room..

        makeLookupVerseAsyncNetworkCall(text_translation_abbreviation.context, book.trim(), chapter, verse, translation)

    }

    lateinit var exactRef: String

    private fun makeLookupVerseAsyncNetworkCall(ctx: Context, book: String, chapter: String, verseNumber: String, translation: String) = launch(UI) {
        // TODO: Change from adding verse to just a refresh dialog
        Log.d("MV", "in makeLookupVerseAsyncNetworkCAll, about to show spinner ")
        val spinner = ctx.indeterminateProgressDialog(message = "Please wait a bit...", title = "Looking up verse")
        exactRef = "$book $chapter:$verseNumber $translation"
        spinner.show()

        try {
            Crashlytics.log("making api call to lookup $exactRef")
            val result = memVersesApi.lookupVerse(translation, book, chapter, verseNumber)

            val response = result.await()

            val verse = response.verse
            if (verse == null) {
                Log.e("NJW-MV", "verse is null; maybe wrong chapt/verse combination for $exactRef")
                throw(Exception("Verse is null; probably non-existent book/chap/verse $exactRef"))
                //onVerseLookupFail(Exception("Verse is null; probably non-existent book/chap/verse $book $chapter:$verseNumber"))
            } else {
                Log.d("MV-MV", "verseId=${verse.id}")
                onVerseLookupSuccess(verse)
            }

        } catch (httpException: HttpException) {
            with(httpException) {
                Log.e("NJW-MV", "httpcode=${code()} message=${message()}; ${response()}")
                val request = response().raw().request().url().toString()
                Crashlytics.log("url==$request")
                onVerseLookupFail(httpException)
            }
        } catch (e: Exception) {
            Log.e("NJWMV", "***in catch ${e.localizedMessage}")

            e.printStackTrace()
            onVerseLookupFail(e)
        } finally {
            spinner.hide()
        }
    }


    //TODO: Utilize failure responseBody with wrapper object
/*
{
"code": 0,
"message": "string"
}
 */
    //TODO: if get a bunch of "user fault" well, we could obviously disallow that b/c its easy to know w/o network call.
    // TODO: when we have last verse/chapter of every book we can eliminate invalid book/chapter/verse/combination
    private fun onVerseLookupFail(e: Exception) {
        Log.e("NJWMV-", "onVerseLookupFail ${e.localizedMessage}")
        Analytics.trackEvent(Analytics.ADD_VERSE_LOOKUP_FAIL)
        // we could change this to yes/no
        text_translation_abbreviation.context.alert("Verse lookup failed - please check and see if it is on memverse.com in this translation and if not please consider adding it - or is this an invalid combination like James 24:10") {
            positiveButton("Yes") { Analytics.trackEvent(Analytics.LOOKUP_VERSE_USER_FAULT) }
            negativeButton("No") {
                Analytics.trackEvent(Analytics.LOOKUP_VERSE_SERVER_FAULT, e.localizedMessage)
                //TODO: Improve this and see if it has a different response
                // if it's a 501 vs if it is just not found vs invalid book/chapter; it probably does.
                Crashlytics.log("User said 'no', it wasn't them: $exactRef")
                Crashlytics.logException(e)
            }
        }.show()


        // TODO: could probably use
        // api endpoint /final_verses one time to save last chapter and verse of books in the Bible
        // yes, it has 1189 entries, one for each chapter
        // so this would work.

        // TODO: we need add by ref for share in anyway.onVerseLookupFail
        // could run this endpoint the first time add verse screen is selected.
        // or ebetter yet, the first time the app is opened, but not necessarily with splash screen
        // never needs updated.
    }

    private fun onVerseLookupSuccess(verse: Verse) {
        Analytics.trackEvent(Analytics.ADD_VERSE_LOOKUP_SUCCESS, verse.ref)
        val context = text_translation_abbreviation.context
        context.alert(verse.text, "Add this verse?") {
            yesButton { onAddVerseYes(verse) }
            noButton { onAddVerseNo(verse) }
        }.show()
    }

    private fun onAddVerseYes(verse: Verse) {
        Log.d("MV_AV", "User chose yes, do add ${verse.ref}")
        Analytics.trackEvent(Analytics.ADD_VERSE_YES, verse)
        makeAddVerseAsyncNetworkCall(text_translation_abbreviation.context, verse)

    }

    // TODO: consider doing this to simplify network calls
// but instead o fe.handleException, make that resource error like earlier.
// https://proandroiddev.com/oversimplified-network-call-using-retrofit-livedata-kotlin-coroutines-and-dsl-512d08eadc16
    private fun makeAddVerseAsyncNetworkCall(ctx: Context, verse: Verse) = launch(UI) {
        // TODO: Use regular progress bars in the fragment - they won't leak causing weird behaviour!!!
        val spinner = ctx.indeterminateProgressDialog(message = "Please wait a bit…", title = "Adding ${verse.ref}")
        spinner.show()

        /// status says 201 creatd..
        // eg 2018-09-07 01:58:22.417 8533-8582/com.spiritflightapps.memverse.debug D/OkHttp: {"response":{"id":2168610,"user_id":1354,"efactor":"2.0","test_interval":1,"rep_n":1,"next_test":"2018-09-06","status":"Pending","prev_verse":null,"ref_interval":1,"next_ref_test":null,"passage_id":554204,"subsection":null,"ref":"Gen 1:22","verse":{"id":2063,"book":"Genesis","chapter":1,"versenum":22,"translation":"NIV","text":"God blessed them and said, \"Be fruitful and increase in number and fill the water in the seas, and let the birds increase on the earth.\"","book_index":1}}}

        val result = memVersesApi.addVerse(MemverseAddRequest(verse.id.toString()))


        try {
            val response = result.await()
            Log.d("MV-NJW***", "Added verse as ${response.newMemverse}")

            addVerseSuccess(response.newMemverse.status)
        } catch (httpException: HttpException) {
            Log.e("NJW-MV***", " ${httpException.code()}; ${httpException.message()}")
            if (httpException.code() == 400) {
                Log.e("NJW-MV***", "400 error, maybe already in list")
                Analytics.trackEvent(Analytics.ADD_VERSE_FAIL_400_MAYBE_IN_LIST)
                ctx.alert("We were unable to add the verse - this might be a verse already in your list") {
                    okButton { }
                }.show()
            } else {
                Log.e("NJW-MV***", "not 400")
                onAddVerseFail(httpException)
            }
        } catch (e: Exception) {
            //TODO: utilize body and response code (?)
            /*
            {
    "error": "not_found",
    "error_description": "The requested resource could not be found."
    }
             */
            onAddVerseFail(e)
        } finally {
            spinner.hide()
        }

    }

    private var isFromYouVersion: Boolean = false

    private fun addVerseSuccess(status: String) {
        if (status.equals("pending", ignoreCase = true)) {
            Analytics.trackEvent(Analytics.ADD_VERSE_TO_PENDING)
        } else {
            Analytics.trackEvent(Analytics.ADD_VERSE_TO_LEARNING)
        }
        text_translation_abbreviation.context.alert("Verse added to $status") {
            okButton {
                if (isFromYouVersion) {
                    context?.startActivity<MainActivity>()
                }
                activity?.finish()
            }
        }.show()

    }
// don't worry about checkign if they already hae the verse b/c soon it will be in db.
// and not many people have that many

    private fun onAddVerseFail(e: Exception) {
        Log.e("NJWMV-  ", "onAddVerseFail ${e.localizedMessage}")

        Analytics.trackEvent(Analytics.ADD_VERSE_FAIL)
        text_translation_abbreviation.context.alert(title = "Unable to add verse", message = "Sever error; please try again later.") {
            okButton { }
        }.show()
        // TODO: use result
        // result has reason
        // 2018-09-07 02:28:48.023 9632-9734/com.spiritflightapps.memverse.debug D/OkHttp: {"error":"bad_request","error_description":"The data given to this server does not meet our criteria.","reason":"Already added previously"}
        //TODO: add ok buttons to them all.
        Crashlytics.log("add verse fail $exactRef; could've already been there in their list")
        Crashlytics.logException(e)
    }


    private fun onAddVerseNo(verse: Verse) {
        Log.d("MV_AV", "User chose no, don't add $exactRef")
        Analytics.trackEvent(Analytics.ADD_VERSE_NO, exactRef)
    }

// TODO: Use this?

    fun handleIntent() {
        activity?.let { activity: FragmentActivity ->
            val sharedText = getTextFromIntent(activity.intent)
            if (sharedText.isNotBlank()) {
                handleSharedText(sharedText)
            }
        }
    }

    //TODO: Could be unit tested.
    private fun handleSharedText(sharedText: String) {
        val simpleVerse = sharedText.getSimpleVerseFromShareString()
        if (simpleVerse == null) {
            Analytics.trackEvent(Analytics.SHARE_PARSE_FAIL, sharedText)
            requireContext().alert {
                okButton { }
            }
            return
        }
        val (book, chapter, verseNumber, version) = simpleVerse
        if (youversionToMemverseMap.containsKey(version)) {
            isFromYouVersion = true
            Log.d("NJW", "---->Not missing $version")
            Analytics.trackEvent(Analytics.YOUVERSION_VERSE_LOOKUP, simpleVerse.reference)
            makeLookupVerseAsyncNetworkCall(requireContext(), book, chapter.toString(), verseNumber.toString(), version)
        } else {
            Log.d("NJW", "---->Missing $version")
            handleMissingVersion(version)
        }
    }

    private fun handleMissingVersion(version: String) {
        Analytics.trackMissingYouVersionVersion(version)
        requireContext().alert("Sorry, we don't currently support $version; let us know via the feedback feature if you want to, and/or pick a different version.") {
            okButton { activity?.finish() }
        }.show()
    }

    //TODO: move to intentextensions
    private fun getTextFromIntent(intent: Intent?): String {
        // TODO: Timber with crashlytics.log for d and up
        return if (intent?.action == Intent.ACTION_SEND) {
            Log.d("MV-NJW", "extras=${intent.extras}")
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            ""
        }
    }



}
