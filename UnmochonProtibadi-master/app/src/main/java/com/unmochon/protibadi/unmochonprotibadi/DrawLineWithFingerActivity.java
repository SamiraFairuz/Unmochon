package com.unmochon.protibadi.unmochonprotibadi;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.unmochon.protibadi.unmochonprotibadi.CustomView.DrawLineCanvas;
import com.unmochon.protibadi.unmochonprotibadi.base.BaseActivity;
import com.unmochon.protibadi.unmochonprotibadi.singletone.AllInterface;
import com.unmochon.protibadi.unmochonprotibadi.singletone.RetrofitSingletone;
import com.unmochon.protibadi.unmochonprotibadi.utility.Utility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DrawLineWithFingerActivity extends BaseActivity {

    private DrawLineCanvas dlc;

    Bitmap b;
    private Retrofit mRetrofit;
    private AllInterface allInterface;
    private static Utility utility;
    private File imageFile = null;
    SharedPreferences shre;
    SharedPreferences.Editor edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_line_with_finger);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dlc = findViewById(R.id.dlc);
        ImageView btn_redo = findViewById(R.id.btn_redo);
        ImageView btn_undo = findViewById(R.id.btn_undo);
        Button btnSaveImage = findViewById(R.id.btn_save_image);
        //Button btn_send_image = findViewById(R.id.btn_send_image);


        utility = new Utility();


        mRetrofit = RetrofitSingletone.getInstance(getBaseContext());
        allInterface = mRetrofit.create(AllInterface.class);
        shre = PreferenceManager.getDefaultSharedPreferences(this);
        edit = shre.edit();

        utility.Log(getClass().getSimpleName(), "onCreate ");

        showImage();


        btn_redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlc.onClickRedo();
            }
        });

        btn_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlc.onClickUndo();
            }
        });

        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //create directory if not exist
                showLoading("Sending...");
                final File dir = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name));
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());


                File output = new File(dir, "edit_unmo_"+fileStamp+".png");
                imageFile = output;
                OutputStream os;

                try {
                    os = new FileOutputStream(output);
                    dlc.buildDrawingCache();
                    b = dlc.getDrawingCache();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, os);

                    os.flush();
                    os.close();

                    final Handler handler = new Handler();

                    upload();

                    //this code will scan the image so that it will appear in your gallery when you open next time
                    MediaScannerConnection.scanFile(DrawLineWithFingerActivity.this, new String[]{output.toString()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(DrawLineWithFingerActivity.this, DrawLineWithFingerActivity.this.getResources().getString(R.string.str_save_image_text) + dir.getPath(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                    );
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });



    }

    @Override
    protected void onPause() {
        super.onPause();
        //dlc.setBackground(null);
        utility.Log(getClass().getSimpleName(), "onPause ");



    }

    private void showImage(){

        try{

            // byte[] byteArray = getIntent().getByteArrayExtra(Utility.BitmapImage);

            // Bitmap photobmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);


            String previouslyEncodedImage = shre.getString(Utility.BitmapImage, "");

            if(!previouslyEncodedImage.isEmpty()){
//                edit.putString(Utility.BitmapImage,"");
//                edit.commit();
                utility.Log(getClass().getSimpleName(), "bitmap string not empty  ->"+previouslyEncodedImage);
                byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                dlc.setBackground(ob);



            }else {
                utility.Log(getClass().getSimpleName(), "bitmap string empty");
            }

        }catch (Exception e){

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        utility.Log(getClass().getSimpleName(), "onResume ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        utility.Log(getClass().getSimpleName(), "onRestart");
        dlc.clearAllPath();
        showImage();

    }

    private void upload() {

        int readPhoneState = ContextCompat.checkSelfPermission(DrawLineWithFingerActivity.this, Manifest.permission.READ_PHONE_STATE);

        String uid = "";
        if(readPhoneState != PackageManager.PERMISSION_GRANTED ) {


            ActivityCompat.requestPermissions(DrawLineWithFingerActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, Utility.REQUEST_CODE_Read_PHONE_STATE_PERMISSION);

        }else {
            TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            uid = tManager.getDeviceId();
        }

        if (utility.isConnected(DrawLineWithFingerActivity.this)) {


            Date c = Calendar.getInstance().getTime();

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String formattedDate = df.format(c);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), reqFile);
            Call<String> call = allInterface.uploadScreenshot(body,
                    RequestBody.create(MediaType.parse("text/plain"),uid),
                    RequestBody.create(MediaType.parse("text/plain"),uid),
                    RequestBody.create(MediaType.parse("text/plain"),formattedDate));

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    hideLoading();
                    if (response.isSuccessful()){

                        //utility.Log(getClass().getSimpleName(), "Success "+response.body());
                        showSnackbar("স্ক্রিনশট টি আমাদের কাছে পৌছেছে । ");


                    }else {
                        showSnackbar("স্ক্রিনশট আপলোড সফল হয় নি।");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    hideLoading();
                    ///utility.Log(getClass().getSimpleName(), "Failed "+call.toString());
                    showSnackbar("স্ক্রিনশট টি আমাদের কাছে পৌছেছে । ");


                }
            });


        }else {

            hideLoading();
            utility.Log(getClass().getSimpleName(), "Success "+imageFile);

            utility.NetworkError(DrawLineWithFingerActivity.this);
        }
    }




}
