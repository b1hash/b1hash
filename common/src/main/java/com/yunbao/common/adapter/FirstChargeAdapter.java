package com.yunbao.common.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.R;
import com.yunbao.common.bean.FirstChargeItemBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.StringUtil;

public class FirstChargeAdapter extends RefreshAdapter<FirstChargeItemBean> {

    public FirstChargeAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_first_charge,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh)vh).setData(mList.get(i));
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        TextView mName;
        TextView mCount;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.icon);
            mName = itemView.findViewById(R.id.name);
            mCount = itemView.findViewById(R.id.count);
        }

        void setData(FirstChargeItemBean bean) {
            ImgLoader.display(mContext,bean.getIcon(),mIcon);
            mCount.setText(StringUtil.contact("x", bean.getCount()));
            mName.setText(bean.getName());
        }
    }
}
