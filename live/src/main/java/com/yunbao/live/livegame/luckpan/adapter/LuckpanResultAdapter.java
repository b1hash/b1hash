package com.yunbao.live.livegame.luckpan.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.live.R;
import com.yunbao.live.livegame.luckpan.bean.LuckpanResultBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class LuckpanResultAdapter extends RefreshAdapter<LuckpanResultBean> {
    public LuckpanResultAdapter(Context context, List<LuckpanResultBean> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.game_luckpan_result, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }

    private class Vh extends RecyclerView.ViewHolder {

        ImageView mGiftIcon;
        TextView mCount;
        TextView mGiftName;
        TextView mTotal;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mGiftIcon = itemView.findViewById(R.id.gift_icon);
            mCount = itemView.findViewById(R.id.count);
            mGiftName = itemView.findViewById(R.id.gift_name);
            mTotal = itemView.findViewById(R.id.total);
        }

        void setData(LuckpanResultBean bean) {
            ImgLoader.display(mContext, bean.getGiftIcon(), mGiftIcon);
            mCount.setText(bean.getCount());
            mGiftName.setText(bean.getGiftNameTrans());
            mTotal.setText(bean.getTotalCoin());
        }
    }
}
