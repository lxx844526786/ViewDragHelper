package com.demo.viewdraghelper;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2017/5/26.
 */
public class SlideMenu extends FrameLayout {

    private ViewDragHelper dragHelper;
    private View menuView;
    private View mainView;
    private int dragRange;
    private int mainWith;
    private int menuWidth;
    //浮点型的估值器
    FloatEvaluator mFloatEvaluator = new FloatEvaluator();
    //色彩值的估值器
    ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();
    public DragState currentState = DragState.CLOSED;

    public DragState getCurrentState() {
        return currentState;
    }

    public enum DragState {
        OPEN, CLOSED
    }

    public SlideMenu(Context context) {
        this(context, null);
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //在这里对ViewDragHelper进行初始化,第一个参数是当前的view,第二个是接口回调
        dragHelper = ViewDragHelper.create(this, callback);

    }

    /**
     * 在viewGroup将子view全部添加之后执行,在onMeasure之前执行,
     * 一般用来初始化view的初始化,但是不能获取子view的宽高
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //菜单界面
        menuView = getChildAt(0);
        //主界面
        mainView = getChildAt(1);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //得到mainView的宽
        mainWith = mainView.getMeasuredWidth();
        //得到menuView的宽
        menuWidth = menuView.getMeasuredWidth();
        dragRange = (int) (getMeasuredWidth() * 0.6f);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //让dragHelper帮助我们判断是否应该拦截

        boolean result = dragHelper.shouldInterceptTouchEvent(ev);

        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //让dragHelper帮助我们处理触摸事件
        dragHelper.processTouchEvent(event);
        //自己拦截事件
        return true;
    }

    private boolean isOpen;
    private boolean isClosed;
    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        //是否对子view进行捕获
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mainView || child == menuView;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
            Log.e("tag", "capturedChild");
        }

        //判断水平方向移动滑动,给个大于零的值
        @Override
        public int getViewHorizontalDragRange(View child) {
            return 100;
        }

        /**用来修正或指定view在水平方向上的移动
         * @param child
         * @param left 是viewDragHelper帮我们计算好的距离
         * @param dx
         * @return 返回值代表我们真正想让view移动的距离
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mainView) {
                if (left > dragRange) {
                    left = dragRange;
                } else if (left < 0) {
                    left = 0;
                }
            }
            return left;
        }

        /**view移动时调用的方法
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //当菜单移动时,主界面伴随移动,但是menuView保持不动
            if (changedView == menuView) {
                //让menuView保持不动
                menuView.layout(0, 0, menuWidth, menuView.getBottom());
                int newLeft = mainView.getLeft() + dx;
                if (newLeft > dragRange) {
                    newLeft = dragRange;
                } else if (newLeft < 0) {
                    newLeft = 0;
                }
                mainView.layout(newLeft, 0, newLeft + mainWith, mainView.getBottom());
            }
            //执行伴随动画,拿到主界面移动时与dragRange的百分比大小
            float fraction = mainView.getLeft() * 1f / dragRange;
            //执行动画
            executeAnimation(fraction);

            //增加一个接口回调的方法
            if (fraction == 1f && currentState != DragState.OPEN) {
                //打开
                currentState = DragState.OPEN;
                if (mListener != null) {
                    isClosed = false;
                    isOpen = true;
                    mListener.open(isOpen);
                }
            } else if (fraction == 0 && currentState != DragState.CLOSED) {
                //关闭
                currentState = DragState.CLOSED;
                if (mListener != null) {
                    isOpen = false;
                    isClosed = true;
                    mListener.close(isClosed);
                }
            }
            if (mListener != null) {
                mListener.dragging(fraction);
            }

        }

        /**手指抬起时的回调
         * @param releasedChild
         * @param xvel
         * @param yvel
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            //判断mainView的左边的大小
            if (mainView.getLeft() > dragRange / 2) {
                smoothToOpen();
            } else {
                smoothToClose();
            }
        }
    };

    public void smoothToOpen() {
        //滑向最右边,有刷新操作
        dragHelper.smoothSlideViewTo(mainView, dragRange, 0);
        //刷新操作
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    public void smoothToClose() {
        //滑向最左边
        dragHelper.smoothSlideViewTo(mainView, 0, 0);
        //刷新操作
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //如果还在执行动画
        if (dragHelper.continueSettling(true)) {
            //刷新
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
        }
    }

    private void executeAnimation(float fraction) {
        //fraction 0--->1  那么mainView的大小 从1 --->0.8

        Log.i("tag", "fraction====" + fraction);
        //让mainView执行缩放
        mainView.setScaleX(mFloatEvaluator.evaluate(fraction, 1f, 0.8f));
        mainView.setScaleY(mFloatEvaluator.evaluate(fraction, 1f, 0.8f));

        //让menuView执行放大动画,和平移动画
        menuView.setScaleX(mFloatEvaluator.evaluate(fraction, 0.5f, 1f));
        menuView.setScaleY(mFloatEvaluator.evaluate(fraction, 0.5f, 1f));

        //从左边平移到右边
        menuView.setTranslationX(mFloatEvaluator.evaluate(fraction, -menuWidth / 2, 0));

        if (getBackground() != null) {
            //给背景添加一个遮罩的效果
            getBackground().setColorFilter((Integer) mArgbEvaluator.evaluate(fraction, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.DST_OVER);
        }
    }


    public onSlideListener mListener;

    public void setOnSlideListener(onSlideListener listener) {
        mListener = listener;
    }

    public interface onSlideListener {
        void close(boolean isClosed);

        void open(boolean isOpen);

        void dragging(float fraction);
    }
}
