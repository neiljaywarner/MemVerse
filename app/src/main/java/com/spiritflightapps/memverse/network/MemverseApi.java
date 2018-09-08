package com.spiritflightapps.memverse.network;


import com.spiritflightapps.memverse.model.AddVerseResponse;
import com.spiritflightapps.memverse.model.MemverseResponse;
import com.spiritflightapps.memverse.model.RatePerformanceResponse;
import com.spiritflightapps.memverse.model.VerseResponse;

import kotlinx.coroutines.experimental.Deferred;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by neil on 1/15/17.
 */

public interface MemverseApi {


    String GRANT_TYPE_CLIENT = "client_credentials";

    String GRANT_TYPE_PASSWORD = "password";
    // TODO: See if this is advised against, see what they do in iOS..

    // also see
    //https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_understanding_username_password_oauth_flow.htm


    // TODO: Convert to kotlin!!
    @FormUrlEncoded
    @POST("/oauth/token")
    Call<BearerTokenResponse> getBearerToken(@Field("grant_type") String grantType);

    @POST("oauth/token")
    Call<BearerTokenResponse> getBearerToken(@Body PasswordTokenRequest passwordTokenRequest);


    // https://www.memverse.com/api/index.html#!/verse/findVerseByTlBkChVs
    // TODO: Look up a verse first , eg. Psalms 27:1 then get its id, confirm it with user, then add it.
    // e.g. https://www.memverse.com/1/verses/lookup?tl=NIV84&bk=Matt%20&ch=28&vs=18


    //and then postmemverses once have id
    //https://www.memverse.com/api/index.html#!/memverse/createMemverse
    // with id 'long' field as the only param but paramtype path
    // JUST LIKE RECORD RATING except with POST woo hoo
    @POST("1/memverses")
    Deferred<AddVerseResponse> addVerse(@Body MemverseAddRequest idRequest);
    // 1/memverses?id=58?? use query with post?  swagger says body.


    // Note: Returns 204 for success and 404 for already deleted/not found
    @DELETE("1/memverses/{id}")
    Call<Void> deleteVerse(@Path("id") String verseId);


    // https://www.memverse.com/api/index.html#!/memverse/showMemverses
    // e.g. https://www.memverse.com/1/memverses?page=1
    @GET("1/memverses?page=1&sort=status")
    Call<MemverseResponse> fetchMemverses();
    // e.g. Learning, Memorized, Pending order.. hopefully... so pending is end of page 1 or not on page 1.
    // 100 per page = plenty for mobile


    // https://www.memverse.com/api/index.html#!/memverse/updateMemverseById/234?q=2
    ///memverses/5?q=2
    @PUT("1/memverses/{id}")
    Call<RatePerformanceResponse> ratePerformance(@Path("id") String verseId, @Query("q") String rating);
    //id path - long
    // 1-5 in q=

        /* response is
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


    // Note for memverse API: /oauth only endpoint with no 1/ in it.

    //Call<VerseResponse> lookupVerse
    // e.g. https://www.memverse.com/1/verses/lookup?tl=NIV&bk=Colossians&ch=1&vs=17
    @GET("/1/verses/lookup")
    Deferred<VerseResponse> lookupVerse(@Query("tl") String translation,
                                        @Query("bk") String book,
                                        @Query("ch") String chapter,
                                        @Query("vs") String verse
    );
}
/*
translations - can use code for add verse
{
  "response": [
    {
      "Name": "English",
      "Abbreviation": "EN",
      "Translations": [
        {
          "Name": "Amplified Bible (Classic Edition) (1987)",
          "Abbreviation": "AMP"
        },
        {
          "Name": "Authorized King James Version (1769)",
          "Abbreviation": "AKJ"
        },
        {
          "Name": "Biblical Greek (SBLGNT) (2010)",
          "Abbreviation": "GRK"
        },
        {
          "Name": "Contemporary English Version (1995)",
          "Abbreviation": "CEV"
        },
        {
          "Name": "Darby Translation",
          "Abbreviation": "DTL"
        },
        {
          "Name": "Easy-to-Read Version (2006)",
          "Abbreviation": "ERV"
        },
        {
          "Name": "English Standard Version (2007)",
          "Abbreviation": "ESV07"
        },
        {
          "Name": "English Standard Version (2011)",
          "Abbreviation": "ESV"
        },
        {
          "Name": "Geneva Bible (1599)",
          "Abbreviation": "GEN"
        },
        {
          "Name": "God's Word Translation (1995)",
          "Abbreviation": "GW"
        },
        {
          "Name": "Good News Translation (1992)",
          "Abbreviation": "GNT"
        },
        {
          "Name": "Holman Christian Standard Bible (2009)",
          "Abbreviation": "HCS"
        },
        {
          "Name": "King James Version (Modernized/1987)",
          "Abbreviation": "KJV"
        },
        {
          "Name": "New American Standard Bible (1995)",
          "Abbreviation": "NAS"
        },
        {
          "Name": "New Century Version (2005)",
          "Abbreviation": "NCV"
        },
        {
          "Name": "New International Reader's Version (1998)",
          "Abbreviation": "IRV"
        },
        {
          "Name": "New International Version (1984)",
          "Abbreviation": "NIV"
        },
        {
          "Name": "New International Version (2011)",
          "Abbreviation": "NNV"
        },
        {
          "Name": "New King James Version (1982)",
          "Abbreviation": "NKJ"
        },
        {
          "Name": "New Living Translation (2007)",
          "Abbreviation": "NLT"
        },
        {
          "Name": "New Living Translation (2015)",
          "Abbreviation": "NLT15"
        },
        {
          "Name": "New Revised Standard Version (1989)",
          "Abbreviation": "NRS"
        },
        {
          "Name": "Revised Standard Version (1971)",
          "Abbreviation": "RSV"
        },
        {
          "Name": "Scottish Metrical Psalter",
          "Abbreviation": "SMP"
        },
        {
          "Name": "Scottish Metrical Psalter: B",
          "Abbreviation": "SMPB"
        },
        {
          "Name": "Scottish Metrical Psalter: C",
          "Abbreviation": "SMPC"
        },
        {
          "Name": "The Message (2002)",
          "Abbreviation": "MSG"
        },
        {
          "Name": "Updated King James Version",
          "Abbreviation": "UKJ"
        }
 */