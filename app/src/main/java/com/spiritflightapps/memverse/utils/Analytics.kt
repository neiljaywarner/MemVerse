package com.spiritflightapps.memverse.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.spiritflightapps.memverse.MVApplication
import com.spiritflightapps.memverse.model.Verse

object Analytics {

    const val ADD_VERSE_CLICK = "add_verse_click"
    const val ADD_VERSE_SUCCESS = "add_verse_success"
    const val ADD_VERSE_FAIL = "add_verse_fail"
    const val TRANSLATION_SELECTED = "translation_selected"
    const val LEARNING_VERSE_CORRECT = "learning_verse_correct"
    const val ADD_VERSE_LOOKUP_SUCCESS = "add_verse_lookup_success"
    const val ADD_VERSE_LOOKUP_FAIL = "add_verse_lookup_fail"
    const val ADD_VERSE_YES = "add_verse_yes"
    const val ADD_VERSE_NO = "add_verse_no"

    fun trackEvent(eventName: String, verse: Verse) = trackEvent(eventName, verse.ref)

    fun trackEvent(eventName: String, ref: String) = trackEvent(eventName, hashMapOf(Pair("ref", ref)))

    fun trackEvent(eventName: String, eventProperties: HashMap<String, String>? = null) {
        //trackEventToAppCenter(eventName, eventProperties) //track event to somewhere else, like testfairy
        trackEventToFirebase(eventName, eventProperties)
    }

    private fun trackEventToFirebase(eventName: String, eventProperties: HashMap<String, String>?) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(MVApplication.instance)

        var bundle: Bundle? = null
        if (eventProperties != null) {
            bundle = Bundle().apply {
                eventProperties.entries.forEach { eventProperty ->
                    putString(eventProperty.key, eventProperty.value)
                }
            }
        }

        firebaseAnalytics.logEvent(eventName, bundle)

    }

}