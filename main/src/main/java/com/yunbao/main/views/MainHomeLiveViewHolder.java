package com.yunbao.main.views;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.activity.WebViewActivity;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.LiveClassBean;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.live.bean.LiveBean;
import com.yunbao.live.utils.LiveStorge;
import com.yunbao.main.R;
import com.yunbao.main.activity.AllLiveClassActivity;
import com.yunbao.main.activity.LiveClassActivity;
import com.yunbao.main.activity.LiveFollowActivity;
import com.yunbao.main.activity.LiveVoiceRoomListActivity;
import com.yunbao.main.adapter.MainHomeLiveAdapter;
import com.yunbao.main.adapter.MainHomeLiveClassAdapter;
import com.yunbao.main.adapter.MainHomeLiveFollowAdapter;
import com.yunbao.main.adapter.MainHomeLiveRecomAdapter;
import com.yunbao.main.bean.BannerBean;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * MainActivity 首页 直播
 */

public class MainHomeLiveViewHolder extends AbsMainHomeChildViewHolder implements OnItemClickListener<LiveBean>, View.OnClickListener {

    private CommonRefreshView mRefreshView;
    private RecyclerView mClassRecyclerView;
    private View mFollowGroup;
    private RecyclerView mFollowRecyclerView;
    private TextView mFollowNum;
    private String mFollowString;
    private MainHomeLiveFollowAdapter mFollowAdapter;
    private MainHomeLiveAdapter mAdapter;
    private Banner mBanner;
    private boolean mBannerNeedUpdate;
    private List<BannerBean> mBannerList;
    private RecyclerView mRecyclerViewVoice;
    private View mVoiceEmptyTip;
    private MainHomeLiveRecomAdapter mLiveRecomAdapter;
    private View mLiveEmptyTip;


    public MainHomeLiveViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_live;
    }

    @Override
    public void init() {
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.clearEmptyLayoutView();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return 2;
                }
                return 1;
            }
        });
        mRefreshView.setLayoutManager(gridLayoutManager);
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 7, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mAdapter = new MainHomeLiveAdapter(mContext);
        mAdapter.setOnItemClickListener(MainHomeLiveViewHolder.this);
        mRefreshView.setRecyclerViewAdapter(mAdapter);
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LiveBean>() {
            @Override
            public RefreshAdapter<LiveBean> getAdapter() {
                return null;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getHot(p, callback);
            }

            @Override
            public List<LiveBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                mBannerNeedUpdate = false;
                List<BannerBean> bannerList = JSON.parseArray(obj.getString("slide"), BannerBean.class);
                if (bannerList != null && bannerList.size() > 0) {
                    if (mBannerList == null || mBannerList.size() != bannerList.size()) {
                        mBannerNeedUpdate = true;
                    } else {
                        for (int i = 0; i < mBannerList.size(); i++) {
                            BannerBean bean = mBannerList.get(i);
                            if (bean == null || !bean.isEqual(bannerList.get(i))) {
                                mBannerNeedUpdate = true;
                                break;
                            }
                        }
                    }
                }
                mBannerList = bannerList;
                List<LiveBean> recomList = JSON.parseArray(obj.getString("recommend"), LiveBean.class);
                if (recomList != null && recomList.size() > 0) {
//                    if (CommonAppConfig.LIVE_ROOM_SCROLL) {
//                        LiveStorge.getInstance().put(Constants.LIVE_CLASS_RECOMMEND, recomList);
//                    }
                    if (mVoiceEmptyTip != null && mVoiceEmptyTip.getVisibility() != View.GONE) {
                        mVoiceEmptyTip.setVisibility(View.GONE);
                    }
                    if (mRecyclerViewVoice != null) {
                        if (mRecyclerViewVoice.getVisibility() != View.VISIBLE) {
                            mRecyclerViewVoice.setVisibility(View.VISIBLE);
                        }
                        if (mLiveRecomAdapter == null) {
                            mLiveRecomAdapter = new MainHomeLiveRecomAdapter(mContext, recomList);
                            mLiveRecomAdapter.setOnItemClickListener(new OnItemClickListener<LiveBean>() {
                                @Override
                                public void onItemClick(LiveBean bean, int position) {
                                    if(CommonAppConfig.getInstance().isBaseFunctionMode()){
                                        return;
                                    }
                                    watchLive(bean, Constants.LIVE_CLASS_RECOMMEND, position);
                                }
                            });
                            mRecyclerViewVoice.setAdapter(mLiveRecomAdapter);
                        } else {
                            mLiveRecomAdapter.refreshData(recomList);
                        }
                    }
                } else {
                    if (mRecyclerViewVoice != null && mRecyclerViewVoice.getVisibility() != View.GONE) {
                        mRecyclerViewVoice.setVisibility(View.GONE);
                    }
                    if (mVoiceEmptyTip != null && mVoiceEmptyTip.getVisibility() != View.VISIBLE) {
                        mVoiceEmptyTip.setVisibility(View.VISIBLE);
                    }

                }

                List<LiveBean> followList = JSON.parseArray(obj.getString("attent_list"), LiveBean.class);
                if (followList != null && followList.size() > 0) {
                    if (followList.size() > 3) {
                        followList = followList.subList(0, 3);
                    }
                    if (mFollowGroup != null && mFollowGroup.getVisibility() != View.VISIBLE) {
                        mFollowGroup.setVisibility(View.VISIBLE);
                    }
                    if (mFollowRecyclerView != null) {
                        if (mFollowAdapter == null) {
                            mFollowAdapter = new MainHomeLiveFollowAdapter(mContext, followList);
                            mFollowAdapter.setOnItemClickListener(new OnItemClickListener<LiveBean>() {
                                @Override
                                public void onItemClick(LiveBean bean, int position) {
                                    if(CommonAppConfig.getInstance().isBaseFunctionMode()){
                                        return;
                                    }
                                    watchLive(bean, "", 0);
                                }
                            });
                            mFollowRecyclerView.setAdapter(mFollowAdapter);
                        } else {
                            mFollowAdapter.refreshData(followList);
                        }
                    }


                } else {
                    if (mFollowGroup != null && mFollowGroup.getVisibility() != View.GONE) {
                        mFollowGroup.setVisibility(View.GONE);
                    }
                }

                if (mFollowNum != null) {
                    String followNum = obj.getString("attent_live_nums");
                    String followTip = String.format(mFollowString, followNum);
                    CharSequence tip = followTip;
                    int index = followTip.indexOf(followNum);
                    if (index >= 0) {
                        SpannableString spannableString = new SpannableString(followTip);
                        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.global)),
                                index, index + followNum.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tip = spannableString;
                    }
                    mFollowNum.setText(tip);
                }
                List<LiveBean> liveList = JSON.parseArray(obj.getString("list"), LiveBean.class);
                if (liveList.size() == 0) {
                    if (mLiveEmptyTip != null && mLiveEmptyTip.getVisibility() != View.VISIBLE) {
                        mLiveEmptyTip.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mLiveEmptyTip != null && mLiveEmptyTip.getVisibility() != View.GONE) {
                        mLiveEmptyTip.setVisibility(View.GONE);
                    }
                }
                return liveList;
            }

            @Override
            public void onRefreshSuccess(List<LiveBean> list, int count) {
                if (CommonAppConfig.LIVE_ROOM_SCROLL) {
                    LiveStorge.getInstance().put(Constants.LIVE_HOME, list);
                }
                showBanner();
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LiveBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        View headView = mAdapter.getHeadView();
        mVoiceEmptyTip = headView.findViewById(R.id.voice_empty_tip);
        mLiveEmptyTip = headView.findViewById(R.id.live_empty_tip);
        mRecyclerViewVoice = headView.findViewById(R.id.recyclerView_voice);
        mRecyclerViewVoice.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        ItemDecoration decoration2 = new ItemDecoration(mContext, 0x00000000, 7, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRecyclerViewVoice.addItemDecoration(decoration2);
        mClassRecyclerView = headView.findViewById(R.id.classRecyclerView);
        mClassRecyclerView.setHasFixedSize(true);
        mClassRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 6, GridLayoutManager.VERTICAL, false));
        List<LiveClassBean> classList = null;
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            classList = configBean.getLiveClass();
        }
        if (classList == null) {
            classList = new ArrayList<>();
        }
        List<LiveClassBean> targetList = null;
        if (classList.size() <= 6) {
            targetList = classList;
        } else {
            targetList = new ArrayList<>();
            targetList.addAll(classList.subList(0, 5));
            LiveClassBean bean = new LiveClassBean();
            bean.setId(-1);
            bean.setName(WordUtil.getString(R.string.all));
            targetList.add(bean);
        }
        MainHomeLiveClassAdapter classAdapter = new MainHomeLiveClassAdapter(mContext, targetList);
        classAdapter.setOnItemClickListener(new OnItemClickListener<LiveClassBean>() {
            @Override
            public void onItemClick(LiveClassBean bean, int position) {
                if (!canClick()) {
                    return;
                }
                if(CommonAppConfig.getInstance().isBaseFunctionMode()){
                    return;
                }
                if (bean.getId() == -1) {//全部分类
                    AllLiveClassActivity.forward(mContext);
                } else {
                    LiveClassActivity.forward(mContext, bean.getId(), bean.getName());
                }
            }
        });
        if (mClassRecyclerView != null) {
            mClassRecyclerView.setAdapter(classAdapter);
        }
        mBanner = (Banner) headView.findViewById(R.id.banner);
        mBanner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                ImgLoader.display(mContext, ((BannerBean) path).getImageUrl(), imageView);
            }
        });
        mBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int p) {
                if(CommonAppConfig.getInstance().isBaseFunctionMode()){
                    return;
                }
                if (mBannerList != null) {
                    if (p >= 0 && p < mBannerList.size()) {
                        BannerBean bean = mBannerList.get(p);
                        if (bean != null) {
                            String link = bean.getLink();
                            if (!TextUtils.isEmpty(link)) {
                                WebViewActivity.forward(mContext, link);
                            }
                        }
                    }
                }
            }
        });

        mFollowGroup = headView.findViewById(R.id.follow_group);
        mFollowRecyclerView = headView.findViewById(R.id.follow_recyclerView);
        mFollowRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration3 = new ItemDecoration(mContext, 0x00000000, 15, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mFollowRecyclerView.addItemDecoration(decoration3);

        mFollowNum = headView.findViewById(R.id.follow_num);
        mFollowString = WordUtil.getString(R.string.a_105);
        headView.findViewById(R.id.btn_more_follow).setOnClickListener(this);
        headView.findViewById(R.id.btn_more_recom).setOnClickListener(this);
    }

    private void showBanner() {
        if (mBanner == null) {
            return;
        }
        if (mBannerList != null && mBannerList.size() > 0) {
            if (mBanner.getVisibility() != View.VISIBLE) {
                mBanner.setVisibility(View.VISIBLE);
            }
            if (mBannerNeedUpdate) {
                mBanner.update(mBannerList);
            }
        } else {
            if (mBanner.getVisibility() != View.GONE) {
                mBanner.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onItemClick(LiveBean bean, int position) {
        if(CommonAppConfig.getInstance().isBaseFunctionMode()){
            return;
        }
        watchLive(bean, Constants.LIVE_HOME, position);
    }

    @Override
    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void release() {
        MainHttpUtil.cancel(MainHttpConsts.GET_HOT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void onClick(View v) {
        if(CommonAppConfig.getInstance().isBaseFunctionMode()){
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_more_recom) {
            LiveVoiceRoomListActivity.forward(mContext);
        } else if (id == R.id.btn_more_follow) {
            if (((AbsActivity) mContext).checkLogin()) {
                LiveFollowActivity.forward(mContext);
            }
        }
    }
}
