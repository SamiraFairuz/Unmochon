package com.unmochon.protibadi.unmochonprotibadi;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.unmochon.protibadi.unmochonprotibadi.utility.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FloatingViewService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private View imageLayout;
    File imgFile;

    private ImageView imageView,imageViewicon;
    private static final String TAG = "FloatingViewService";

    private int resultCode;
    private static Intent resultData;


    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    final private HandlerThread handlerThread=
            new HandlerThread(getClass().getSimpleName(),
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    private MediaProjectionManager mgr;
    private WindowManager wmgr;
    private ImageTransmogrifier it;
    static final int VIRT_DISPLAY_FLAGS=
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    static final String EXTRA_RESULT_CODE="resultCode";
    static final String EXTRA_RESULT_INTENT="resultIntent";
    private View collapsedView, expandedView;
    private byte[] byteArrayofImage;
    private  SharedPreferences.Editor edit;
    private SharedPreferences shre;


    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);

        wmgr=(WindowManager)getSystemService(WINDOW_SERVICE);

        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());
        shre = PreferenceManager.getDefaultSharedPreferences(FloatingViewService.this);
        edit =shre.edit();

        Log.i(getClass().getSimpleName(), "FloatingViewService 1");


        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        /*//The root element of the collapsed view layout
        final View collapsedView = mFloatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view layout
        final View expandedView = mFloatingView.findViewById(R.id.expanded_container);
*/

        //The root element of the collapsed view layout
         collapsedView = mFloatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view layout
         expandedView = mFloatingView.findViewById(R.id.expanded_container);
         imageViewicon = mFloatingView.findViewById(R.id.imageViewicon);


        //Set the close button
        ImageView closeButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.close_btn);
        imageView = (ImageView) mFloatingView.findViewById(R.id.imageViewicon);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the service and remove the from from the window


                stopSelf();
            }
        });


        ImageView screenshotButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.collapsed_screenshot_button);
        screenshotButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int writeExternalStoragePermission = ContextCompat.checkSelfPermission(FloatingViewService.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int readExternalStoragePermission = ContextCompat.checkSelfPermission(FloatingViewService.this, Manifest.permission.READ_EXTERNAL_STORAGE);


                if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED && readExternalStoragePermission!= PackageManager.PERMISSION_GRANTED )
                {
                    // Request user to grant write external storage permission.
                    Toast.makeText(FloatingViewService.this,
                    "You did not give permission to access files , please open Unmochon app again and give access.", Toast.LENGTH_LONG).show();


                }
                else {
                    startCapture();

                }
            }
        });

        ImageView editButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.collapsedactivity_button);
        editButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.e(getClass().getSimpleName(), "Here in FloatingService");

                if (byteArrayofImage != null) {

                    String encodedImage = Base64.encodeToString(byteArrayofImage, Base64.DEFAULT);
                    Log.i(getClass().getSimpleName(), "byteArray"+byteArrayofImage);
                    byteArrayofImage = null;



                    edit.putString(Utility.BitmapImage,encodedImage);
                    edit.commit();

                    Intent dialogIntent = new Intent(FloatingViewService.this, DrawLineWithFingerActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //dialogIntent.putExtra(Utility.BitmapImage, byteArrayofImage);
                    startActivity(dialogIntent);
                }else{
                    Log.i(getClass().getSimpleName(), "bytearray null");

                }
            }
        });


        /*ImageView sendButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.collapsed_send_button);
        sendButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/



        //Set the close button
        Button closeButton = (Button) mFloatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });

        //Open the application on thi button click
        Button openButton = (Button) mFloatingView.findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open the application  click.
                Intent intent = new Intent(FloatingViewService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                //close the service and remove view from the view hierarchy
                stopSelf();
            }
        });

        //Drag and move floating view using user's touch action.
        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        /*if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                startCapture();
//                                collapsedView.setVisibility(View.GONE);
//                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }*/
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }



    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private boolean isViewCollapsed() {

        Log.i(getClass().getSimpleName(), "isViewCollapsed called "+mFloatingView.findViewById(R.id.collapse_view).getVisibility() );

        return mFloatingView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {


         stopCapture();
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);

    }



    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        if (i.getAction()== null) {

            resultCode=i.getIntExtra(EXTRA_RESULT_CODE, 1337);
            resultData=i.getParcelableExtra(EXTRA_RESULT_INTENT);

        }
        else  {

                Intent ui=
                        new Intent(this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(ui);

        }


        return(START_NOT_STICKY);
    }


    private void startCapture() {

        collapsedView.setVisibility(View.GONE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                collapsedView.setVisibility(View.VISIBLE); // Shows view
            }
        }, 3000); // After 3 seconds



        projection = mgr.getMediaProjection(resultCode, resultData);
        it=new ImageTransmogrifier(FloatingViewService.this);

        MediaProjection.Callback cb=new MediaProjection.Callback() {
            @Override
            public void onStop() {

                vdisplay.release();

            }
        };

        vdisplay=projection.createVirtualDisplay("unmochon",
                it.getWidth(), it.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
        projection.registerCallback(cb, handler);
    }

    WindowManager getWindowManager() {
        return(wmgr);
    }

    Handler getHandler() {
        return(handler);
    }

    void processImage(final byte[] png, final Bitmap bitmap) {



        String root = Environment.getExternalStorageDirectory().toString();
        File newDir = new File(root+"/Unmochon");
        newDir.mkdirs();


        String fileStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File output = new File(newDir, "unmochon_"+fileStamp);


        try {


            FileOutputStream fos=new FileOutputStream(output);

            fos.write(png);
            fos.flush();
            fos.getFD().sync();
            fos.close();

            Log.i(getClass().getSimpleName(), "Screen captured 1 png ->"+png);

            byteArrayofImage = png;

            MediaScannerConnection.scanFile(FloatingViewService.this,
                    new String[] {output.getAbsolutePath()},
                    new String[] {"image/png"},
                    null);



        }
        catch (Exception e) {

            e.printStackTrace();

            //Log.i(getClass().getSimpleName(), "Exception writing out screenshot", e);
        }



        stopCapture();

    }




    private void stopCapture() {
        if (projection!=null) {
            projection.stop();
            vdisplay.release();
            projection=null;
        }
    }
}
