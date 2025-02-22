package com.yunbao.main.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;

import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.JsonUtil;
import com.yunbao.common.views.AbsCommonViewHolder;
import com.yunbao.main.R;
import com.yunbao.main.adapter.VideoHomeAdapter;
import com.yunbao.video.activity.VideoPlayActivity;
import com.yunbao.video.bean.VideoBean;
import com.yunbao.video.event.VideoDeleteEvent;
import com.yunbao.video.event.VideoLikeEvent;
import com.yunbao.video.event.VideoScrollPageEvent;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.interfaces.VideoScrollDataHelper;
import com.yunbao.video.utils.VideoStorge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by cxf on 2018/12/14.
 * 用户个人中心的喜欢视频列表
 */

public class VideoLikeViewHolder extends AbsCommonViewHolder implements OnItemClickListener<VideoBean> {

    private CommonRefreshView mRefreshView;
    private VideoHomeAdapter mAdapter;
    private VideoScrollDataHelper mVideoScrollDataHelper;
    private String mKey;
    private boolean mPaused;
    private HashSet<String> mCancelLikeSet;//取消喜欢的视频id

    public VideoLikeViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.view_video_home;
    }

    @Override
    public void init() {
        mCancelLikeSet = new HashSet<>();
        mKey = Constants.VIDEO_LIKE + this.hashCode();
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_video_like);
        mRefreshView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 2, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoBean>() {
            @Override
            public RefreshAdapter<VideoBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new VideoHomeAdapter(mContext);
                    mAdapter.setOnItemClickListener(VideoLikeViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                VideoHttpUtil.getLikeVideo(p, callback);
            }

            @Override
            public List<VideoBean> processData(String[] info) {
                return JsonUtil.getJsonToList(Arrays.toString(info), VideoBean.class);
            }

            @Override
            public void onRefreshSuccess(List<VideoBean> list, int listCount) {
                VideoStorge.getInstance().put(mKey, list);
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<VideoBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });

        mVideoScrollDataHelper = new VideoScrollDataHelper() {

            @Override
            public void loadData(int p, HttpCallback callback) {
                VideoHttpUtil.getLikeVideo(p, callback);
            }
        };
        EventBus.getDefault().register(VideoLikeViewHolder.this);
    }


    @Override
    public void loadData() {
        if (isFirstLoadData()) {
            mRefreshView.initData();
        }
    }

    public void release() {
        mVideoScrollDataHelper = null;
        VideoHttpUtil.cancel(VideoHttpConsts.GET_LIKE_VIDEO);
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoScrollPageEvent(VideoScrollPageEvent e) {
        if (!TextUtils.isEmpty(mKey) && mKey.equals(e.getKey()) && mRefreshView != null) {
            mRefreshView.setPageCount(e.getPage());
        }
    }

    /**
     * 点赞发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoLikeEvent(VideoLikeEvent e) {
        if(mCancelLikeSet!=null){
            if (e.getIsLike() == 0) {
                mCancelLikeSet.add(e.getVideoId());
            } else {
                mCancelLikeSet.remove(e.getVideoId());
            }
        }
    }


    @Override
    public void onItemClick(VideoBean bean, int position) {
        int page = 1;
        if (mRefreshView != null) {
            page = mRefreshView.getPageCount();
        }
        VideoStorge.getInstance().putDataHelper(mKey, mVideoScrollDataHelper);
        VideoPlayActivity.forward(mContext, position, mKey, page);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPaused) {
            mPaused = false;
            if (mCancelLikeSet != null && !mCancelLikeSet.isEmpty()) {
                if (mAdapter != null) {
                    mAdapter.cancelLikeVideo(mCancelLikeSet);
                    if (mAdapter.getItemCount() == 0 && mRefreshView != null) {
                        mRefreshView.showEmpty();
                    }
                    mCancelLikeSet.clear();
                }
            }
        }
    }

}
