package com.yunbao.mall.bean;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yunbao.common.glide.ImgLoader;

public class MainMallGoodsBean extends GoodsSimpleBean {
    private ImageView mImageView;
    private Drawable mDrawable;
    private int mDisplayHeight;

    public void display(Context context, ImageView imageView, final int displayWidth, final int displayHeightMax) {
        imageView.setImageDrawable(null);
        mImageView = imageView;
        if (mDrawable == null) {
            ImgLoader.displayDrawable(context, this.mThumb, new ImgLoader.DrawableCallback() {
                @Override
                public void onLoadSuccess(Drawable drawable) {
                    mDrawable = drawable;
                    showDrawable(displayWidth, displayHeightMax);
                }

                @Override
                public void onLoadFailed() {
                    if (mImageView != null) {
                        ViewGroup.LayoutParams lp = mImageView.getLayoutParams();
                        if (lp.height != displayWidth) {
                            lp.height = displayWidth;
                            mImageView.requestLayout();
                        }
                        mImageView.setImageDrawable(null);
                    }
                }
            });
        } else {
            showDrawable(displayWidth, displayHeightMax);
        }
    }

    private void showDrawable(final int displayWidth, final int displayHeightMax) {
        if (mDrawable != null) {
            if (mDisplayHeight == 0) {
                float w = mDrawable.getIntrinsicWidth();
                float h = mDrawable.getIntrinsicHeight();
                int displayHeight = (int) (h / w * displayWidth);
                if (displayHeight > displayHeightMax) {
                    displayHeight = displayHeightMax;
                }
                mDisplayHeight = displayHeight;
            }
            if (mImageView != null) {
                ViewGroup.LayoutParams lp = mImageView.getLayoutParams();
                if (lp.height != mDisplayHeight) {
                    lp.height = mDisplayHeight;
                    mImageView.requestLayout();
                }
                mImageView.setImageDrawable(mDrawable);
            }
        }
    }
}
