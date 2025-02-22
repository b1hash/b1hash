package com.yunbao.main.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.ClickUtil;
import com.yunbao.common.utils.LanguageUtil;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.main.R;
import com.yunbao.main.activity.SettingActivity;
import com.yunbao.main.bean.SettingBean;
import com.yunbao.main.http.MainHttpUtil;

import java.util.List;

/**
 * Created by cxf on 2018/9/30.
 */

public class SettingAdapter extends RecyclerView.Adapter {

    private static final int NORMAL = 0;
    private static final int VERSION = 1;
    private static final int LAST = 2;
    private static final int CHECK = 3;
    private static final int LANGUAGE = 4;
    private Context mContext;
    private List<SettingBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private ActionListener mActionListener;
    private String mVersionString;
    private String mCacheString;
    private Drawable mRadioCheckDrawable;
    private Drawable mRadioUnCheckDrawable;
    private View.OnClickListener mOnRadioBtnClickListener;
    private String mLangString;
    private View.OnClickListener mLanguageClickListener;


    public SettingAdapter(Context context, List<SettingBean> list, String version, String cache) {
        mContext = context;
        mList = list;
        mVersionString = version;
        mCacheString = cache;
        mLangString = getLanguageString();
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    SettingBean bean = mList.get(position);
                    if (mActionListener != null) {
                        mActionListener.onItemClick(bean, position);
                    }
                }
            }
        };
        mRadioCheckDrawable = ContextCompat.getDrawable(context, R.mipmap.icon_btn_radio_1);
        mRadioUnCheckDrawable = ContextCompat.getDrawable(context, R.mipmap.icon_btn_radio_0);
        mOnRadioBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = (int) v.getTag();
                final SettingBean settingBean = mList.get(position);
                if (settingBean.getId() == -3) {
                    MainHttpUtil.setLiveWindow(new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                boolean checked = obj.getIntValue("status") == 1;
                                CommonAppConfig.getInstance().setShowLiveFloatWindow(checked);
                                settingBean.setChecked(checked);
                                notifyItemChanged(position, Constants.PAYLOAD);
                            }
                            ToastUtil.show(msg);
                        }
                    });
                } else {
                    settingBean.setChecked(!settingBean.isChecked());
                    notifyItemChanged(position, Constants.PAYLOAD);
                    if (mActionListener != null) {
                        mActionListener.onCheckChanged(settingBean);
                    }
                }
            }
        };
        mLanguageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickUtil.canClick()) {
                    if (mActionListener != null) {
                        mActionListener.onLanguageClick();
                    }
                }
            }
        };
    }


    private String getLanguageString() {
        String lang = LanguageUtil.getInstance().getLanguage();
        if (Constants.LANG_ZH.equals(lang)) {
            return Constants.CHINESE;
        }
        if (Constants.LANG_AR.equals(lang)) {
            return Constants.ARABIC;
        }
        if (Constants.LANG_RU.equals(lang)) {
            return Constants.RUSSIAN;
        }
        if (Constants.LANG_HI.equals(lang)) {
            return Constants.INDIA;
        }
        return Constants.ENGLISH;
    }

    public void setCacheString(String cacheString) {
        mCacheString = cacheString;
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public int getItemViewType(int position) {
        SettingBean bean = mList.get(position);
        if (bean.getId() == Constants.SETTING_UPDATE_ID || bean.getId() == Constants.SETTING_CLEAR_CACHE) {
            return VERSION;
        } else if (bean.getId() == -1 || bean.getId() == -2 || bean.getId() == -3) {
            return CHECK;
        } else if (bean.getId() == -4) {
            return LANGUAGE;
        } else if (bean.getId() == -5) {
            return LAST;
        } else {
            return NORMAL;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VERSION) {
            return new Vh2(mInflater.inflate(R.layout.item_setting_1, parent, false));
        } else if (viewType == CHECK) {
            return new RadioButtonVh(mInflater.inflate(R.layout.item_setting_3, parent, false));
        } else if (viewType == LANGUAGE) {
            return new LanguageVh(mInflater.inflate(R.layout.item_setting_4, parent, false));
        } else if (viewType == LAST) {
            return new Vh(mInflater.inflate(R.layout.item_setting_2, parent, false));
        } else {
            return new Vh(mInflater.inflate(R.layout.item_setting, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        if (vh instanceof Vh) {
            ((Vh) vh).setData(mList.get(position), position);
        } else if (vh instanceof RadioButtonVh) {
            Object payload = payloads.size() > 0 ? payloads.get(0) : null;
            ((RadioButtonVh) vh).setData(mList.get(position), position, payload);
        } else if (vh instanceof LanguageVh) {
            Object payload = payloads.size() > 0 ? payloads.get(0) : null;
            ((LanguageVh) vh).setData(mList.get(position), payload);
        } else {
            ((Vh2) vh).setData(mList.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mName;

        public Vh(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(SettingBean bean, int position) {
            itemView.setTag(position);
            mName.setText(bean.getName());
        }
    }

    class Vh2 extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mText;

        public Vh2(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            mText = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(SettingBean bean, int position) {
            itemView.setTag(position);
            mName.setText(bean.getName());
            if (bean.getId() == Constants.SETTING_CLEAR_CACHE) {
                mText.setText(mCacheString);
            } else {
                mText.setText(mVersionString);
            }
        }
    }


    class RadioButtonVh extends RecyclerView.ViewHolder {

        TextView mName;
        ImageView mBtnRadio;

        public RadioButtonVh(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            mBtnRadio = itemView.findViewById(R.id.btn_radio);
            mBtnRadio.setOnClickListener(mOnRadioBtnClickListener);
        }

        void setData(SettingBean bean, int position, Object payload) {
            if (payload == null) {
                mName.setText(bean.getName());
                mBtnRadio.setTag(position);
            }
            mBtnRadio.setImageDrawable(bean.isChecked() ? mRadioCheckDrawable : mRadioUnCheckDrawable);
        }
    }

    class LanguageVh extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mText;

        public LanguageVh(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            mText = itemView.findViewById(R.id.text);
            itemView.setOnClickListener(mLanguageClickListener);
        }

        void setData(SettingBean bean, Object payload) {
            if (payload == null) {
                mName.setText(bean.getName());
            }
            mText.setText(mLangString);
        }
    }

    public interface ActionListener {
        void onItemClick(SettingBean bean, int position);

        void onCheckChanged(SettingBean bean);

        void onLanguageClick();
    }


}
