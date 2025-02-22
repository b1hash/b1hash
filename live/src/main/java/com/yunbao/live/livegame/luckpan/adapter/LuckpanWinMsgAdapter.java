package com.yunbao.live.livegame.luckpan.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.yunbao.common.banner.adapter.BannerAdapter;
import com.yunbao.live.R;
import com.yunbao.live.livegame.luckpan.bean.LuckpanWinMsgBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/2/27.
 */
public class LuckpanWinMsgAdapter extends BannerAdapter<LuckpanWinMsgBean> {

    public LuckpanWinMsgAdapter(Context context, List<LuckpanWinMsgBean> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.game_luckpan_win_msg, viewGroup, false));
    }

    @Override
    public void onBindView(RecyclerView.ViewHolder vh, LuckpanWinMsgBean data, int position, int size) {
        ((Vh) vh).setData(data);
    }

    private class Vh extends RecyclerView.ViewHolder {

        TextView mTitle;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
        }

        void setData(LuckpanWinMsgBean bean) {
            mTitle.setText(bean.getTitleTrans());
        }
    }
}
