package com.tomcan.android.gestures.testapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;

import com.tomcan.android.gestures.AndroidGesturesManager;
import com.mapbox.android.gestures.testapp.R;

public class TestActivity extends AppCompatActivity {

  public AndroidGesturesManager gesturesManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    gesturesManager = new AndroidGesturesManager(this);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return gesturesManager.onTouchEvent(event) || super.onTouchEvent(event);
  }
}
