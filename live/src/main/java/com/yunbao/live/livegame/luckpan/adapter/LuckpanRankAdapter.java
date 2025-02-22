package com.yunbao.live.livegame.luckpan.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.live.R;
import com.yunbao.live.livegame.luckpan.bean.LuckRankBean;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class LuckpanRankAdapter extends RefreshAdapter<LuckRankBean> {

    private Drawable mRank0;
    private Drawable mRank1;
    private Drawable mRank2;

    public LuckpanRankAdapter(Context context) {
        super(context);
        mRank0 = ContextCompat.getDrawable(context, R.drawable.game_star_rank_top_0);
        mRank1 = ContextCompat.getDrawable(context, R.drawable.game_star_rank_top_1);
        mRank2 = ContextCompat.getDrawable(context, R.drawable.game_star_rank_top_2);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.game_star_rank_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i), i);
    }

    private class Vh extends RecyclerView.ViewHolder {

        TextView mRankTop;
        TextView mRank;
        ImageView mAvatar;
        TextView mName;
        TextView mCoin;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mRankTop = itemView.findViewById(R.id.rank_top);
            mRank = itemView.findViewById(R.id.rank);
            mAvatar = itemView.findViewById(R.id.avatar);
            mName = itemView.findViewById(R.id.name);
            mCoin = itemView.findViewById(R.id.coin);
        }

        void setData(LuckRankBean bean, int position) {
            if (position > 2) {
                if (mRank.getVisibility() != View.VISIBLE) {
                    mRank.setVisibility(View.VISIBLE);
                }
                if (mRankTop.getVisibility() != View.INVISIBLE) {
                    mRankTop.setVisibility(View.INVISIBLE);
                }
                mRank.setText(String.valueOf(position + 1));
            } else {
                if (mRankTop.getVisibility() != View.VISIBLE) {
                    mRankTop.setVisibility(View.VISIBLE);
                }
                if (mRank.getVisibility() != View.INVISIBLE) {
                    mRank.setVisibility(View.INVISIBLE);
                }
                mRankTop.setText(String.valueOf(position + 1));
                if (position == 0) {
                    mRankTop.setBackground(mRank0);
                } else if (position == 1) {
                    mRankTop.setBackground(mRank1);
                } else if (position == 2) {
                    mRankTop.setBackground(mRank2);
                }
            }
            ImgLoader.display(mContext, bean.getAvatar(), mAvatar);
            mName.setText(bean.getUserName());
            mCoin.setText(bean.getCoin());
        }
    }
}
