package com.mran.haloringpulltorefresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

/**
 * Created by M on 2017/10/10.
 */

public class HaloRingRefreshLayout extends FrameLayout {
    Context mContext;
    AttributeSet mAttributeSet;
    View mChildView;
    HaloRingAnimation mHeaderView;
    float mInitialDownY;//手指按下的位置
    float mTouchSlop = 8;//最小滑动值,用来判断拖动是否有效
    float mInitialMotionY;//开始滑动的位置
    float mMaxDragDistance = 280;//childView的最大移动距离
    float mPercent = 0.01f;//当前拖动的百分比
    float dragRatio = 0.3f;//拖动阻力
    ValueAnimator mBackUpValueAnimator;
    long BACK_UP_DURATION = 350;//返回向上的持续时间
    boolean mIsRefreshing = false;//正在刷新的标记
    boolean mIsBeingDragged = false;//是否开始滑动
    boolean mPullEnd;//下拉到位的标记
    float overScrollTop;//当前childView移动的距离
    int mRingRadius;
    int mRingColor;
    float mHeaderHeight;
    int mPointColor;
    int mRingTop;


    public HaloRingRefreshLayout(@NonNull Context context) {
        super(context);
    }

    public HaloRingRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public HaloRingRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mContext = context;
        mAttributeSet = attributeSet;
        if (getChildCount() > 1) {
            throw new RuntimeException("you can only attach one child");
        }
        getAttrs();
        this.post(new Runnable() {
            @Override
            public void run() {
                mChildView = getChildAt(0);
                addHeaderView();
            }
        });

        mBackUpValueAnimator = new ValueAnimator();
        mBackUpValueAnimator.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return (float) (1.0 - (1.0 - input) * (1.0 - input));
            }
        });
        mBackUpValueAnimator.setDuration(BACK_UP_DURATION);
        mBackUpValueAnimator.setFloatValues(1);
        mBackUpValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float offsetUp = mMaxDragDistance * (1.0f - (float) animation.getAnimatedValue());
                mChildView.setTranslationY(offsetUp);
                fingerMove(offsetUp);
                mHeaderView.setPercent(mPercent);
            }
        });

        mBackUpValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHeaderView.stopAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void addHeaderView() {
        mHeaderView = new HaloRingAnimation(mContext);
        mHeaderView.setRingRadius(mRingRadius);
        mHeaderView.setPointColor(mPointColor);
        mHeaderView.setRingColor(mRingColor);
        mHeaderView.setRingTop(mRingTop);
        mHeaderView.initParams();
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mHeaderHeight);
        params.gravity = Gravity.TOP;
        mHeaderView.setLayoutParams(params);
        addViewInternal(mHeaderView);
    }

    private void addViewInternal(@NonNull View child) {
        super.addView(child, 0);
    }

    @Override
    public void addView(View child) {
        if (getChildCount() >= 1) {
            throw new RuntimeException("you can only attach one child");
        }
        mChildView = child;
        super.addView(child);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (mIsRefreshing) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsRefreshing = false;
                mInitialDownY = event.getY();
                mIsBeingDragged = false;
                mHeaderView.setEndpull(false);
                Log.d("HaloRingRefreshLayout", "onInterceptTouchEvent: ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                startDragging(y);
                Log.d("HaloRingRefreshLayout", "onInterceptTouchEvent: ACTION_MOVE");

                if (mIsBeingDragged)
                    return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mIsRefreshing) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                startDragging(y);

                if (mIsBeingDragged) {
                    overScrollTop = (y - mInitialMotionY) * dragRatio;
                    if (overScrollTop > 0) {
                        fingerMove(overScrollTop);
                        mHeaderView.setPercent(mPercent);
                        if (overScrollTop < mMaxDragDistance) {
                            if (!mHeaderView.isAnimationRunning())
                                mHeaderView.startAnimation();
                            mChildView.setTranslationY(overScrollTop);
                        }
                    } else {
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                if (mPercent == 1) {
                    mPullEnd = true;
                    mIsRefreshing = true;
                    mHeaderView.setEndpull(true);
                    mHeaderView.setAnimationCurrentPlayTime(400);
                } else {
                    mPullEnd = false;
                    mIsRefreshing = false;
                    mHeaderView.setEndpull(false);
                    mBackUpValueAnimator.setCurrentPlayTime((long) ((1 - mPercent) * BACK_UP_DURATION));
                    mBackUpValueAnimator.start();

                }
                break;
        }
        return true;
    }

    private void fingerMove(float overScrollTop) {
        float originalDragPercent = overScrollTop / mMaxDragDistance;//确定当前移动的百分比
        mPercent = Math.min(1f, Math.abs(originalDragPercent));
        dragRatio = 0.5f - 0.15f * mPercent;//产生拖动的阻力效果

    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
            mPullEnd = false;
        }
    }
    private void getAttrs() {
        TypedArray typedArray = mContext.obtainStyledAttributes(mAttributeSet, R.styleable.HaloRingRefreshLayout);
        mRingRadius = (int) typedArray.getDimension(R.styleable.HaloRingRefreshLayout_ringRadius, 50);
        mRingTop = (int) typedArray.getDimension(R.styleable.HaloRingRefreshLayout_ringTop, 120);
        mRingColor = typedArray.getColor(R.styleable.HaloRingRefreshLayout_ringColor, 0xFF828EFA);
        mHeaderHeight = typedArray.getDimension(R.styleable.HaloRingRefreshLayout_headerHeight,mMaxDragDistance);
        mPointColor = typedArray.getColor(R.styleable.HaloRingRefreshLayout_pointColor, 0xFF828EFA);
        mMaxDragDistance=mHeaderHeight;
        typedArray.recycle();


    }

    public float getMaxDragDistance() {
        return mMaxDragDistance;
    }

    public void setMaxDragDistance(float maxDragDistance) {
        mMaxDragDistance = maxDragDistance;
    }

    public float getPercent() {
        return mPercent;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    public boolean isBeingDragged() {
        return mIsBeingDragged;
    }

    public boolean isPullEnd() {
        return mPullEnd;
    }

    public void stopRefresh() {
        if (mIsRefreshing) {
            mPullEnd = false;
            mIsRefreshing = false;
            mHeaderView.setEndpull(false);
            mBackUpValueAnimator.setCurrentPlayTime((long) ((1 - mPercent) * BACK_UP_DURATION));
            mBackUpValueAnimator.start();
        }
    }
}
