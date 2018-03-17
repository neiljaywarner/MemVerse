package com.spiritflightapps.memverse.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by neil on 1/15/17.
 * Response from https://api.twitter.com/oauth2/token
 * e.g. {"token_type":"bearer","access_token":"AAAA%2FAAA%3DAAAAAAAA"}
 */

public class BearerTokenResponse {

    @SerializedName("token_type")
    @Expose
    private String tokenType;

    @SerializedName("access_token")
    @Expose
    private String accessToken;

    public String getTokenType() {
        return tokenType;
    }


    public String getAccessToken() {
        return accessToken;
    }


}
