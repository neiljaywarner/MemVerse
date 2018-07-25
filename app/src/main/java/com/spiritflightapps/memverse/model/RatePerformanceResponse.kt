package com.spiritflightapps.memverse.model


//TODO: Fix me eventually; this i the actual json response
//2018-07-25 07:08:08.068 6005-7830/com.spiritflightapps.memverse D/OkHttp:
// {"response":{"user_id":1354,"status":"Learning","next_test":"2018-08-10","rep_n":7,"test_interval":16,"efactor":"1.3","id":699690,"prev_verse":699689,"ref_interval":1,"next_ref_test":"2017-01-18","passage_id":12442,"subsection":null,"ref":"Ps 94:19","verse":{"id":69696,"book":"Psalms","chapter":94,"versenum":19,"translation":"CEV","text":"And when I was burdened with worries, you comforted me and made me feel secure.","book_index":19}}}

data class RatePerformanceResponse(val id: Int, val status: String, val next_test: String, val next_ref_test: String)
//todo: add error class response.
/*

{
    "id": 0,
    "verse_id": 0,
    "user_id": 0,
    "efactor": 0,
    "test_interval": 0,
    "rep_n": 0,
    "next_test": "string",
    "status": "string",
    "prev_verse": 0,
    "ref_interval": 0,
    "next_ref_test": "string",
    "passage_id": 0,
    "subsection": 0
}
        */