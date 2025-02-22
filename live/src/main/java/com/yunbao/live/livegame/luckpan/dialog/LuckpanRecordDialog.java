package com.yunbao.live.livegame.luckpan.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.live.R;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.livegame.luckpan.adapter.LuckpanRecordAdapter;
import com.yunbao.live.livegame.luckpan.bean.LuckpanRecordBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/2/24.
 * 幸运转盘 中奖记录
 */
public class LuckpanRecordDialog extends AbsDialogFragment implements View.OnClickListener {

    private LuckpanRecordAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game_luckpan_record;
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
        window.setWindowAnimations(R.style.leftToRightAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_close).setOnClickListener(this);
        CommonRefreshView refreshView = findViewById(R.id.refreshView);
        refreshView.setEmptyLayoutId(R.layout.game_luckpan_record_empty);
        refreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        refreshView.setDataHelper(new CommonRefreshView.DataHelper<LuckpanRecordBean>() {
            @Override
            public RefreshAdapter<LuckpanRecordBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LuckpanRecordAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.gameLuckpanRecord(p, callback);
            }

            @Override
            public List<LuckpanRecordBean> processData(String[] info) {
                if(info.length>0){
                    String data = JSON.parseObject(info[0]).getString("list");
                    return JSON.parseArray(data, LuckpanRecordBean.class);
                }else{
                    return new ArrayList<>();
                }
            }

            @Override
            public void onRefreshSuccess(List<LuckpanRecordBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LuckpanRecordBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        refreshView.initData();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_close) {
            dismiss();
        }
    }

    @Override
    public void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.GAME_LUCKPAN_RECORD);
        super.onDestroy();
    }
}
