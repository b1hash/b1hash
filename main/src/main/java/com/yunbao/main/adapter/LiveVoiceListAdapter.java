package com.yunbao.main.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.main.R;
import com.yunbao.live.bean.LiveBean;

/**
 * Created by 云豹科技 on 2022/1/21.
 */
public class LiveVoiceListAdapter extends RefreshAdapter<LiveBean> {

    private View.OnClickListener mOnClickListener;

    public LiveVoiceListAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (mOnItemClickListener != null && tag != null) {
                    mOnItemClickListener.onItemClick((LiveBean) tag, 0);
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_live_voice_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }


    private class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        ImageView mAvatar;
        TextView mName;
        TextView mTitle;
        TextView mNum;
        View mRecom;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mAvatar = itemView.findViewById(R.id.avatar);
            mName = itemView.findViewById(R.id.name);
            mTitle = itemView.findViewById(R.id.title);
            mNum = itemView.findViewById(R.id.num);
            mRecom = itemView.findViewById(R.id.icon_recom);
            itemView.setOnClickListener(mOnClickListener);
        }


        void setData(LiveBean liveBean) {
            itemView.setTag(liveBean);
            ImgLoader.display(mContext, liveBean.getThumb(), mThumb);
            ImgLoader.display(mContext, liveBean.getAvatar(), mAvatar);
            mName.setText(liveBean.getUserNiceName());
            mTitle.setText(liveBean.getTitle());
            mNum.setText(liveBean.getNums());
            if (liveBean.getRecommend() == 1) {
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
