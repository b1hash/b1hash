package com.yunbao.main.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.bean.UserItemBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.main.R;

import java.util.List;

/**
 * Created by cxf on 2018/9/28.
 */

public class MainMeAdapter2 extends RecyclerView.Adapter<MainMeAdapter2.Vh> {


    private Context mContext;
    private List<UserItemBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<UserItemBean> mOnItemClickListener;

    public MainMeAdapter2(Context context, List<UserItemBean> list) {
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    UserItemBean bean = (UserItemBean) tag;
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(bean, 0);
                    }
                }
            }
        };
    }

    public void setOnItemClickListener(OnItemClickListener<UserItemBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    public void setList(List<UserItemBean> list) {
        if (list == null) {
            return;
        }
        boolean changed = false;
        if (mList.size() != list.size()) {
            changed = true;
        } else {
            for (int i = 0, size = mList.size(); i < size; i++) {
                if (!mList.get(i).isEqual(list.get(i))) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            mList = list;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_main_me_2, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
        vh.setData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;

        public Vh(View itemView) {
            super(itemView);
            mThumb = (ImageView) itemView.findViewById(R.id.thumb);
            mName = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(UserItemBean bean) {
            itemView.setTag(bean);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
            mName.setText(bean.getName());
        }
    }
}
