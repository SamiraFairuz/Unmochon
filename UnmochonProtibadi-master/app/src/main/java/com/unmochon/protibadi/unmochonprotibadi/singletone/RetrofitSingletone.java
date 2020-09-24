package com.unmochon.protibadi.unmochonprotibadi.singletone;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mitu on 7/1/2018.
 */

public class RetrofitSingletone {

    private static Context mContext;
    private static Retrofit mRetrofit;

    public RetrofitSingletone() {
    }

    public static synchronized Retrofit getInstance(Context context){
        mContext = context;
        if (mRetrofit == null){
            createRetrofit();
        }
        return  mRetrofit;
    }


    public static void  createRetrofit(){

        /*Builder b = new Builder();
        b.connectTimeout(10,TimeUnit.SECONDS);
        b.readTimeout(10, TimeUnit.SECONDS);
        b.writeTimeout(30, TimeUnit.MILLISECONDS);
// set other properties

        OkHttpClient client = b.build();*/

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
// now you can use client`

        mRetrofit = new Retrofit.Builder().baseUrl("http://extension.mitu.latentsoft.com/").
                addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
    }
}
