package com.yunbao.live.livegame.star.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.banner.adapter.BannerAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.live.R;
import com.yunbao.live.livegame.star.bean.StarWinMsgBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/2/27.
 */
public class StarWinMsgAdapter extends BannerAdapter<StarWinMsgBean> {

    public StarWinMsgAdapter(Context context, List<StarWinMsgBean> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.game_star_win_msg, viewGroup, false));
    }

    @Override
    public void onBindView(RecyclerView.ViewHolder vh, StarWinMsgBean data, int position, int size) {
        ((Vh) vh).setData(data);
    }

    private class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        TextView mTitle;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.icon);
            mTitle = itemView.findViewById(R.id.title);
        }

        void setData(StarWinMsgBean bean) {
            String icon = bean.getGiftIcon();
            if (TextUtils.isEmpty(icon)) {
                mIcon.setImageDrawable(null);
            } else {
                ImgLoader.display(mContext, icon, mIcon);
            }
            mTitle.setText(bean.getTitleTrans());
        }
    }
}
