package com.yunbao.live.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.live.R;
import com.yunbao.live.bean.LuckPanBean;

/**
 * Created by cxf on 2019/8/27.
 */

public class LuckPanRecordAdapter extends RefreshAdapter<LuckPanBean> {

    private boolean mIsDialog;

    public LuckPanRecordAdapter(Context context, boolean isDialog) {
        super(context);
        mIsDialog = isDialog;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(mIsDialog ? R.layout.item_luck_pan_record_2 : R.layout.item_luck_pan_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }


    class Vh extends RecyclerView.ViewHolder {

        TextView mNum;
        TextView mTime;
        ImageView mIcon;
        TextView mCount;

        public Vh(View itemView) {
            super(itemView);
            if (mIsDialog) {
                mNum = itemView.findViewById(R.id.num);
            }
            mTime = itemView.findViewById(R.id.time);
            mIcon = itemView.findViewById(R.id.icon);
            mCount = itemView.findViewById(R.id.count);
        }

        void setData(LuckPanBean bean, int postion) {
            if (mIsDialog) {
                mNum.setText(String.valueOf(postion + 1));
            }
            mTime.setText(bean.getAddtime());
            mCount.setText("x" + bean.getNums());
            ImgLoader.display(mContext, bean.getThumb(), mIcon);
        }

    }
}
