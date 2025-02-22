package com.yunbao.live.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.im.utils.ImUnReadCount;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.activity.LiveAnchorActivity;
import com.yunbao.live.bean.LiveFunctionBean;
import com.yunbao.common.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/9.
 */

public class LiveFunctionAdapter extends RecyclerView.Adapter<LiveFunctionAdapter.Vh> {

    private List<LiveFunctionBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private OnItemClickListener<Integer> mOnItemClickListener;
    private String mImUnReadCount;

    public LiveFunctionAdapter(Context context, boolean hasGame, boolean openFlash, boolean taskSwitch, boolean luckPanSwitch, boolean hasFace, boolean screenRecord, boolean isLinkMic) {
        mList = new ArrayList<>();
        if (((LiveActivity) context).isChatRoom()) {
            if (context instanceof LiveAnchorActivity) {
                //mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MUSIC, R.mipmap.icon_live_func_music, R.string.live_music));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_SHARE, R.mipmap.icon_live_func_share, R.string.live_share));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_RED_PACK, R.mipmap.icon_live_func_rp, R.string.live_red_pack));
                if (taskSwitch) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_TASK, R.mipmap.icon_live_func_task, R.string.daily_task));
                }
//                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LUCK, R.mipmap.icon_live_func_luck, R.string.a_002));

            } else {
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_SHARE, R.mipmap.icon_live_func_share, R.string.live_share));
                if (!CommonAppConfig.getInstance().isTeenagerType()) {
                    if (luckPanSwitch) {
                        mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_PAN, R.mipmap.icon_live_luck_pan, R.string.a_003));
                    }
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_RED_PACK, R.mipmap.icon_live_func_rp, R.string.live_red_pack));
                    if (taskSwitch) {
                        mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_TASK, R.mipmap.icon_live_func_task, R.string.daily_task));
                    }
                }
                if (hasFace) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_FACE, R.mipmap.icon_live_voice_face, R.string.表情));
                }
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MSG, R.mipmap.icon_live_func_msg, R.string.pri_msg));
//                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LUCK, R.mipmap.icon_live_func_luck, R.string.a_002));
            }
        } else {
            if (context instanceof LiveAnchorActivity) {
                if (!screenRecord) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_BEAUTY, R.mipmap.icon_live_func_beauty, R.string.live_beauty));
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_CAMERA, R.mipmap.icon_live_func_camera, R.string.live_camera));
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_FLASH, openFlash ? R.mipmap.icon_live_func_flash : R.mipmap.icon_live_func_flash_1, R.string.live_flash));
                }
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MUSIC, R.mipmap.icon_live_func_music, R.string.live_music));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_SHARE, R.mipmap.icon_live_func_share, R.string.live_share));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LINK_MIC_AUD, isLinkMic ? R.mipmap.icon_live_func_linkmic_anc_1
                        : R.mipmap.icon_live_func_linkmic_anc_0, R.string.用户连麦));

                if (hasGame) {//含有游戏
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_GAME, R.mipmap.icon_live_func_game, R.string.live_game));
                }
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_RED_PACK, R.mipmap.icon_live_func_rp, R.string.live_red_pack));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LINK_MIC, R.mipmap.icon_live_func_lm, R.string.live_link_mic));
                if (!screenRecord) {
                    mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MIRROR, R.mipmap.icon_live_func_jx, R.string.live_mirror));
                }
            } else {
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_MSG, R.mipmap.icon_live_func_msg, R.string.pri_msg));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_SHARE, R.mipmap.icon_live_func_share, R.string.live_share));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_RED_PACK, R.mipmap.icon_live_func_rp, R.string.live_red_pack));
                mList.add(new LiveFunctionBean(Constants.LIVE_FUNC_LINK_MIC_AUD, R.mipmap.icon_live_func_linkmic_aud,
                        isLinkMic ? R.string.live_link_mic_close : R.string.live_link_mic));
            }

        }
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    int functionID = (int) tag;
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(functionID, 0);
                    }
                }
            }
        };
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        mImUnReadCount = unReadCount != null ? unReadCount.getLiveRoomUnReadCount() : "0";
    }

    public void setOnItemClickListener(OnItemClickListener<Integer> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getID();
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constants.LIVE_FUNC_MSG) {
            return new MsgVh(mInflater.inflate(R.layout.item_live_function_msg, parent, false));
        }
        return new Vh(mInflater.inflate(R.layout.item_live_function, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position, @NonNull List<Object> payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        vh.setData(mList.get(position), payload);
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {

    }

    public void updateImUnReadCount(String imUnReadCount) {
        mImUnReadCount = imUnReadCount;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getID() == Constants.LIVE_FUNC_MSG) {
                notifyItemChanged(i, Constants.PAYLOAD);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        TextView mName;

        public Vh(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveFunctionBean bean, Object payload) {
            if (payload == null) {
                itemView.setTag(bean.getID());
                mIcon.setImageResource(bean.getIcon());
                mName.setText(bean.getName());
            }
        }
    }

    class MsgVh extends Vh {

        TextView mRedPoint;

        public MsgVh(View itemView) {
            super(itemView);
            mRedPoint = itemView.findViewById(R.id.red_point);
        }

        void setData(LiveFunctionBean bean, Object payload) {
            super.setData(bean, payload);
            if (mRedPoint != null) {
                if ("0".equals(mImUnReadCount)) {
                    if (mRedPoint.getVisibility() == View.VISIBLE) {
                        mRedPoint.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mRedPoint.getVisibility() != View.VISIBLE) {
                        mRedPoint.setVisibility(View.VISIBLE);
                    }
                }
                mRedPoint.setText(mImUnReadCount);
            }
        }
    }
}
