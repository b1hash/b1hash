package com.yunbao.live.livegame.luckpan.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.HtmlConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.banner.Banner2;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.dialog.WebViewDialog;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.live.R;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.livegame.luckpan.adapter.LuckpanWinMsgAdapter;
import com.yunbao.live.livegame.luckpan.bean.LuckpanResultBean;
import com.yunbao.live.livegame.luckpan.bean.LuckpanWinMsgBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/2/24.
 * 幸运转盘
 */
public class LiveGameLuckpanDialog extends AbsDialogFragment implements View.OnClickListener {

    private ImageView[] mImgs;
    private View mBtnMore;
    private TextView mCoin;
    private TextView[] mCoins;
    private Banner2 mBanner2;
    private LuckpanWinMsgAdapter mAdapter;
    private String mLiveUid;
    private String mStream;
    private String mCoinName;
    private String mWinTip;
    private String mWinTipEn;
    private boolean mSkip;
    private ImageView mSkipCheck;
    private ActionListener mActionListener;
    private boolean mAnimRunning;
    private int mAnimIndex = -1;
    private boolean mEnd;
    private int mCheckImgIndex = -1;
    private Drawable mCheckedDrawable;
    private Drawable mUnCheckedDrawable;
    private ValueAnimator mAnimator;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game_luckpan;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLiveUid = bundle.getString(Constants.LIVE_UID);
            mStream = bundle.getString(Constants.STREAM);
        }
        mCoinName = CommonAppConfig.getInstance().getCoinName();
        mWinTip = "恭喜%1$s获得%2$s%3$s";
        mWinTipEn = "Congratulations to %1$s for getting %2$s%3$s";
        mBtnMore = findViewById(R.id.btn_more);
        mBtnMore.setOnClickListener(this);
        mImgs = new ImageView[8];
        mImgs[0] = findViewById(R.id.img_0);
        mImgs[1] = findViewById(R.id.img_1);
        mImgs[2] = findViewById(R.id.img_2);
        mImgs[3] = findViewById(R.id.img_3);
        mImgs[4] = findViewById(R.id.img_4);
        mImgs[5] = findViewById(R.id.img_5);
        mImgs[6] = findViewById(R.id.img_6);
        mImgs[7] = findViewById(R.id.img_7);
        mCheckedDrawable = ContextCompat.getDrawable(mContext, R.mipmap.game_luckpan_05);
        mUnCheckedDrawable = ContextCompat.getDrawable(mContext, R.mipmap.game_luckpan_04);
        mCoins = new TextView[3];
        mCoins[0] = findViewById(R.id.coin_0);
        mCoins[1] = findViewById(R.id.coin_1);
        mCoins[2] = findViewById(R.id.coin_2);
        mCoin = findViewById(R.id.coin);
        findViewById(R.id.btn_coin).setOnClickListener(this);
        findViewById(R.id.btn_get_1).setOnClickListener(this);
        findViewById(R.id.btn_get_10).setOnClickListener(this);
        findViewById(R.id.btn_get_100).setOnClickListener(this);
        findViewById(R.id.btn_pack).setOnClickListener(this);
        findViewById(R.id.btn_rank).setOnClickListener(this);
        findViewById(R.id.btn_skip).setOnClickListener(this);
        mSkipCheck = findViewById(R.id.img_skip_check);
        mBanner2 = findViewById(R.id.banner);
        mBanner2.setUserInputEnabled(false);
        mBanner2.setLoopTime(3000);
        mBanner2.setScrollTime(1200);
        mBanner2.setOrientation(Banner2.VERTICAL);
        ViewPager2 viewPager2 = mBanner2.getViewPager2();
        if (viewPager2 != null) {
            viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
            viewPager2.setOffscreenPageLimit(1000);
        }

        LiveHttpUtil.gameGetLuckpanInfo(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mCoin != null) {
                        mCoin.setText(obj.getString("coin"));
                    }
                    JSONArray array = obj.getJSONArray("gift_list");
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        if (i < 8 && mImgs[i] != null) {
                            ImgLoader.display(mContext, item.getString("gifticon"), mImgs[i]);
                        }
                    }
                    JSONObject priceObj = obj.getJSONObject("price_list");
                    if (mCoins[0] != null) {
                        mCoins[0].setText(priceObj.getString("xydzp_one_price") + mCoinName);
                    }
                    if (mCoins[1] != null) {
                        mCoins[1].setText(priceObj.getString("xydzp_ten_price") + mCoinName);
                    }
                    if (mCoins[2] != null) {
                        mCoins[2].setText(priceObj.getString("xydzp_hundred_price") + mCoinName);
                    }
                    if (mBanner2 != null) {
                        List<LuckpanWinMsgBean> list = JSON.parseArray(obj.getString("win_list"), LuckpanWinMsgBean.class);
                        mAdapter = new LuckpanWinMsgAdapter(mContext, list);
                        mBanner2.setAdapter(mAdapter);
                        mBanner2.isAutoLoop(true);
                        mBanner2.start2();
                    }
                }
            }

        });
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_more) {
            showMorePopWindow();
        } else if (i == R.id.btn_rank) {
            openRankDialog();
        } else if (i == R.id.btn_coin) {
            dismiss();
            if (mActionListener != null) {
                mActionListener.onChargeClick();
            }
        } else if (i == R.id.btn_get_1) {
            playGame("one");
        } else if (i == R.id.btn_get_10) {
            playGame("ten");
        } else if (i == R.id.btn_get_100) {
            playGame("hundred");
        } else if (i == R.id.btn_pack) {
            dismiss();
            if (mActionListener != null) {
                mActionListener.onPackClick();
            }
        } else if (i == R.id.btn_skip) {
            toggleSkip();
        }
    }

    private void toggleSkip() {
        mSkip = !mSkip;
        if (mSkipCheck != null) {
            if (mSkip) {
                if (mSkipCheck.getVisibility() != View.VISIBLE) {
                    mSkipCheck.setVisibility(View.VISIBLE);
                }
            } else {
                if (mSkipCheck.getVisibility() != View.INVISIBLE) {
                    mSkipCheck.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void showMorePopWindow() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_game_luckpan_pop, null);
        v.measure(0, 0);
        int vw = v.getMeasuredWidth();
        int vh = v.getMeasuredHeight();
        final PopupWindow popupWindow = new PopupWindow(v, vw, vh, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        int[] arr = new int[2];
        mBtnMore.getLocationInWindow(arr);
        int offsetX = arr[0];
        int offsetY = arr[1];
        popupWindow.showAtLocation(mBtnMore, Gravity.LEFT | Gravity.TOP, offsetX, offsetY);
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.btn_rule_explain) {
                    popupWindow.dismiss();
                    openRuleExplain();
                } else if (i == R.id.btn_win_record) {
                    popupWindow.dismiss();
                    openWinRecord();
                }
            }
        };
        v.findViewById(R.id.btn_rule_explain).setOnClickListener(onClick);
        v.findViewById(R.id.btn_win_record).setOnClickListener(onClick);
    }

    /**
     * 规则说明
     */
    private void openRuleExplain() {
        WebViewDialog dialog = new WebViewDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.URL, HtmlConfig.GAME_RULE_LUCKPAN);
        dialog.setArguments(bundle);
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "WebViewDialog");
    }

    /**
     * 中奖记录
     */
    private void openWinRecord() {
        LuckpanRecordDialog dialog = new LuckpanRecordDialog();
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LuckpanRecordDialog");
    }

    /**
     * 排行榜
     */
    private void openRankDialog() {
        LuckpanRankDialog dialog = new LuckpanRankDialog();
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LuckpanRankDialog");
    }


    private void playGame(final String type) {
        if (mAnimRunning) {
            return;
        }
        LiveHttpUtil.gameLuckpanPlay(mLiveUid, mStream, type, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, final String[] info) {
                if (code == 0 && info.length > 0) {
                    if (mSkip) {
                        showGameResult(type, info);
                    } else {
                        startAnim(new Runnable() {
                            @Override
                            public void run() {
                                showGameResult(type, info);
                            }
                        });
                    }
                } else if (code == 1002) {
                    DialogUitl.showSimpleDialog(mContext, msg, true, new DialogUitl.SimpleCallback() {
                        @Override
                        public void onConfirmClick(Dialog dialog, String content) {
                            dismiss();
                            RouteUtil.forwardMyCoin(mContext);
                        }
                    });
                }
            }
        });
    }


    private void showGameResult(final String type, String[] info) {
        JSONObject obj = JSON.parseObject(info[0]);
        if (mCoin != null) {
            mCoin.setText(obj.getString("coin"));
        }
        List<LuckpanResultBean> resultList = JSON.parseArray(obj.getString("gift_list"), LuckpanResultBean.class);
        if (resultList != null && resultList.size() > 0) {
            LuckpanResultDialog resultDialog = new LuckpanResultDialog();
            resultDialog.setResultList(resultList);
            resultDialog.setActionListener(new LuckpanResultDialog.ActionListener() {
                @Override
                public void againGame() {
                    playGame(type);
                }
            });
            resultDialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LuckpanResultDialog");
            UserBean self = CommonAppConfig.getInstance().getUserBean();
            if (self != null) {
                String userName = self.getUserNiceName();
                if (!TextUtils.isEmpty(userName) && userName.length() > 1) {
                    String replaceString = userName.substring(1);
                    if (!TextUtils.isEmpty(replaceString)) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < replaceString.length(); i++) {
                            sb.append("*");
                        }
                        String newStr = sb.toString();
                        if (!TextUtils.isEmpty(newStr)) {
                            userName = userName.replace(replaceString, newStr);
                        }
                    }
                }
                JSONArray array = new JSONArray();
                for (LuckpanResultBean bean : resultList) {
                    JSONObject item = new JSONObject();
//                    item.put("gifticon", bean.getGiftIcon());
                    item.put("title", String.format(mWinTip, userName, bean.getGiftName(), bean.getCount()));
                    item.put("title_en", String.format(mWinTipEn, userName, bean.getGiftNameEn(), bean.getCount()));
                    array.add(item);
                }
                if (mActionListener != null) {
                    mActionListener.sendWinSocket(array.toJSONString());
                }
            }
        } else {
            LuckpanResultEmptyDialog emptyDialog = new LuckpanResultEmptyDialog();
            emptyDialog.setActionListener(new LuckpanResultEmptyDialog.ActionListener() {
                @Override
                public void againGame() {
                    playGame(type);
                }
            });
            emptyDialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LuckpanResultEmptyDialog");
        }
    }

    public void onWinResultChanged(List<LuckpanWinMsgBean> list) {
        if (mBanner2 != null) {
            mBanner2.stop();
            if (mAdapter != null) {
                mAdapter.refreshData(list);
            }
            mBanner2.setCurrentItem(mBanner2.getStartPosition(), false);
            mBanner2.isAutoLoop(true);
            mBanner2.start2();
        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public void onDestroy() {
        mEnd = true;
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        if (mActionListener != null) {
            mActionListener.onClose();
        }
        mActionListener = null;
        if (mBanner2 != null) {
            mBanner2.destroy();
            mBanner2 = null;
        }
        LiveHttpUtil.cancel(LiveHttpConsts.GAME_GET_LUCKPAN_INFO);
        LiveHttpUtil.cancel(LiveHttpConsts.GAME_LUCKPAN_PLAY);
        super.onDestroy();
    }


    public interface ActionListener {
        void onClose();

        void sendWinSocket(String json);

        void onPackClick();

        void onChargeClick();
    }

    private void startAnim(final Runnable runnable) {
        if (!mAnimRunning) {
            int random = StringUtil.getRandomInt(8);
            int max = 32 + random;
            ValueAnimator animator = ValueAnimator.ofInt(0, max).setDuration(max * 80L);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (mAnimIndex != value) {
                        mAnimIndex = value;
                        int index = value % 8;
                        if (mImgs[index] != null) {
                            mImgs[index].setBackground(mCheckedDrawable);
                        }
                        if (mCheckImgIndex >= 0 && mImgs[mCheckImgIndex] != null) {
                            mImgs[mCheckImgIndex].setBackground(mUnCheckedDrawable);
                        }
                        mCheckImgIndex = index;
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimator = null;
                    mAnimRunning = false;
                    if (mCheckImgIndex >= 0 && mImgs[mCheckImgIndex] != null) {
                        mImgs[mCheckImgIndex].setBackground(mUnCheckedDrawable);
                    }
                    if (!mEnd && runnable != null) {
                        runnable.run();
                    }
                }
            });
            mAnimator = animator;
            mAnimRunning = true;
            mAnimIndex = -1;
            mCheckImgIndex = -1;
            animator.start();
        }
    }

}
