package com.yunbao.main.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.Constants;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.main.R;
import com.yunbao.main.adapter.MainStartDialogAdapter;
import com.yunbao.main.bean.MainStartDialogBean;
import com.yunbao.main.interfaces.MainStartChooseCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/29.
 */

public class MainStartDialogFragment extends AbsDialogFragment implements OnItemClickListener<MainStartDialogBean> {

    private MainStartChooseCallback mCallback;
    private JSONObject mStartLiveInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_main_start;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog;
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
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        LiveHttpUtil.getLiveSdk(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mStartLiveInfo = obj;
                    List<MainStartDialogBean> list = new ArrayList<>();
                    list.add(new MainStartDialogBean(R.mipmap.icon_main_start_live, R.string.main_start_live));
                    list.add(new MainStartDialogBean(R.mipmap.icon_main_start_voice, R.string.a_001));
                    list.add(new MainStartDialogBean(R.mipmap.icon_main_start_live_screen, R.string.手游直播));
                    list.add(new MainStartDialogBean(R.mipmap.icon_main_start_video, R.string.main_start_video));
                    MainStartDialogAdapter adapter = new MainStartDialogAdapter(mContext, list);
                    adapter.setOnItemClickListener(MainStartDialogFragment.this);
                    RecyclerView recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }

    public void setMainStartChooseCallback(MainStartChooseCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onItemClick(MainStartDialogBean bean, int position) {
        dismiss();
        int textRes = bean.getTextRes();
        if (textRes == R.string.main_start_live) {
            if (mCallback != null) {
                mCallback.onLiveClick(mStartLiveInfo);
            }
        } else if (textRes == R.string.a_001) {
            if (mCallback != null) {
                mCallback.onVoiceClick(mStartLiveInfo);
            }
        } else if (textRes == R.string.main_start_video) {
            if (mCallback != null) {
                mCallback.onVideoClick();
            }
        } else if (textRes == R.string.手游直播) {
            if (mCallback != null) {
                mCallback.onScreenRecordLive(mStartLiveInfo);
            }
        }
    }

    @Override
    public void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_SDK);
        mCallback = null;
        super.onDestroy();
    }
}
