package com.watchpixiv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

/**
 * 用于实现图片识别手势缩放功能
 */
public class DialogImageView extends androidx.appcompat.widget.AppCompatImageView {

    public String filename;
    private boolean _firstScale = true;
    public static final float SCALE_MAX = 3.0f;
    private static final float SCALE_MIN = 0.7f;
    private final Matrix _scaleMatrix = new Matrix();
    private final float[] _matrixValues = new float[9];

    private ScaleGestureDetector _scaleGestureDetector;
    private GestureDetector _gestureDetector;
    private float _fitScale = 1.0f;
    private float _nowScale = 1.0f;
    private float _maxScale;
    private float _minScale;

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
    public void initView(){
        Log.d("DialogImageView","InitView");
        setScaleType(ScaleType.MATRIX);
        _sourceWidth = _sourceBitmap.getWidth();
        _sourceHeight = _sourceBitmap.getHeight();
        float xFactor = (float)getWidth()/_sourceWidth;
        float yFactor = (float)getHeight()/_sourceHeight;
        float dX;
        float dY;
        if(xFactor <= yFactor){
            _fitScale = xFactor;
            _maxScale = yFactor * SCALE_MAX;
            _minScale = xFactor * SCALE_MIN;
            dX = 0;
            dY = (getHeight() - _fitScale *_sourceHeight)/2;
        }else{
            _fitScale = yFactor;
            _maxScale = xFactor * SCALE_MAX;
            _minScale = yFactor * SCALE_MIN;
            dX = (getWidth() - _fitScale *_sourceWidth)/2;
            dY = 0;
        }
        _scaleMatrix.postScale(_fitScale, _fitScale);
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

    private void fitView(){
        Log.d("DialogImageView","FitView");
        float scaleFactor = _fitScale/getScale();
        _scaleMatrix.postScale(scaleFactor,scaleFactor);
        _scaleMatrix.getValues(_matrixValues);
        float left = _matrixValues[Matrix.MTRANS_X];
        float top = _matrixValues[Matrix.MTRANS_Y];
        float width = _fitScale * _sourceWidth;
        float height = _fitScale * _sourceHeight;
        _scaleMatrix.postTranslate(
                (getWidth() - width) / 2 - left,
                (getHeight() - height) / 2 - top
        );
        setImageMatrix(_scaleMatrix);
//        Log.d("Trans","left: "+left+",top:"+top+",width:"+width+",height:"+height+",dX:"+((getWidth() - width) / 2 - left)+",dY:"+((getHeight() - height) / 2 - top)+",vW:"+getWidth()+",vH:"+getHeight());
    }
    private void init(Context context){

        Log.d("DialogImageView","Init");
        _scaleGestureDetector = new ScaleGestureDetector(context,new ScaleGestureDetector.OnScaleGestureListener(){
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector scaleGestureDetector) {
                float scale = getScale();
                float scaleFactor = scaleGestureDetector.getScaleFactor();
                Log.d("Scale","onScale: "+scaleFactor+","+scale+","+scaleGestureDetector.getFocusX()+","+scaleGestureDetector.getFocusY() );

                if (getDrawable() == null) {
                    Log.d("Drawable","Is Null");
                    return true;
                }

                if (_minScale < _nowScale*scaleFactor && _nowScale*scaleFactor< _maxScale){
                    scaleFactor = _nowScale/scale * scaleFactor;
                    _scaleMatrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                    // 位置偏移的进一步控制
                    _scaleMatrix.getValues(_matrixValues);
                    float dX = 0f;
                    float dY = 0f;
                    float left = _matrixValues[Matrix.MTRANS_X];
                    float top = _matrixValues[Matrix.MTRANS_Y];
                    float width = _matrixValues[Matrix.MSCALE_X] * _sourceWidth;
                    float height = _matrixValues[Matrix.MSCALE_Y] * _sourceHeight;
                    if (width > getWidth()){
                        if(left > 0){
                            dX = -left;
                        } else if (left + width < getWidth()) {
                            dX = getWidth() - left - width;
                        }
                    }else{
                        dX = (getWidth() - width) / 2 - left;
                    }
                    if (height > getHeight()){
                        if(top > 0){
                            dY = -top;
                        } else if (top + height < getHeight()) {
                            dY = getHeight() - top - height;
                        }
                    }else{
                        dY = (getHeight() - height) / 2 - top;
                    }
//                    Log.d("Trans","left: "+left+",top:"+top+",width:"+width+",height:"+height+",dX:"+dX+",dY:"+dY+",vW:"+getWidth()+",vH:"+getHeight());
//                    Log.d("Scale","TransX:" +_matrixValues[Matrix.MTRANS_X] + "TransY:"+_matrixValues[Matrix.MTRANS_Y]);
                    _scaleMatrix.postTranslate(dX,dY);
                    setImageMatrix(_scaleMatrix);
                }

                return false;
            }

            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector scaleGestureDetector) {
                Log.d("Scale","onScaleBegin");
                if(_firstScale){
                    _firstScale = false;
                    initView();
                };
                _nowScale = getScale();
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector scaleGestureDetector) {
                Log.d("Scale","onScaleEnd");
            }
        });

        _gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(@NonNull MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
                Log.d("Scroll","v:"+v+",v1:"+v1);
                _scaleMatrix.getValues(_matrixValues);
                float dX = 0f;
                float dY = 0f;
                float left = _matrixValues[Matrix.MTRANS_X];
                float top = _matrixValues[Matrix.MTRANS_Y];
                float width = _matrixValues[Matrix.MSCALE_X] * _sourceWidth;
                float height = _matrixValues[Matrix.MSCALE_Y] * _sourceHeight;
                if(left - v < 0 && left + width - v > getWidth()){
                    dX = -v;
                }
                if(top - v1 < 0 && top + height - v1 > getHeight()){
                    dY = -v1;
                }
                _scaleMatrix.postTranslate(dX,dY);
                setImageMatrix(_scaleMatrix);
                return false;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent motionEvent) {
                Log.d("ShowPress","x:"+motionEvent.getX()+",y:"+motionEvent.getY());
                Snackbar.make(context,DialogImageView.this,"要保存图片到相册吗？",Snackbar.LENGTH_SHORT)
                        .setAction("保存", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MediaStore.Images.Media.insertImage(
                                        context.getContentResolver(),
                                        _sourceBitmap,
                                        filename,
                                        null
                                        );
                                Snackbar.make(context,DialogImageView.this,"已保存到相册",Snackbar.LENGTH_SHORT).show();
                                Log.d("Snackbar","Saved");
                            }
                        })
                        .show();
            }

            @Override
            public boolean onFling(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
                return false;
            }
        });

        _gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent motionEvent) {
                fitView();
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(@NonNull MotionEvent motionEvent) {
                return false;
            }
        });

        setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                _scaleGestureDetector.onTouchEvent(motionEvent);
                _gestureDetector.onTouchEvent(motionEvent);
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
