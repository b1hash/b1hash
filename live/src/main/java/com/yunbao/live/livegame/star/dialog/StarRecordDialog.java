package com.yunbao.live.livegame.star.dialog;

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
import com.yunbao.live.livegame.star.adapter.StarRecordAdapter;
import com.yunbao.live.livegame.star.bean.StarRecordBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/2/24.
 * 星球探宝 中奖记录
 */
public class StarRecordDialog extends AbsDialogFragment implements View.OnClickListener {

    private StarRecordAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game_star_record;
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
        refreshView.setEmptyLayoutId(R.layout.game_star_record_empty);
        refreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        refreshView.setDataHelper(new CommonRefreshView.DataHelper<StarRecordBean>() {
            @Override
            public RefreshAdapter<StarRecordBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new StarRecordAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.gameXqtbRecord(p, callback);
            }

            @Override
            public List<StarRecordBean> processData(String[] info) {
                if(info.length>0){
                    String data = JSON.parseObject(info[0]).getString("list");
                    return JSON.parseArray(data, StarRecordBean.class);
                }else{
                    return new ArrayList<>();
                }
            }

            @Override
            public void onRefreshSuccess(List<StarRecordBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<StarRecordBean> loadItemList, int loadItemCount) {

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
        LiveHttpUtil.cancel(LiveHttpConsts.GAME_XQTB_RECORD);
        super.onDestroy();
    }
}
