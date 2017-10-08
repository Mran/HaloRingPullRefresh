package com.mran.haloringpulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by M on 2017/10/4.
 */

public class HaloRingAnimation extends View {
    private Paint mPaint;
    int width;
    int height;
    int CICLE = 120;
    int mRingRadius;
    Path mPath;
    int startAngle = 270;//270->90
    int sweepAngle = 0;//0->360
    float ratio = 0.01f;//0->1
    private Context mContext;
    private AttributeSet mAttributeSet;
    private ArrayList<HaloPoint> mHaloPoints;
    float lasty = 0;
    boolean endPull = true;
    float mInitialDownY;//手指按下的位置
    float mTouchSlop = 8;//最小滑动值
    boolean mIsBeingDragged = false;//是否开始滑动
    float mInitialMotionY;//开始滑动的位置
    float mTotalDragDistance = 100;

    public HaloRingAnimation(Context context) {
        super(context);
    }

    public HaloRingAnimation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HaloRingAnimation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attrs) {
        mRingRadius = 50;
        mContext = context;
        mAttributeSet = attrs;
        mPaint = new Paint();
        mPaint.setColor(0xFF3C49FF);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setShadowLayer(20, 0, 0, 0xFF3C49FF);
        mPath = new Path();
        mPaint.setTextSize(20);
        pathMeasure = new PathMeasure(mPath, false);
        mHaloPoints = new ArrayList<>();
        for (int i = 0; i < 360; i++) {
            HaloPoint haloPoint = new HaloPoint();
            haloPoint.setPos((float) (Math.random() * width), (float) (10 * Math.random()));
            haloPoint.setRadius((float) (2 + Math.random() * 3));
            mHaloPoints.add(haloPoint);
        }
    }

    PathMeasure pathMeasure;
    float time = 0.5f;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getSize(300, widthMeasureSpec);/*bottombar的宽度*/
        height = getSize(300, heightMeasureSpec);/*--的高度*/

        for (int i = 0; i < 360; i++) {
            HaloPoint haloPoint = mHaloPoints.get(i);
            haloPoint.setRadius((float) (1 + Math.random() * 3));
            setStartPos((float) (Math.random() * width), -5, haloPoint.pos);
            setEndPos(width / 2, CICLE + mRingRadius, -90 + i, haloPoint.endPos);
            haloPoint.mPath = setPath(haloPoint.pos, haloPoint.endPos);
            pathMeasure.setPath(haloPoint.mPath, false);
            haloPoint.length = pathMeasure.getLength();
            haloPoint.startRatio = i > 180 ? (1 - (i / 360.0f)) : time / 180 * i;
            Log.d("HaloRingAnimation", "onMeasure: startRatio" + haloPoint.startRatio + "---i=" + i);
        }

    }

    private int getSize(int defaultSize, int measureSpec) {

        int mySize = defaultSize;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                mySize = defaultSize;
                break;
            case MeasureSpec.AT_MOST:
                mySize = size;
                break;
            case MeasureSpec.EXACTLY:
                mySize = size;
                break;
        }
        return mySize;
    }

    void setStartPos(float xDot, float yDot, float pos[]) {
        pos[0] = xDot;
        pos[1] = yDot;
    }

    void setEndPos(int xDot, int yDot, int radius, float pos[]) {
        pos[0] = (float) (xDot + mRingRadius * (Math.cos(radius * Math.PI / 180)));
        pos[1] = (float) (yDot + mRingRadius * (Math.sin(radius * Math.PI / 180)));
    }

    Path setPath(float startPos[], float endPos[]) {
        Path path = new Path();
        path.moveTo(startPos[0], startPos[1]);
        path.quadTo((float) (Math.random() * width), (float) (Math.random() * CICLE), (float) (endPos[0] * Math.random()), (float) (endPos[1] * Math.random()));
        path.quadTo((float) (Math.random() * width), (float) (Math.random() * CICLE), endPos[0], endPos[1]);
//        path.lineTo(endPos[0], endPos[1]);
        return path;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIsBeingDragged) {
            if (ratio >= 1)
                ratio = 1;
            mPaint.setColor(0xff4f5ff3);
            for (int i = 0; i < 360; i += 6) {
                HaloPoint haloPoint = mHaloPoints.get(i);
                mPaint.setStyle(Paint.Style.FILL);
                haloPoint.drawAble = haloPoint.startRatio < ratio;

                pathMeasure.setPath(haloPoint.mPath, false);
                pathMeasure.getPosTan(haloPoint.length * (ratio - haloPoint.startRatio) / time, haloPoint.pos, null);
//                canvas.drawPoints(haloPoint.pos, mPaint);
//                if (ratio <= haloPoint.startRatio)
                haloPoint.radius = (float) (Math.abs(Math.sin((ratio - haloPoint.startRatio) / time * 100)) * 5 + 1);
//                if (haloPoint.pos[0] == haloPoint.endPos[0] && haloPoint.pos[1] == haloPoint.endPos[1])
//                    haloPoint.radius = 5;
                canvas.drawCircle(haloPoint.pos[0], haloPoint.pos[1], haloPoint.radius, mPaint);

            }
            {
                mPaint.setColor(0xFFFFEA00);
                HaloPoint haloPoint = mHaloPoints.get(180);
                haloPoint.drawAble = haloPoint.startRatio < ratio;

                pathMeasure.setPath(haloPoint.mPath, false);
                pathMeasure.getPosTan(haloPoint.length * (ratio - haloPoint.startRatio) / time, haloPoint.pos, null);
//                canvas.drawPoints(haloPoint.pos, mPaint);
                haloPoint.radius = (float) (3 + Math.random() * 1);
                canvas.drawCircle(haloPoint.pos[0], haloPoint.pos[1], haloPoint.radius - 1, mPaint);
                Log.d("HaloRingAnimation", "onDraw:length " + haloPoint.length * (ratio - haloPoint.startRatio) / time);
                mPaint.setStrokeWidth(8);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawText(String.valueOf(haloPoint.length * (ratio - haloPoint.startRatio) / time), 30, 50, mPaint);
                mPaint.setStyle(Paint.Style.STROKE);
            }
            if (mHaloPoints.get(180).drawAble)
                Log.d("HaloRingAnimation", "onDraw:length " + mHaloPoints.get(180).length * (ratio - mHaloPoints.get(180).startRatio) / time);
        }
        mPaint.setStrokeWidth(8);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(String.valueOf(ratio), 30, 30, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        if (endPull) {
            mPaint.setColor(0xFF3C49FF);
            startAngle = (int) (270 - 180 * ratio);
            sweepAngle = (int) (360 * ratio);
            mPath.addArc(width / 2 - mRingRadius, CICLE, width / 2 + mRingRadius, CICLE + mRingRadius * 2, startAngle, sweepAngle);
//            canvas.drawPath(mPath, mPaint);
            mPath.reset();

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lasty = event.getY();
                mIsBeingDragged = false;
                mInitialDownY = event.getY();
                Log.d("HaloRingAnimation", "dispatchTouchEvent:ACTION_DOWN ");
                return true;

            case MotionEvent.ACTION_MOVE:
                Log.d("HaloRingAnimation", "dispatchTouchEvent:ACTION_MOVE ");
                float y = event.getY();
                startDragging(y);

                if (mIsBeingDragged) {

                    final float overscrollTop = (y - mInitialMotionY) * dragRatio;
                    if (overscrollTop > 0) {
                        finggerMove(overscrollTop);
                    } else {
                        return false;
                    }
                }
                invalidate();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    float dragRatio = 0.3f;

    private void finggerMove(float overscrollTop) {
        float originalDragPercent = overscrollTop / mTotalDragDistance;

        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
//        ratio= (float) Math.log(dragPercent*9+1);
        ratio = dragPercent;
        dragRatio = 0.25f - 0.1f * ratio;

    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d("HaloRingAnimation", "onTouchEvent:ACTION_DOWN ");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("HaloRingAnimation", "onTouchEvent:ACTION_MOVE ");

                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private class HaloPoint {
        float pos[] = {0, 0};//坐标,x,y;
        float radius = 8;//点的半径
        Path mPath;
        float length;
        float endPos[] = {0, 0};//终点位置坐标
        float startRatio = 0f;
        boolean drawAble = false;

        public float[] getPos() {
            return pos;
        }

        void setPos(float x, float y) {
            this.pos[0] = x;
            this.pos[1] = y;
        }


        public float getRadius() {
            return radius;
        }

        void setRadius(float radius) {
            this.radius = radius;
        }
    }
}
