package com.lifedrop.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class MapPreviewView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MapPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(232, 238, 233));
        canvas.drawRoundRect(new RectF(0, 0, w, h), dp(22), dp(22), paint);

        // subtle road/grid lines
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(4));
        paint.setColor(Color.rgb(198, 212, 200));

        Path p1 = new Path();
        p1.moveTo(-20, h * .75f);
        p1.lineTo(w * .30f, h * .45f);
        p1.lineTo(w * .62f, h * .55f);
        p1.lineTo(w + 30, h * .18f);
        canvas.drawPath(p1, paint);

        Path p2 = new Path();
        p2.moveTo(w * .15f, -10);
        p2.lineTo(w * .42f, h * .40f);
        p2.lineTo(w * .82f, h * .90f);
        canvas.drawPath(p2, paint);

        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.rgb(212, 222, 212));
        canvas.drawLine(0, h*.25f, w, h*.82f, paint);

        drawPin(canvas, w*.22f, h*.30f, false);
        drawPin(canvas, w*.56f, h*.59f, false);
        drawPin(canvas, w*.84f, h*.24f, true);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(235,255,255,255));
        canvas.drawRoundRect(new RectF(dp(12), h-dp(30), dp(116), h-dp(10)), dp(12), dp(12), paint);
        paint.setColor(Color.rgb(101,100,95));
        paint.setTextSize(dp(10));
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        canvas.drawText("3 centers nearby", dp(23), h-dp(17), paint);
    }

    private void drawPin(Canvas c, float x, float y, boolean white) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(dp(5), 0, dp(2), Color.argb(70,0,0,0));
        paint.setColor(white ? Color.WHITE : Color.rgb(201,55,55));
        c.drawCircle(x, y, dp(15), paint);
        paint.clearShadowLayer();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(white ? Color.rgb(201,55,55) : Color.WHITE);
        c.drawCircle(x, y, dp(5), paint);
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(x, y, dp(2), paint);
    }

    private float dp(int v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
