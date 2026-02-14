package com.example.cooking.data;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "https://cookingbenefits.onrender.com/api/";

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(createOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Log.d(TAG, "Retrofit created");
        }
        return retrofit.create(ApiService.class);
    }

    private static OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);


        builder.addInterceptor(new AuthInterceptor());


        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

        return builder.build();
    }

    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            String token = SharedPreferencesManager.getToken();
            String tokenType = SharedPreferencesManager.getTokenType();

            Request.Builder requestBuilder = original.newBuilder();
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", tokenType + " " + token);
            }

            Response response = chain.proceed(requestBuilder.build());


            if (response.code() == 401) {
                Log.e("AuthInterceptor", "Token expired or invalid");

            }

            return response;
        }
    }
}