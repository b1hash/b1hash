package com.yunbao.live.livegame.luckpan.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.live.R;
import com.yunbao.live.livegame.luckpan.bean.LuckpanRecordBean;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class LuckpanRecordAdapter extends RefreshAdapter<LuckpanRecordBean> {
    public LuckpanRecordAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.game_luckpan_record_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }

    private class Vh extends RecyclerView.ViewHolder {

        TextView mTitle;
        TextView mTime;
        RecyclerView mRecyclerView;
        LuckpanRecordGiftAdapter mAdapter;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mTime = itemView.findViewById(R.id.time);
            mRecyclerView = itemView.findViewById(R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            mAdapter = new LuckpanRecordGiftAdapter(mContext);
            mRecyclerView.setAdapter(mAdapter);
        }

        void setData(LuckpanRecordBean bean) {
            mTitle.setText(bean.getTitle());
            mTime.setText(bean.getAddTime());
            mAdapter.refreshData(bean.getList());
        }
    }
}
