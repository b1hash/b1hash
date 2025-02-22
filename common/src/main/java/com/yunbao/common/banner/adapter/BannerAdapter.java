package com.yunbao.common.banner.adapter;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.banner.config.BannerConfig;
import com.yunbao.common.banner.util.BannerUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class BannerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Context mContext;
    protected List<T> mList;
    protected LayoutInflater mInflater;

    private int mIncreaseCount = BannerConfig.INCREASE_COUNT;


    public BannerAdapter(Context context) {
        this(context, new ArrayList<T>());
    }

    public BannerAdapter(Context context, List<T> list) {
        mList = list;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }



    /**
     * 设置实体集合
     * @param list
     */
    public void refreshData(List<T> list) {
        if (mList != null && list != null) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    /**
     * 获取指定的实体（可以在自己的adapter自定义，不一定非要使用）
     *
     * @param position 真实的position
     * @return
     */
    public T getData(int position) {
        return mList.get(position);
    }

    /**
     * 获取指定的实体（可以在自己的adapter自定义，不一定非要使用）
     *
     * @param position 这里传的position不是真实的，获取时转换了一次
     * @return
     */
    public T getRealData(int position) {
        return mList.get(getRealPosition(position));
    }


    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
         int real = getRealPosition(position);
         T data = mList.get(real);
        onBindView(holder, data, real, getRealCount());
    }

    /**
     * 绑定布局数据
     *
     * @param holder   XViewHolder
     * @param data     数据实体
     * @param position 当前位置
     * @param size     总数
     */
    public abstract void onBindView(RecyclerView.ViewHolder holder, T data, int position, int size);


    @Override
    public int getItemCount() {
        return getRealCount() > 1 ? getRealCount() + mIncreaseCount : getRealCount();
    }

    public int getRealCount() {
        return mList == null ? 0 : mList.size();
    }

    public int getRealPosition(int position) {
        return BannerUtils.getRealPosition(mIncreaseCount == BannerConfig.INCREASE_COUNT, position, getRealCount());
    }

    public void setIncreaseCount(int increaseCount) {
        this.mIncreaseCount = increaseCount;
    }
}