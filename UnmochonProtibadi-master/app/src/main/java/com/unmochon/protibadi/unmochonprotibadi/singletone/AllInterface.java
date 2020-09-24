package com.unmochon.protibadi.unmochonprotibadi.singletone;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AllInterface {


    @Multipart
    @POST("/store")


    Call<String> uploadScreenshot(@Part MultipartBody.Part image,
                                  @Part("messenger_id") RequestBody messenger_id,
                                  @Part("name") RequestBody name,

                                   @Part("date_time") RequestBody date_time);


}
