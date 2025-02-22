package com.yunbao.main.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.main.R;
import com.yunbao.video.bean.VideoBean;

import java.util.List;

/**
 * Created by cxf on 2018/9/26.
 */

public class MainHomeVideoAdapter extends RefreshAdapter<VideoBean> {


    private View.OnClickListener mOnClickListener;
    private Drawable mDrawableMall;
    private Drawable mDrawablePay;

    public MainHomeVideoAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mList.get(position), position);
                    }
                }
            }
        };
        mDrawableMall = ContextCompat.getDrawable(context, R.drawable.icon_main_video_mall);
        mDrawablePay = ContextCompat.getDrawable(context, R.drawable.icon_main_video_pay);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_main_home_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }

    /**
     * 删除视频
     */
    public void deleteVideo(String videoId) {
        if (TextUtils.isEmpty(videoId)) {
            return;
        }
        notifyDataSetChanged();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mCover;
        ImageView mAvatar;
        TextView mName;
        TextView mTitle;
//        TextView mNum;
        ImageView mTypeImg;
        View mAd;

        public Vh(View itemView) {
            super(itemView);
            mCover = (ImageView) itemView.findViewById(R.id.cover);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mTitle = (TextView) itemView.findViewById(R.id.title);
//            mNum = (TextView) itemView.findViewById(R.id.num);
            mTypeImg = (ImageView) itemView.findViewById(R.id.type_img);
            mAd = itemView.findViewById(R.id.ad);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(VideoBean bean, int position, Object payload) {
            itemView.setTag(position);
            ImgLoader.display(mContext, bean.getThumb(), mCover);
            mTitle.setText(bean.getTitle());
//            mNum.setText(bean.getViewNum());
            UserBean userBean = bean.getUserBean();
            if (userBean != null) {
                ImgLoader.display(mContext, userBean.getAvatar(), mAvatar);
                mName.setText(userBean.getUserNiceName());
            }
            int type = bean.getType();
            if (type == 1) {
                mTypeImg.setImageDrawable(mDrawableMall);
            } else if (type == 2) {
                mTypeImg.setImageDrawable(mDrawablePay);
            } else {
                mTypeImg.setImageDrawable(null);
            }
            if (bean.getIsAd() == 1) {
                if (mAd.getVisibility() != View.VISIBLE) {
                    mAd.setVisibility(View.VISIBLE);
                }
            } else {
                if (mAd.getVisibility() != View.INVISIBLE) {
                    mAd.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


}
