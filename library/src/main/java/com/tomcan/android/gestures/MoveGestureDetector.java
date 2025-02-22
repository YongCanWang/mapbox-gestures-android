package com.tomcan.android.gestures;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gesture detector handling move gesture.
 * <p>
 * {@link MoveGestureDetector} serves similar purpose to
 * {@link StandardGestureDetector.StandardOnGestureListener
 * #onScroll(MotionEvent, MotionEvent, float, float)}, however, it's a {@link ProgressiveGesture} that
 * introduces {@link OnMoveGestureListener#onMoveBegin(MoveGestureDetector)},
 * {@link OnMoveGestureListener#onMoveEnd(MoveGestureDetector, float, float)},
 * threshold with {@link MoveGestureDetector#setMoveThreshold(float)} and multi finger support thanks to
 * {@link MoveDistancesObject}.
 */
@UiThread
public class MoveGestureDetector extends ProgressiveGesture<MoveGestureDetector.OnMoveGestureListener> {
  private static final int MOVE_REQUIRED_POINTERS_COUNT = 1;
  private static final Set<Integer> handledTypes = new HashSet<>();
  private PointF previousFocalPoint;
  private boolean resetFocal;
  float lastDistanceX;
  float lastDistanceY;

  static {
    handledTypes.add(AndroidGesturesManager.GESTURE_TYPE_MOVE);
  }

  @Nullable
  private RectF moveThresholdRect;
  private float moveThreshold;

  private final Map<Integer, MoveDistancesObject> moveDistancesObjectMap = new HashMap<>();

  public MoveGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  public interface OnMoveGestureListener {
    /**
     * Indicates that the move gesture started.
     *
     * @param detector this detector
     * @return true if you want to receive subsequent {@link #onMove(MoveGestureDetector, float, float)} callbacks,
     * false if you want to ignore this gesture.
     */
    boolean onMoveBegin(@NonNull MoveGestureDetector detector);

    /**
     * Called for every move change during the gesture.
     *
     * @param detector  this detector
     * @param distanceX X distance of the focal point in pixel since last call
     * @param distanceY Y distance of the focal point in pixel since last call
     * @return true if the gesture was handled, false otherwise
     */
    boolean onMove(@NonNull MoveGestureDetector detector, float distanceX, float distanceY);

    /**
     * Indicates that the move gesture ended.
     *
     * @param velocityX velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY velocityY of the gesture in the moment of lifting the fingers
     * @param detector  this detector
     */
    void onMoveEnd(@NonNull MoveGestureDetector detector, float velocityX, float velocityY);
  }

  public static class SimpleOnMoveGestureListener implements OnMoveGestureListener {

    @Override
    public boolean onMoveBegin(@NonNull MoveGestureDetector detector) {
      return true;
    }

    @Override
    public boolean onMove(@NonNull MoveGestureDetector detector, float distanceX, float distanceY) {
      return false;
    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector detector, float velocityX, float velocityY) {
      // No implementation
    }
  }

  @Override
  protected boolean analyzeEvent(@NonNull MotionEvent motionEvent) {
    switch (motionEvent.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        resetFocal = true; //recalculating focal point

        float x = motionEvent.getX(motionEvent.getActionIndex());
        float y = motionEvent.getY(motionEvent.getActionIndex());
        moveDistancesObjectMap.put(
          motionEvent.getPointerId(motionEvent.getActionIndex()),
          new MoveDistancesObject(x, y)
        );
        break;

      case MotionEvent.ACTION_UP:
        moveDistancesObjectMap.clear();
        break;

      case MotionEvent.ACTION_POINTER_UP:
        resetFocal = true; //recalculating focal point

        moveDistancesObjectMap.remove(motionEvent.getPointerId(motionEvent.getActionIndex()));
        break;

      case MotionEvent.ACTION_CANCEL:
        moveDistancesObjectMap.clear();
        break;

      default:
        break;
    }

    return super.analyzeEvent(motionEvent);
  }

  @Override
  protected boolean analyzeMovement() {
    super.analyzeMovement();
    updateMoveDistancesObjects();

    if (isInProgress()) {
      PointF currentFocalPoint = getFocalPoint();
      lastDistanceX = previousFocalPoint.x - currentFocalPoint.x;
      lastDistanceY = previousFocalPoint.y - currentFocalPoint.y;
      previousFocalPoint = currentFocalPoint;
      if (resetFocal) {
        resetFocal = false;
        return listener.onMove(this, 0, 0);
      }
      return listener.onMove(this, lastDistanceX, lastDistanceY);
    } else if (canExecute(AndroidGesturesManager.GESTURE_TYPE_MOVE)) {
      if (listener.onMoveBegin(this)) {
        gestureStarted();
        previousFocalPoint = getFocalPoint();
        resetFocal = false;
        return true;
      }
    }
    return false;
  }

  private void updateMoveDistancesObjects() {
    for (int pointerId : pointerIdList) {
      moveDistancesObjectMap.get(pointerId).addNewPosition(
        getCurrentEvent().getX(getCurrentEvent().findPointerIndex(pointerId)),
        getCurrentEvent().getY(getCurrentEvent().findPointerIndex(pointerId))
      );
    }
  }

  boolean checkAnyMoveAboveThreshold() {
    for (MoveDistancesObject moveDistancesObject : moveDistancesObjectMap.values()) {
      boolean thresholdExceeded = Math.abs(moveDistancesObject.getDistanceXSinceStart()) >= moveThreshold
        || Math.abs(moveDistancesObject.getDistanceYSinceStart()) >= moveThreshold;

      boolean isInRect = moveThresholdRect != null && moveThresholdRect.contains(getFocalPoint().x, getFocalPoint().y);
      return !isInRect && thresholdExceeded;
    }
    return false;
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return super.canExecute(invokedGestureType) && checkAnyMoveAboveThreshold();
  }

  @Override
  protected void reset() {
    super.reset();
  }

  @Override
  protected void gestureStopped() {
    super.gestureStopped();
    listener.onMoveEnd(this, velocityX, velocityY);
  }

  @Override
  protected int getRequiredPointersCount() {
    return MOVE_REQUIRED_POINTERS_COUNT;
  }

  /**
   * Get the delta pixel threshold required to qualify it as a move gesture.
   *
   * @return delta pixel threshold
   * @see #getMoveThresholdRect()
   */
  public float getMoveThreshold() {
    return moveThreshold;
  }

  /**
   * Set the delta pixel threshold required to qualify it as a move gesture.
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param moveThreshold delta threshold
   * @see #setMoveThresholdRect(RectF)
   */
  public void setMoveThreshold(float moveThreshold) {
    this.moveThreshold = moveThreshold;
  }

  /**
   * Get the screen area in which the move gesture cannot be started.
   * If the gesture is already in progress, this value is ignored.
   * This condition is evaluated before {@link #setMoveThreshold(float)}.
   *
   * @return the screen area in which the gesture cannot be started
   */
  @Nullable
  public RectF getMoveThresholdRect() {
    return moveThresholdRect;
  }

  /**
   * Set the screen area in which the move gesture cannot be started.
   * If the gesture is already in progress, this value is ignored.
   * This condition is evaluated before {@link #setMoveThreshold(float)}.
   *
   * @param moveThresholdRect the screen area in which the gesture cannot be started
   */
  public void setMoveThresholdRect(@Nullable RectF moveThresholdRect) {
    this.moveThresholdRect = moveThresholdRect;
  }

  /**
   * Set the delta dp threshold required to qualify it as a move gesture.
   *
   * @param moveThresholdDimen delta threshold
   */
  public void setMoveThresholdResource(@DimenRes int moveThresholdDimen) {
    setMoveThreshold(context.getResources().getDimension(moveThresholdDimen));
  }

  /**
   * Returns X distance of the focal point in pixels
   * calculated during the last {@link OnMoveGestureListener#onMove(MoveGestureDetector, float, float)} call.
   *
   * @return X distance of the focal point in pixel
   */
  public float getLastDistanceX() {
    return lastDistanceX;
  }

  /**
   * Returns Y distance of the focal point in pixels
   * calculated during the last {@link OnMoveGestureListener#onMove(MoveGestureDetector, float, float)} call.
   *
   * @return Y distance of the focal point in pixel
   */
  public float getLastDistanceY() {
    return lastDistanceY;
  }

  /**
   * Returns {@link MoveDistancesObject} referencing the pointer held under passed index.
   * <p>
   * Pointers are sorted by the time they were placed on the screen until lifted up.
   * This means that index 0 will reflect the oldest added, still active pointer
   * and index ({@link #getPointersCount()} - 1) will reflect the latest added, still active pointer.
   * <p>
   *
   * @param pointerIndex pointer's index
   * @return distances object of the referenced pointer
   */
  public MoveDistancesObject getMoveObject(int pointerIndex) {
    if (isInProgress()) {
      if (pointerIndex >= 0 && pointerIndex < getPointersCount()) {
        return moveDistancesObjectMap.get(pointerIdList.get(pointerIndex));
      }
    }
    return null;
  }
}
