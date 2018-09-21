package com.spiritflightapps.memverse.utils

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.spiritflightapps.memverse.BuildConfig
import com.spiritflightapps.memverse.MVApplication
import com.spiritflightapps.memverse.model.Verse

object Analytics {

    val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(MVApplication.instance) }

    const val ADD_VERSE_CLICK = "add_verse_click"
    const val ADD_VERSE_TO_LEARNING = "add_verse_to_learning"
    const val ADD_VERSE_TO_PENDING = "add_verse_to_pending"
    const val ADD_VERSE_FAIL = "add_verse_fail"
    const val TRANSLATION_SELECTED = "translation_selected"
    const val LEARNING_VERSE_CORRECT = "learning_verse_correct"
    const val ADD_VERSE_LOOKUP_SUCCESS = "add_verse_lookup_success"
    const val ADD_VERSE_LOOKUP_FAIL = "add_verse_lookup_fail"
    const val ADD_VERSE_YES = "add_verse_yes"
    const val ADD_VERSE_NO = "add_verse_no"
    const val LOOKUP_VERSE_SERVER_FAULT: String = "lookup_verse_server_fault"
    const val LOOKUP_VERSE_USER_FAULT = "lookup_verse_userfault"
    const val ADD_VERSE_FAIL_400_MAYBE_IN_LIST = "add_verse_fail_maybe_user"
    const val MISSING_YOUVERSION_TRANSLATION = "missing_youversion_translation"
    const val YOUVERSION_VERSE_LOOKUP = "youversion_lookup"

    fun trackEvent(eventName: String, verse: Verse) = trackEvent(eventName, verse.ref)

    fun trackEvent(eventName: String, ref: String) = trackEvent(eventName, hashMapOf(Pair(FirebaseAnalytics.Param.ITEM_NAME, ref)))

    fun trackEvent(eventName: String, eventProperties: HashMap<String, String>? = null) {
        if (BuildConfig.DEBUG) {
            Log.d("NJW-MV-Analytics", "eventName=$eventName;properties=$eventProperties")
        } else {
            trackEventToFirebase(eventName, eventProperties)
        }
    }

    private fun trackEventToFirebase(eventName: String, eventProperties: HashMap<String, String>?) {

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

    fun addUserProperty(userPropertyKey: String, userPropertyValue: String) {
        firebaseAnalytics.setUserProperty(userPropertyKey, userPropertyValue)
    }

    fun trackMissingYouVersionVersion(translation: String) {
        trackEvent(MISSING_YOUVERSION_TRANSLATION, hashMapOf(Pair(FirebaseAnalytics.Param.ITEM_NAME, translation)))
    }

}