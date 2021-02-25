package com.example.digimon;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.widget.RelativeLayout;

@SuppressLint("AppCompatCustomView")
public class AttractView extends ImageView {
    private int visible=0;
    private AnimationDrawable anim;
    private RelativeLayout.LayoutParams par;

    public AttractView(Context context){
        super(context);
    }

    public AttractView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public AttractView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAnim(AnimationDrawable anim){
        this.anim=anim;
    }

    public void setXML(){
        this.setBackgroundResource(R.drawable.rightrun);
    }

    public void getPar(){
        this.par= (RelativeLayout.LayoutParams) this.getLayoutParams();
    }

    public int getVisible(){return this.visible;}
    public void setLocation(int x,int y){
        getPar();
        par.leftMargin=x-par.width/2;
        par.topMargin=y-par.width/2;
        this.setLayoutParams(par);
    }
    @Override
    public void setVisibility(int visibility){
        switch(visibility){
            case View.VISIBLE:
                this.visible=View.VISIBLE;
                super.setVisibility(visibility);
                break;
            case View.INVISIBLE:
                this.visible=View.INVISIBLE;
                super.setVisibility(visibility);
                break;
            default:
                break;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        try{
            Field field=AnimationDrawable.class
                    .getDeclaredField("mCurFrame");
            field.setAccessible(true);
            int curFrame=field.getInt(anim);
            if(curFrame== anim.getNumberOfFrames()-1){
                anim.selectDrawable(0);
            }
        }catch(Exception e){e.printStackTrace();}
        super.onDraw(canvas);
    }
}
