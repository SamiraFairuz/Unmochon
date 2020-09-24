package com.unmochon.protibadi.unmochonprotibadi.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class Utility {
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CODE_Read_EXTERNAL_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CODE_Read_PHONE_STATE_PERMISSION = 1;

    public static final String BitmapImage = "BitmapImagetoEdit";


    public static boolean isConnected(Context context) {

        ConnectivityManager ConnectionManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = ConnectionManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()==true )
        {
            return  true;
        }else {
            return  false;

        }
    }





    public void Log(String tag, String msg){
        boolean willShow = true;
        if (willShow) {
            Log.e(tag, msg);
        }
    }


    public void NetworkError(Context context){
        //isRestarted = false;
        Toast.makeText(context,"ইন্টার্নেট কানেকশন নেই।", Toast.LENGTH_LONG).show();
    }
}
