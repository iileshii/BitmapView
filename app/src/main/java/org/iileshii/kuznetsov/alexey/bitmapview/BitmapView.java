package org.iileshii.kuznetsov.alexey.bitmapview;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Bitmap view
 * Created by Alexey on 23.07.2015.
 */
public class BitmapView extends View {
    // View border params
    private static final int LEFT_VIEW_BORDER = 0;
    private static final int TOP_VIEW_BORDER = 0;
    private static int RIGHT_VIEW_BORDER;
    private static int BOTTOM_VIEW_BORDER;
    private Paint paint;
    private Bitmap bitmap;
    private Matrix matrix;
    private float[] zero = {0, 0};
    private float[] end = {0, 0};


    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        matrix = new Matrix();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);

        RIGHT_VIEW_BORDER = bitmap.getWidth();
        BOTTOM_VIEW_BORDER = bitmap.getHeight();
        end[0] = bitmap.getWidth();
        end[1] = bitmap.getHeight();

        setOnDragListener(new CustomDragListener());
        setOnTouchListener(new CustomTouchListener());
    }

    public float getBitmapLeft() {
        float[] dst = new float[2];
        matrix.mapPoints(dst, zero);
        return Math.min(dst[0], dst[1]);
    }

    public float getBitmapTop() {
        float[] dst = new float[2];
        matrix.mapPoints(dst, zero);
        return Math.min(dst[0], dst[1]);
    }

    public float getBitmapRight() {
        float[] dst = new float[2];
        matrix.mapPoints(dst, end);
        return Math.max(dst[0], dst[1]);
    }

    public float getBitmapBottom() {
        float[] dst = new float[2];
        matrix.mapPoints(dst, end);
        return Math.max(dst[0], dst[1]);
    }

    public float getCenterX() {
        return getBitmapRight() + (getBitmapRight() - getBitmapLeft()) / 2;
    }

    public float getCenterY() {
        return getBitmapTop() + (getBitmapBottom() - getBitmapTop()) / 2;
    }

    public void moving(int dX, int dY) {
        if (getBitmapLeft() + dX >= LEFT_VIEW_BORDER &&
                getBitmapTop() + dY >= TOP_VIEW_BORDER &&
                getBitmapRight() + dX <= RIGHT_VIEW_BORDER &&
                getBitmapBottom() + dY <= BOTTOM_VIEW_BORDER) {
                    matrix.postTranslate(dX, dY);
                }
    }


    public boolean isContent(float x, float y) {
        return (x >= getBitmapLeft() && y >= getBitmapTop() && x <= getBitmapRight() && y <= getBitmapBottom());
    }

    public void setRotate(float angle) {
        matrix.postRotate(angle, getCenterX(), getCenterY());
    }

    public void setScale(float scaleFactor) {
        if (scaleFactor < 2.0f && scaleFactor > 0.4f) {
            matrix.postScale(scaleFactor, scaleFactor);
        }
    }

    private float getAngle(float xZero, float yZero, float xNext, float yNext) {
        float angle = (float) Math.toDegrees(Math.atan2((yNext - yZero), (xNext - xZero)));

//        if (angle < 0) {
//            angle += 360;
//        }

        // TODO   return angle;
        return 1;
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        RIGHT_VIEW_BORDER = getRight();
        BOTTOM_VIEW_BORDER = getBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawBitmap(bitmap, matrix, paint);
        canvas.restore();
    }

    private class CustomDragListener implements OnDragListener {
        int lastX;
        int lastY;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    ((BitmapView) v) //Draw grey rectangle follows finger
                            .moving((int) event.getX() - lastX, (int) event.getY() - lastY);
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    invalidate();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
                case DragEvent.ACTION_DROP:
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    private class CustomTouchListener implements OnTouchListener {
        final int IDLE = 0;
        final int TOUCH = 1;
        final int PINCH = 2;
        int touchState;
        double distCurrent = 1;
        double dist0 = 1;
        float zeroX;
        float zeroY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            float distx, disty;
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    touchState = TOUCH;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    touchState = PINCH;
                    distx = motionEvent.getX(0) - motionEvent.getX(1);
                    disty = motionEvent.getY(0) - motionEvent.getY(1);
                    zeroX = motionEvent.getX(0);
                    zeroY = motionEvent.getY(0);
                    dist0 = Math.sqrt(distx * distx + disty * disty);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (touchState == PINCH) {
                        distx = motionEvent.getX(0) - motionEvent.getX(1);
                        disty = motionEvent.getY(0) - motionEvent.getY(1);
                        distCurrent = Math.sqrt(distx * distx + disty * disty);
                        float angle = getAngle(
                                zeroX,
                                zeroY,
                                motionEvent.getX(0),
                                motionEvent.getY(0));
                        setRotate(angle);
                        setScale((float) (distCurrent / dist0));
                        invalidate();
                    } else if (touchState == TOUCH && isContent(motionEvent.getX(), motionEvent.getY())) {
                        ClipData data = ClipData.newPlainText("", "");
                        DragShadowBuilder shadowBuilder = new DragShadowBuilder();
                        view.startDrag(data, shadowBuilder, view, 0);
                        view.setVisibility(VISIBLE);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    touchState = IDLE;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    touchState = TOUCH;
                    break;
            }

            return true;
        }
    }
}
