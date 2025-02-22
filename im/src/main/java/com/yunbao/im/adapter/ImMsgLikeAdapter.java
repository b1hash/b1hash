package com.yunbao.im.adapter;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.im.R;
import com.yunbao.im.bean.VideoImMsgBean;
import com.yunbao.video.activity.VideoPlayActivity;
import com.yunbao.video.bean.VideoBean;
import com.yunbao.video.http.VideoHttpUtil;

/**
 * Created by 云豹科技 on 2022/1/18.
 */
public class ImMsgLikeAdapter extends RefreshAdapter<VideoImMsgBean> {

    private String mTip;
    private String mTip2;
    private int mColor;
    private View.OnClickListener mAvatarClickListener;
    private View.OnClickListener mOnClickListener;

    public ImMsgLikeAdapter(Context context) {
        super(context);
        mTip = WordUtil.getString(R.string.a_089);
        mTip2 = WordUtil.getString(R.string.a_097);
        mColor = ContextCompat.getColor(context, R.color.textColor);
        mAvatarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag(R.id.avatar);
                if (tag != null) {
                    VideoImMsgBean bean = (VideoImMsgBean) tag;
                    RouteUtil.forwardUserHome(mContext, bean.getFromUid());
                }
            }
        };
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    VideoImMsgBean bean = (VideoImMsgBean) tag;
                    VideoHttpUtil.getVideoInfo(bean.getVideoId(), new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                VideoBean videoBean = JSON.parseObject(info[0], VideoBean.class);
                                VideoPlayActivity.forwardSingle(mContext, videoBean);
                            }
                        }

                        @Override
                        public boolean showLoadingDialog() {
                            return true;
                        }

                        @Override
                        public Dialog createLoadingDialog() {
                            return DialogUitl.loadingDialog(mContext);
                        }
                    });
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_im_msg, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mTitle;
        TextView mAddTime;
        ImageView mAvatar;
        ImageView mThumb;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mAddTime = itemView.findViewById(R.id.add_time);
            mAvatar = itemView.findViewById(R.id.avatar);
            mThumb = itemView.findViewById(R.id.thumb);
            mAvatar.setOnClickListener(mAvatarClickListener);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(VideoImMsgBean bean) {
            mAvatar.setTag(R.id.avatar, bean);
            itemView.setTag(bean);
            String userName = bean.getUserName();
            String content = String.format(bean.getType() == 1 ? mTip : mTip2, userName);
            SpannableString span = new SpannableString(content);
            span.setSpan(new ForegroundColorSpan(mColor), 0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTitle.setText(span);
            mAddTime.setText(bean.getAddTime());
            ImgLoader.displayAvatar(mContext, bean.getAvatar(), mAvatar);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
        }
    }
}
