package com.yunbao.common.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper

/**
 * Created by http://www.yunbaokj.com on 2023/6/27.
 */
class DragLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val mDragHelper = ViewDragHelper.create(this, object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            if (left < 0) {
                return 0
            } else if (left > width - child.width) {
                return width - child.width
            }
            return left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            if (top < 0) {
                return 0
            } else if (top > height - child.height) {
                return height - child.height
            }
            return top
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return width - child.width
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return height - child.height
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val centerLeft = (width - releasedChild.width) / 2
            if (releasedChild.left < centerLeft) {
                moveViewTo(releasedChild, 0)
            } else {
                moveViewTo(releasedChild, width - releasedChild.width)
            }
        }
    })


    private fun moveViewTo(releasedChild: View, left: Int) {
        mDragHelper.smoothSlideViewTo(releasedChild, left, releasedChild.top)
        ViewCompat.postInvalidateOnAnimation(this);
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return mDragHelper.shouldInterceptTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        mDragHelper.processTouchEvent(e)
        return mDragHelper.viewDragState == ViewDragHelper.STATE_DRAGGING
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}