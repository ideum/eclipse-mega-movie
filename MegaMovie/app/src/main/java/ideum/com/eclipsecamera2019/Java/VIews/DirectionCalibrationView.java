package ideum.com.eclipsecamera2019.Java.VIews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import ideum.com.eclipsecamera2019.R;

/**
 * Created by MT_User on 6/23/2017.
 */



public class DirectionCalibrationView extends View {
    private static final float TOLERANCE = 0.23f;


    public static class ScreenVector {
        public float x;
        public float y;

        public ScreenVector(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void setXY(float newX,float newY) {
            x = newX;
            y = newY;
        }
    }

   private float arrowX;
    public void setArrowX(float length) {
        arrowX = length;
        invalidate();
        requestLayout();
    }
    private float arrowY;

    public void setArrowY(float angle) {
        arrowY = angle;
        invalidate();
        requestLayout();
    }
    private float circleRadius;


    public void setCircleRadius(float radius) {
        if (radius > 1-TOLERANCE) {
            circleColor = pointedColor;
        } else {
            circleColor = unpointedColor;
        }


        circleRadius = radius;
        invalidate();
        requestLayout();
    }

    private RectF circleRect;


    private Paint mCirclePaint;
    private Paint mFullCirclePaint;
    private Paint mArrowPaint;
    private int arrowColor;
    private int circleColor;
    private int unpointedColor;
    private int pointedColor;

    private float getMaxRadius() {
        float a = getWidth()/2;
        float b = getHeight()/2;
        return Math.min(a,b);
    }



    public DirectionCalibrationView(Context context, AttributeSet attrs) {
        super(context,attrs);

        arrowColor = Color.RED;
        unpointedColor = context.getResources().getColor(R.color.colorPrimary);
        pointedColor = context.getResources().getColor(R.color.colorPointed);
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(circleColor);


        int fullCircleColor = context.getResources().getColor(R.color.intro_color_2);
        mFullCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFullCirclePaint.setColor(fullCircleColor);
        mFullCirclePaint.setStyle(Paint.Style.STROKE);
        mFullCirclePaint.setStrokeWidth(3.0f);

        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setColor(arrowColor);
        mArrowPaint.setStrokeWidth(10.0f);

        arrowX = 0;
        arrowY = 0;

        circleRadius = 50;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCirclePaint.setColor(circleColor);
        float maxR = getMaxRadius();

        float startX = getWidth()/2.0f;
        float startY = getHeight()/2.0f;
        float endX = startX + maxR * arrowX;
        float endY = startY + maxR * arrowY;
        float r = maxR * circleRadius;
        float rOuter = maxR * (1-TOLERANCE);

        canvas.drawCircle(startX,startY,r,mCirclePaint);
        canvas.drawLine(startX,startY,endX,endY,mArrowPaint);
        canvas.drawCircle(startX,startY,rOuter,mFullCirclePaint);
    }
}
