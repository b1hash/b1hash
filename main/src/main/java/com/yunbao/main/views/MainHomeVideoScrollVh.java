package com.yunbao.main.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.event.FollowEvent;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.main.R;
import com.yunbao.video.adapter.MainVideoScrollAdapter;
import com.yunbao.video.bean.VideoBean;
import com.yunbao.video.custom.VideoLoadingBar;
import com.yunbao.video.event.VideoCommentEvent;
import com.yunbao.video.event.VideoLikeEvent;
import com.yunbao.video.event.VideoShareEvent;
import com.yunbao.video.http.VideoHttpConsts;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.interfaces.VideoScrollDataHelper;
import com.yunbao.video.views.VideoPlayViewHolder;
import com.yunbao.video.views.VideoPlayWrapViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/5/3.
 */
public class MainHomeVideoScrollVh extends AbsMainHomeChildViewHolder implements
        MainVideoScrollAdapter.ActionListener, VideoPlayViewHolder.ActionListener {

    private RecyclerView mRecyclerView;
    private MainVideoScrollAdapter mVideoScrollAdapter;
    private VideoPlayWrapViewHolder mVideoPlayWrapViewHolder;
    private VideoLoadingBar mVideoLoadingBar;
    private int mPage;

    private HttpCallback mRefreshCallback;
    private HttpCallback mLoadMoreCallback;
    private VideoScrollDataHelper mVideoDataHelper;
    private VideoBean mVideoBean;
    private boolean mFirstShowed = true;

    private VideoPlayViewHolder mVideoPlayViewHolder;
    private View mPlayView;


    public MainHomeVideoScrollVh(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.main_home_video_scroll;
    }

    @Override
    public void init() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mVideoScrollAdapter = new MainVideoScrollAdapter(mContext, new ArrayList<>(), 0);
        mVideoScrollAdapter.setActionListener(this);
        mRecyclerView.setAdapter(mVideoScrollAdapter);
        mVideoLoadingBar = (VideoLoadingBar) findViewById(R.id.video_loading);
        EventBus.getDefault().register(this);
        mRefreshCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoBean> list = JSON.parseArray(Arrays.toString(info), VideoBean.class);
                    if (mVideoScrollAdapter != null) {
                        mVideoScrollAdapter.setList(list);
                    }
                    if (mRecyclerView != null) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onFinish() {
//                if (mRefreshLayout != null) {
//                    mRefreshLayout.setRefreshing(false);
//                }
            }
        };
        mLoadMoreCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoBean> list = JSON.parseArray(Arrays.toString(info), VideoBean.class);
                    if (list.size() > 0) {
                        if (mVideoScrollAdapter != null) {
                            mVideoScrollAdapter.insertList(list);
                        }
                    } else {
//                        ToastUtil.show(R.string.video_no_more_video);
//                        mPage--;
                        mPage=0;
                        onLoadMore();
                    }
                } else {
                    mPage--;
                }
            }
        };
        mVideoDataHelper = new VideoScrollDataHelper() {

            @Override
            public void loadData(int p, HttpCallback callback) {
                VideoHttpUtil.getHomeVideoList(p, callback);
            }
        };

        mVideoPlayViewHolder = new VideoPlayViewHolder(mContext, null);
        mVideoPlayViewHolder.setActionListener(this);
        mPlayView = mVideoPlayViewHolder.getContentView();
    }


    @Override
    public void onPageSelected(VideoPlayWrapViewHolder videoPlayWrapViewHolder, boolean needLoadMore) {
        if (videoPlayWrapViewHolder != null) {
            VideoBean videoBean = videoPlayWrapViewHolder.getVideoBean();
            if (videoBean != null) {
                mVideoBean = videoBean;
                mVideoPlayWrapViewHolder = videoPlayWrapViewHolder;
                if (mPlayView != null) {
                    videoPlayWrapViewHolder.addVideoView(mPlayView);
                }
                if (mVideoPlayViewHolder != null) {
                    mVideoPlayViewHolder.startPlay(videoBean);
                }
                if (mVideoLoadingBar != null) {
                    mVideoLoadingBar.setLoading(true);
                }
            }
            if (needLoadMore) {
                onLoadMore();
            }
        }
    }

    @Override
    public void onPageOutWindow(VideoPlayWrapViewHolder vh) {
        if (mVideoPlayWrapViewHolder != null && mVideoPlayWrapViewHolder == vh) {
            if (mVideoPlayViewHolder != null) {
                mVideoPlayViewHolder.stopPlay();
            }
        }
    }

    @Override
    public void onVideoDeleteAll() {

    }

    public void release() {
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.release();
            mVideoPlayViewHolder = null;
        }
        if (isShowed()) {
            VideoHttpUtil.endWatchVideo();
        }
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_LIST);
        EventBus.getDefault().unregister(this);
        mVideoPlayWrapViewHolder = null;
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.endLoading();
        }
        mVideoLoadingBar = null;
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.release();
        }
        mVideoScrollAdapter = null;
        mVideoDataHelper = null;
    }

    @Override
    public void loadData() {
        if (isFirstLoadData()) {
            refreshData();
        }
    }

    public void refreshData() {
        mPage = 1;
        if (mVideoDataHelper != null) {
            mVideoDataHelper.loadData(mPage, mRefreshCallback);
        }
    }

    /**
     * 加载更多
     */
    private void onLoadMore() {
        mPage++;
        if (mVideoDataHelper != null) {
            mVideoDataHelper.loadData(mPage, mLoadMoreCallback);
        }
    }

    @Override
    public void onPlayBegin() {
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.setLoading(false);
        }
    }

    @Override
    public void onPlayLoading() {
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.setLoading(true);
        }
    }

    @Override
    public void onFirstFrame() {
        if (mVideoPlayWrapViewHolder != null) {
            mVideoPlayWrapViewHolder.onFirstFrame();
        }
    }

    /**
     * 关注发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (mVideoScrollAdapter != null && mVideoPlayWrapViewHolder != null) {
            VideoBean videoBean = mVideoPlayWrapViewHolder.getVideoBean();
            if (videoBean != null) {
                mVideoScrollAdapter.onFollowChanged(false, videoBean.getId(), e.getToUid(), e.getIsAttention());
            }
        }
    }

    /**
     * 点赞发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoLikeEvent(VideoLikeEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onLikeChanged( e.getVideoId(), e.getIsLike(), e.getLikeNum());
        }
    }

    /**
     * 分享数发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoShareEvent(VideoShareEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onShareChanged(e.getVideoId(), e.getShareNum());
        }
    }

    /**
     * 评论数发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoCommentEvent(VideoCommentEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onCommentChanged(e.getVideoId(), e.getCommentNum());
        }
    }

    /**
     * 删除视频
     */
    public void deleteVideo(VideoBean videoBean) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.deleteVideo(videoBean);
        }
    }


    @Override
    public void setShowed(boolean showed) {
        if(mShowed==showed){
            return;
        }
        super.setShowed(showed);
        if (showed) {
            if (mFirstShowed) {
                mFirstShowed = false;
            } else {
                if (mVideoPlayViewHolder != null) {
                    mVideoPlayViewHolder.resumePlay();
                }
            }
            VideoHttpUtil.startWatchVideo();
        } else {
            if (mVideoPlayViewHolder != null) {
                mVideoPlayViewHolder.pausePlay();
            }
            VideoHttpUtil.endWatchVideo();
        }
    }



    public void pausePlay() {
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.pausePlay();
        }
    }


    public void resumePlay() {
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.resumePlay();
        }
    }


}
