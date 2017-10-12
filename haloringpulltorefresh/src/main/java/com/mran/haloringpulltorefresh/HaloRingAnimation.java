package com.mran.haloringpulltorefresh;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by M on 2017/10/4.
 */

public class HaloRingAnimation extends View {
    private Paint mPointPaint;
    private Paint mRingPaint;
    private int width;
    private int height;
    private int mRingTop ;

    private int mRingRadius;
    private int mRingColor;
    private float mHeight;
    private int mPointColor;
    int mRingRadiusONLY;

    private float mPercent = 0.01f;//0->1
    private Context mContext;
    private ArrayList<HaloPoint> mHaloPoints;
    private float time = 0.5f;
    private float timeRationAlpha = 0;//能随时间改变的透明度
    private float timeRationRadius = 0;//能随时间改变的半径
    private boolean mPullEnd = false;
    PathMeasure pathMeasure;
    AttributeSet mAttributeSet;
    ValueAnimator mPointAlphaRadiusValueAnimator;

    public HaloRingAnimation(Context context) {
        super(context);
        init(context, null);
    }

    public HaloRingAnimation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HaloRingAnimation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mAttributeSet = attrs;

        pathMeasure = new PathMeasure(null, false);
        mHaloPoints = new ArrayList<>();
        for (int i = 0; i < 360; i ++) {
            HaloPoint haloPoint = new HaloPoint();
            haloPoint.setPos(width / 4 + (float) (Math.random() * width / 2), 0);
            haloPoint.setRadius((float) (2 + Math.random() * 3));
            mHaloPoints.add(haloPoint);
        }
        mPointAlphaRadiusValueAnimator = new ValueAnimator();
        mPointAlphaRadiusValueAnimator.setDuration(800);
        mPointAlphaRadiusValueAnimator.setFloatValues(100);
        mPointAlphaRadiusValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mPointAlphaRadiusValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mPointAlphaRadiusValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                timeRationAlpha = (float) animation.getAnimatedValue();
                timeRationRadius = (float) animation.getAnimatedValue() * 0.05f;
                mRingRadius = (int) ((float) animation.getAnimatedValue() * 0.1f + mRingRadiusONLY-5);
                invalidate();
            }
        });
    }

    public void initParams() {

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStrokeWidth(8);
        mPointPaint.setColor(mPointColor);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setShadowLayer(6, 0, 0, mPointColor);

        mRingPaint = new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(mRingColor);
        mRingPaint.setShadowLayer(6, 0, 0, mRingColor);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(12);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getSize(300, widthMeasureSpec);/*bottombar的宽度*/
        height = getSize(300, heightMeasureSpec);/*--的高度*/
        if (mHaloPoints != null)
            for (int i = 0; i <360; i++) {
                HaloPoint haloPoint = mHaloPoints.get(i);
                haloPoint.setRadius((float) (1 + Math.random() * 3));
                setStartPos((float) (Math.random() * width), -5, haloPoint.pos);
                setEndPos(width / 2, mRingTop + mRingRadius, -90 + i, haloPoint.endPos);
                haloPoint.mPath = setPath(haloPoint.pos, haloPoint.endPos);
                pathMeasure.setPath(haloPoint.mPath, false);
                haloPoint.length = pathMeasure.getLength();
                haloPoint.startRatio = i > 180 ? (1 - (i / 360.0f)) : time / 180 * i;//设置point应该何时出现
                haloPoint.alpha = 100 + (int) (50 * Math.random());
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

    private Path setPath(float startPos[], float endPos[]) {
        Path path = new Path();
        path.moveTo(startPos[0], startPos[1]);
        path.quadTo((float) (Math.random() * width), (float) (Math.random() * mRingTop), (float) width / 4 + (float) (Math.random() * width / 2), (float) (Math.random() * mRingTop));
        path.quadTo((float) (Math.random() * width), (float) (Math.random() * mRingTop), endPos[0], endPos[1]);
//        path.lineTo(endPos[0], endPos[1]);
        return path;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mPullEnd) {
            for (int i = 0; i < 360; i+=5) {//调整i的间隔数可以实现不同的效果
                HaloPoint haloPoint = mHaloPoints.get(i);
                haloPoint.drawAble = haloPoint.startRatio < mPercent;
                if (!haloPoint.drawAble)
                    continue;
                pathMeasure.setPath(haloPoint.mPath, false);
                pathMeasure.getPosTan(haloPoint.length * (mPercent - haloPoint.startRatio) / time, haloPoint.pos, null);
                int alpha = Math.min((int) (haloPoint.alpha + i + timeRationAlpha), 255);
                mPointPaint.setAlpha(alpha);

                float pointRadius;
                if (i % 2 == 0)
                    pointRadius = (float) (timeRationRadius * ((Math.cos((mPercent - haloPoint.startRatio) / time * 100))) + 6);
                else
                    pointRadius = (float) (timeRationRadius * (Math.abs(Math.sin((mPercent - haloPoint.startRatio) / time * 100))) + 4);

                if ((mPercent - haloPoint.startRatio) / time >= 1) {
                    pointRadius = 7;
                    mPointPaint.setAlpha(255);
                    mPointPaint.clearShadowLayer();
                }
//                haloPoint.radius = mPointRadius;
                canvas.drawCircle(haloPoint.pos[0], haloPoint.pos[1], pointRadius, mPointPaint);
            }
        } else {
            canvas.drawCircle(width / 2, mRingTop + mRingRadiusONLY, mRingRadius, mRingPaint);
        }
    }


    public void setPercent(float percent) {
        this.mPercent = percent;
    }

    public void startAnimation() {
        mPointAlphaRadiusValueAnimator.start();
    }

    public boolean isAnimationRunning() {
        return mPointAlphaRadiusValueAnimator != null && mPointAlphaRadiusValueAnimator.isRunning();
    }

    public void setEndpull(boolean endPull) {
        this.mPullEnd = endPull;
    }

    public void stopAnimation() {
        if (mPointAlphaRadiusValueAnimator != null && mPointAlphaRadiusValueAnimator.isRunning())
            mPointAlphaRadiusValueAnimator.cancel();
    }

    public void setAnimationCurrentPlayTime(long time) {
        if (mPointAlphaRadiusValueAnimator != null) {
            mPointAlphaRadiusValueAnimator.setCurrentPlayTime(time);
        }
    }

    public int getRingRadius() {
        return mRingRadius;
    }
    public void setRingRadius(int ringRadius) {
        mRingRadius = ringRadius;
        mRingRadiusONLY=ringRadius;
    }

    public int getRingColor() {
        return mRingColor;
    }

    public void setRingColor(int ringColor) {
        mRingColor = ringColor;
    }

    public int getPointColor() {
        return mPointColor;
    }

    public void setPointColor(int pointColor) {
        mPointColor = pointColor;
    }

    public int getRingTop() {
        return mRingTop;
    }

    public void setRingTop(int ringTop) {
        mRingTop = ringTop;
    }

    private class HaloPoint {
        float pos[] = {0, 0};//坐标,x,y;
        float radius = 8;//点的半径
        Path mPath;
        float length;
        float endPos[] = {0, 0};//终点位置坐标
        float startRatio = 0f;
        boolean drawAble = false;
        int alpha;

        void setPos(float x, float y) {
            this.pos[0] = x;
            this.pos[1] = y;
        }

        void setRadius(float radius) {
            this.radius = radius;
        }
    }
}
