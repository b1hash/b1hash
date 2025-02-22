package com.yunbao.im.event;

import com.yunbao.im.bean.ImConversationBean;

/**
 * Created by cxf on 2018/10/24.
 */

public class ImConversationEvent {
    private ImConversationBean mConBean;

    public ImConversationEvent(ImConversationBean conBean) {
        mConBean = conBean;
    }

    public ImConversationBean getConBean() {
        return mConBean;
    }

    public String getFrom() {
        if(mConBean!=null){
            return mConBean.getUserID();
        }
        return "";
    }
}
