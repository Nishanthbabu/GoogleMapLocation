package com.example.skilledanswers_d1.map.MapMotion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.skilledanswers_d1.map.UberMap;
import com.google.android.gms.maps.SupportMapFragment;

public class TouchableMapFragment extends SupportMapFragment {

    private View mOriginalContentView;
    private TouchableWrapper mTouchView;

  /*  public void setTouchListener(TouchableWrapper.OnTouchListener onTouchListener) {
        mTouchView.setTouchListener(onTouchListener);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        mOriginalContentView = super.onCreateView(inflater, parent,
                savedInstanceState);



        mTouchView = new TouchableWrapper(getActivity());
        mTouchView.addView(mOriginalContentView);

        mTouchView.setTouchListener(new TouchableWrapper.OnTouchListener() {
            @Override
            public void onTouch() {

                if (getActivity() != null){
               UberMap Uber = (UberMap) getActivity();
                    Uber.pointer.setVisibility(View.INVISIBLE);
                    Uber.locationlayout.setVisibility(View.INVISIBLE);

                }
            }

            @Override
            public void onRelease() {

//                textView.setVisibility(View.VISIBLE);
                if (getActivity() != null){
                    UberMap Uber = (UberMap) getActivity();
                    Uber.pointer.setVisibility(View.VISIBLE);
                    Uber.locationlayout.setVisibility(View.VISIBLE);
//                    UberMap.PLACE_AUTOCOMPLETE_ENTERED_FOR_PICKUP =false; ///// reseting the asynctask on map drag
//                    UberMap.PLACE_AUTOCOMPLETE_ENTERED_FOR_DROP =false; ///// reseting the asynctask on map drag


                }
            }
        });

        return mTouchView;
    }

    @Override
    public View getView() {
        return mOriginalContentView;
    }

}
