package com.yunbao.main.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.live.bean.LiveBean;
import com.yunbao.main.R;
import com.yunbao.main.utils.MainIconUtil;

import java.util.List;

/**
 * Created by cxf on 2018/9/26.
 * 首页 直播 推荐
 */

public class MainHomeLiveRecomAdapter extends RefreshAdapter<LiveBean> {

    private View.OnClickListener mOnClickListener;

    public MainHomeLiveRecomAdapter(Context context, List<LiveBean> list) {
        super(context, list);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                int position = (int) v.getTag();
                mOnItemClickListener.onItemClick(mList.get(position), position);
            }
        };
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_main_home_live_recom, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mCover;
        ImageView mAvatar;
        TextView mName;
        TextView mNum;
        View mRecom;

        public Vh(View itemView) {
            super(itemView);
            mCover = (ImageView) itemView.findViewById(R.id.cover);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mNum = (TextView) itemView.findViewById(R.id.num);
            mRecom = itemView.findViewById(R.id.icon_recom);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveBean bean, int position) {
            itemView.setTag(position);
            ImgLoader.display(mContext, bean.getThumb(), mCover);
            ImgLoader.display(mContext, bean.getAvatar(), mAvatar);
            mName.setText(bean.getUserNiceName());
            mNum.setText(bean.getNums());
            if (bean.getRecommend() == 1) {
                if (mRecom.getVisibility() != View.VISIBLE) {
                    mRecom.setVisibility(View.VISIBLE);
                }
            } else {
                if (mRecom.getVisibility() == View.VISIBLE) {
                    mRecom.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

}
