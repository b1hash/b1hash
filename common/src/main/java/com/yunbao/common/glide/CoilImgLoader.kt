package com.yunbao.common.glide

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.request.videoFrameMillis
import coil.target.ViewTarget
import coil.util.CoilUtils
import coil.util.Logger
import com.yunbao.common.CommonAppConfig
import com.yunbao.common.CommonAppContext
import com.yunbao.common.R
import com.yunbao.common.utils.L
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import java.io.File
import java.math.BigDecimal

/**
 * Created by http://www.yunbaokj.com on 2023/6/21.
 */
class CoilImgLoader : ImgLoaderStrategy {

    private val mImgLoader: ImageLoader = ImageLoader.Builder(CommonAppContext.getInstance())
        .components {
            add(VideoFrameDecoder.Factory())
            add(SvgDecoder.Factory())
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
//        .logger(object : Logger {
//            override var level = 0
//            override fun log(
//                tag: String,
//                priority: Int,
//                message: String?,
//                throwable: Throwable?
//            ) {
//                L.e("Coil", message)
//            }
//
//        })
        .build()
    private val mRequestListener = object : ImageRequest.Listener {
        override fun onStart(request: ImageRequest) {
        }

        override fun onCancel(request: ImageRequest) {
//            (request.target as? ViewTarget<*>)?.let { CoilUtils.dispose(it.view) }
        }

        override fun onError(request: ImageRequest, result: ErrorResult) {
            (request.target as? ViewTarget<*>)?.let { CoilUtils.dispose(it.view) }
        }

        override fun onSuccess(request: ImageRequest, result: SuccessResult) {
            (request.target as? ViewTarget<*>)?.let { CoilUtils.dispose(it.view) }
        }
    }

    private val mHeaders: Headers = CommonAppConfig.HEADER.toHeaders()

    init {
        Coil.setImageLoader(mImgLoader)
    }


    override fun display(context: Context?, url: String?, imageView: ImageView?) {
        imageView?.load(url) {
            headers(mHeaders)
            listener(mRequestListener)
        }
    }

    override fun display(context: Context?, file: File?, imageView: ImageView?) {
        imageView?.load(file){
            listener(mRequestListener)
        }
    }

    override fun display(context: Context?, uri: Uri?, imageView: ImageView?) {
        imageView?.load(uri){
            listener(mRequestListener)
        }
    }

    override fun display(context: Context?, res: Int, imageView: ImageView?) {
        imageView?.load(res){
            listener(mRequestListener)
        }
    }

    override fun displayAvatar(context: Context?, url: String?, imageView: ImageView?) {
        imageView?.load(url) {
            error(R.mipmap.icon_avatar_placeholder)
            headers(mHeaders)
            listener(mRequestListener)
        }
    }

    override fun displayVideoThumb(context: Context?, videoPath: String?, imageView: ImageView?) {
        imageView?.load(videoPath) {
            videoFrameMillis(0)
            listener(mRequestListener)
        }
    }

    override fun displayVideoThumb(context: Context?, uri: Uri?, imageView: ImageView?) {
        imageView?.load(uri) {
            videoFrameMillis(0)
            listener(mRequestListener)
        }
    }

    override fun displayVideoThumb(context: Context?, file: File?, imageView: ImageView?) {
        imageView?.load(file) {
            videoFrameMillis(0)
            listener(mRequestListener)
        }
    }

    override fun displayDrawable(
        context: Context?,
        url: String?,
        callback: ImgLoader.DrawableCallback?
    ) {
        if (context != null) {
            val request = ImageRequest.Builder(context)
                .data(url)
                .target(
                    onSuccess = { result ->
                        callback?.onLoadSuccess(result)
                    },
                    onError = { error ->
                        callback?.onLoadFailed()
                    }
                )
                .headers(mHeaders)
                .build()
            mImgLoader.enqueue(request)
        }
    }

    override fun displayDrawable(
        context: Context?,
        file: File?,
        callback: ImgLoader.DrawableCallback?
    ) {
        if (context != null) {
            val request = ImageRequest.Builder(context)
                .data(file)
                .target(
                    onSuccess = { result ->
                        callback?.onLoadSuccess(result)
                    },
                    onError = { error ->
                        callback?.onLoadFailed()
                    }
                )
                .build()
            mImgLoader.enqueue(request)
        }
    }

    override fun displayDrawable(
        context: Context?,
        uri: Uri?,
        callback: ImgLoader.DrawableCallback?
    ) {
        if (context != null) {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .target(
                    onSuccess = { result ->
                        callback?.onLoadSuccess(result)
                    },
                    onError = { error ->
                        callback?.onLoadFailed()
                    }
                )
                .build()
            mImgLoader.enqueue(request)
        }
    }

    override fun displayBlur(context: Context?, url: String?, imageView: ImageView?) {
        if (context != null) {
            imageView?.load(url) {
                headers(mHeaders)
                listener(mRequestListener)
                transformations(CoilBlurTransformation(context))
            }
        }
    }

    override fun clearImageCache() {
        mImgLoader.diskCache?.clear()
        mImgLoader.memoryCache?.clear()
    }

    /**
     * 获取缓存的大小
     */
    override fun getCacheSize(): String {
        val memoryCacheSize = mImgLoader.memoryCache?.size ?: 0
        val diskCacheSize = mImgLoader.diskCache?.size ?: 0
        val total = memoryCacheSize + diskCacheSize
        val KB = total.toDouble() / 1024
        if (KB < 1) {
            return "0.00MB"
        }
        val MB = KB / 1024f;
        if (MB < 1) {
            return BigDecimal(KB.toString()).setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "KB"
        }
        val GB = MB / 1024f;
        if (GB < 1) {
            return BigDecimal(MB.toString()).setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "MB"
        }
        val TB = GB / 1024f;
        if (TB < 1) {
            return BigDecimal(GB.toString()).setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "GB"
        }
        return BigDecimal(TB.toString()).setScale(2, BigDecimal.ROUND_HALF_UP)
            .toPlainString() + "TB"
    }
}