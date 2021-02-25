package com.example.digimon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;

/**
 * 自定义view实现同时监听单击，双击，长按，滑动事件，且各个事件互不干扰，即不存在事件并发的bug
 * Created by Vaiyee on 2018/11/2.
 */

public class MotionEventView extends View {
    long first=0;      //第一次点击的时间
    long second =0;   //第二次点击的时间
    boolean isDoubleClick = false; //双击标记位
    boolean isLongPress = true; //长按标记位
    boolean isPerformLongPress = false; // 是否响应了长按事件
    boolean isSlide =false;  //滑动标记位
    CallBackListener listener;   //事件回调监听接口，用于回调触发的各种事件
    Thread LongPressThread;  //长按线程
    double lastX=0,lastY=0;
    public void setListener(CallBackListener listener)
    {
        this.listener = listener;
    }
    public MotionEventView(Context context) {
        super(context);
    }

    public MotionEventView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MotionEventView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    VelocityTracker velocityTracker;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        double nowX = event.getX();
        double nowY = event.getY();
//        System.out.println("保留两位小数"+(float)Math.round(nowX*100)/100); //这里如果要求精确4位就*10000然后/10000，其他位数同理
//        DecimalFormat decimalFormat = new DecimalFormat(".00");
//        System.out.println("保留两位小数"+decimalFormat.format(nowY));
        velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(1000); //计算手指一秒内划过的距离（像素）
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                isLongPress = true;
                isPerformLongPress = false;
                isSlide = false;
                if (first==0)
                {
                    LongPressThread = new Thread(new LongPressThread());
                    LongPressThread.start();
                    isDoubleClick = false;
                    first = System.currentTimeMillis();
                    //System.out.println("第一次单击");
                }else
                {
                    second =System.currentTimeMillis();
                    // System.out.println("第二次单击");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                double deltaX = nowX-lastX;  //滑动的距离差值
                double deltaY = nowY-lastY;
                if (Math.abs(deltaX)>Math.abs(deltaY))
                {
                    if (Math.abs(deltaX)>ViewConfiguration.get(getContext()).getScaledTouchSlop())//大于系统能识别的最小滑动量才判定为滑动
                    {
                        if (Math.abs(velocityTracker.getXVelocity())>=500) //滑动速度足够快
                        {
                            isSlide = true; //表示滑动事件
                            first = 0;
                            second = 0;
                            if (deltaX < 0) {
                                listener.slideLeft();
                            } else {
                                listener.slideRight();
                            }
                        }
                    }
                }
                else
                {
                    if (Math.abs(deltaY) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                        if (Math.abs(velocityTracker.getXVelocity())>=100) //滑动速度足够快
                        {
                            isSlide = true; //表示滑动事件
                            first = 0;
                            second = 0;
                            if (deltaY < 0) {
                                listener.slideUp();
                            } else {
                                listener.slideDown();
                            }

                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (LongPressThread.isAlive()) {
                    LongPressThread.interrupt(); //抬起手时就吵醒正在睡眠的长按监听线程，防止再次点击时触发上一次的长按事件
                }
                if (second-first<=300&&second-first>0)  //如果两次点击事件差小于300ms,则为双击事件
                {
                    listener.doubleClick(); //双击事件回调
                    first = 0;
                    second =0;
                    isDoubleClick = true;
                    isLongPress = false;
                }
                else //如果小于0则为单击事件
                {
                    if (!isSlide) {   //如果没有触发滑动事件，就是单击事件
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200);
                                    if (second == 0 && !isDoubleClick && !isPerformLongPress&&!isSlide) //延时200ms后，如果没有第二次双击行为以及没有长按行为和滑动后手指还停留在屏幕上，就触发单击行为
                                    {
                                        listener.singleClick(); //单击事件回调
                                        first = 0;
                                        isLongPress = false;
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    }
                }


                break;
        }

        lastX = nowX;
        lastY = nowY;
        return true;
    }

    class LongPressThread implements Runnable
    {

        @Override
        public void run() {
            try {
                Thread.sleep(2000);  //长按时间为2秒
                if (!isSlide) {   //防止滑动手势触发后手指还停留在屏幕导致触发长按事件
                    listener.longPress();     //长按事件回调
                    first = 0; //触发长按事件后也要将第一次点击时间归0
                    second = 0;
                    isPerformLongPress = true;
                }

            } catch (InterruptedException e) {
                //System.out.println("长按线程被吵醒");
                //e.printStackTrace();
            }
        }
    }

   // @Override
//    protected void onDetachedFromWindow() {
//        velocityTracker.clear();
//        velocityTracker.recycle();
//        super.onDetachedFromWindow();
//    }

    public interface CallBackListener
    {
        void doubleClick();
        void singleClick();
        void longPress();
        void slideRight();
        void slideLeft();
        void slideUp();
        void slideDown();
    }
}