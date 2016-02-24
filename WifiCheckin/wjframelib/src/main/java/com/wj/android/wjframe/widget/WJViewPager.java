package com.wj.android.wjframe.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by wecan-mac on 15/12/7.
 */
public class WJViewPager extends ViewGroup {

    private final Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mCurScreen;
    private final int mDefaultScreen=0;
    private static final int TOUCH_STATE_REST=0;
    private static final int TOUCH_STATE_SCROLLING=1;
    private static final int SNAP_VELOCITY=600;
    private int mTouchState=TOUCH_STATE_REST;
    private final int mTouchSlop;
    private float mLastMotionX;
    private float mLastMotionY;
    private OnViewChangeListener mOnViewChangeListener;


    private boolean isScroll=true;

    public WJViewPager(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WJViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
        mCurScreen=mDefaultScreen;
        mTouchSlop= ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }


    /**
     * 对该控件里面的子控件定位
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int childLeft=0;
        final int childCount=getChildCount();
        for (int i=0;i<childCount;i++){
            final View childView=getChildAt(i);
            if (childView.getVisibility()!=View.GONE){
                final int childWidth=childView.getMeasuredWidth();
                childView.layout(childLeft,0,childLeft+childWidth,childView.getMeasuredHeight());
                childLeft+=childWidth;
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width=MeasureSpec.getSize(widthMeasureSpec);

        final int count =getChildCount();
        for (int i=0;i<count;i++){
            getChildAt(i).measure(widthMeasureSpec,heightMeasureSpec);
        }
        scrollTo(mCurScreen * width, 0);
    }

    public void snapToDestination(){
        final int screenWidth=getWidth();
        final int destScreen=(getScrollX()+screenWidth/2)/screenWidth;
        snapToScreen(destScreen);

    }

    private void snapToScreen(int whichScreen) {
        if (!isScroll){
            setToScreen(whichScreen);
        }
        else {
            scrollToScreen(whichScreen);
        }
    }

    private void setToScreen(int whichScreen) {

        whichScreen=Math.max(0,Math.min(whichScreen,getChildCount()-1));
        mCurScreen=whichScreen;
        scrollTo(whichScreen*getWidth(),0);

        if(mOnViewChangeListener!=null){
            mOnViewChangeListener.OnViewChange(mCurScreen);
        }
    }

    private void scrollToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        if (getScrollX() != (whichScreen * getWidth())) {
            final int delta = whichScreen * getWidth() - getScrollX();
            mScroller.startScroll(getScrollX(), 0, delta, 0,
                    Math.abs(delta) * 1);// 持续滚动时间 以毫秒为单位
            mCurScreen = whichScreen;
            invalidate(); // Redraw the layout

            if (mOnViewChangeListener != null) {
                mOnViewChangeListener.OnViewChange(mCurScreen);
            }
        }

    }

    public int getCurScreen() {
        return mCurScreen;
    }


    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isScroll){
            return false;
        }

        if(mVelocityTracker==null){
            mVelocityTracker=VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final float x=event.getX();
        final float y=event.getY();
        final int action=event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastMotionX - x);
                int deltaY = (int) (mLastMotionY - y);
                if (Math.abs(deltaX) < 200 && Math.abs(deltaY) > 10)
                    break;
                mLastMotionY = y;
                mLastMotionX = x;
                scrollBy(deltaX, 0);
                break;
            case MotionEvent.ACTION_UP:
                // if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
                    // Fling enough to move left
                    snapToScreen(mCurScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY
                        && mCurScreen < getChildCount() - 1) {
                    // Fling enough to move right
                    snapToScreen(mCurScreen + 1);
                } else {
                    snapToDestination();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return true;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isScroll) {
            return false;
        }
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                if (xDiff > mTouchSlop) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    /**
     * 设置屏幕切换监听器
     *
     * @param listener
     */
    public void setOnViewChangeListener(OnViewChangeListener listener){
        mOnViewChangeListener=listener;
    }



    /**
     * 屏幕切换监听器
     */
    public interface OnViewChangeListener{
        public void OnViewChange(int View);
    }

    /**
     * 设置控件是否可以左右滑动
     *
     * @param isScroll
     */
    public void setCanScroll(boolean isScroll) {
        this.isScroll = isScroll;
    }

}
