package com.example.digimon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;


import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
    DisplayMetrics metric = new DisplayMetrics();
    public static int screenWidth=0;
    public static int screenHeight=0;
    private AnimationDrawable attractAnim =null;
    private AnimationDrawable monAnim =null;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //背景手势事件
        MotionEventView view=findViewById(R.id.aa);
        view.setListener(new MotionEventView.CallBackListener() {
            @Override
            public void doubleClick() {
                Log.i(TAG, "doubleClick: ");
                Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
            @Override
            public void singleClick() {
                Log.i(TAG, "singleClick: ");
            }
            @Override
            public void longPress() {
                Log.i(TAG, "longPress: ");
            }
            @Override
            public void slideRight() {
                Log.i(TAG, "slideRight: ");
            }
            @Override
            public void slideLeft() {
                Log.i(TAG, "slideLeft: ");
            }
            @Override
            public void slideUp() {
                Log.i(TAG, "slideUp: ");
            }
            @Override
            public void slideDown() {
                Log.i(TAG, "slideDown: ");
            }
        });


        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenWidth = metric.widthPixels;
        screenHeight = metric.heightPixels;
        //产生效果吸引mon
        final AttractView attractView=findViewById(R.id.Attract);
        attractView.setXML();
        attractView.setVisibility(View.INVISIBLE);
        attractAnim =(AnimationDrawable) attractView.getBackground();
        attractView.setAnim(attractAnim);
        //Mon
        final MonView monView=findViewById(R.id.Mon);
        monView.setBackgroundResource(R.drawable.leftrun);
        monView.setLocation(screenWidth/2,screenHeight/2);
        monAnim=(AnimationDrawable)monView.getBackground();
        monView.setAnim(monAnim);
        monAnim.start();
        monView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x=0;
                int y=0;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;
                        x = nowX;
                        y = nowY;
                        monView.setLocation(movedX,movedY);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                RelativeLayout.LayoutParams par=monView.getPar();
                int x_cord=(int)event.getRawX();
                int y_cord=(int)event.getRawY();
                if(x_cord>=par.leftMargin&&x_cord<=par.leftMargin+par.width
                &&y_cord>=par.topMargin&&y_cord<=par.topMargin+par.width){
                    if(monView.attracted){
                        monView.attracted=false;
                        monView.setBackgroundResource(R.drawable.rightrun);
                        monAnim=(AnimationDrawable)monView.getBackground();
                        monAnim.start();
                    }
                    //屏幕事件发生在monView范围内
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            monView.dragged=true;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (attractView.getVisible() == View.VISIBLE)
                                attractView.setVisibility(View.INVISIBLE);
                            monView.setLocation(x_cord, y_cord);
                            break;
                        case MotionEvent.ACTION_UP:
                            monView.dragged=false;
                            break;
                    }
                }
                else{
                    //屏幕事件触发attractView
//                    if(!monView.attracted){
//                        monView.attracted=true;
//                        monView.setBackgroundResource(R.drawable.);
//                        monAnim=(AnimationDrawable)monView.getBackground();
//                        monAnim.start();
//                    }
                    int mov_x=0,mov_y=0;
                    if(x_cord-monView.getPar().leftMargin<0)mov_x=-monView.speed;
                    else if(x_cord-monView.getPar().leftMargin>0)mov_x=monView.speed;
                    if(y_cord-monView.getPar().topMargin<0)mov_y=-monView.speed;
                    else if(y_cord-monView.getPar().topMargin>0)mov_y=monView.speed;
                    monView.mov(mov_x,mov_y);
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            attractView.setLocation(x_cord,y_cord);  //View显示的位置
                            attractView.setVisibility(View.VISIBLE);
                            attractAnim.start();
                            break;
                        case MotionEvent.ACTION_UP:
                            attractView.setVisibility(View.INVISIBLE);
                            attractAnim.stop();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            attractView.setLocation(x_cord,y_cord);
                            break;
                    }
                }
                return false;
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                    startService(new Intent(MainActivity.this, FloatingImageDisplayService.class));
                }
            }
        }
    }

    public void startFloatingImageDisplayService(View view) {
        if (FloatingImageDisplayService.isStarted) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1);
            } else {
                startService(new Intent(MainActivity.this, FloatingImageDisplayService.class));
            }
        }
    }

}
