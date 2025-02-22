package com.yunbao.common.http.fuel

import android.net.Uri
import com.alibaba.fastjson.JSON
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.extensions.httpString
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.yunbao.common.BuildConfig
import com.yunbao.common.CommonAppContext
import com.yunbao.common.R
import com.yunbao.common.event.LoginInvalidEvent
import com.yunbao.common.http.*
import com.yunbao.common.utils.L
import com.yunbao.common.utils.RouteUtil
import com.yunbao.common.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.*

/**
 * Created by http://www.yunbaokj.com on 2023/3/14.
 */
class FuelRequest(
    val mUrl: String,
    val mMehtod: Method = Method.GET,
) : IHttpRequest {

    enum class Method {
        GET, POST
    }

    val mParams: MutableList<Pair<String, Any?>> = mutableListOf()
    val mDebug: Boolean = BuildConfig.DEBUG
    var mReq: CancellableRequest? = null
    var mHeaders: MutableMap<String, String>? = null

    fun setServiceName(serviceName: String): IHttpRequest {
        mParams.add(Pair("service", serviceName))
        return this
    }


    override fun params(key: String, value: Any?): IHttpRequest {
        if (value != null) {
            mParams.add(Pair(key, value))
        }
        return this
    }

    override fun headers(key: String, value: String): IHttpRequest {
        val map = mHeaders ?: mutableMapOf<String, String>().apply { mHeaders = this }
        map.put(key, value)
        return this
    }


    fun cancel() {
        mReq?.cancel()
        mReq = null
    }

    private fun log(content: String) {
        L.e("http", content)
    }

    override fun execute(callback: HttpCallback) {
        val req = if (mMehtod == Method.POST) {
            Fuel.post(mUrl, mParams)
        } else {
            Fuel.get(mUrl, mParams)
        }
        mHeaders?.let {
            req.header(it)
        }
        if (mDebug) {
            log("发起请求===>${req.httpString()}")
        }
        callback.onStart()
        mReq = req.responseString(object : ResponseHandler<String> {
            override fun success(request: Request, response: Response, value: String) {
                if (mDebug) {
                    log("请求成功<===${request.httpString()}")
                    log("返回数据：$value")
                }
                val str = if (!value.startsWith("{")) {
                    val index = value.indexOf("{")
                    if (index != -1) {
                        value.substring(index)
                    } else {
                        value
                    }
                } else value
                val jsonObject = try {
                    JSON.parseObject(str)
                } catch (e: Exception) {
                    null
                }
                if (jsonObject != null) {
                    val ret = jsonObject.getIntValue("ret");
                    val msg = jsonObject.getString("msg");
                    if (200 == ret) {
                        val data = try {
                            JSON.parseObject(jsonObject.getString("data"), Data::class.java)
                        } catch (e: Exception) {
                            null
                        }
                        if (data != null) {
                            if (700 == data.code) {
                                //token过期，重新登录
                                if (callback.isUseLoginInvalid()) {
                                    callback.onLoginInvalid()
                                } else {
                                    EventBus.getDefault().post(LoginInvalidEvent())
                                    RouteUtil.forwardLoginInvalid(data.msg)
                                }
                            } else {
                                callback.onSuccess(data.code, data.msg, data.info)
                            }
                        } else {
                            if (mDebug) {
                                log("服务器返回值异常--->ret: $ret msg: $msg")
                            }
                        }
                    } else {
                        if (mDebug) {
                            log("服务器返回值异常--->ret: $ret msg: $msg")
                        }
                    }
                } else {
                    if (mDebug) {
                        log("服务器返回值异常--->jsonObject = null")
                    }
                }
                callback.onFinish()
                mReq = null
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                val t: Throwable = error.exception
                if (mDebug) {
                    log("请求失败<===${request.httpString()}")
                    log("错误--message-->${error.message}--Throwable-->${t.javaClass}")
                }
                if (t is SocketTimeoutException || t is ConnectException || t is UnknownHostException || t is UnknownServiceException || t is SocketException) {
                    ToastUtil.show(R.string.load_failure)
                }
                callback.onError()
                callback.onFinish()
                mReq = null
            }
        })
    }

    override fun execute(callback: StringHttpCallback) {
        val req = if (mMehtod == Method.POST) {
            Fuel.post(mUrl, mParams)
        } else {
            Fuel.get(mUrl, mParams)
        }
        mHeaders?.let {
            req.header(it)
        }
        if (mDebug) {
            log("发起请求===>${req.httpString()}")
        }
        mReq = req.responseString(object : ResponseHandler<String> {
            override fun success(request: Request, response: Response, value: String) {
                if (mDebug) {
                    log("请求成功<===${request.httpString()}")
                    log("返回数据：$value")
                }
                callback.onSuccess(value)
                callback.onFinish()
                mReq = null
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                val e: Throwable = error.exception
                if (mDebug) {
                    log("请求失败<===${request.httpString()}")
                    log("错误--message-->${error.message}--Throwable-->${e.javaClass}")
                }
                callback.onError(e)
                callback.onFinish()
                mReq = null
            }
        })
    }

    override fun execute(dir: File, fileName: String, callback: FileDownloadCallback) {
        val saveFile = File(dir, fileName)
        var intputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            var p1 = 0;
            outputStream = FileOutputStream(saveFile)
            val req = Fuel.download(path = mUrl, parameters = mParams)
            val executor = req.executionOptions.callbackExecutor
            req.streamDestination { response, request ->
                Pair(outputStream, {
                    intputStream = request.body.toStream()
                    intputStream!!
                })
            }.progress { readBytes, totalBytes ->
                val p2: Int = (readBytes.toFloat() / totalBytes.toFloat() * 100).toInt()
                if (p1 != p2) {
                    p1 = p2
                    if (mDebug) {
                        log("下载进度--->$p1")
                    }
                    executor.execute { callback.onProgress(p1 / 100f) }
                }
            }
            mHeaders?.let {
                req.header(it)
            }
            if (mDebug) {
                log("开始下载===>${req.httpString()}")
            }
            mReq = req.response(object : ResponseHandler<ByteArray> {
                override fun success(request: Request, response: Response, value: ByteArray) {
                    try {
                        intputStream?.close()
                        outputStream.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (mDebug) {
                        log("下载成功--path->${saveFile.absolutePath}--size-->${saveFile.length()}")
                    }
                    callback.onSuccess(saveFile)
                    callback.onFinish()
                }

                override fun failure(request: Request, response: Response, error: FuelError) {
                    try {
                        intputStream?.close()
                        outputStream.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val e: Throwable = error.exception
                    if (mDebug) {
                        log("下载失败---message-->${error.message}--Throwable-->${e.javaClass}")
                    }
                    callback.onError(e)
                    callback.onFinish()
                }
            })
        } catch (e: Exception) {
            if (mDebug) {
                log("下载报错---Exception-->${e.printStackTrace()}")
            }
        }
    }

    override fun execute(uri: Uri, callback: UriDownloadCallback) {
        var intputStream: InputStream? = null
        val outputStream: OutputStream? =
            CommonAppContext.getInstance().contentResolver.openOutputStream(uri)
        if (outputStream == null) {
            return
        }
        try {
            var p1 = 0;
            val req = Fuel.download(path = mUrl, parameters = mParams)
            val executor = req.executionOptions.callbackExecutor
            req.streamDestination { response, request ->
                Pair(outputStream, {
                    intputStream = request.body.toStream()
                    intputStream!!
                })
            }.progress { readBytes, totalBytes ->
                val p2: Int = (readBytes.toFloat() / totalBytes.toFloat() * 100).toInt()
                if (p1 != p2) {
                    p1 = p2
                    if (mDebug) {
                        log("下载进度--->$p1")
                    }
                    executor.execute { callback.onProgress(p1 / 100f) }
                }
            }
            mHeaders?.let {
                req.header(it)
            }
            if (mDebug) {
                log("开始下载===>${req.httpString()}")
            }
            mReq = req.response(object : ResponseHandler<ByteArray> {
                override fun success(request: Request, response: Response, value: ByteArray) {
                    try {
                        intputStream?.close()
                        outputStream.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (mDebug) {
                        log("下载成功--uri->${uri}")
                    }
                    callback.onSuccess()
                    callback.onFinish()
                }

                override fun failure(request: Request, response: Response, error: FuelError) {
                    try {
                        intputStream?.close()
                        outputStream.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val e: Throwable = error.exception
                    if (mDebug) {
                        log("下载失败---message-->${error.message}--Throwable-->${e.javaClass}")
                    }
                    callback.onError(e)
                    callback.onFinish()
                }
            })
        } catch (e: Exception) {
            if (mDebug) {
                log("下载报错---Exception-->${e.printStackTrace()}")
            }
        }
    }
}