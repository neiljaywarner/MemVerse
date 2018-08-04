package com.spiritflightapps.memverse.network;


import com.spiritflightapps.memverse.model.MemverseResponse;
import com.spiritflightapps.memverse.model.RatePerformanceResponse;

import retrofit2.Call;
import retrofit2.http.Body;
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




    // https://www.memverse.com/api/index.html#!/memverse/showMemverses
    // e.g. https://www.memverse.com/1/memverses?page=1
    @GET("1/memverses?page=1&sort=status")
    Call<MemverseResponse> fetchMemverses();
    // e.g. Learning, Memorized, Pending order.. hopefully... so pending is end of page 1 or not on page 1.
    // 100 per page = plenty for mobile


    // https://www.memverse.com/api/index.html#!/memverse/updateMemverseById/234?q=2
    ///memverses/5?q=2
    @PUT("1/memverses/{user}")
    Call<RatePerformanceResponse> ratePerformance(@Path("user") String verseId, @Query("q") String rating);
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
}
