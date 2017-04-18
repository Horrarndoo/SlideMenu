package com.zyw.horrarndoo.slidemenu.view;

import android.animation.IntEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nineoldandroids.view.ViewHelper;
import com.zyw.horrarndoo.slidemenu.Utils.ColorUtil;

/**
 * Created by Horrarndoo on 2017/3/31.
 */

public class SlideMenu extends FrameLayout {
    private View menuView;//菜单view
    private SlideFrameLayout mainView;//主界面view
    private ImageView iv_main_src;
    private ViewDragHelper viewDragHelper;
    private float dragRange;//最大拖拽范围，mainView的最大left
    private IntEvaluator intEvaluator;//int的计算器
    private GestureDetectorCompat mGestureDetector;

    private boolean isTouchDrag;//是否touch拖拽SlideMenu，因为放手之后存在一个弹回去的过程
                                        //此时onViewPositionChanged一样会回调，需要区分自动弹回去还是touch拖动

    public SlideMenu(Context context) {
        this(context, null);
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 定义状态常量
     */
    public enum DragState {
        Open, Close
    }

    private DragState currentState = DragState.Close;//默认关闭

    private void init(Context context) {
        viewDragHelper = ViewDragHelper.create(this, callback);
        intEvaluator = new IntEvaluator();
        //通过手势判断器判断touch事件是否消费掉
        mGestureDetector = new GestureDetectorCompat(context, new GestureDetector
                .SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float
                    distanceY) {
                //滑动的时候 只有x坐标改变值>=y坐标改变值时，才消费事件
                return Math.abs(distanceX) >= Math.abs(distanceY);
            }
        });
    }

    /**
     * 获取当前的状态
     *
     * @return
     */
    public DragState getCurrentState() {
        return currentState;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() != 2) {
            throw new IllegalArgumentException("SlideMenu only can 2 children!");
        }
        menuView = getChildAt(0);
        mainView = (SlideFrameLayout) getChildAt(1);
        iv_main_src = (ImageView) mainView.getChildAt(1);
    }

    /**
     * onMeasure执行完之后执行
     * 初始化自己和子View的宽高
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        dragRange = getMeasuredWidth() * 0.8f;
    }


    private float eventX;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //由viewDragHelper和手势判断器判断是否需要拦截touch事件
        return viewDragHelper.shouldInterceptTouchEvent(ev) & mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //由viewDragHelper处理touch事件
        viewDragHelper.processTouchEvent(event);

        //消费掉事件
        return true;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         * 用于判断是否捕获当前child的触摸事件
         * @param child
         *              当前触摸的子View
         * @param pointerId
         * @return
         *          true:捕获并解析
         *          false：不处理
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            isTouchDrag = true;
            return child == menuView || child == mainView;
        }

        /**
         * 获取view水平方向的拖拽范围，不能限制拖拽范围
         * @param child
         *          拖拽的child view
         * @return
         *          拖拽范围
         */
        public int getViewHorizontalDragRange(View child) {
            return (int) dragRange;
        }

        /**
         * 控制child在水平方向的移动
         * @param child
         *              控制移动的view
         * @param left
         *              ViewDragHelper判断当前child的left改变的值
         * @param dx
         *              本次child水平方向移动的距离
         * @return
         *              child最终的left值
         */
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mainView) {
                if (left < 0)
                    left = 0;//限制mainView的左边
                if (left > dragRange)
                    left = (int) dragRange;//限制mainView的右边
            }
            return left;
        }

        /**
         * child位置改变的时候执行，一般用来做其它子View的伴随移动
         * @param changedView
         *              位置改变的view
         * @param left
         *              child当前最新的left
         * @param top
         *              child当前最新的top
         * @param dx
         *              本次水平移动的距离
         * @param dy
         *              本次垂直移动的距离
         */
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == menuView) {
                //固定住menuView
                menuView.layout(0, 0, menuView.getMeasuredWidth(), menuView.getMeasuredHeight());
                //让mainView移动起来
                int newLeft = mainView.getLeft() + dx;
                if (newLeft < 0)
                    newLeft = 0;//限制mainView的左边
                if (newLeft > dragRange) {
                    newLeft = (int) dragRange;//限制mainView的右边
                }
                mainView.layout(newLeft, mainView.getTop() + dy, newLeft + mainView
                        .getMeasuredWidth(), mainView.getBottom() + dy);
            }

            //1.计算滑动的百分比
            float fraction = mainView.getLeft() / dragRange;
            //2.执行伴随动画
            executeAnim(fraction);

            //只有touch拖动view导致child位置变化时才回调onDrag
            if(isTouchDrag) {
                //将drag的fraction暴漏给外界
                if (listener != null) {
                    listener.onDrag(fraction);
                }
            }
        }

        /**
         * 手指抬起的时候执行
         * @param releasedChild
         *              当前抬起的child view
         * @param xvel
         *              x方向移动的速度 负：向做移动 正：向右移动
         * @param yvel
         *              y方向移动的速度
         */
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            isTouchDrag = false;

            if (mainView.getLeft() < dragRange / 2) {
                //在左半边
                close();
            } else {
                //在右半边
                open();
            }

            //处理用户的稍微滑动
            if (xvel > 200 && currentState != DragState.Open) {
                open();
            } else if (xvel < -200 && currentState != DragState.Close) {
                close();
            }
        }
    };

    /**
     * 打开菜单
     */
    public void close() {
        //更改状态为关闭，并回调关闭的方法
        if (listener != null) {
            listener.onClose();
            currentState = DragState.Close;
        }

        viewDragHelper.smoothSlideViewTo(mainView, 0, mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    /**
     * 打开菜单
     */
    public void open() {

        //更改状态为打开，并回调打开的方法
        if (listener != null) {
            listener.onOpen();
            currentState = DragState.Open;
        }

        viewDragHelper.smoothSlideViewTo(mainView, (int) dragRange, mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    /**
     * 执行伴随动画
     *
     * @param fraction
     */
    private void executeAnim(float fraction) {
        //移动menuView
        ViewHelper.setTranslationX(menuView, intEvaluator.evaluate(fraction, -menuView
                .getMeasuredWidth() / 2, 0));

        //给mainView设置黑色的遮罩效果
        //实际上是设置mainView的表层ImageView颜色过滤效果，达到设置整个view黑色遮罩效果
        try {
//            iv_main_src.setBackgroundColor((Integer) ColorUtil.evaluateColor(fraction, Color
//                            .TRANSPARENT,
//                    Color.parseColor("#33000000")));
            iv_main_src.getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(fraction, Color
                            .TRANSPARENT,
                    Color.parseColor("#33000000")), PorterDuff.Mode.SCREEN);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
        }
    }

    private OnDragStateChangeListener listener;

    public void setOnDragStateChangeListener(OnDragStateChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 拖拽监听接口
     */
    public interface OnDragStateChangeListener {
        /**
         * 打开回调
         */
        void onOpen();

        /**
         * 关闭回调
         */
        void onClose();

        /**
         * 拖拽中回调
         *
         * @param fraction
         */
        void onDrag(float fraction);
    }

    public void destory(){
        try {
            iv_main_src.getBackground().clearColorFilter();//清除黑色过滤效果
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
