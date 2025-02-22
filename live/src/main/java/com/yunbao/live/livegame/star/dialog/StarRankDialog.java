package com.yunbao.live.livegame.star.dialog;

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
import com.yunbao.live.livegame.star.adapter.StarRankAdapter;
import com.yunbao.live.livegame.star.bean.StarRankBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/2/24.
 * 星球探宝 排行榜
 */
public class StarRankDialog extends AbsDialogFragment implements View.OnClickListener {

    private StarRankAdapter mAdapter;
    private View mSelfGroup;
    private TextView mSelfRank;
    private TextView mSelfName;
    private ImageView mSelfAvatar;
    private TextView mSelfCoin;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game_star_rank;
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
        refreshView.setDataHelper(new CommonRefreshView.DataHelper<StarRankBean>() {
            @Override
            public RefreshAdapter<StarRankBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new StarRankAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.gameXqtbRank(callback);
            }

            @Override
            public List<StarRankBean> processData(String[] info) {
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
                return JSON.parseArray(obj.getString("list"), StarRankBean.class);
            }

            @Override
            public void onRefreshSuccess(List<StarRankBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<StarRankBean> loadItemList, int loadItemCount) {

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
        LiveHttpUtil.cancel(LiveHttpConsts.GAME_XQTB_RANK);
        super.onDestroy();
    }
}
