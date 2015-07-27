package org.iileshii.kuznetsov.alexey.bitmapview;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
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
    private static final float LEFT_VIEW_BORDER = 0f;
    private static final float TOP_VIEW_BORDER = 0f;
    private static float RIGHT_VIEW_BORDER;
    private static float BOTTOM_VIEW_BORDER;
    private Paint paint;
    private Bitmap bitmap;
    private Matrix matrix;
    private float[] end = {0f, 0f};
    private float[] center = {0f, 0f};

    private RectF rect;

    private float lastAngle = 0;
    private float lastScale = 1f;


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
        center[0] = end[0] / 2;
        center[1] = end[1] / 2;
        rect = new RectF(0f, 0f, end[0], end[1]);

        setOnDragListener(new CustomDragListener());
        setOnTouchListener(new CustomTouchListener());
    }

    public float getBitmapLeft() {
        RectF dst = new RectF();
        matrix.mapRect(dst, rect);
        return dst.left;
    }

    public float getBitmapTop() {
        RectF dst = new RectF();
        matrix.mapRect(dst, rect);
        return dst.top;
    }

    public float getBitmapRight() {
        RectF dst = new RectF();
        matrix.mapRect(dst, rect);
        return dst.right;
    }

    public float getBitmapBottom() {
        RectF dst = new RectF();
        matrix.mapRect(dst, rect);
        return dst.bottom;
    }

    public float getCenterX() {
        float[] dst = new float[2];
        matrix.mapPoints(dst, center);
        return dst[0];
    }

    public float getCenterY() {
        float[] dst = new float[2];
        matrix.mapPoints(dst, center);
        return dst[1];
    }

    public void moving(int dX, int dY) {
        boolean overboard = false;
        if (getBitmapLeft() < LEFT_VIEW_BORDER ||
                getBitmapTop() < TOP_VIEW_BORDER ||
                getBitmapRight() > RIGHT_VIEW_BORDER ||
                getBitmapBottom() > BOTTOM_VIEW_BORDER) {
            overboard = true;
        }
        if ((getBitmapLeft() + dX >= LEFT_VIEW_BORDER &&
                getBitmapTop() + dY >= TOP_VIEW_BORDER &&
                getBitmapRight() + dX <= RIGHT_VIEW_BORDER &&
                getBitmapBottom() + dY <= BOTTOM_VIEW_BORDER) || overboard) {
            matrix.postTranslate(dX, dY);
        }
    }


    public boolean isContent(float x, float y) {
        return (x >= getBitmapLeft() && y >= getBitmapTop() && x <= getBitmapRight() && y <= getBitmapBottom());
    }

    private void setRotate(float zeroX, float zeroY, float x, float y) {
        float angle = getAngle(zeroX, zeroY, x, y);

        Matrix checkMatrix = new Matrix(matrix);
        RectF dst = new RectF();
        checkMatrix.mapRect(dst, rect);

        boolean overboard = false;
        if (dst.left < LEFT_VIEW_BORDER ||
                dst.top < TOP_VIEW_BORDER ||
                dst.right > RIGHT_VIEW_BORDER ||
                dst.bottom > BOTTOM_VIEW_BORDER) {
            overboard = true;
        }
        checkMatrix.postRotate(angle - lastAngle, getCenterX(), getCenterY());

        checkMatrix.mapRect(dst, rect);
        if (overboard || (dst.left >= LEFT_VIEW_BORDER &&
                dst.top >= TOP_VIEW_BORDER &&
                dst.right <= RIGHT_VIEW_BORDER &&
                dst.bottom <= BOTTOM_VIEW_BORDER)) {
            matrix.postRotate(angle - lastAngle, getCenterX(), getCenterY());
            lastAngle = angle;
        }
    }

    private void setScale(double distCurrent, double dist0) {
        float currentScale = (float) (distCurrent / dist0);
        float scaleFactor = currentScale / lastScale;

        Matrix checkMatrix = new Matrix(matrix);
        RectF dst = new RectF();
        checkMatrix.mapRect(dst, rect);

        boolean overboard = false;
        if (dst.left < LEFT_VIEW_BORDER ||
                dst.top < TOP_VIEW_BORDER ||
                dst.right > RIGHT_VIEW_BORDER ||
                dst.bottom > BOTTOM_VIEW_BORDER) {
            overboard = true;
        }
        checkMatrix.postScale(scaleFactor, scaleFactor);

        checkMatrix.mapRect(dst, rect);
        if ((overboard && scaleFactor < 1) || (dst.left >= LEFT_VIEW_BORDER &&
                dst.top >= TOP_VIEW_BORDER &&
                dst.right <= RIGHT_VIEW_BORDER &&
                dst.bottom <= BOTTOM_VIEW_BORDER)) {
            if (dst.height() > 48f) {
                matrix.postScale(scaleFactor, scaleFactor, getCenterX(), getCenterY());
                lastScale = currentScale;
            }
        }
    }

    private float getAngle(float xZero, float yZero, float xNext, float yNext) {
        float angle = (float) Math.toDegrees(Math.atan2((yNext - yZero), (xNext - xZero)));

        if (angle < 0) {
            angle = angle + 360;
        }

        return angle;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        RIGHT_VIEW_BORDER = getRight();
        BOTTOM_VIEW_BORDER = getBottom();
        matrix.postTranslate(
                RIGHT_VIEW_BORDER / 2 - getCenterX(),
                BOTTOM_VIEW_BORDER / 2 - getCenterY());
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
                    lastAngle = getAngle(motionEvent.getX(1), motionEvent.getY(1), motionEvent.getX(0), motionEvent.getY(0));
                    lastScale = 1f;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (touchState == PINCH) {
                        distx = motionEvent.getX(0) - motionEvent.getX(1);
                        disty = motionEvent.getY(0) - motionEvent.getY(1);
                        distCurrent = Math.sqrt(distx * distx + disty * disty);

                        setScale(distCurrent, dist0);
                        setRotate(motionEvent.getX(1), motionEvent.getY(1), motionEvent.getX(0), motionEvent.getY(0));
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
