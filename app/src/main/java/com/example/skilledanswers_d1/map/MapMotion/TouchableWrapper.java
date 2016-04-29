package com.example.skilledanswers_d1.map.MapMotion;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

public class TouchableWrapper extends FrameLayout {
    private OnTouchListener onTouchListener;
    public TouchableWrapper(Context context) {
        super(context);
    }

    public void setTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchListener.onTouch();
              //  Toast.makeText(getContext(),"touch ", Toast.LENGTH_LONG).show();
                break;
            case MotionEvent.ACTION_UP:
                onTouchListener.onRelease();
              //  Toast.makeText(getContext(),"release ", Toast.LENGTH_LONG).show();
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    public interface OnTouchListener {
         void onTouch();
         void onRelease();
    }
}
