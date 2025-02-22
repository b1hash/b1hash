package com.yunbao.live.views

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSON
import com.tencent.liteav.txcvodplayer.renderer.TextureRenderView
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.rtmp.*
import com.yunbao.common.CommonAppConfig
import com.yunbao.common.utils.DpUtil
import com.yunbao.common.utils.L
import com.yunbao.common.utils.ToastUtil
import com.yunbao.common.utils.WordUtil
import com.yunbao.common.views.AbsCommonViewHolder
import com.yunbao.live.R

/**
 * Created by http://www.yunbaokj.com on 2023/6/27.
 */
open class LiveFloatViewHolder(context: Context?, parentView: ViewGroup?, vararg args: Any?) :
    AbsCommonViewHolder(context, parentView, *args) {
    override fun getLayoutId() = R.layout.view_live_float


    private var mLivePlayer: V2TXLivePlayer? = null
    private var mVodPlayer: TXVodPlayer? = null
    private var mPlayContainer: View? = null
    var mActionListener: ActionListener? = null

    override fun init() {
        val onClick = View.OnClickListener {
            when (it.id) {
                R.id.btn_close -> {
                    stopPlay()
                    mActionListener?.onCloseClick()
                }
            }
        }
        findViewById<View>(R.id.btn_close).setOnClickListener(onClick)
        mPlayContainer = findViewById(R.id.play_container)

    }

    fun startPlay(playUrl: String?) {
        playUrl.takeIf { !it.isNullOrBlank() }?.let {
            if (it.startsWith("trtc://")
                || it.startsWith("rtmp://")
                || it.contains(".flv")
                || it.contains(".FLV")
            ) {
                startLivePlay(it)
            } else {
                startVideoPlay(it)
            }
        }
    }

    private fun startLivePlay(playUrl: String) {
        mLivePlayer = V2TXLivePlayerImpl(mContext).apply {
            setObserver(object : V2TXLivePlayerObserver() {
                override fun onVideoResolutionChanged(
                    player: V2TXLivePlayer?,
                    width: Int,
                    height: Int
                ) {
                    val vw = width.toFloat()
                    val vh = height.toFloat()
                    mPlayContainer?.apply {
                        val lp = layoutParams
                        if (vw < vh) {
                            lp.width = DpUtil.dp2px(100)
                            lp.height = (lp.width * vh / vw).toInt()
                        } else {
                            lp.height = DpUtil.dp2px(100)
                            lp.width = (lp.height * vw / vh).toInt()
                        }
                        mActionListener?.onVideoSizeChanged(lp.width, lp.height)
                        requestLayout()
                    }
                }


                override fun onError(
                    player: V2TXLivePlayer?,
                    code: Int,
                    msg: String?,
                    extraInfo: Bundle?
                ) {
                    L.e("窗口播放", "error-->code：$code,msg：$msg")
                    if (code == -8) {//断开连接
                        stopPlay()
                        mActionListener?.onCloseClick()
                    }
                }

                override fun onReceiveSeiMessage(
                    player: V2TXLivePlayer?,
                    payloadType: Int,
                    data: ByteArray?
                ) {
                    if (data != null) {
                        try {
                            val msg = String(data)
                            L.e("窗口播放", "onReceiveSeiMessage-->msg：$msg")
                            val obj = JSON.parseObject(msg)
                            val method = obj.getString("method")
                            when (method) {
                                "kick" -> {
                                    val touid = obj.getString("touid")
                                    if (touid == CommonAppConfig.getInstance().uid) {
                                        stopPlay()
                                        mActionListener?.onCloseClick()
                                        ToastUtil.show(WordUtil.getString(R.string.live_kicked_2))
                                    }
                                }
                                "endlive" -> {
                                    stopPlay()
                                    mActionListener?.onCloseClick()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
            setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill)
            setRenderView(findViewById<TextureView>(R.id.live_view))
            enableReceiveSeiMessage(true, 242)
            startLivePlay(playUrl)
        }
    }


    fun startVideoPlay(playUrl: String) {
        mVodPlayer = TXVodPlayer(mContext).apply {
            setConfig(TXVodPlayConfig().apply {
                progressInterval = 200
                headers = CommonAppConfig.HEADER
            })
            isLoop = true
            setAutoPlay(true)
            setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN)
            setVodListener(object : ITXVodPlayListener {
                override fun onPlayEvent(player: TXVodPlayer, event: Int, bundle: Bundle) {
                    when (event) {
                        TXVodConstants.VOD_PLAY_EVT_CHANGE_RESOLUTION -> {
                            val vw = bundle.getInt("EVT_PARAM1", 0).toFloat()
                            val vh = bundle.getInt("EVT_PARAM2", 0).toFloat()
                            mPlayContainer?.apply {
                                val lp = layoutParams
                                if (vw < vh) {
                                    lp.width = DpUtil.dp2px(100)
                                    lp.height = (lp.width * vh / vw).toInt()
                                } else {
                                    lp.height = DpUtil.dp2px(100)
                                    lp.width = (lp.height * vw / vh).toInt()
                                }
                                mActionListener?.onVideoSizeChanged(
                                    lp.width,
                                    lp.height
                                )
                                requestLayout()
                            }
                        }
                    }
                }

                override fun onNetStatus(p0: TXVodPlayer?, p1: Bundle?) {
                }

            })
            setPlayerView(findViewById<TextureRenderView>(R.id.video_view))
            startVodPlay(playUrl)
        }
    }

    open fun setMute(mute: Boolean) {
        if (mute) {
            mLivePlayer?.setPlayoutVolume(0)
            mVodPlayer?.setMute(true)
        } else {
            mLivePlayer?.setPlayoutVolume(100)
            mVodPlayer?.setMute(false)
        }
    }


    open fun stopPlay() {
        mLivePlayer?.apply {
            stopPlay()
            setObserver(null)
        }
        mLivePlayer = null
        mVodPlayer?.apply {
            stopPlay(false)
            setVodListener(null)
        }
        mVodPlayer = null
    }


    interface ActionListener {
        fun onCloseClick()
        fun onVideoSizeChanged(w: Int, h: Int)
    }
}