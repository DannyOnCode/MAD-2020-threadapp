package com.threadteam.thread;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.interfaces.RecyclerViewClickListener;

// RECYCLER TOUCH LISTENER CLASS
//
// PROGRAMMER-IN-CHARGE:
// EUGENE LONG, S10193060J
//
// DESCRIPTION
// Handles onItemTouch events and acts similarly to
// OnClickListener. Used for RecyclerViews.

public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

    // DATA STORE
    private GestureDetector gestureDetector;
    private RecyclerViewClickListener clickListener;

    // CONSTRUCTOR
    public RecyclerTouchListener(Context context, final RecyclerView recyclerView,
                                 final RecyclerViewClickListener clickListener) {
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });
    }

    // METHOD OVERRIDES FOR ON ITEM TOUCH LISTENER
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {

            // CALL INTERFACE WITH VIEW AND POSITION
            clickListener.onClick(child, rv.getChildAdapterPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
}
