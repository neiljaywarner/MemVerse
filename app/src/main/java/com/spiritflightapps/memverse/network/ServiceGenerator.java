package com.spiritflightapps.memverse.network;


import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory;
import com.spiritflightapps.memverse.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by neil on 1/15/17.
 */

public class ServiceGenerator {
    // https://www.memverse.com/oauth/token
    private static final String API_BASE_URL = "https://www.memverse.com/";
    public static final String AUTH_TOKEN_PREFS_KEY = "pref_auth_token";
    private static String sPasswordAuthToken = "";

    public static void setPasswordAuthToken(String passwordAuthToken) {
        sPasswordAuthToken = passwordAuthToken;
    }

    public static boolean hasPasswordAuthToken() {
        return !sPasswordAuthToken.isEmpty();
    }

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit.Builder coroutineBuilder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, sPasswordAuthToken);
    }

    public static <S> S createDeferredService(Class<S> serviceClass) {
        return createDeferredService(serviceClass, sPasswordAuthToken);
    }

    private static <S> S createService(Class<S> serviceClass, final String authToken) {
        if (authToken != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + authToken)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createDeferredService(Class<S> serviceClass, final String authToken) {
        if (authToken != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + authToken)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = coroutineBuilder.client(client).build();
        return retrofit.create(serviceClass);
    }



    /**
     * Create Retrofit2 service that can be used to get BearerKey
     *
     * @param credentials
     * @return
     */
    public static MemverseApi createBearerKeyService(String credentials) {

        final String basic = "Basic " + credentials;
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                        .method(original.method(), original.body());
                // NOTE: Must leave accept-encoding gzip off so okhttp automatically unzips it.
                // http://stackoverflow.com/questions/33889840/retrofit-and-okhttp-gzip-decode

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient.build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(client).build();

        return retrofit.create(MemverseApi.class);
    }

    /**
     * Create Retrofit2 service that can be used to get BearerKey
     *
     * @param credentials
     * @return
     */
    public static MemverseApi createBearerKeyDeferredService(String credentials) {

        final String basic = "Basic " + credentials;
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                        .method(original.method(), original.body());
                // NOTE: Must leave accept-encoding gzip off so okhttp automatically unzips it.
                // http://stackoverflow.com/questions/33889840/retrofit-and-okhttp-gzip-decode

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient.build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addCallAdapterFactory(CoroutineCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(client).build();

        return retrofit.create(MemverseApi.class);
    }
}