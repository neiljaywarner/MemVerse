package com.spiritflightapps.memverse.utils

import android.util.Log

data class YouVersionVerse(val book: String, val chapter: Int, val verse: Int, val version: String)


fun String.getSimpleVerseFromShareString(): YouVersionVerse? {
    if (isYouVersionVerse(this)) {
        return this.getSimpleVerseFromYouVersionShareString()
    } else {
        return null
    }
}

private fun String.getSimpleVerseFromYouVersionShareString(): YouVersionVerse? {
    try {
        Log.d("NJW", "youVersionShareString = $this")
        val textWithoutLink = substringBefore("https://bible.com").trim()
        Log.d("NJW", "textWithoutLink = $textWithoutLink")
        val ref = textWithoutLink.split("\n")[1]
        Log.d("NJW", "ref=$ref")
        val refSplitBySpaces = ref.split(" ")
        val lastIndex = refSplitBySpaces.lastIndex
        val translation = refSplitBySpaces[lastIndex]
        Log.d("NJW", "Translation=$translation")
        val chapterAndVerseString = refSplitBySpaces[lastIndex - 1]
        Log.d("NJW", "ChapterAndVerseString=$chapterAndVerseString")
        val chapterString = chapterAndVerseString.split(":")[0]
        Log.d("NJW", "ChapterString=$chapterString")
        val verseString = chapterAndVerseString.split(":")[1]
        Log.d("NJW", "verseString=$verseString")
        val book = refSplitBySpaces[lastIndex - 2]
        Log.d("NJW", "Book=$book")
        return YouVersionVerse(book, chapterString.toInt(), verseString.toInt(), translation)
    } catch (e: Exception) {
        Log.e("NJW", "exception=${e.localizedMessage}")
        return null
    }

}

fun isYouVersionVerse(sharedText: String) = sharedText.contains("https://bible.com/")

/*
f (sharedText.contains("https://bible.com/")) {
                    handleYouversionSharedText(sharedText)
                }
 */