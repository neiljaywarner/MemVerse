package com.spiritflightapps.memverse.ui.ui.addverse

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.utils.Analytics
import com.spiritflightapps.memverse.utils.BOOKS_OF_BIBLE
import com.spiritflightapps.memverse.utils.Prefs
import com.spiritflightapps.memverse.utils.TRANSLATIONS_ABBREVIATIONS_TEXT
import kotlinx.android.synthetic.main.add_verse_fragment.*
import org.jetbrains.anko.selector


class AddVerseFragment : Fragment() {

    companion object {
        fun newInstance() = AddVerseFragment()
    }

    private lateinit var viewModel: AddVerseViewModel

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
        //validate first
        val book = autocomplete_book.text.toString()
        val chapter = edit_chapter.text.toString()
        val verse = edit_verse.text.toString()
        Log.d("NJW", "$book $chapter:$verse")
        val ref = "$book $chapter:$verse"
        Analytics.trackEvent(Analytics.ADD_VERSE_CLICK, ref)

    }

}
