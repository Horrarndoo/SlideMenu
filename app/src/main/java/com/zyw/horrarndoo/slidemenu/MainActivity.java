package com.zyw.horrarndoo.slidemenu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zyw.horrarndoo.slidemenu.Utils.UIUtils;
import com.zyw.horrarndoo.slidemenu.view.SlideMainAdapter;
import com.zyw.horrarndoo.slidemenu.view.SlideFrameLayout;
import com.zyw.horrarndoo.slidemenu.view.SlideMenu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SlideMenu slideMenu;
    private SlideFrameLayout sll_layout;
    private ListView lv_main;
    private LinearLayout ll_title;
    private TextView tv_title;
    private List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initState();
        initView();
        initData();
    }

    private void initData() {
        slideMenu.setOnDragStateChangeListener(new SlideMenu.OnDragStateChangeListener() {
            @Override
            public void onOpen() {
                //Log.e("tag", "onOpen");
            }

            @Override
            public void onDrag(float fraction) {
                //Log.e("tag", "onDrag fraction:" + fraction);
                if(fraction >= 0.6f){
                    ll_title.setBackgroundColor(Color.WHITE);
                    tv_title.setTextColor(Color.parseColor("#00ccff"));
                }else{
                    ll_title.setBackgroundColor(Color.parseColor("#00ccff"));
                    tv_title.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onClose() {
                //Log.e("tag", "onClose");
            }
        });

        sll_layout.setSlideMenu(slideMenu);
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        slideMenu = (SlideMenu) findViewById(R.id.slide_menu);
        sll_layout = (SlideFrameLayout) findViewById(R.id.sll_layout);
        ll_title = (LinearLayout) findViewById(R.id.ll_title);
        tv_title = (TextView) findViewById(R.id.tv_title);
        lv_main = (ListView) findViewById(R.id.lv_main);
        initList();
        lv_main.setAdapter(new SlideMainAdapter(this, list));
        lv_main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "item - " + position + " is clicked.", Toast
                        .LENGTH_SHORT).show();
            }
        });
    }

    private void initList() {
        for (int i = 0; i < 50; i++) {
            list.add("content - " + i);
        }
    }

    /**
     * 初始化状态栏状态
     * 设置Activity状态栏透明效果
     * 隐藏ActionBar
     */
    private void initState() {
        //将状态栏设置成透明色
        UIUtils.setBarColor(this, Color.TRANSPARENT);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}
