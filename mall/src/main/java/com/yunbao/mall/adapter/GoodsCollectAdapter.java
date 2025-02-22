package com.yunbao.mall.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.GoodsBean;
import com.yunbao.common.custom.ItemSlideHelper;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.mall.R;
import com.yunbao.mall.activity.GoodsCollectActivity;
import com.yunbao.mall.activity.GoodsDetailActivity;
import com.yunbao.mall.http.MallHttpUtil;

import java.util.List;

public class GoodsCollectAdapter extends RefreshAdapter<GoodsBean> implements ItemSlideHelper.Callback{

    private String mMoneySymbol;
    private View.OnClickListener mOnClickListener;
    private View.OnClickListener mDeleteClickListener;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;
    private boolean mShowCheck;

    public GoodsCollectAdapter(Context context) {
        super(context);
        mMoneySymbol = WordUtil.getString(R.string.money_symbol);
        mCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_check_1);
        mUnCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_check_0);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                GoodsBean bean = mList.get(position);
                if (mShowCheck) {
                    bean.setAdded(!bean.isAdded());
                    notifyItemChanged(position, Constants.PAYLOAD);
                    if (mContext != null) {
                        ((GoodsCollectActivity) mContext).setCanDelete(hasChecked());
                    }
                } else {
                    GoodsDetailActivity.forward(mContext, bean.getId(), bean.getType());
                }

            }
        };
        mDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = (int) v.getTag();
                final GoodsBean bean = mList.get(position);
                new DialogUitl.Builder(mContext)
                        .setContent(WordUtil.getString(R.string.a_083))
                        .setCancelable(true)
                        .setIsHideTitle(true)
                        .setBackgroundDimEnabled(true)
                        .setClickCallback(new DialogUitl.SimpleCallback2() {

                            @Override
                            public void onCancelClick() {
                                setShowCheck(false);
                                ((GoodsCollectActivity) mContext).setCanDelete(false);
                            }

                            @Override
                            public void onConfirmClick(Dialog dialog, String content) {
                                MallHttpUtil.deleteGoodsCollect(bean.getId(), new HttpCallback() {
                                    @Override
                                    public void onSuccess(int code, String msg, String[] info) {
                                        if (code == 0) {
                                            mList.remove(position);
                                            notifyDataSetChanged();
                                            if (mContext != null) {
                                                ((GoodsCollectActivity) mContext).setCanDelete(hasChecked());
                                            }
                                        }
                                        ToastUtil.show(msg);
                                    }
                                });
                            }
                        })
                        .build()
                        .show();

            }
        };
    }


    public void setShowCheck(boolean showCheck) {
        if (mShowCheck == showCheck) {
            return;
        }
        mShowCheck = showCheck;
        for (GoodsBean bean : mList) {
            bean.setAdded(false);
        }
        notifyDataSetChanged();
    }

    private boolean hasChecked() {
        for (GoodsBean bean : mList) {
            if (bean.isAdded()) {
                return true;
            }
        }
        return false;
    }

    public String getCheckedId() {
        StringBuilder sb = null;
        for (GoodsBean bean : mList) {
            if (bean.isAdded()) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(bean.getId());
                sb.append(",");
            }
        }
        if (sb != null) {
            String s = sb.toString();
            if (s.length() > 1) {
                s = s.substring(0, s.length() - 1);
            }
            return s;
        }
        return null;
    }

    public void toggleShowCheck() {
        mShowCheck = !mShowCheck;
        for (GoodsBean bean : mList) {
            bean.setAdded(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public void refreshData(List<GoodsBean> list) {
        mShowCheck = false;
        super.refreshData(list);
        if (mContext != null) {
            ((GoodsCollectActivity) mContext).setCanDelete(hasChecked());
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_goods_collect, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(i), i, payload);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView.addOnItemTouchListener(new ItemSlideHelper(mContext, this));
    }

    @Override
    public int getHorizontalRange(RecyclerView.ViewHolder vh) {
        return mShowCheck?0: DpUtil.dp2px(60);
    }

    @Override
    public RecyclerView.ViewHolder getChildViewHolder(View childView) {
        if (mRecyclerView != null && childView != null) {
            return mRecyclerView.getChildViewHolder(childView);
        }
        return null;
    }

    @Override
    public View findTargetView(float x, float y) {
        return mRecyclerView.findChildViewUnder(x, y);
    }

    @Override
    public boolean useLeftScroll() {
        return !mShowCheck;
    }

    @Override
    public void onLeftScroll(RecyclerView.ViewHolder holder) {

    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;
        TextView mPriceNow;
        TextView mPriceOrigin;
        View mBtnDelete;
        ImageView mImgCheck;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            mPriceNow = itemView.findViewById(R.id.price_now);
            mPriceOrigin = itemView.findViewById(R.id.price_origin);
            mPriceOrigin.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            itemView.setOnClickListener(mOnClickListener);
            mBtnDelete = itemView.findViewById(R.id.btn_delete);
            mBtnDelete.setOnClickListener(mDeleteClickListener);
            mImgCheck = itemView.findViewById(R.id.img_check);
        }

        void setData(GoodsBean bean ,int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                ImgLoader.display(mContext, bean.getThumb(), mThumb);
                mName.setText(bean.getName());
                mPriceNow.setText(bean.getPriceNow());
                if (bean.getType() == 1) {
                    mPriceOrigin.setText(StringUtil.contact(mMoneySymbol, bean.getOriginPrice()));
                } else {
                    mPriceOrigin.setText(null);
                }
                mBtnDelete.setTag(position);
                if (mShowCheck) {
                    if (mImgCheck.getVisibility() != View.VISIBLE) {
                        mImgCheck.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mImgCheck.getVisibility() != View.GONE) {
                        mImgCheck.setVisibility(View.GONE);
                    }
                }
            }
            mImgCheck.setImageDrawable(bean.isAdded() ? mCheckedDrawable : mUnCheckedDrawable);

        }
    }
}
