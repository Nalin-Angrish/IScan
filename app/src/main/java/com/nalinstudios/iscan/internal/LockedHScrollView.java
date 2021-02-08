package com.nalinstudios.iscan.internal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;


/**
 * A basic scrollView that disables built-in scrolling and implements custom scrolling.
 * @author Nalin Angrish.
 */
public class LockedHScrollView extends HorizontalScrollView {

    /** The variables used to detect left-right swipes*/
    float x1, x2;
    /** variable to check whether the swipes made were valid (distance was large enough) */
    final float distance = 200;



    /** One of the entry points for constructing the ScrollView*/
    public LockedHScrollView(Context context){super(context);init();}
    /** One of the entry points for constructing the ScrollView*/
    public LockedHScrollView(Context context, AttributeSet attrs){super(context, attrs);init();}
    /** One of the entry points for constructing the ScrollView*/
    public LockedHScrollView(Context context, AttributeSet attrs, int defStyleAttr){super(context, attrs, defStyleAttr);init();}


    /**
     * The main initializer.
     */
    public void init(){
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
    }


    /**
     * A function to scroll towards the left.
     */
    public void scrollLeft(){
        int x = getResources().getDisplayMetrics().widthPixels;
        scrollBy(-x, 0); // scroll by the amount of the screen's width
    }


    /**
     * A function to scroll towards the right.
     */
    public void scrollRight(){
        int x = getResources().getDisplayMetrics().widthPixels;
        scrollBy(x, 0); // scroll by the amount of the screen's width
    }


    /**
     * A function to check swipes for scrolling between multiple result fragments.
     * @param event the touch event
     * @return super
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_DOWN){
            x1 = event.getX();
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            x2 = event.getX();
            if ((x2-x1)>0){             //Right swipe
                if (x2-x1 >= distance){        // Validate that the swipe was long enough
                    scrollLeft();
                }
            }else if((x1-x2)>0){        // Left swipe
                if ((x1-x2)>=distance){        // Validate that the swipe was long enough
                    scrollRight();
                }
            }
        }
        return true;
    }
}
