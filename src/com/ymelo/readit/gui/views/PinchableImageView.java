package com.ymelo.readit.gui.views;

import com.ymelo.readit.Config;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class PinchableImageView extends View {
	private Drawable drawable;
	private static final int INVALID_POINTER_ID = -1;
	private static final String TAG = PinchableImageView.class.getSimpleName();
	private int mActivePointerId = INVALID_POINTER_ID;
	private float mPosX;
	private float mPosY;

	private float mLastTouchX;
	private float mLastTouchY;
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;

	public PinchableImageView(Context context) {
		super(context);

		// Create our ScaleGestureDetector
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	public PinchableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Create our ScaleGestureDetector
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}
	
	public void setImageBitmap(Bitmap bmp) {
		drawable = new BitmapDrawable(getContext().getResources(), bmp);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
		// Let the ScaleGestureDetector inspect all events.
		mScaleDetector.onTouchEvent(ev);

		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: {
				getParent().requestDisallowInterceptTouchEvent(true);
				final float x = ev.getX();
				final float y = ev.getY();
	
				mLastTouchX = x;
				mLastTouchY = y;
				mActivePointerId = ev.getPointerId(0);
				break;
			}
	
			case MotionEvent.ACTION_MOVE: {
				final int pointerIndex = ev.findPointerIndex(mActivePointerId);
				final float x = ev.getX(pointerIndex);
				final float y = ev.getY(pointerIndex);
	
				// Only move if the ScaleGestureDetector isn't processing a gesture.
				if (!mScaleDetector.isInProgress()) {
					
					final float dx = x - mLastTouchX;
					final float dy = y - mLastTouchY;
	
					mPosX += dx;
					mPosY += dy;
					if(Config.DEBUG) {
						Log.d(TAG, "moving to " + mPosX + "," + mPosY);
					}
					invalidate();
				}
	
				mLastTouchX = x;
				mLastTouchY = y;
	
				break;
			}
	
			case MotionEvent.ACTION_UP: {
				mActivePointerId = INVALID_POINTER_ID;
				break;
			}
	
			case MotionEvent.ACTION_CANCEL: {
				mActivePointerId = INVALID_POINTER_ID;
				break;
			}
	
			case MotionEvent.ACTION_POINTER_UP: {
				final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = ev.getPointerId(pointerIndex);
				if (pointerId == mActivePointerId) {
					// This was our active pointer going up. Choose a new
					// active pointer and adjust accordingly.
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					mLastTouchX = ev.getX(newPointerIndex);
					mLastTouchY = ev.getY(newPointerIndex);
					mActivePointerId = ev.getPointerId(newPointerIndex);
				}
				break;
			}
		}

		return true;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
//		canvas.getMatrix();
//		canvas.translate(mPosX, mPosY);
//		canvas.scale(mScaleFactor, mScaleFactor);
		 Matrix m = canvas.getMatrix();
		 m.postScale(mScaleFactor, mScaleFactor);//, lastEventX, lastEventY);
		 m.postTranslate(mPosX, mPosY);
		 canvas.setMatrix(m);

		drawable.draw(canvas);
		canvas.restore();
		if(Config.DEBUG) {
			Log.d(TAG, "canvas size " + canvas.getWidth() + "," + canvas.getHeight());
		}
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		private static final String TAG = "scalegesturedetector";

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();

			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

			if(mScaleFactor <= 1.1f && mScaleFactor >= 0.9f) {
				mScaleFactor = 1.0f;
			}
			invalidate();
			if(Config.DEBUG) {
				Log.d(TAG, "scale factor: " + mScaleFactor);
			}
			return true;
		}
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		super.dispatchTouchEvent(event);
		return true;
	}

}