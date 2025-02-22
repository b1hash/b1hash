package com.yunbao.main.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.LevelBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.CommonIconUtil;
import com.yunbao.main.R;
import com.yunbao.main.bean.GoodsShareUserBean;

import java.util.List;

/**
 * Created by cxf on 2018/9/29.
 */

public class GoodsShareAdapter extends RefreshAdapter<GoodsShareUserBean> {

    private View.OnClickListener mClickListener;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;


    public GoodsShareAdapter(Context context) {
        super(context);
        mCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_check_1);
        mUnCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_check_0);
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                GoodsShareUserBean bean = mList.get(position);
                bean.setChecked(!bean.isChecked());
                notifyItemChanged(position, Constants.PAYLOAD);
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_goods_share, parent, false));
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

        ImageView mImgCheck;
        ImageView mAvatar;
        TextView mName;
        TextView mSign;
        ImageView mSex;
        ImageView mLevelAnchor;
        ImageView mLevel;

        public Vh(View itemView) {
            super(itemView);
            mImgCheck = (ImageView) itemView.findViewById(R.id.img_check);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mSign = (TextView) itemView.findViewById(R.id.sign);
            mSex = (ImageView) itemView.findViewById(R.id.sex);
            mLevelAnchor = (ImageView) itemView.findViewById(R.id.level_anchor);
            mLevel = (ImageView) itemView.findViewById(R.id.level);
            itemView.setOnClickListener(mClickListener);
        }

        void setData(GoodsShareUserBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                ImgLoader.displayAvatar(mContext, bean.getAvatar(), mAvatar);
                mName.setText(bean.getUserNiceName());
                mSign.setText(bean.getSignature());
                mSex.setImageResource(CommonIconUtil.getSexIcon(bean.getSex()));
                LevelBean anchorLevelBean = CommonAppConfig.getInstance().getAnchorLevel(bean.getLevelAnchor());
                if (anchorLevelBean != null) {
                    ImgLoader.display(mContext, anchorLevelBean.getThumb(), mLevelAnchor);
                }
                LevelBean levelBean = CommonAppConfig.getInstance().getLevel(bean.getLevel());
                if (levelBean != null) {
                    ImgLoader.display(mContext, levelBean.getThumb(), mLevel);
                }
            }
            mImgCheck.setImageDrawable(bean.isChecked() ? mCheckedDrawable : mUnCheckedDrawable);
        }

    }
}
