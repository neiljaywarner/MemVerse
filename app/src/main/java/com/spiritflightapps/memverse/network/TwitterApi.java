package com.spiritflightapps.memverse.network;

import com.neiljaywarner.twitteruserstatus.model.MemverseResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by neil on 1/15/17.
 */

public interface TwitterApi {


    String GRANT_TYPE_CLIENT = "client_credentials";

    String GRANT_TYPE_PASSWORD = "password";
    // TODO: See if this is advised against, see what they do in iOS..

    // also see
    //https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_understanding_username_password_oauth_flow.htm

    @FormUrlEncoded
    @POST("/oauth/token")
    Call<BearerTokenResponse> getBearerToken(@Field("grant_type") String grantType);

    @POST("oauth/token")
    Call<BearerTokenResponse> getBearerToken(@Body PasswordTokenRequest passwordTokenRequest);

    // https://www.memverse.com/api/index.html#!/memverse/showMemverses
    // e.g. https://www.memverse.com/1/memverses?page=1
    @GET("1/memverses?page=1&sort=id")
    Call<MemverseResponse> getMemverses();


    // Note for memverse API: /oauth only endpoint with no 1/ in it.
}
