package com.demo.viewdraghelper;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/5/26.
 */
public class MyLinearLayout extends LinearLayout {


    private SlideMenu mSlideMenu;
    private int mDownX;
    private int mDownY;

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    boolean isListViewCanDrag = true;
    public void setSlideMenu(SlideMenu slideMenu){
        mSlideMenu = slideMenu;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mSlideMenu != null) {
            if (mSlideMenu.getCurrentState() == SlideMenu.DragState.OPEN) {
                //请求不拦截
                getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = (int) (event.getX()+.5f);
                        mDownY = (int) (event.getY()+.5f);
                        Log.d("tag","-------------->按下了");
                        break;
                    case MotionEvent.ACTION_UP:
                        int mUpX = (int) (event.getX()+.5f);
                        int mUpY = (int) (event.getY()+.5f);
                        Log.d("tag","-------------->抬起了"+mUpX+"-----"+mUpY);
                        Log.d("tag",mDownX+"-----"+mDownY);
                        if(mDownX-mUpX== 0 &&mDownY - mUpY ==0){
                            Log.d("tag","即将关闭----->");
                            mSlideMenu.smoothToClose();
                            Toast.makeText(getContext(), "点击了主界面", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }else {
                return super.dispatchTouchEvent(event);
            }
        }else {
            return super.dispatchTouchEvent(event);
        }
    }

}
