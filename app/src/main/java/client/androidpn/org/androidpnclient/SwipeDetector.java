package client.androidpn.org.androidpnclient;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwipeDetector implements View.OnTouchListener {

    public static enum Action {
        LR, // Left to Right
        RL, // Right to Left
        TB, // Top to bottom
        BT, // Bottom to Top
        None // when no action was detected
    }

    private static final String logTag = "SwipeDetector";
    private static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;
    private Action mSwipeDetected = Action.None;

    public boolean swipeDetected() {
        return mSwipeDetected != Action.None;
    }

    public Action getAction() {
        return mSwipeDetected;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                mSwipeDetected = Action.None;
                return false; // allow other events like Click to be processed
            case MotionEvent.ACTION_UP:
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                // horizontal swipe detection
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0) {
                        Log.i(logTag, "Swipe Left to Right");
                        mSwipeDetected = Action.LR;
                        return false;
                    }
                    if (deltaX > 0) {
                        Log.i(logTag, "Swipe Right to Left");
                        mSwipeDetected = Action.RL;
                        return false;
                    }
                } else if (Math.abs(deltaY) > MIN_DISTANCE) { // vertical swipe
                    // detection
                    // top or down
                    if (deltaY < 0) {
                        Log.i(logTag, "Swipe Top to Bottom");
                        mSwipeDetected = Action.TB;
                        return false;
                    }
                    if (deltaY > 0) {
                        Log.i(logTag, "Swipe Bottom to Top");
                        mSwipeDetected = Action.BT;
                        return false;
                    }
                }
                return false;
        }
        return false;
    }
}