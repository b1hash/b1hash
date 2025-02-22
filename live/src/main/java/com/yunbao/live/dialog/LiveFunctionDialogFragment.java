package com.yunbao.live.dialog;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.yunbao.common.Constants;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.im.event.ImUnReadCountEvent;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.im.utils.ImUnReadCount;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.adapter.LiveFunctionAdapter;
import com.yunbao.live.interfaces.LiveFunctionClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by cxf on 2018/10/9.
 */

public class LiveFunctionDialogFragment extends AbsDialogFragment implements OnItemClickListener<Integer> {

    private LiveFunctionClickListener mFunctionClickListener;
    private boolean mHasMsg;
    private LiveFunctionAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_function;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
//        params.y = DpUtil.dp2px(50);
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean hasGame = false;
        boolean openFlash = false;
        boolean taskSwitch = false;
        boolean luckPanSwitch = false;
        boolean hasFace = false;
        boolean screenRecord = false;
        boolean isLinkMic = false;
        Bundle bundle = getArguments();
        if (bundle != null) {
            hasGame = bundle.getBoolean(Constants.HAS_GAME, false);
            openFlash = bundle.getBoolean(Constants.OPEN_FLASH, false);
            taskSwitch = bundle.getBoolean("TASK", false);
            luckPanSwitch = bundle.getBoolean("LUCK_PAN", false);
            hasFace = bundle.getBoolean("HAS_FACE", false);
            mHasMsg = bundle.getBoolean("HAS_MSG", false);
            screenRecord = bundle.getBoolean("screenRecord", false);
            isLinkMic = bundle.getBoolean(Constants.LINK_MIC, false);
        }
        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 5, GridLayoutManager.VERTICAL, false));
        LiveFunctionAdapter adapter = new LiveFunctionAdapter(mContext, hasGame, openFlash, taskSwitch, luckPanSwitch, hasFace, screenRecord, isLinkMic);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        mAdapter = adapter;
        if (mHasMsg) {
            EventBus.getDefault().register(this);
        }
    }

    public void setFunctionClickListener(LiveFunctionClickListener functionClickListener) {
        mFunctionClickListener = functionClickListener;
    }

    @Override
    public void onItemClick(Integer bean, int position) {
        dismiss();
        if (mFunctionClickListener != null) {
            mFunctionClickListener.onClick(bean);
        }
    }

    @Override
    public void onDestroy() {
        if (mHasMsg) {
            EventBus.getDefault().unregister(this);
        }
        mFunctionClickListener = null;
//        ((LiveActivity) mContext).setBtnFunctionDark();
        super.onDestroy();
    }


    /**
     * 监听私信未读消息数变化事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImUnReadCountEvent(ImUnReadCountEvent e) {
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        if (unReadCount != null) {
            String count = unReadCount.getLiveRoomUnReadCount();
            if (mAdapter != null) {
                mAdapter.updateImUnReadCount(count);
            }
        }
    }

}
