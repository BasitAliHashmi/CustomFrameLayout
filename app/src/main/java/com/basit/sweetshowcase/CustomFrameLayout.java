package com.basit.sweetshowcase;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class CustomFrameLayout extends FrameLayout implements GestureDetector.OnGestureListener {

    private final String TAG = CustomFrameLayout.class.getSimpleName();
    private static int mTouchSlop;
    private static final float SCROLL_COMPLETION_LIMIT = 0.6f;
    private ViewConfiguration vc = ViewConfiguration.get(this.getRootView().getContext());

    private float mPosition;
    private float mLastMotionX;
    private float mLastMotionY;
    private float  mLastX;
    private float mStartY;
    private float mStartX;
    private float mLastY;

    private int mDirection;
    private int mLeft = 0;
    private int mRight = 1;
    private int mTop = 2;
    private int  mBottom = 3;

    private boolean isScrolling;
    private boolean mEnableSliding = true;

    public boolean isEnableSwipe() {
        return mEnableSliding;
    }

    public void setEnableSwipe(boolean mEnableSliding) {
        this.mEnableSliding = mEnableSliding;
    }

    private CustomFrameLayoutListener mCustomFrameLayoutListener;
    private GestureDetectorCompat mGestureDetector;


    public CustomFrameLayout(@NonNull Context context) {
        super(context);
        mGestureDetector = new GestureDetectorCompat(context, this);
        init();
    }

    public CustomFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetectorCompat(context, this);
        init();
    }

    public CustomFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetectorCompat(context, this);
        init();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        updatePosition(mPosition, 0, true);
        return true;
    }

    private void init() {
        mTouchSlop = vc.getScaledTouchSlop();
    }

    private void updatePosition(float startValue, float endValue, final boolean isScrollCompleted) {

        isScrolling = true;
        ValueAnimator va = ValueAnimator.ofFloat(startValue, endValue);
        va.setInterpolator(new AccelerateDecelerateInterpolator());
        va.setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mCustomFrameLayoutListener != null) {
                    mPosition = (Float) animation.getAnimatedValue();

                    if (mDirection == mRight) {
                        mCustomFrameLayoutListener.onSlideRight(mPosition);
                    } else if (mDirection == mLeft) {
                        mCustomFrameLayoutListener.onSlideLeft(mPosition);
                    } else if (mDirection == mTop) {
                        mCustomFrameLayoutListener.onSlideUp(mPosition);
                    } else if (mDirection == mBottom) {
                        mCustomFrameLayoutListener.onSlideDown(mPosition);
                    }
                }
            }
        });
        va.start();

        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isScrolling = false;

                if (isScrollCompleted) {
                    if (mCustomFrameLayoutListener != null) {
                        mCustomFrameLayoutListener.onScrollCompleted();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mEnableSliding) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (isScrolling) {
                        updatePosition(mPosition, 1, false);
                    }

                    /*if (mEnableSlideShow) {
                        startSlideShowTimer();
                    }*/

                    if (mCustomFrameLayoutListener != null)
                        mCustomFrameLayoutListener.onRelease();

                    break;
                case MotionEvent.ACTION_MOVE:
                    drag(event);
                    break;
            }
            return mGestureDetector.onTouchEvent(event);

        } else {
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (mEnableSliding) {
            // In general, we don't want to intercept touch events. They should be
            // handled by the child view.
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    /*if (mEnableSlideShow) {
                        resetSlideShowTimer();
                    }*/

                    if (mCustomFrameLayoutListener != null)
                        mCustomFrameLayoutListener.onPress();

                    mLastMotionX = event.getX();
                    mLastMotionY = event.getY();
                    mStartX = mLastX;
                    mStartY = mLastY;
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    isScrolling = false;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();

                    float deltaY = y - mLastMotionY;
                    float deltaX = x - mLastMotionX;

                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (Math.abs(deltaX) > mTouchSlop) {
                            mStartX = x;
                            mStartY = y;
                            return true;
                        }
                    } else {
                        if (Math.abs(deltaY) > mTouchSlop) {
                            isScrolling = true;
                            mStartX = x;
                            mStartY = y;
                            return true;
                        }
                    }
                    break;
            }

            return false;
        } else {
            return false;
        }
    }

    private void drag(MotionEvent e2) {
        try {
            isScrolling = true;

            float deltaX = e2.getX() - mLastMotionX;
            float deltaY = e2.getY() - mLastMotionY;

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > mTouchSlop) {

                    float startX = mLastMotionX;
                    float scrollingX = e2.getX();
                    float startWidth = this.getWidth();

                    float distanceMeasuredX = Math.abs(scrollingX - startX);

                    mPosition = 1 - ((distanceMeasuredX / startWidth * 100) / 100);

                    if (deltaX > 0) {

                        mDirection = mRight;

                        if (mCustomFrameLayoutListener != null)
                            mCustomFrameLayoutListener.onSlideRight(mPosition);

                    } else {

                        mDirection = mLeft;

                        if (mCustomFrameLayoutListener != null)
                            mCustomFrameLayoutListener.onSlideLeft(mPosition);
                    }
                }
            } else {
                if (Math.abs(deltaY) > mTouchSlop) {

                    float startY = mLastMotionY;
                    float scrollingY = e2.getY();
                    float startHeight = this.getHeight();

                    float distanceMeasuredY = Math.abs(scrollingY - startY);

                    mPosition = 1 - ((distanceMeasuredY / startHeight * 100) / 100);

                    if (deltaY > 0) {

                        mDirection = mBottom;

                        if (mCustomFrameLayoutListener != null)
                            mCustomFrameLayoutListener.onSlideDown(mPosition);
                    } else {

                        mDirection = mTop;

                        if (mCustomFrameLayoutListener != null)
                            mCustomFrameLayoutListener.onSlideUp(mPosition);
                    }
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
        }
    }

    public void addOnSwipeListener(CustomFrameLayoutListener listener){
        mCustomFrameLayoutListener = listener;
    }
}
