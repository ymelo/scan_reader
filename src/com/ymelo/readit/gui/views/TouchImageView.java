package com.ymelo.readit.gui.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import com.ymelo.readit.Config;

public class TouchImageView extends ImageView {
	private static final String TAG = TouchImageView.class.getSimpleName();
	private VelocityTracker mVelocityTracker;

	Matrix matrix;

	// We can be in one of these 3 states

	static final int NONE = 0;

	static final int DRAG = 1;

	static final int ZOOM = 2;

	int mode = NONE;

	// Remember some things for zooming

	PointF last = new PointF();

	PointF start = new PointF();

	float minScale = 1f;

	float maxScale = 3f;

	float[] m;

	int viewWidth, viewHeight;

	static final int CLICK = 3;

	float saveScale = 1f;

	protected float origWidth, origHeight;

	int oldMeasuredWidth, oldMeasuredHeight;

	ScaleGestureDetector mScaleDetector;
	GestureDetector mFlingDetector;
	
	
	Context context;
	private int mMinimumVelocity;

	public OnDoubleTapListener listener;
	public interface OnDoubleTapListener {
		public void onDoubleTap();
	}

	
	
	
	public TouchImageView(Context context) {

		super(context);

		sharedConstructing(context);
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
	}

	public TouchImageView(Context context, AttributeSet attrs) {

		super(context, attrs);

		sharedConstructing(context);

	}

	private void sharedConstructing(Context context) {

		super.setClickable(true);

		this.context = context;

		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mFlingDetector = new GestureDetector(context, new FlingListener());

		matrix = new Matrix();

		m = new float[9];

		setImageMatrix(matrix);

		setScaleType(ScaleType.MATRIX);

		setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				mScaleDetector.onTouchEvent(event);
				mFlingDetector.onTouchEvent(event);
				PointF curr = new PointF(event.getX(), event.getY());

				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					//getParent().requestDisallowInterceptTouchEvent(true);
					mVelocityTracker = VelocityTracker.obtain();
					mVelocityTracker.addMovement(event);
					last.set(curr);

					start.set(last);

					mode = DRAG;

					break;

				case MotionEvent.ACTION_MOVE:

					if (mode == DRAG) {

						float deltaX = curr.x - last.x;

						float deltaY = curr.y - last.y;

						float fixTransX = getFixDragTrans(deltaX, viewWidth,
								origWidth * saveScale);

						float fixTransY = getFixDragTrans(deltaY, viewHeight,
								origHeight * saveScale);

						matrix.postTranslate(fixTransX, fixTransY);

						fixTrans();

						last.set(curr.x, curr.y);
						if (null != mVelocityTracker) {
							mVelocityTracker.addMovement(event);
						}
					}

					break;

				case MotionEvent.ACTION_UP:

					mode = NONE;

					int xDiff = (int) Math.abs(curr.x - start.x);

					int yDiff = (int) Math.abs(curr.y - start.y);

					if (xDiff < CLICK && yDiff < CLICK)

						performClick();

					break;

				case MotionEvent.ACTION_POINTER_UP:
					mVelocityTracker.addMovement(event);
					mVelocityTracker.computeCurrentVelocity(1000);

					final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker.getYVelocity();

					// If the velocity is greater than minVelocity, call
					// listener
					if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
						onFling(event.getX(), event.getY(), -vX, -vY);
					}
					mode = NONE;

					break;
				case MotionEvent.ACTION_CANCEL:
					// Recycle Velocity Tracker
					if (null != mVelocityTracker) {
						mVelocityTracker.recycle();
						mVelocityTracker = null;
					}
					break;
				
				}

				setImageMatrix(matrix);

				invalidate();

				return true; // indicate event was handled

			}

		});

	}

	public void setMaxZoom(float x) {

		maxScale = x;

	}
	public boolean onFling(float x, float y, float velocityX,
			float velocityY) {
		if(Config.DEBUG) {
			Log.d(TAG, "onFling");
		}
		return true;
	}
	private class FlingListener extends GestureDetector.SimpleOnGestureListener {
		

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			getParent().requestDisallowInterceptTouchEvent(false);
			if(Config.DEBUG) {
				Log.d(TAG, "onFling");
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
        @Override
        public boolean onDoubleTap(MotionEvent e) {
			listener.onDoubleTap();
			return false;
        }
	}
	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {

			mode = ZOOM;

			return true;

		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {

			float mScaleFactor = detector.getScaleFactor();

			float origScale = saveScale;

			saveScale *= mScaleFactor;

			if (saveScale > maxScale) {

				saveScale = maxScale;

				mScaleFactor = maxScale / origScale;

			} else if (saveScale < minScale) {

				saveScale = minScale;

				mScaleFactor = minScale / origScale;

			}

			if (origWidth * saveScale <= viewWidth
					|| origHeight * saveScale <= viewHeight)

				matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
						viewHeight / 2);

			else

				matrix.postScale(mScaleFactor, mScaleFactor,
						detector.getFocusX(), detector.getFocusY());

			fixTrans();

			return true;

		}

	}

	void fixTrans() {

		matrix.getValues(m);

		float transX = m[Matrix.MTRANS_X];

		float transY = m[Matrix.MTRANS_Y];

		float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);

		float fixTransY = getFixTrans(transY, viewHeight, origHeight
				* saveScale);

		if (fixTransX != 0 || fixTransY != 0)

			matrix.postTranslate(fixTransX, fixTransY);

	}

	float getFixTrans(float trans, float viewSize, float contentSize) {

		float minTrans, maxTrans;

		if (contentSize <= viewSize) {

			minTrans = 0;

			maxTrans = viewSize - contentSize;

		} else {

			minTrans = viewSize - contentSize;

			maxTrans = 0;

		}

		if (trans < minTrans)

			return -trans + minTrans;

		if (trans > maxTrans)

			return -trans + maxTrans;

		return 0;

	}

	float getFixDragTrans(float delta, float viewSize, float contentSize) {

		if (contentSize <= viewSize) {

			return 0;

		}

		return delta;

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		viewWidth = MeasureSpec.getSize(widthMeasureSpec);

		viewHeight = MeasureSpec.getSize(heightMeasureSpec);

		//

		// Rescales image on rotation

		//

		if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight

		|| viewWidth == 0 || viewHeight == 0)

			return;

		oldMeasuredHeight = viewHeight;

		oldMeasuredWidth = viewWidth;

		if (saveScale == 1) {

			// Fit to screen.

			float scale;

			Drawable drawable = getDrawable();

			if (drawable == null || drawable.getIntrinsicWidth() == 0
					|| drawable.getIntrinsicHeight() == 0)

				return;

			int bmWidth = drawable.getIntrinsicWidth();

			int bmHeight = drawable.getIntrinsicHeight();
			if(Config.DEBUG) {
				Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);
			}
			float scaleX = (float) viewWidth / (float) bmWidth;

			float scaleY = (float) viewHeight / (float) bmHeight;

			scale = Math.min(scaleX, scaleY);

			matrix.setScale(scale, scale);

			// Center the image

			float redundantYSpace = (float) viewHeight
					- (scale * (float) bmHeight);

			float redundantXSpace = (float) viewWidth
					- (scale * (float) bmWidth);

			redundantYSpace /= (float) 2;

			redundantXSpace /= (float) 2;

			matrix.postTranslate(redundantXSpace, redundantYSpace);

			origWidth = viewWidth - 2 * redundantXSpace;

			origHeight = viewHeight - 2 * redundantYSpace;

			setImageMatrix(matrix);

		}

		fixTrans();

	}

}