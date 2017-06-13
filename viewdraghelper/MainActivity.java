package com.demo.viewdraghelper;

import android.animation.FloatEvaluator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.menu_listview)
    ListView mMenuListview;
    @InjectView(R.id.iv_head)
    ImageView mIvHead;
    @InjectView(R.id.main_listview)
    ListView mMainListview;
    @InjectView(R.id.my_linearlayout)
    MyLinearLayout mMyLinearlayout;
    @InjectView(R.id.slidemenu)
    SlideMenu mSlidemenu;
    FloatEvaluator mFloatEvaluator = new FloatEvaluator();

    //定义一个布尔记录当前是否被打开;
    private boolean isOpen;
    private boolean isClosed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initAdapter();
        initListener();
    }

    private void initAdapter() {
        mMenuListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.sCheeseStrings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                return view;
            }
        });
        mMainListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.NAMES));
    }

    private void initListener() {
        //设置滑动的监听器
        mSlidemenu.setOnSlideListener(new SlideMenu.onSlideListener() {
            @Override
            public void close(boolean close) {
                Toast.makeText(MainActivity.this, "关闭了", Toast.LENGTH_SHORT).show();
                isClosed = close;
            }

            @Override
            public void open(boolean open) {
                Toast.makeText(MainActivity.this, "打开了", Toast.LENGTH_SHORT).show();
                isOpen = open;
            }

            @Override
            public void dragging(float fraction) {
                mIvHead.setAlpha(mFloatEvaluator.evaluate(fraction, 1f, 0));
            }
        });
        //头像的点击事件,点击打开侧滑
        mIvHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidemenu.smoothToOpen();
            }
        });

        //主界面的点击事件,当侧滑界面打开时,
        mMyLinearlayout.setSlideMenu(mSlidemenu);
    }

}
