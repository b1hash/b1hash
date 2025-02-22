package com.yunbao.main.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.main.R;
import com.yunbao.main.bean.MainStartDialogBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/11/15.
 */
public class MainStartDialogAdapter extends RefreshAdapter<MainStartDialogBean> {

    private View.OnClickListener mOnClickListener;

    public MainStartDialogAdapter(Context context, List<MainStartDialogBean> list) {
        super(context, list);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!canClick()){
                    return;
                }
                MainStartDialogBean bean = (MainStartDialogBean) v.getTag();
                if (bean != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(bean, 0);
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_dialog_main_start, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position));
    }

    private class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        TextView mText;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.icon);
            mText = itemView.findViewById(R.id.text);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(MainStartDialogBean bean) {
            itemView.setTag(bean);
            mIcon.setImageResource(bean.getIconRes());
            mText.setText(bean.getTextRes());
        }
    }
}
