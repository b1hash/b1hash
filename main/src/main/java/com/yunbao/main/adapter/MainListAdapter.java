package com.yunbao.main.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.MyRadioButton;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.bean.ListBean;

import java.util.List;

/**
 * Created by cxf on 2018/9/27.
 */

public class MainListAdapter extends RefreshAdapter<ListBean> {

    private final String mCoinName;
    private final String mFollow;
    private final String mFollowing;
    private final View.OnClickListener mFollowClickListener;
    private final View.OnClickListener mItemClickListener;

    public MainListAdapter(Context context, String coinName) {
        super(context);
        mCoinName = coinName;
        mFollow = WordUtil.getString(R.string.follow);
        mFollowing = WordUtil.getString(R.string.following);
        mItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                ListBean bean = (ListBean) v.getTag();
                if (bean != null) {
                    RouteUtil.forwardUserHome(mContext, bean.getUid());
                }
            }
        };
        mFollowClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((AbsActivity) mContext).checkLogin()) {
                    return;
                }
                if (!canClick()) {
                    return;
                }
                Object tag = v.getTag();
                if (tag != null) {
                    final int position = (int) tag;
                    final ListBean bean = mList.get(position);
                    CommonHttpUtil.setAttention(bean.getUid(), null);
                }
            }
        };

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_main_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }


    class Vh extends RecyclerView.ViewHolder {

        TextView mOrder;
        ImageView mAvatar;
        TextView mName;
        TextView mVotes;
        MyRadioButton mBtnFollow;

        public Vh(View itemView) {
            super(itemView);
            mOrder = (TextView) itemView.findViewById(R.id.order);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mVotes = (TextView) itemView.findViewById(R.id.votes);
            mBtnFollow = (MyRadioButton) itemView.findViewById(R.id.btn_follow);
            mBtnFollow.setOnClickListener(mFollowClickListener);
            itemView.setOnClickListener(mItemClickListener);
        }

        void setData(ListBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(bean);
                mOrder.setText(StringUtil.contact("NO.", String.valueOf(position + 4)));
                ImgLoader.display(mContext, bean.getAvatarThumb(), mAvatar);
                mName.setText(bean.getUserNiceName());
                mVotes.setText(StringUtil.contact(bean.getTotalCoinFormat(), " ", mCoinName));
            }
            mBtnFollow.setTag(position);
            if (bean.getAttention() == 1) {
                mBtnFollow.doChecked(true);
                mBtnFollow.setText(mFollowing);
            } else {
                mBtnFollow.doChecked(false);
                mBtnFollow.setText(mFollow);
            }
        }
    }


    public void updateItem(String touid, int attention) {
        for (int i = 0, size = mList.size(); i < size; i++) {
            ListBean bean = mList.get(i);
            if (bean != null && touid.equals(bean.getUid())) {
                bean.setAttention(attention);
                notifyItemChanged(i, Constants.PAYLOAD);
                break;
            }
        }
    }

}
