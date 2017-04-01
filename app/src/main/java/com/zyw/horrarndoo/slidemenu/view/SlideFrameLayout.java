package com.zyw.horrarndoo.slidemenu.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.zyw.horrarndoo.slidemenu.view.SlideMenu.DragState;

/**
 * SlideMenu主界面layout
 * 当SlideMenu打开的时候，拦截并消费掉触摸事件
 * Created by Horrarndoo on 2017/3/30
 */
public class SlideFrameLayout extends FrameLayout {
    public SlideFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SlideFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideFrameLayout(Context context) {
        super(context);
    }

    private SlideMenu slideMenu;

    public void setSlideMenu(SlideMenu slideMenu) {
        this.slideMenu = slideMenu;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (slideMenu != null && slideMenu.getCurrentState() == DragState.Open) {
            //如果slideMenu打开，拦截并消费掉事件
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (slideMenu != null && slideMenu.getCurrentState() == DragState.Open) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //抬起关闭slideMenu
                slideMenu.close();
            }
            //如果slideMenu打开，拦截并消费掉事件
            return true;
        }
        return super.onTouchEvent(event);
    }
}
