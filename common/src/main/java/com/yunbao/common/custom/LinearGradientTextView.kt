package com.yunbao.common.custom

import android.content.Context
import android.graphics.*
import android.graphics.Paint.FontMetricsInt
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.yunbao.common.R

/**
 * Created by http://www.yunbaokj.com on 2023/6/25.
 */
class LinearGradientTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private val mStartColor: Int
    private val mEndColor: Int
    private val mPaint: Paint

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LinearGradientTextView)
        mStartColor = ta.getColor(R.styleable.LinearGradientTextView_lgt_startColor, 0)
        mEndColor = ta.getColor(R.styleable.LinearGradientTextView_lgt_endColor, 0)
        val textSize = ta.getDimension(R.styleable.LinearGradientTextView_lgt_textSize, 0f)
        ta.recycle()
        mPaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            setTextSize(textSize)
            typeface= Typeface.DEFAULT_BOLD
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mPaint.setShader(
            LinearGradient(
                0f,
                0f,
                w.toFloat(),
                h.toFloat(),
                mStartColor,
                mEndColor,
                Shader.TileMode.CLAMP
            )
        )
    }

    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
        val s = text.toString()
        val fontMetrics: FontMetricsInt = mPaint.getFontMetricsInt()
        val y = height-fontMetrics.bottom
        canvas?.drawText(s, 0f,y.toFloat() , mPaint)
    }


}