package com.unmochon.protibadi.unmochonprotibadi.singletone;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mitu on 7/25/2018.
 */

public class BackgroundService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BackgroundService(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {


        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
// now you can use client`


        Retrofit mRetrofit = new Retrofit.Builder().baseUrl("http://extension.mitu.latentsoft.com/").
                addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();

        AllInterface allInterface = mRetrofit.create(AllInterface.class);

//        Call<ViewPostResponseModel> call = allInterface.viewPost(userRespons.getData().getId(), userRespons.getAuth_token(), new ViewPostBodyModel(viewPostByDate));
//        try {
//            Response<ViewPostResponseModel> result = call.execute();
//        }catch (Exception e){
//
//        }


    }
}
