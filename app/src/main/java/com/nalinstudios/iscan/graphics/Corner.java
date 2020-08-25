package com.nalinstudios.iscan.graphics;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.nalinstudios.iscan.R;

public class Corner implements View.OnTouchListener {
    private ConstraintLayout layout;
    private ImageView view;

    public Corner(int leftPercent, int topPercent, ConstraintLayout layout, Context context){
        this.layout = layout;
        ShapeDrawable sd = new ShapeDrawable(new OvalShape());
        sd.setIntrinsicHeight(30);
        sd.setIntrinsicWidth(30);
        sd.getPaint().setColor(Color.parseColor("#99ff99"));

        view = new ImageView(context);
        view.setImageDrawable(sd);
        view.setBackground(sd);
        view.setId(View.generateViewId());

        int leftMargin = (layout.getWidth()/100)*leftPercent;
        int topMargin = (layout.getHeight()/100)*topPercent;

        layout.addView(view);
        view.setVisibility(View.VISIBLE);

        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.connect(view.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, leftMargin);
        set.connect(view.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, topMargin);
        set.applyTo(layout);

        Log.println(Log.ASSERT, "view", view.getLeft() + " x "+view.getTop());
    }

    private void removeConstraints(){
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.clear(view.getId());
        set.applyTo(layout);
    }

    private void setConstraints(MotionEvent event){
        int x = (int)event.getX(), y= (int)event.getY();
        view.setX(x);
        view.setY(y);

        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.connect(view.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT);
        set.connect(view.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
        set.applyTo(layout);
    }
    private void validateConstraints(){

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.equals(view)){
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                removeConstraints();
            }else if (event.getAction() == MotionEvent.ACTION_UP){
                setConstraints(event);
                validateConstraints();
            }else if (event.getAction() == MotionEvent.ACTION_MOVE){
                view.setX(event.getX());
                view.setY(event.getY());
            }
            return true;
        }else {
            return false;
        }
    }
}
