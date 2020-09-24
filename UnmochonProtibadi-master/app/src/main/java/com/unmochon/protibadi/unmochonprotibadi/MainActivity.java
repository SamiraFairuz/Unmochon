package com.unmochon.protibadi.unmochonprotibadi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.unmochon.protibadi.unmochonprotibadi.utility.Utility;

public class MainActivity extends AppCompatActivity {

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int REQUEST_SCREENSHOT=59706;
    private static int track = 0;
    private static MediaProjectionManager mgr;


    public static Intent newintent;
    public static  int newResultCode;
    private TextView textview_details,know_unmo_textview;
    private Button notify_me;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mgr=(MediaProjectionManager)getBaseContext().getSystemService(MEDIA_PROJECTION_SERVICE);

        /*startActivityForResult(mgr.createScreenCaptureIntent(),
                REQUEST_SCREENSHOT);*/

        newintent = new Intent();
        know_unmo_textview = findViewById(R.id.know_unmo_textview);
        textview_details = findViewById(R.id.textview_details);
        notify_me = findViewById(R.id.notify_me);

        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        // If do not grant write external storage permission.



        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {

           // Log.i(getClass().getSimpleName(), "MainActivity 1");

            startActivityForResult(mgr.createScreenCaptureIntent(),
                        REQUEST_SCREENSHOT);



            if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED )
            {
                // Request user to grant write external storage permission.
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Utility.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);


            }
            if(readExternalStoragePermission!= PackageManager.PERMISSION_GRANTED )
            {
                // Request user to grant write external storage permission.
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Utility.REQUEST_CODE_Read_EXTERNAL_STORAGE_PERMISSION);


            }

            initializeView();
        }

        know_unmo_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (textview_details.getVisibility() == View.VISIBLE){

                    textview_details.setVisibility(View.GONE);
                }
                else {
                    textview_details.setVisibility(View.VISIBLE);

                }

            }
        });
    }

    /**
     * Set and initialize the view elements.
     */


    private void initializeView() {
        notify_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent i=
                        new Intent(MainActivity.this, FloatingViewService.class)
                                .putExtra(FloatingViewService.EXTRA_RESULT_CODE, newResultCode)
                                .putExtra(FloatingViewService.EXTRA_RESULT_INTENT, newintent);

                startService(i);

                finish();


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        //Check if the permission is granted or not.
        newintent = data;
        newResultCode = resultCode;

        if (resultCode == RESULT_OK) {

            initializeView();

        } else { //Permission is not available


            Toast.makeText(this,
                    "Please approve the permission or open the app again.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }




}
