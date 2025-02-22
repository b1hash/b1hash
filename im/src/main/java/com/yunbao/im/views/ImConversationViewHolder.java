package com.yunbao.im.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.event.FollowEvent;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.im.R;
import com.yunbao.im.activity.ImMsgAtActivity;
import com.yunbao.im.activity.ImMsgCommentActivity;
import com.yunbao.im.activity.ImMsgConcatActivity;
import com.yunbao.im.activity.ImMsgLikeActivity;
import com.yunbao.im.activity.SystemMessageActivity;
import com.yunbao.im.adapter.ImListAdapter;
import com.yunbao.im.bean.ImConUserBean;
import com.yunbao.im.bean.ImConversationBean;
import com.yunbao.im.bean.SystemMessageBean;
import com.yunbao.im.dialog.SystemMessageDialogFragment;
import com.yunbao.im.event.ImMessageRevokeEvent;
import com.yunbao.im.event.ImConversationEvent;
import com.yunbao.im.event.ImUnReadCountEvent;
import com.yunbao.im.event.SystemMsgEvent;
import com.yunbao.im.http.ImHttpConsts;
import com.yunbao.im.http.ImHttpUtil;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.im.utils.ImUnReadCount;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by cxf on 2018/10/24.
 */

public class ImConversationViewHolder extends AbsViewHolder implements View.OnClickListener, ImListAdapter.ActionListener {

    public static final int TYPE_ACTIVITY = 0;
    public static final int TYPE_DIALOG = 1;
    private int mType;
    private View mBtnSystemMsg;
    private RecyclerView mRecyclerView;
    private ImListAdapter mAdapter;
    private ActionListener mActionListener;
    private View mSystemMsgRedPoint;//系统消息的红点
    private TextView mSystemMsgContent;
    private TextView mSystemTime;
    private HttpCallback mSystemMsgCallback;
    private View mBtnBack;
    private String mLiveUid;//主播的uid
    private TextView mRedPointConcat;//联系人
    private TextView mRedPointLike;//赞
    private TextView mRedPointAt;//@我的
    private TextView mRedPointComment;//评论


    public ImConversationViewHolder(Context context, ViewGroup parentView, int type) {
        super(context, parentView, type);
    }

    @Override
    protected void processArguments(Object... args) {
        mType = (int) args[0];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_im_conversation;
    }

    @Override
    public void init() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ImListAdapter(mContext);
        mAdapter.setActionListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mBtnBack = findViewById(R.id.btn_back);
        if (mType == TYPE_ACTIVITY) {
            mBtnBack.setOnClickListener(this);
        } else {
            mBtnBack.setVisibility(View.INVISIBLE);
            View top = findViewById(R.id.top);
            top.setBackgroundColor(0xfff9fafb);
        }
        findViewById(R.id.btn_ignore).setOnClickListener(this);
        View headView = mAdapter.getHeadView();
        View groupMore = headView.findViewById(R.id.group_more);
        if (mType == TYPE_DIALOG) {
            groupMore.setVisibility(View.GONE);
        } else {
            mRedPointConcat = headView.findViewById(R.id.red_point_concat);
            mRedPointLike = headView.findViewById(R.id.red_point_like);
            mRedPointAt = headView.findViewById(R.id.red_point_at);
            mRedPointComment = headView.findViewById(R.id.red_point_comment);
            headView.findViewById(R.id.btn_concat).setOnClickListener(this);
            headView.findViewById(R.id.btn_zan).setOnClickListener(this);
            headView.findViewById(R.id.btn_at).setOnClickListener(this);
            headView.findViewById(R.id.btn_comment).setOnClickListener(this);

            ImUnReadCount imUnReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
            showUnReadCount(mRedPointConcat, imUnReadCount.getConcatUnReadCount());
            showUnReadCount(mRedPointLike, imUnReadCount.getLikeUnReadCount());
            showUnReadCount(mRedPointAt, imUnReadCount.getAtUnReadCount());
            showUnReadCount(mRedPointComment, imUnReadCount.getCommentUnReadCount());
        }
        mBtnSystemMsg = headView.findViewById(R.id.btn_system_msg);
        mBtnSystemMsg.setOnClickListener(this);
        mSystemMsgRedPoint = headView.findViewById(R.id.red_point);
        mSystemMsgContent = headView.findViewById(R.id.msg);
        mSystemTime = headView.findViewById(R.id.time);
        mSystemMsgCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    SystemMessageBean bean = JSON.parseObject(info[0], SystemMessageBean.class);
                    if (mSystemMsgContent != null) {
                        mSystemMsgContent.setText(bean.getContent());
                    }
                    if (mSystemTime != null) {
                        mSystemTime.setText(bean.getAddtime());
                    }
                    if (SpUtil.getInstance().getIntValue(SpUtil.SYSTEM_MSG_COUNT, 0) > 0) {
                        if (mSystemMsgRedPoint != null && mSystemMsgRedPoint.getVisibility() != View.VISIBLE) {
                            mSystemMsgRedPoint.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mSystemMsgRedPoint != null && mSystemMsgRedPoint.getVisibility() != View.INVISIBLE) {
                            mSystemMsgRedPoint.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        };
        ImageView avatar = headView.findViewById(R.id.avatar);
        avatar.setImageResource(CommonAppConfig.getInstance().getAppIconRes());
        EventBus.getDefault().register(this);
    }


    private void showUnReadCount(TextView redPointView, String unReadCount) {
        if (redPointView != null) {
            if (!"0".equals(unReadCount)) {
                if (redPointView.getVisibility() != View.VISIBLE) {
                    redPointView.setVisibility(View.VISIBLE);
                }
                redPointView.setText(unReadCount);
            } else {
                if (redPointView.getVisibility() == View.VISIBLE) {
                    redPointView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void release() {
        EventBus.getDefault().unregister(this);
        mActionListener = null;
        ImHttpUtil.cancel(ImHttpConsts.GET_SYSTEM_MESSAGE_LIST);
        ImHttpUtil.cancel(ImHttpConsts.GET_IM_USER_INFO);
    }

    public void setLiveUid(String liveUid) {
        mLiveUid = liveUid;
    }

    public void loadData() {
        getSystemMessageList();
        if (!CommonAppConfig.getInstance().isPrivateMsgSwitchOpen()) {
            return;
        }
        ImMessageUtil.getInstance().getConversationList(new CommonCallback<List<ImConversationBean>>() {
            @Override
            public void callback(final List<ImConversationBean> conBeanList) {
                //是否需要添加一个主播的会话
                final boolean needShowAnchorItem = mType == TYPE_DIALOG
                        && !TextUtils.isEmpty(mLiveUid)
                        && !mLiveUid.equals(CommonAppConfig.getInstance().getUid());
                //主播的会话是否存在
                boolean hasAnchorItem = false;
                if (needShowAnchorItem) {
                    for (ImConversationBean bean : conBeanList) {
                        if (bean.getUserID().equals(mLiveUid)) {
                            hasAnchorItem = true;
                            break;
                        }
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0, size = conBeanList.size(); i < size; i++) {
                    sb.append(conBeanList.get(i).getUserID());
                    if (i < size - 1) {
                        sb.append(",");
                    }
                }
                if (needShowAnchorItem && !hasAnchorItem) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(mLiveUid);
                }
                String uids = sb.toString();
                if (TextUtils.isEmpty(uids)) {
                    return;
                }
                ImHttpUtil.getImUserInfo(uids, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            List<ImConUserBean> conUserList = JSON.parseArray(Arrays.toString(info), ImConUserBean.class);
                            for (ImConUserBean conUserBean : conUserList) {
                                if (needShowAnchorItem && conUserBean.getId().equals(mLiveUid)) {
                                    conUserBean.setAnchorItem(true);
                                    conUserBean.setTips(WordUtil.getString(R.string.im_live_anchor_msg));
                                }
                                for (ImConversationBean conBean : conBeanList) {
                                    if (conBean.getUserID().equals(conUserBean.getId())) {
                                        conUserBean.setConBean(conBean);
                                        break;
                                    }
                                }
                            }
                            Collections.sort(conUserList, new Comparator<ImConUserBean>() {
                                @Override
                                public int compare(ImConUserBean bean1, ImConUserBean bean2) {
                                    if (bean1.isAnchorItem()) {
                                        return -1;
                                    } else if (bean2.isAnchorItem()) {
                                        return 1;
                                    }
                                    if (Constants.MALL_GOODS_ORDER.equals(bean1.getConUserId())) {
                                        return -1;
                                    } else if (Constants.MALL_GOODS_ORDER.equals(bean2.getConUserId())) {
                                        return 1;
                                    }
                                    return (int) (bean2.getMsgTime() - bean1.getMsgTime());
                                }
                            });

                            if (mRecyclerView != null && mAdapter != null) {
                                mAdapter.setList(conUserList);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_back) {
            if (mActionListener != null) {
                mActionListener.onCloseClick();
            }
        } else if (i == R.id.btn_ignore) {
            ignoreUnReadCount();

        } else if (i == R.id.btn_system_msg) {
            forwardSystemMessage();
        } else if (i == R.id.btn_concat) {
            ImMessageUtil.getInstance().markConversationAsRead(Constants.IM_MSG_CONCAT);
            ImMsgConcatActivity.forward(mContext);
        } else if (i == R.id.btn_zan) {
            ImMessageUtil.getInstance().markConversationAsRead(Constants.IM_MSG_LIKE);
            ImMsgLikeActivity.forward(mContext);
        } else if (i == R.id.btn_at) {
            ImMessageUtil.getInstance().markConversationAsRead(Constants.IM_MSG_AT);
            ImMsgAtActivity.forward(mContext);
        } else if (i == R.id.btn_comment) {
            ImMessageUtil.getInstance().markConversationAsRead(Constants.IM_MSG_COMMENT);
            ImMsgCommentActivity.forward(mContext);
        }

    }

    /**
     * 前往系统消息
     */
    private void forwardSystemMessage() {
        SpUtil.getInstance().setIntValue(SpUtil.SYSTEM_MSG_COUNT, 0);
        EventBus.getDefault().post(new ImUnReadCountEvent());
        if (mSystemMsgRedPoint != null && mSystemMsgRedPoint.getVisibility() == View.VISIBLE) {
            mSystemMsgRedPoint.setVisibility(View.INVISIBLE);
        }
        if (mType == TYPE_ACTIVITY) {
            SystemMessageActivity.forward(mContext);
        } else {
            SystemMessageDialogFragment fragment = new SystemMessageDialogFragment();
            fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "SystemMessageDialogFragment");
        }
    }

    @Override
    public void onItemClick(final ImConUserBean bean) {
        CommonHttpUtil.getConfig(new HttpCallback() {

            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (obj.getIntValue("letter_switch") == 1) {
                        if (bean != null) {
                            ImMessageUtil.getInstance().markConversationAsRead(bean.getId());
                            if (mActionListener != null) {
                                mActionListener.onItemClick(bean);
                            }
                        }
                    } else {
                        ToastUtil.show(R.string.private_msg_close_tip);
                    }
                }
            }
        });

    }

    @Override
    public void onItemDelete(ImConUserBean bean, int size) {
        ImMessageUtil.getInstance().removeConversation(bean.getId());
    }

    public interface ActionListener {
        void onCloseClick();

        void onItemClick(ImConUserBean bean);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (e != null) {
            if (mAdapter != null) {
                mAdapter.setFollow(e.getToUid(), e.getIsAttention());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSystemMsgEvent(SystemMsgEvent e) {
        getSystemMessageList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImUserMsgEvent(final ImConversationEvent e) {
        String from = e.getFrom();
        if (Constants.MALL_GOODS_ORDER.equals(from) && CommonAppConfig.getInstance().isTeenagerType()) {
            return;
        }
        if (Constants.IM_MSG_CONCAT.equals(from)
                || Constants.IM_MSG_LIKE.equals(from)
                || Constants.IM_MSG_AT.equals(from)
                || Constants.IM_MSG_COMMENT.equals(from)) {
            return;
        }
        if (CommonAppConfig.getInstance().isPrivateMsgSwitchOpen() && mRecyclerView != null && mAdapter != null) {
            int position = mAdapter.getPosition(from);
            if (position < 0) {
                String requsetTag = "getImUserInfo_" + from;
                ImHttpUtil.cancel(requsetTag);
                ImHttpUtil.getImUserInfo(from, requsetTag, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            ImConUserBean bean = JSON.parseObject(info[0], ImConUserBean.class);
                            bean.setConBean(e.getConBean());
                            mAdapter.insertItem(bean);
                        }
                    }
                });
            } else {
                mAdapter.updateItem(e.getConBean(), position);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImUnReadCountEvent(ImUnReadCountEvent e) {
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        if (unReadCount != null) {
            showUnReadCount(mRedPointConcat, unReadCount.getConcatUnReadCount());
            showUnReadCount(mRedPointLike, unReadCount.getLikeUnReadCount());
            showUnReadCount(mRedPointAt, unReadCount.getAtUnReadCount());
            showUnReadCount(mRedPointComment, unReadCount.getCommentUnReadCount());
        }
    }

    /**
     * 忽略未读
     */
    private void ignoreUnReadCount() {
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        String totalUnReadCount =
                unReadCount != null ? unReadCount.getTotalUnReadCount(false) : "0";
        boolean hasSystemMsg = SpUtil.getInstance().getIntValue(SpUtil.SYSTEM_MSG_COUNT, 0) > 0;
        if ("0".equals(totalUnReadCount) && !hasSystemMsg) {
            DialogUitl.showSimpleTipDialog(mContext, WordUtil.getString(R.string.im_msg_ignore_unread_3), true);
            return;
        }
        SpUtil.getInstance().setIntValue(SpUtil.SYSTEM_MSG_COUNT, 0);
        if (mSystemMsgRedPoint != null && mSystemMsgRedPoint.getVisibility() == View.VISIBLE) {
            mSystemMsgRedPoint.setVisibility(View.INVISIBLE);
        }
        ImMessageUtil.getInstance().markAllConversationAsRead();
        if (mAdapter != null) {
            mAdapter.resetAllUnReadCount();
        }
        if ("0".equals(totalUnReadCount)) {
            EventBus.getDefault().post(new ImUnReadCountEvent());
        }
        ToastUtil.show(R.string.im_msg_ignore_unread_2);
    }

    /**
     * 获取系统消息
     */
    private void getSystemMessageList() {
        ImHttpUtil.getSystemMessageList(1, mSystemMsgCallback);
    }


    /**
     * 撤回消息的回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImMessageRevokeEvent(ImMessageRevokeEvent e) {
        if (mAdapter != null) {
            mAdapter.onRevokeMessage(e.getMsgId(), e.getToUid(), e.isSelf());
        }
    }


}
