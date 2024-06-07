package com.watchpixiv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 用于实现图片识别手势缩放功能
 */
public class DialogImageView extends androidx.appcompat.widget.AppCompatImageView {

    private boolean _firstScale = true;
    public static final float SCALE_MAX = 3.0f;
    private static final float SCALE_MID = 1.5f;
    private final Matrix _scaleMatrix = new Matrix();
    private final float[] _matrixValues = new float[9];

    private ScaleGestureDetector _scaleGestureDetector;
    private float _fitFactor = 1.0f;

    private int _sourceWidth;
    private int _sourceHeight;

    private Bitmap _sourceBitmap;
    public DialogImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DialogImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DialogImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setSourceBitmap(Bitmap bitmap){
        _sourceBitmap = bitmap;
        setImageBitmap(bitmap);
    }

    /*
     * 用于初始缩放图片到合适大小
     */
    public void fitView(){
        Log.d("DialogImageView","fitView");
        setScaleType(ScaleType.MATRIX);
        _sourceWidth = _sourceBitmap.getWidth();
        _sourceHeight = _sourceBitmap.getHeight();
        float xFactor = (float)getWidth()/_sourceWidth;
        float yFactor = (float)getHeight()/_sourceHeight;
        float dX;
        float dY;
        if(xFactor <= yFactor){
            _fitFactor = xFactor;
            dX = 0;
            dY = (getHeight() - _fitFactor*_sourceHeight)/2;
        }else{
            _fitFactor = yFactor;
            dX = (getWidth() - _fitFactor*_sourceWidth)/2;
            dY = 0;
        }
        _scaleMatrix.postScale(_fitFactor,_fitFactor);
        setImageMatrix(_scaleMatrix);
        _scaleMatrix.postTranslate(dX,dY);
        setImageMatrix(_scaleMatrix);

        // 如果这句不执行，最开始加载不出问题，但是缩放会黑屏
        _scaleMatrix.getValues(_matrixValues);
        Log.d("Height", String.valueOf(getHeight()));
        Log.d("Width", String.valueOf(getWidth()));
        Log.d("sHeight", String.valueOf(_sourceHeight));
        Log.d("sWidth", String.valueOf(_sourceWidth));
        Log.d("PosX", String.valueOf(_matrixValues[Matrix.MTRANS_X]));
        Log.d("PosY", String.valueOf(_matrixValues[Matrix.MTRANS_Y]));
    }

    private void init(Context context){

        Log.d("DialogImageView","Init");
        _scaleGestureDetector = new ScaleGestureDetector(context,new ScaleGestureDetector.OnScaleGestureListener(){
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector scaleGestureDetector) {
                float scale = getScale();
                float scaleFactor = scaleGestureDetector.getScaleFactor();
                Log.d("Scale","onScale: "+scaleFactor+","+scaleGestureDetector.getFocusX()+","+scaleGestureDetector.getFocusY() );

                if (getDrawable() == null) {
                    Log.d("Drawable","Is Null");
                    return true;
                }

                if ((scale < SCALE_MAX && scaleFactor > 1.0f) || (scale > _fitFactor && scaleFactor < 1.0f)) {
                    if (scaleFactor * scale < _fitFactor) {
                        scaleFactor = _fitFactor / scale;
                    }
                    if (scaleFactor * scale > SCALE_MAX) {
                        scaleFactor = SCALE_MAX / scale;
                    }
                    // TODO 位置偏移的进一步控制
                    _scaleMatrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                    setImageMatrix(_scaleMatrix);
                }
                return false;
            }

            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector scaleGestureDetector) {
                Log.d("Scale","onScaleBegin");
                if(_firstScale){
                    _firstScale = false;
                    fitView();
                };
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector scaleGestureDetector) {
                Log.d("Scale","onScaleEnd");
            }
        });
        setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                _scaleGestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });

        // 请不要去除这段，否则OnTouchListener将不会执行
        setOnClickListener((view)->{
            Log.d("BigImage","Clicked");
            // dialog.dismiss();
        });

    }

    private float getScale(){
        _scaleMatrix.getValues(_matrixValues);
        return _matrixValues[Matrix.MSCALE_X];
    }

}
