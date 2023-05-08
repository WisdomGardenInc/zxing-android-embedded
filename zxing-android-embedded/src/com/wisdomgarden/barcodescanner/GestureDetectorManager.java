package com.wisdomgarden.barcodescanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.zxing.common.detector.MathUtils;

public class GestureDetectorManager extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
    private final GestureDetector detector;
    private float mOldDis;
    private final GestureHandler gestureHandler;

    public GestureDetectorManager(Context context, View viewfinderView, GestureHandler gestureHandler) {
        this.detector = new GestureDetector(context, this);
        this.gestureHandler = gestureHandler;
        viewfinderView.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        detector.onTouchEvent(event);
        int pointCount = event.getPointerCount();
        if (pointCount != 2) {
            return false;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mOldDis = getFingerSpacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                float newDis = getFingerSpacing(event);
                if (newDis > mOldDis) {
                    handleZoom(true);
                } else if (newDis < mOldDis) {
                    handleZoom(false);
                }
                mOldDis = newDis;
                break;
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return super.onDoubleTap(e);
    }

    private float getFingerSpacing(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            return MathUtils.distance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
        }
        return -1;
    }

    private void handleZoom(boolean isZoomIn) {
        gestureHandler.zoom(isZoomIn);
    }

    public interface GestureHandler {
        void zoom(boolean isZoomIn);
    }
}

