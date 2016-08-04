package com.fish.myscrollviewpractise;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * Created by fish on 16/8/2.
 */
public class MyScrollView extends FrameLayout {

    private boolean mIsBeingDragged = false;
    /**
     * Position of the last motion event.
     */
    private int mLastMotionY;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    private Scroller mScroller;


    /* ID of the active pointer. This is used to retain consistency during
    * drags/flings if multiple pointers are used.
            */
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;


    public MyScrollView(Context context) {
        this(context, null);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initScrollView();
    }

    private void initScrollView() {
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        setWillNotDraw(false);
        mScroller = new Scroller(getContext());
    }


    //让内部的LinearLayout高度可以很大很大
    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {

        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = (int) event.getY();
                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                int delta = (int) (event.getY() - mLastMotionY);
                if (mIsBeingDragged) {
                    scrollBy(0, -delta);
                    mLastMotionY = (int) event.getY();
                } else if (Math.abs(delta) > mTouchSlop) {
                    mIsBeingDragged = true;
                    mLastMotionY = (int) event.getY();
                    scrollBy(0, -delta);
                }
                break;

            case MotionEvent.ACTION_UP:

                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);

                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        mScroller.startScroll(getScrollX(), getScrollY(), 0, initialVelocity > 0 ? -300 : 300, 4000);
                        invalidate();
                    }
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }

        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //view第二次重绘
            postInvalidate();
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;
        recycleVelocityTracker();
    }

    @Override
    protected int computeVerticalScrollOffset() {
//        LogUtil.fish("computeVerticalScrollOffset");
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override
    protected int computeVerticalScrollRange() {
//        LogUtil.fish("computeVerticalScrollRange");
        final int count = getChildCount();
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        if (count == 0) {
            return contentHeight;
        }

        int scrollRange = getChildAt(0).getBottom();
        final int scrollY = getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
//        if (scrollY < 0) {
//            scrollRange -= scrollY;
//        } else if (scrollY > overscrollBottom) {
//            scrollRange += scrollY - overscrollBottom;
//        }

        return overscrollBottom;
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

}
