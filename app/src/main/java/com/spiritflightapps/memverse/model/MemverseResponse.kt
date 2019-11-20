package com.spiritflightapps.memverse.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*


data class MemverseResponse(val count: Int = 0,

                            @SerializedName("response")
                            val verses: List<Memverse>)

// todo; unit test and mke a comparator or something
data class Memverse(val id: String = "",

                    @SerializedName("verse")
                    val verse: Verse,

                    val ref: String = "",
                    val status: String = "",
                    val next_test: String = "") {

    val nextTestDate: Date
        get() {
            // yyyy-MM-dd
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            return format.parse(next_test)
        }

    fun toDisplayString() = "${verse.text} $ref ${verse.translation}"
}


/*
       @SerializedName("response")
       val verseItems : List<MemverseResponseItem>  = emptyList()
       */

// sam's 7 verses when one is pending and sort by pending
//  https://www.memverse.com/1/memverses?page=1&sort=status,next_test
/*
{
  "response": [
    {
      "id": 1826890,
      "user_id": 43889,
      "efactor": "1.3",
      "test_interval": 5,
      "rep_n": 3,
      "next_test": "2018-02-25",
      "status": "Learning",
      "prev_verse": null,
      "ref_interval": 1,
      "next_ref_test": null,
      "passage_id": 467098,
      "subsection": null,
      "ref": "Deut 5:16",
      "verse": {
        "id": 15967,
        "translation": "NIV",
        "book_index": 5,
        "book": "Deuteronomy",
        "chapter": 5,
        "versenum": 16,
        "text": "\u201cHonor your father and your mother, as the LORD your God has commanded you, so that you may live long and that it may go well with you in the land the LORD your God is giving you."
      }
    },
    {
      "id": 1826891,
      "user_id": 43889,
      "efactor": "1.5",
      "test_interval": 6,
      "rep_n": 3,
      "next_test": "2018-02-26",
      "status": "Learning",
      "prev_verse": null,
      "ref_interval": 2,
      "next_ref_test": "2018-02-22",
      "passage_id": 467099,
      "subsection": null,
      "ref": "Phil 2:14",
      "verse": {
        "id": 2614,
        "translation": "NIV",
        "book_index": 50,
        "book": "Philippians",
        "chapter": 2,
        "versenum": 14,
        "text": "Do everything without complaining or arguing,"
      }
    },
    {
      "id": 1958226,
      "user_id": 43889,
      "efactor": "1.4",
      "test_interval": 6,
      "rep_n": 3,
      "next_test": "2018-02-26",
      "status": "Learning",
      "prev_verse": null,
      "ref_interval": 1,
      "next_ref_test": null,
      "passage_id": 503503,
      "subsection": null,
      "ref": "Deut 6:5",
      "verse": {
        "id": 4853,
        "translation": "NIV",
        "book_index": 5,
        "book": "Deuteronomy",
        "chapter": 6,
        "versenum": 5,
        "text": "Love the LORD your God with all your heart and with all your soul and with all your strength."
      }
    },
    {
      "id": 1960182,
      "user_id": 43889,
      "efactor": "1.5",
      "test_interval": 11,
      "rep_n": 5,
      "next_test": "2018-03-03",
      "status": "Learning",
      "prev_verse": null,
      "ref_interval": 1,
      "next_ref_test": null,
      "passage_id": 503861,
      "subsection": null,
      "ref": "Matt 22:37",
      "verse": {
        "id": 3034,
        "translation": "NIV",
        "book_index": 40,
        "book": "Matthew",
        "chapter": 22,
        "versenum": 37,
        "text": "Jesus replied: \u201c\u2018Love the Lord your God with all your heart and with all your soul and with all your mind.\u2019"
      }
    },
    {
      "id": 1826895,
      "user_id": 43889,
      "efactor": "1.6",
      "test_interval": 18,
      "rep_n": 6,
      "next_test": "2018-03-10",
      "status": "Learning",
      "prev_verse": null,
      "ref_interval": 1,
      "next_ref_test": null,
      "passage_id": 467103,
      "subsection": null,
      "ref": "Phil 4:13",
      "verse": {
        "id": 103,
        "translation": "NIV",
        "book_index": 50,
        "book": "Philippians",
        "chapter": 4,
        "versenum": 13,
        "text": "I can do everything through him who gives me strength."
      }
    },
    {
      "id": 1955908,
      "user_id": 43889,
      "efactor": "1.9",
      "test_interval": 44,
      "rep_n": 6,
      "next_test": "2018-03-07",
      "status": "Memorized",
      "prev_verse": null,
      "ref_interval": 1,
      "next_ref_test": null,
      "passage_id": 502915,
      "subsection": null,
      "ref": "Col 1:17",
      "verse": {
        "id": 4860,
        "translation": "NIV",
        "book_index": 51,
        "book": "Colossians",
        "chapter": 1,
        "versenum": 17,
        "text": "He is before all things, and in him all things hold together."
      }
    },
    {
      "id": 1958225,
      "user_id": 43889,
      "efactor": "1.3",
      "test_interval": 1,
      "rep_n": 1,
      "next_test": "2018-02-21",
      "status": "Pending",
      "prev_verse": null,
      "ref_interval": 1,
      "next_ref_test": null,
      "passage_id": 503502,
      "subsection": null,
      "ref": "Prov 3:5",
      "verse": {
        "id": 402,
        "translation": "NIV",
        "book_index": 20,
        "book": "Proverbs",
        "chapter": 3,
        "versenum": 5,
        "text": "Trust in the LORD with all your heart and lean not on your own understanding;"
      }
    }
  ],
  "count": 7,
  "pagination": {
    "current": 1,
    "previous": null,
    "next": null,
    "per_page": 100,
    "pages": 1,
    "count": 7
  }
}
 */