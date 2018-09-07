package com.spiritflightapps.memverse.ui.ui.addverse

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.crashlytics.android.Crashlytics
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.model.Verse
import com.spiritflightapps.memverse.network.MemverseApi
import com.spiritflightapps.memverse.network.ServiceGenerator
import com.spiritflightapps.memverse.utils.Analytics
import com.spiritflightapps.memverse.utils.BOOKS_OF_BIBLE
import com.spiritflightapps.memverse.utils.Prefs
import com.spiritflightapps.memverse.utils.TRANSLATIONS_ABBREVIATIONS_TEXT
import kotlinx.android.synthetic.main.add_verse_fragment.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*


class AddVerseFragment : Fragment() {

    companion object {
        fun newInstance() = AddVerseFragment()
        const val TAG = "MV_AddVerseFragment"
    }

    private lateinit var viewModel: AddVerseViewModel

    val memVersesApi = ServiceGenerator.createDeferredService(MemverseApi::class.java)

    val memVersesApi2 = ServiceGenerator.createService(MemverseApi::class.java)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.add_verse_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.title = getString(R.string.add_verse)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autocomplete_book.apply {
            val array = BOOKS_OF_BIBLE.split(",").toTypedArray()
            val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, array)
            setAdapter(adapter)
            setOnItemClickListener { _, _, _, _ -> edit_chapter.requestFocus() }
        }

        autocomplete_translation.apply {
            val array = TRANSLATIONS_ABBREVIATIONS_TEXT.split(",").toTypedArray()
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

    // this might be a temporary technique
    // as string like The Message (2002) - MSG
    fun getTranslationAbbreviation(translationDescription: String): String {
        return translationDescription.split("-")[1].trim()
    }

    private fun addVerse() {
        //TODO: validate first based on a) is it too many chap/verse, more than in the book James 20:200
        // b) does the user already have it in their list?
        val book = autocomplete_book.text.toString().trim()
        val chapter = edit_chapter.text.toString().trim()
        val verse = edit_verse.text.toString().trim()
        Log.e("MV-NJW-AdddVerse", "$book $chapter:$verse ${text_translation_abbreviation.text}")
        val ref = "$book $chapter:$verse"
        val translation = text_translation_abbreviation.text.toString()
        Analytics.trackEvent(Analytics.ADD_VERSE_CLICK, "$ref $translation")
        // *** TODO* Check to see if

        // TODO: Dn't let the user add a verse if they already have it in their list
        // they shludn't have to have a network call for that.
        // check via room..

        makeLookupVerseAsyncNetworkCall(text_translation_abbreviation.context, book.trim(), chapter, verse, translation)

    }

    private fun makeLookupVerseAsyncNetworkCall(ctx: Context, book: String, chapter: String, verseNumber: String, translation: String) = launch(UI) {
        // TODO: Change from adding verse to just a refresh dialog
        Log.e("MV", "in makeLookupVerseAsyncNetworkCAll, abot to show spinner, can we do that?= ")
        //val spinner = ctx.indeterminateProgressDialog(message = "Please wait a bit…", title = "Looking up verse")
        //spinner.show()

        try {
            Log.e("MV", "Agout to create deferred service")

            val memVersesApi = ServiceGenerator.createDeferredService(MemverseApi::class.java)

            Log.e("MV", "Agout to make api call..")
            val result = memVersesApi.lookupVerse(translation, book, chapter, verseNumber)
            Log.e("MV", "Agout to await api call..")

            val response = result.await()
            Log.e("MV", "back from  api call..")

            val verse = response.verse
            if (verse == null) {
                Log.e("NJW-MV", "verse is null")
                onAddVerseFail(Exception("Verse is null; probably non-existent book/chap/verse $book $chapter:$verse"))
            } else {
                Log.d("MV-MV", "verseId=${verse.id}")
                onVerseLookupSuccess(verse)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("NJWMV", e.localizedMessage)
            onVerseLookupFail(e)
        } finally {
            //spinner.hide()
        }

    }

    //TODO: Utilize failure responseBody with wrapper object
    /*
    {
  "code": 0,
  "message": "string"
}
     */
    private fun onVerseLookupFail(e: Exception) {
        Analytics.trackEvent(Analytics.ADD_VERSE_LOOKUP_FAIL)
        Crashlytics.logException(e)

        //val context = text_translation_abbreviation.context
        //context.alert("Lookup verse failed.could you have picked invalid book/chapter/verse combination").show()
        // TODO: could probably use
        // api endpoint /final_verses one time to save last chapter and verse of books in the Bible
        // yes, it has 1189 entries, one for each chapter
        // so this would work.

        // TODO: we need add by ref for share in anyway.
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
        val spinner = ctx.indeterminateProgressDialog(message = "Please wait a bit…", title = "Adding verse")
        spinner.show()

        val result = memVersesApi.addVerse(verse.id)
        try {
            val response = result.await()
            val next_test = response.next_test

            Log.d("MV-AV", "nextTest=$next_test")
            addVerseSuccess()
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

    private fun addVerseSuccess() {
        Analytics.trackEvent(Analytics.ADD_VERSE_SUCCESS)
        text_translation_abbreviation.context.alert("add verse success").show()
        activity?.finish()
    }

    private fun onAddVerseFail(e: Exception) {
        Log.e("NJWMV", e.localizedMessage)

        Analytics.trackEvent(Analytics.ADD_VERSE_FAIL)
        text_translation_abbreviation.context.alert("Add verse fail could you have picked book/chapter/verse you already have") {
            okButton { }
        }
        //TODO: add ok buttons to them all.
        Crashlytics.log("add verse fail; could've already been there.")
        Crashlytics.logException(e)
    }

    private fun onAddVerseNo(verse: Verse) {
        Log.d("MV_AV", "User chose no, don't add ${verse.ref}")
        Analytics.trackEvent(Analytics.ADD_VERSE_NO, verse)
    }

    private fun clearFields() {
        edit_chapter.setText("")
        edit_verse.setText("")
        autocomplete_book.setText("")
    }


    // TODO: Use this?
// https://medium.com/@raghunandan2005/retrofit2-and-koltin-coroutines-sample-938a6842b0a1





}
