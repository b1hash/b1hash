package com.yunbao.common.glide;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by http://www.yunbaokj.com on 2023/6/21.
 */
interface ImgLoaderStrategy {
    void display(Context context, String url, ImageView imageView);

    void displayAvatar(Context context, String url, ImageView imageView);

    void display(Context context, File file, ImageView imageView);

    void display(Context context, Uri uri, ImageView imageView);

    void display(Context context, int res, ImageView imageView);

    /**
     * 显示视频封面缩略图
     */
    void displayVideoThumb(Context context, String videoPath, ImageView imageView);

    /**
     * 显示视频封面缩略图
     */
    void displayVideoThumb(Context context, Uri uri, ImageView imageView);

    /**
     * 显示视频封面缩略图
     */
    void displayVideoThumb(Context context, File file, ImageView imageView);

    void displayDrawable(Context context, String url, final ImgLoader.DrawableCallback callback);

    void displayDrawable(Context context, File file, final ImgLoader.DrawableCallback callback);

    void displayDrawable(Context context, Uri uri, final ImgLoader.DrawableCallback callback);
    /**
     * 显示模糊的毛玻璃图片
     */
    void displayBlur(Context context, String url, ImageView imageView);

    String getCacheSize();

    void clearImageCache();
}
