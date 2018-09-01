package com.spiritflightapps.memverse.model

data class Verse(val id: String, val translation: String, val book: String, val chapter: Int, val versenum: Int, val text: String)
// NOTE: Sometimes the response is longer, some fields are optional bu translation/book/chapt/verse/text are probably not.