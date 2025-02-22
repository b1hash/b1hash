package com.yunbao.live.interfaces;

/**
 * Created by http://www.yunbaokj.com on 2023/11/4.
 */
public interface TxLinkMicPusher {
    /**
     * 腾讯sdk连麦时候主播混流
     *
     * @param linkMicType 混流类型 1主播与主播连麦  0 用户与主播连麦
     * @param touid       连麦者的uid
     * @param toStream    连麦者的stream
     */
    void onLinkMicTxMixStreamEvent(int linkMicType, String touid, String toStream);
}
