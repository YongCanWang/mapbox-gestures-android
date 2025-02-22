package com.tomcan.android.gestures;

import android.content.Context;

import androidx.annotation.UiThread;
import androidx.annotation.NonNull;

import android.view.GestureDetector;
import android.view.MotionEvent;

import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_DOUBLE_TAP;
import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_DOUBLE_TAP_EVENT;
import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_DOWN;
import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_FLING;
import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_LONG_PRESS;
import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SCROLL;
import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SHOW_PRESS;
import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SINGLE_TAP_CONFIRMED;
import static com.tomcan.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SINGLE_TAP_UP;

/**
 * Detector that wraps {@link GestureDetector}.
 */
@UiThread
public class StandardGestureDetector extends BaseGesture<StandardGestureDetector.StandardOnGestureListener> {

  private final GestureDetector gestureDetector;

  public StandardGestureDetector(Context context, AndroidGesturesManager androidGesturesManager) {
    super(context, androidGesturesManager);

    this.gestureDetector = new GestureDetector(context, innerListener);
  }

  final StandardOnGestureListener innerListener = new StandardOnGestureListener() {

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      return canExecute(GESTURE_TYPE_SINGLE_TAP_UP) && listener.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
      if (canExecute(GESTURE_TYPE_LONG_PRESS)) {
        listener.onLongPress(e);
      }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return canExecute(GESTURE_TYPE_SCROLL) && listener.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return canExecute(GESTURE_TYPE_FLING) && listener.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void onShowPress(MotionEvent e) {
      if (canExecute(GESTURE_TYPE_SHOW_PRESS)) {
        listener.onShowPress(e);
      }
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return canExecute(GESTURE_TYPE_DOWN) && listener.onDown(e);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      return canExecute(GESTURE_TYPE_DOUBLE_TAP) && listener.onDoubleTap(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      return canExecute(GESTURE_TYPE_DOUBLE_TAP_EVENT) && listener.onDoubleTapEvent(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      return canExecute(GESTURE_TYPE_SINGLE_TAP_CONFIRMED) && listener.onSingleTapConfirmed(e);
    }
  };

  /**
   * Listener that merges {@link android.view.GestureDetector.OnGestureListener}
   * and {@link android.view.GestureDetector.OnDoubleTapListener}.
   */
  public interface StandardOnGestureListener extends GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {
  }

  /**
   * Listener that mirrors {@link android.view.GestureDetector.SimpleOnGestureListener}.
   */
  public static class SimpleStandardOnGestureListener implements StandardOnGestureListener {
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1,  MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }

  @Override
  protected boolean analyzeEvent(@NonNull MotionEvent motionEvent) {
    return gestureDetector.onTouchEvent(motionEvent);
  }

  /**
   * @return True if longpress in enabled, false otherwise.
   * @see GestureDetector#isLongpressEnabled()
   */
  public boolean isLongpressEnabled() {
    return gestureDetector.isLongpressEnabled();
  }

  /**
   * @param enabled True if longpress should be enabled, false otherwise.
   * @see GestureDetector#setIsLongpressEnabled(boolean)
   */
  public void setIsLongpressEnabled(boolean enabled) {
    gestureDetector.setIsLongpressEnabled(enabled);
  }
}
