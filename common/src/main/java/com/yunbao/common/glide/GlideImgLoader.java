package com.yunbao.common.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.R;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by cxf on 2017/8/9.
 */

public class GlideImgLoader implements ImgLoaderStrategy{
    private final boolean mSkipMemoryCache = false;

    private final BlurTransformation sBlurTransformation;
    private final Headers sHeaders;

    public GlideImgLoader() {
        sBlurTransformation = new BlurTransformation(25);
        sHeaders = new Headers() {
            @Override
            public Map<String, String> getHeaders() {
                return CommonAppConfig.HEADER;
            }
        };
    }

    @Override
    public void display(Context context, String url, ImageView imageView) {
        if (context == null || TextUtils.isEmpty(url) || imageView == null) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders)).skipMemoryCache(mSkipMemoryCache).into(imageView);
    }

    @Override
    public void displayAvatar(Context context, String url, ImageView imageView) {
        if (context == null || TextUtils.isEmpty(url) || imageView == null) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders)).error(R.mipmap.icon_avatar_placeholder).skipMemoryCache(mSkipMemoryCache).into(imageView);
    }
    @Override
    public void display(Context context, File file, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }
        Glide.with(context).asDrawable().load(file).skipMemoryCache(mSkipMemoryCache).into(imageView);
    }
    @Override
    public void display(Context context, Uri uri, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }
        Glide.with(context).asDrawable().load(uri).skipMemoryCache(mSkipMemoryCache).into(imageView);
    }
    @Override
    public void display(Context context, int res, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }
        Glide.with(context).asDrawable().load(res).skipMemoryCache(mSkipMemoryCache).into(imageView);
    }

    /**
     * 显示视频封面缩略图
     */
    @Override
    public void displayVideoThumb(Context context, String videoPath, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }
        Glide.with(context).asDrawable().load(new File(videoPath)).skipMemoryCache(mSkipMemoryCache).into(imageView);
    }

    /**
     * 显示视频封面缩略图
     */
    @Override
    public void displayVideoThumb(Context context, Uri uri, ImageView imageView) {
        if (context == null) {
            return;
        }
        Glide.with(context).asDrawable().load(uri).skipMemoryCache(mSkipMemoryCache).into(imageView);
    }

    /**
     * 显示视频封面缩略图
     */
    @Override
    public void displayVideoThumb(Context context, File file, ImageView imageView) {
        if (context == null) {
            return;
        }
        Glide.with(context).asDrawable().load(file).skipMemoryCache(mSkipMemoryCache).into(imageView);
    }

    @Override
    public void displayDrawable(Context context, String url, final ImgLoader.DrawableCallback callback) {
        if (context == null || TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders)).skipMemoryCache(mSkipMemoryCache).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (callback != null) {
                    callback.onLoadSuccess(resource);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (callback != null) {
                    callback.onLoadFailed();
                }
            }

        });
    }

    @Override
    public void displayDrawable(Context context, File file, final ImgLoader.DrawableCallback callback) {
        if (context == null || file == null || !file.exists()) {
            return;
        }
        Glide.with(context).asDrawable().load(file).skipMemoryCache(mSkipMemoryCache).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (callback != null) {
                    callback.onLoadSuccess(resource);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (callback != null) {
                    callback.onLoadFailed();
                }
            }

        });
    }

    @Override
    public void displayDrawable(Context context, Uri uri, ImgLoader.DrawableCallback callback) {
        if (context == null) {
            return;
        }
        Glide.with(context).asDrawable().load(uri).skipMemoryCache(mSkipMemoryCache).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (callback != null) {
                    callback.onLoadSuccess(resource);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (callback != null) {
                    callback.onLoadFailed();
                }
            }

        });
    }

    /**
     * 显示模糊的毛玻璃图片
     */
    @Override
    public void displayBlur(Context context, String url, ImageView imageView) {
        if (context == null || TextUtils.isEmpty(url) || imageView == null) {
            return;
        }
        Glide.with(context).asDrawable().load(new GlideUrl(url, sHeaders))
                .skipMemoryCache(mSkipMemoryCache)
                .apply(RequestOptions.bitmapTransform(sBlurTransformation))
                .into(imageView);
    }

    @Override
    public String getCacheSize() {
        try {
            return getFormatSize(getFolderSize(new File(CommonAppContext.getInstance().getCacheDir() + "/" + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void clearImageCache() {
        clearImageDiskCache();
        clearImageMemoryCache();
        String ImageExternalCatchDir = CommonAppContext.getInstance().getExternalCacheDir() + ExternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR;
        deleteFolderFile(ImageExternalCatchDir, true);
    }

    /**
     * 清除图片磁盘缓存
     */
    private void clearImageDiskCache() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(CommonAppContext.getInstance()).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(CommonAppContext.getInstance()).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片内存缓存
     */
    private void clearImageMemoryCache() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(CommonAppContext.getInstance()).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     * @throws Exception
     */
    private long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     *
     * @param filePath       filePath
     * @param deleteThisPath deleteThisPath
     */
    private void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        file.delete();
                    } else {
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 格式化单位
     *
     * @param size size
     * @return size
     */
    private static String getFormatSize(double size) {

        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return "0.00MB";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);

        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

}
