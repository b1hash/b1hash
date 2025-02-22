package com.yunbao.common.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.yunbao.common.R;

/**
 * Created by http://www.yunbaokj.com on 2023/3/7.
 */
public class DashLineView extends View {

    private Paint mPaint;
    private Path mPath;

    public DashLineView(Context context) {
        this(context, null);
    }

    public DashLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DashLineView);
        int color = ta.getColor(R.styleable.DashLineView_dl_color, 0);
        float strokeWidth = ta.getDimension(R.styleable.DashLineView_dl_stroke_width, 0);
        float dashWidth = ta.getDimension(R.styleable.DashLineView_dl_dash_width, 0);
        float dashGap = ta.getDimension(R.styleable.DashLineView_dl_dash_gap, 0);
        ta.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(getWidth(), 0);
        canvas.drawPath(mPath, mPaint);
    }
}
