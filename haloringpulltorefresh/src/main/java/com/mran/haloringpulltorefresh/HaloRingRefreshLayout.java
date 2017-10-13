package com.mran.haloringpulltorefresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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
    private float mInitialDownY;//手指按下的位置
    private float MTOUCHSLOP = 8;//最小滑动值,用来判断拖动是否有效
    private float mInitialMotionY;//开始滑动的位置
    private float mMaxDragDistance;//childView的最大移动距离
    private float mPercent = 0.00f;//当前拖动的百分比
    private float dragRatio = 0.3f;//拖动阻力
    private ValueAnimator mBackUpValueAnimator;//向上返回的动画
    private long BACK_UP_DURATION = 350;//返回向上的持续时间
    private boolean mEnableRefresh = true;
    private boolean mIsRefreshing = false;//正在刷新的标记
    private boolean mIsBeingDragged = false;//是否开始滑动
    private boolean mPullEnd;//下拉到位的标记
    private int mRingRadius;//0光环的半径
    private int mRingColor;//光环的颜色
    private float mHeaderHeight;//光环的整体view的高度
    private int mPointColor;//形成光环的点的颜色
    private int mRingTop;//光环距离上部的高度


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

    //进行一些初始化操作
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
        //对返回动画设置一个插值器
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
                if (mChildView != null) {
                    mChildView.setTranslationY(offsetUp);
                    fingerMove(offsetUp);
                    mHeaderView.setPercent(mPercent);
                }
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

    //添加HaloRingAnimation,并进行一些初始化
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
        //确保只能添加一个view
        if (getChildCount() >= 1) {
            throw new RuntimeException("you can only attach one child");
        }
        mChildView = child;
        super.addView(child);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (mIsRefreshing || !mEnableRefresh) {
            return super.onInterceptTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsRefreshing = false;
                mInitialDownY = event.getY();
                mIsBeingDragged = false;
                mHeaderView.setEndpull(false);
                break;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                startDragging(y);
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
                    //加入滑动阻力,计算出控件应该移动的距离
                    float overScrollTop = (y - mInitialMotionY) * dragRatio;
                    if (overScrollTop > 0) {
                        fingerMove(overScrollTop);
                        mHeaderView.setPercent(mPercent);
                        if (overScrollTop < mMaxDragDistance) {
                            if (!mHeaderView.isAnimationRunning())
                                mHeaderView.startAnimation();
                            if (mChildView != null)
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
                //根据不同的滑动程度来确定不同的手指离开后的效果
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

    //获取当前移动的百分比,并调整滑动阻力
    private void fingerMove(float overScrollTop) {
        float originalDragPercent = overScrollTop / mMaxDragDistance;//确定当前移动的百分比
        mPercent = Math.min(1f, Math.abs(originalDragPercent));
        dragRatio = 0.5f - 0.15f * mPercent;//产生拖动的阻力效果

    }

    //判断是否在滑动
    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > MTOUCHSLOP && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + MTOUCHSLOP;
            mIsBeingDragged = true;
            mPullEnd = false;
        }
    }

    private void getAttrs() {
        TypedArray typedArray = mContext.obtainStyledAttributes(mAttributeSet, R.styleable.HaloRingRefreshLayout);
        mRingRadius = (int) typedArray.getDimension(R.styleable.HaloRingRefreshLayout_ringRadius, 50);
        mRingTop = (int) typedArray.getDimension(R.styleable.HaloRingRefreshLayout_ringTop, 120);
        mRingColor = typedArray.getColor(R.styleable.HaloRingRefreshLayout_ringColor, 0xFF828EFA);
        mHeaderHeight = typedArray.getDimension(R.styleable.HaloRingRefreshLayout_headerHeight, 280);
        mPointColor = typedArray.getColor(R.styleable.HaloRingRefreshLayout_pointColor, 0xFF828EFA);
        mMaxDragDistance = mHeaderHeight;
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

    public boolean isEnableRefresh() {
        return mEnableRefresh;
    }

    public void setEnableRefresh(boolean enableRefresh) {
        mEnableRefresh = enableRefresh;
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
