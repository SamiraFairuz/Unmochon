package com.unmochon.protibadi.unmochonprotibadi.CustomView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawLineCanvas extends View  {

    private Canvas c;

    private Paint pLine, pBg;
    private Path touchPath;

    private Bitmap b;

    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();



    public DrawLineCanvas(Context context) {
        super(context);
    }

    public DrawLineCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);

        pBg = new Paint();
        pBg.setColor(Color.WHITE);

        pLine = new Paint();
        pLine.setColor(Color.BLUE);
        pLine.setAntiAlias(true);
        pLine.setStyle(Paint.Style.STROKE);
        pLine.setStrokeWidth(24);

        touchPath = new Path();
    }

    public DrawLineCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        c = new Canvas(b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                undonePaths.clear();
                touchPath.moveTo(touchX, touchY);

                break;
            case MotionEvent.ACTION_MOVE:
                touchPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                touchPath.lineTo(touchX, touchY);
                c.drawPath(touchPath, pLine);
                paths.add(touchPath);
                touchPath = new Path();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Path p : paths){
            canvas.drawPath(p, pLine);
        }

      //  canvas.drawBitmap(b, 0, 0, pBg);

        canvas.drawPath(touchPath, pLine);
    }


   public void clearAllPath(){
        paths.clear();
        invalidate();
        undonePaths.clear();
        invalidate();
   }


    public void onClickUndo () {


        if (paths.size()>0)
        {
            //Log.i(getClass().getSimpleName(), "in if Undo");

            undonePaths.add(paths.remove(paths.size()-1));
            invalidate();
        }
        else
        {

        }
        //toast the user
    }

    public void onClickRedo (){
        //Log.i(getClass().getSimpleName(), "Redo");

        if (undonePaths.size()>0)
        {
           // Log.i(getClass().getSimpleName(), "in if Redo");

            paths.add(undonePaths.remove(undonePaths.size()-1));
            invalidate();
        }
        else
        {

        }
        //toast the user
    }



}
