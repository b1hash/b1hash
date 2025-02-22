package com.yunbao.live.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.live.R;
import com.yunbao.live.bean.LiveVoiceGiftBean;

import java.util.List;

public class LiveVoiceGiftAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<LiveVoiceGiftBean> mList;
    private Drawable mBgDrawable;
    private Drawable mBgDrawableQm1;
    private Drawable mBgDrawableQm0;
    private int mCheckedColor;
    private int mUnCheckedColor;
    private View.OnClickListener mOnClickListener;

    public LiveVoiceGiftAdapter(Context context, List<LiveVoiceGiftBean> list) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        for (LiveVoiceGiftBean bean : list) {
            int type = bean.getType();
            if (type == -2) {//全麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.全麦);
            } else if (type == -1) {//主持
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.主持);
            } else if (type == 0) {//一麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.一麦);
            } else if (type == 1) {//二麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.二麦);
            } else if (type == 2) {//三麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.三麦);
            } else if (type == 3) {//四麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.四麦);
            } else if (type == 4) {//五麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.五麦);
            } else if (type == 5) {//六麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.六麦);
            } else if (type == 6) {//七麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.七麦);
            } else if (type == 7) {//八麦
                bean.setDrawable(context, R.drawable.bg_live_voice_gift_1, R.drawable.bg_live_voice_gift_0);
                bean.setTextRes(R.string.八麦);
            }
        }
        mList = list;
        mBgDrawable = ContextCompat.getDrawable(context, R.drawable.bg_live_voice_gift);
        mBgDrawableQm1 = ContextCompat.getDrawable(context, R.mipmap.ic_live_voice_gift_qm_1);
        mBgDrawableQm0 = ContextCompat.getDrawable(context, R.mipmap.ic_live_voice_gift_qm_0);
        mCheckedColor = ContextCompat.getColor(context, R.color.white);
        mUnCheckedColor = ContextCompat.getColor(context, R.color.textColor);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                LiveVoiceGiftBean bean = mList.get(position);
                bean.setChecked(!bean.isChecked());
                if (bean.getType() == -2) {//全麦
                    for (int i = 1, size = mList.size(); i < size; i++) {
                        LiveVoiceGiftBean bean1 = mList.get(i);
                        bean1.setChecked(bean.isChecked());
                    }
                    notifyDataSetChanged();
                } else {
                    notifyItemChanged(position, Constants.PAYLOAD);
                    boolean allChecked = true;
                    for (int i = 1, size = mList.size(); i < size; i++) {
                        if (!mList.get(i).isChecked()) {
                            allChecked = false;
                            break;
                        }
                    }
                    LiveVoiceGiftBean allBean = mList.get(0);
                    if (allBean.isChecked() != allChecked) {
                        allBean.setChecked(allChecked);
                        notifyItemChanged(0, Constants.PAYLOAD);
                    }
                }
            }
        };
    }

    public Object[] getCheckedUids() {
        StringBuilder sb = null;
        String toUids = "";
        int count = 0;
        for (int i = 1, size = mList.size(); i < size; i++) {
            LiveVoiceGiftBean bean = mList.get(i);
            if (bean.isChecked()) {
                String uid = bean.getUid();
                if (!TextUtils.isEmpty(uid)) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append(uid);
                    sb.append(",");
                    count++;
                }
            }
        }
        if (sb != null) {
            String s = sb.toString();
            toUids = s.substring(0, s.length() - 1);
        }
        return new Object[]{toUids, count};
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_live_voice_gift, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(i), i, payload);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mBg;
        ImageView mAvatar;
        TextView mImgPosition;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mBg = itemView.findViewById(R.id.bg);
            mAvatar = itemView.findViewById(R.id.avatar);
            mImgPosition = itemView.findViewById(R.id.img_position);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveVoiceGiftBean bean, int position, Object payload) {
            itemView.setTag(position);
            if (payload == null) {
                if (bean.getType() == -2) {
                    mAvatar.setImageDrawable(null);
                } else {
                    ImgLoader.displayAvatar(mContext, bean.getAvatar(), mAvatar);
                }
                mImgPosition.setText(bean.getTextRes());
            }
            if (bean.isChecked()) {
                if (bean.getType() == -2) {
                    mBg.setImageDrawable(mBgDrawableQm1);
                } else {
                    mBg.setImageDrawable(mBgDrawable);
                }
                mImgPosition.setBackground(bean.getCheckedDrawable());
                mImgPosition.setTextColor(mCheckedColor);
            } else {
                if (bean.getType() == -2) {
                    mBg.setImageDrawable(mBgDrawableQm0);
                } else {
                    mBg.setImageDrawable(null);
                }
                mImgPosition.setBackground(bean.getUnCheckedDrawable());
                mImgPosition.setTextColor(mUnCheckedColor);
            }
        }
    }


}
