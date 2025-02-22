package com.yunbao.common.banner.holder;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public interface IViewHolder<T> {

    /**
     * 创建ViewHolder
     *
     * @return XViewHolder
     */
    RecyclerView.ViewHolder onCreateHolder(ViewGroup parent, int viewType);

    /**
     * 绑定布局数据
     *
     * @param holder   XViewHolder
     * @param data     数据实体
     * @param position 当前位置
     * @param size     总数
     */
    void onBindView(RecyclerView.ViewHolder holder, T data, int position, int size);

}
