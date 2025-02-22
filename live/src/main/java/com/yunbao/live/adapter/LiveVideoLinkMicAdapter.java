package com.yunbao.live.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.bean.LiveVoiceLinkMicBean;
import com.yunbao.live.utils.LiveIconUtil;

import java.util.List;

public class LiveVideoLinkMicAdapter extends RefreshAdapter<LiveVoiceLinkMicBean> {

    private Drawable mDrawable0;
    private Drawable mDrawable1;
    private View.OnClickListener mOnClickListener;


    public LiveVideoLinkMicAdapter(Context context, List<LiveVoiceLinkMicBean> list) {
        super(context, list);
        mDrawable0 = ContextCompat.getDrawable(context, R.mipmap.ic_live_voice_4);
        mDrawable1 = ContextCompat.getDrawable(context, R.mipmap.ic_live_voice_5);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveVoiceLinkMicBean bean = (LiveVoiceLinkMicBean) v.getTag();
                if (!bean.isEmpty()) {
                    ((LiveActivity) mContext).showUserDialog(bean.getUid());
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_live_video_link_mic, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), payload);
    }

    class Vh extends RecyclerView.ViewHolder {

        View mViewEmpty;
        TextView mName;
        ImageView mImgMute;
        ImageView mFace;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mViewEmpty = itemView.findViewById(R.id.view_empty);
            mName = itemView.findViewById(R.id.name);
            mImgMute = itemView.findViewById(R.id.img_mute);
            mFace = itemView.findViewById(R.id.face);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveVoiceLinkMicBean bean, Object payload) {
            if (Constants.VOICE_FACE.equals(payload)) {
                if (bean.getFaceIndex() == -1) {
                    mFace.setImageDrawable(null);
                } else {
                    int res = LiveIconUtil.getVoiceRoomFaceRes(bean.getFaceIndex());
                    if (res > 0) {
                        mFace.setImageResource(res);
                    } else {
                        mFace.setImageDrawable(null);
                    }
                }
                return;
            }
            if (bean.isEmpty()) {
                if(payload == null){
                    itemView.setTag(bean);
                    mFace.setImageDrawable(null);
                    if (mViewEmpty.getVisibility() != View.VISIBLE) {
                        mViewEmpty.setVisibility(View.VISIBLE);
                    }
                    if (mImgMute.getVisibility() != View.INVISIBLE) {
                        mImgMute.setVisibility(View.INVISIBLE);
                    }
                    if (mName.getVisibility() != View.INVISIBLE) {
                        mName.setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                if (payload == null) {
                    itemView.setTag(bean);
                    if (mViewEmpty.getVisibility() != View.INVISIBLE) {
                        mViewEmpty.setVisibility(View.INVISIBLE);
                    }
                    if (mName.getVisibility() != View.VISIBLE) {
                        mName.setVisibility(View.VISIBLE);
                    }
                    mName.setText(bean.getUserName());
                    mFace.setImageDrawable(null);
                    if (mImgMute.getVisibility() != View.VISIBLE) {
                        mImgMute.setVisibility(View.VISIBLE);
                    }
                }
                mImgMute.setImageDrawable(bean.getStatus() == Constants.VOICE_CTRL_CLOSE ? mDrawable0 : mDrawable1);
            }
        }
    }
}
