package org.iileshii.kuznetsov.alexey.bitmapview;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * @class creates CustomView
 * Created by Alexey on 22.07.2015.
 */
public class BitmapView extends View {
    private Paint paint;
    private Bitmap bitmap;
    private Matrix matrix;

    private float scaleFactor = 0.4f;

    private float left;
    private float top;

    private boolean moving;


    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        setColor(Color.GREEN);
        matrix = new Matrix();
        left = 0;
        top = 0;
        matrix.setScale(scaleFactor, scaleFactor);
        moving = false;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        setOnDragListener(new CustomDragListener());
        setOnTouchListener(new CustomTouchListener());
    }


    public float getBitmapLeft() {
        return left;
    }

    public float getBitmapTop() {
        return top;
    }

    public float getBitmapRight() {
        return left + bitmap.getWidth() * scaleFactor / 2;
    }

    public float getBitmapBottom() {
        return top + bitmap.getHeight() * scaleFactor / 2;
    }

    public void setPlace(float dX, float dY) {
        if (left + dX < 0 || top + dY < 0 ||
                getBitmapRight() + dX > 700 || getBitmapBottom() + dY > 1050)
            return;
        left = left + dX;
        top = top + dY;
        moving = true;
        invalidate();
    }

    public boolean isContent(float x, float y) {
        return (x >= left && y >= top && x <= getBitmapRight() && y <= getBitmapRight());
    }

    public void setScale(float scaleFactor) {
        this.scaleFactor *= Math.pow(scaleFactor, 1.0 / 5);
        this.scaleFactor = Math.max(0.1f, Math.min(this.scaleFactor, 2.0f));
        matrix.setScale(this.scaleFactor, this.scaleFactor);
        invalidate();
    }

    public void setRotate(float angle) {
        matrix.setRotate(
                angle,
                bitmap.getWidth() * scaleFactor / 2,
                bitmap.getHeight() * scaleFactor / 2);
        invalidate();
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        if (moving) {
            canvas.drawBitmap(bitmap, left, top, paint);
            moving = false;
        } else {
            canvas.drawBitmap(bitmap, matrix, paint);
            left = 0;
            top = 0;
        }

        canvas.restore();
    }

    private float getAngle(float xZero, float yZero, float xNext, float yNext) {
        float angle = (float) Math.toDegrees(Math.atan2((yNext - yZero), (xNext - xZero)));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    private class CustomDragListener implements OnDragListener {
        float firstX, firstY;
        FrameLayout.LayoutParams layoutParams;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    firstX = (int) event.getX() -
                            ((BitmapView) v).getBitmapLeft() + layoutParams.leftMargin;
                    firstY = (int) event.getY() -
                            ((BitmapView) v).getBitmapTop() + layoutParams.topMargin;
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    ((BitmapView) v).setPlace(
                            (int) event.getX() - firstX,
                            (int) event.getY() - firstY);
                    invalidate();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
                case DragEvent.ACTION_DROP:
                    ((BitmapView) v).setPlace(
                            (int) event.getX() - firstX,
                            (int) event.getY() - firstY);
                    invalidate();
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
        Point fingerPosition0;
        Point fingerPosition1;
        float zeroX;
        float zeroY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            float distx, disty;
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    touchState = TOUCH;
                    fingerPosition0 = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
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
                        if (Math.abs(distCurrent - dist0) < 50f
                                && Math.abs(distCurrent - dist0) > 1f) {
                            float angle = getAngle(
                                    zeroX,
                                    zeroY,
                                    motionEvent.getX(0),
                                    motionEvent.getY(0));
                            setRotate(angle);
                        } else {
                            setScale((float) (distCurrent / dist0));
                        }
                    } else if (touchState == TOUCH
                            && isContent(motionEvent.getX(), motionEvent.getY())) {
                        fingerPosition1 =
                                new Point((int) motionEvent.getX(), (int) motionEvent.getY());
                        int dX = fingerPosition1.x - fingerPosition0.x;
                        int dY = fingerPosition1.y - fingerPosition0.y;
                        double distPosition = Math.sqrt(dX * dX + dY * dY);
                        if (distPosition > 1f) {
                            ClipData data = ClipData.newPlainText("", "");
                            DragShadowBuilder shadowBuilder = new DragShadowBuilder();
                            view.startDrag(data, shadowBuilder, view, 0);
                            view.setVisibility(VISIBLE);
                        }
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
