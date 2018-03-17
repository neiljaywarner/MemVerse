package com.spiritflightapps.memverse.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.neiljaywarner.twitteruserstatus.model.MemverseResponse;
import com.spiritflightapps.memverse.R;
import com.spiritflightapps.memverse.network.BearerTokenResponse;
import com.spiritflightapps.memverse.network.PasswordTokenRequest;
import com.spiritflightapps.memverse.network.ServiceGenerator;
import com.spiritflightapps.memverse.network.TwitterApi;
import com.spiritflightapps.memverse.network.TwitterAuthUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // TODO: Save bearer token, but they never expire, so get a strategy, and get encryption
        retrieveBearerToken();
        // TODO: Spinner

    }

    private void makeGetMemversesNetworkCall(final String authToken) {
        Log.d(TAG, "***** makeGetMemversesNetworkCall");

        // TODO: Handle auth token in a better way
        TwitterApi twitterApi = ServiceGenerator.createService(TwitterApi.class, authToken);

        Call<MemverseResponse> tweetsCall = twitterApi.getMemverses();

        final Context context = this;
        tweetsCall.enqueue(new Callback<MemverseResponse>() {
            @Override
            public void onResponse(Call<MemverseResponse> call, Response<MemverseResponse> response) {
                Log.d(TAG, "tweetsCall:Response code: " + response.code());
                if (response.code() == 200) {
                    MemverseResponse tweets = response.body();
                    Log.d("NJW", "count=" + tweets.getCount());
                    // TODO: Num verses
                    MainActivity.this.setTitle(" verses");
                    //updateList(tweets.getTweetList());
                } else {
                    //TODO: Could check other response codes or if have network connection
                    Toast.makeText(MainActivity.this, "sorry, something went wrong with network call ", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MemverseResponse> call, Throwable t) {
                Log.e(TAG, "tweetsCall Failure:" + call.request().toString()
                        + t.getMessage());
                Toast.makeText(MainActivity.this, "sorry, something went wrong with network call ", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void retrieveBearerToken() {
        Log.d(TAG, "*** Retrieving bearer token.");
        TwitterApi twitterApi = ServiceGenerator.createBearerKeyService(
                TwitterAuthUtils.generateEncodedBearerTokenCredentials());

        PasswordTokenRequest passwordTokenRequest = new PasswordTokenRequest();
        Call<BearerTokenResponse> bearerTokenCall = twitterApi.getBearerToken(passwordTokenRequest);
        // TODO: Consider https://auth0.com/docs/api-auth/grant/authorization-code-pkce

        // also consider https://github.com/openid/AppAuth-Android
        // also consider https://github.com/auth0/Auth0.Android


        bearerTokenCall.enqueue(new Callback<BearerTokenResponse>() {
            @Override
            public void onResponse(Call<BearerTokenResponse> call, Response<BearerTokenResponse> response) {
                Log.d(TAG, "bearerTokenCall:Response code: " + response.code());
                if (response.code() == 200) {
                    BearerTokenResponse bearerTokenResponse = response.body();

                    Log.d(TAG, "bearerTokenCall:token_type=" + bearerTokenResponse.getTokenType());
                    makeGetMemversesNetworkCall(bearerTokenResponse.getAccessToken());


                } else {
                    Log.e(TAG, "Response invalid, check consumer key/secret combination if 403");
                }

            }

            @Override
            public void onFailure(Call<BearerTokenResponse> call, Throwable t) {
                Log.e(TAG, "bearerTokenCall Failure:" + call.request().toString()
                        + t.getMessage());
            }
        });
    }
    // TODO: Pick kotlin secure sharedprefs to encrypt token
    // ** it never expires

}

// Note:
// iOS basic getMemverses is https://github.com/avitus/Memverse_iOS/blob/master/Memverse_iOS/MemorizeViewController.swift
// wit realm local db fetch before today's date and pending
