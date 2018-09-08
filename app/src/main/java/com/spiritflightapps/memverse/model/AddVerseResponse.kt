package com.spiritflightapps.memverse.model

import com.google.gson.annotations.SerializedName

data class AddVerseResponse(@SerializedName("response")
                            val newMemverse: Memverse)


// NOTE: may want to be more carefu.
// e.g. from https://www.memverse.com/1/verses/lookup?tl=NIV&bk=Colossians&ch=1&vs=17
/*
{
  "response": {
    "id": 4860,
    "translation": "NIV",
    "book_index": 51,
    "book": "Colossians",
    "chapter": 1,
    "versenum": 17,
    "text": "He is before all things, and in him all things hold together.",
    "created_at": "2009-12-18T19:38:41.000Z",
    "updated_at": "2018-07-29T03:36:13.000Z",
    "verified": true,
    "error_flag": false,
    "uberverse_id": 29485,
    "checked_by": null,
    "memverses_count": 109,
    "difficulty": "99.58",
    "popularity": "54.61"
  }
}
 */