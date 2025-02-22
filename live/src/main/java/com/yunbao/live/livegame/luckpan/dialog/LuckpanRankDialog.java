package com.yunbao.live.livegame.luckpan.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.live.R;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.livegame.luckpan.adapter.LuckpanRankAdapter;
import com.yunbao.live.livegame.luckpan.bean.LuckRankBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/2/24.
 * 幸运转盘 排行榜
 */
public class LuckpanRankDialog extends AbsDialogFragment implements View.OnClickListener {

    private LuckpanRankAdapter mAdapter;
    private View mSelfGroup;
    private TextView mSelfRank;
    private TextView mSelfName;
    private ImageView mSelfAvatar;
    private TextView mSelfCoin;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game_luckpan_rank;
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
        mSelfGroup = findViewById(R.id.group_self);
        mSelfRank = findViewById(R.id.self_rank);
        mSelfName = findViewById(R.id.self_name);
        mSelfAvatar = findViewById(R.id.self_avatar);
        mSelfCoin = findViewById(R.id.self_coin);
        CommonRefreshView refreshView = findViewById(R.id.refreshView);
        refreshView.setEmptyLayoutId(R.layout.game_star_rank_empty);
        refreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        refreshView.setDataHelper(new CommonRefreshView.DataHelper<LuckRankBean>() {
            @Override
            public RefreshAdapter<LuckRankBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LuckpanRankAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.gameLuckpanRank(callback);
            }

            @Override
            public List<LuckRankBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                JSONObject current = obj.getJSONObject("current");
                if (mSelfRank != null) {
                    mSelfRank.setText(current.getString("num"));
                }
                if (mSelfName != null) {
                    mSelfName.setText(current.getString("user_nickname"));
                }
                if (mSelfAvatar != null) {
                    ImgLoader.display(mContext, current.getString("avatar"), mSelfAvatar);
                }
                if (mSelfCoin != null) {
                    mSelfCoin.setText(current.getString("total"));
                }
                return JSON.parseArray(obj.getString("list"), LuckRankBean.class);
            }

            @Override
            public void onRefreshSuccess(List<LuckRankBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LuckRankBean> loadItemList, int loadItemCount) {

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
        LiveHttpUtil.cancel(LiveHttpConsts.GAME_LUCKPAN_RANK);
        super.onDestroy();
    }
}
