package com.yunbao.live.livegame.star.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
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
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.live.R;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.livegame.star.adapter.StarWinMsgAdapter;
import com.yunbao.live.livegame.star.bean.StarResultBean;
import com.yunbao.live.livegame.star.bean.StarWinMsgBean;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/2/24.
 * 星球探宝
 */
public class LiveGameStarDialog extends AbsDialogFragment implements View.OnClickListener {

    private View[] mStars;
    private View[] mIndicators;
    private TextView[] mCoins;
    private int mChooseStarIndex;
    private boolean mAnimRunning;
    private int mDp100;
    private int mDp130;
    private View mBtnMore;
    private TextView mCoin;
    private int mStarType = 1;
    private Banner2 mBanner2;
    private StarWinMsgAdapter mAdapter;
    private String mLiveUid;
    private String mStream;
    private String mCoinName;
    private String mWinTip;
    private String mWinTipEn;
    private ActionListener mActionListener;
    private Animation mAnim;


    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game_star;
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
        mDp100 = DpUtil.dp2px(100);
        mDp130 = DpUtil.dp2px(130);
        mStars = new View[3];
        mStars[0] = findViewById(R.id.star_0);
        mStars[1] = findViewById(R.id.star_1);
        mStars[2] = findViewById(R.id.star_2);
        for (View star : mStars) {
            star.setOnClickListener(this);
        }
        mIndicators = new View[3];
        mIndicators[0] = findViewById(R.id.indicator_0);
        mIndicators[1] = findViewById(R.id.indicator_1);
        mIndicators[2] = findViewById(R.id.indicator_2);
        mCoins = new TextView[3];
        mCoins[0] = findViewById(R.id.coin_0);
        mCoins[1] = findViewById(R.id.coin_1);
        mCoins[2] = findViewById(R.id.coin_2);
        mBtnMore = findViewById(R.id.btn_more);
        mBtnMore.setOnClickListener(this);
        mCoin = findViewById(R.id.coin);
        findViewById(R.id.btn_coin).setOnClickListener(this);
        findViewById(R.id.btn_get_1).setOnClickListener(this);
        findViewById(R.id.btn_get_10).setOnClickListener(this);
        findViewById(R.id.btn_get_50).setOnClickListener(this);
        findViewById(R.id.btn_pack).setOnClickListener(this);
        findViewById(R.id.btn_rank).setOnClickListener(this);
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
        mAnim = new TranslateAnimation(0, 0, -DpUtil.dp2px(5), DpUtil.dp2px(5));
        mAnim.setDuration(600);
        mAnim.setRepeatCount(Animation.INFINITE);
        mAnim.setRepeatMode(Animation.REVERSE);
        mStars[0].startAnimation(mAnim);
        LiveHttpUtil.gameGetXqtbInfo(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mCoin != null) {
                        mCoin.setText(obj.getString("coin"));
                    }
                    JSONArray array = obj.getJSONArray("star_list");
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject item = array.getJSONObject(i);
//                        String name = item.getString("name");
                        String price = item.getString("price");
                        if (mCoins[i] != null) {
                            mCoins[i].setText(price + mCoinName);
                        }
                        /*if ("冥王星".equals(name)) {
                            if (mCoins[0] != null) {
                                mCoins[0].setText(price + mCoinName);
                            }
                        } else if ("天王星".equals(name)) {
                            if (mCoins[1] != null) {
                                mCoins[1].setText(price + mCoinName);
                            }
                        } else if ("海王星".equals(name)) {
                            if (mCoins[2] != null) {
                                mCoins[2].setText(price + mCoinName);
                            }
                        }*/
                    }
                    if (mBanner2 != null) {
                        List<StarWinMsgBean> list = JSON.parseArray(obj.getString("win_list"), StarWinMsgBean.class);
                        mAdapter = new StarWinMsgAdapter(mContext, list);
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
        if (i == R.id.star_0) {
            chooseStar(0);
        } else if (i == R.id.star_1) {
            chooseStar(1);
        } else if (i == R.id.star_2) {
            chooseStar(2);
        } else if (i == R.id.btn_more) {
            showMorePopWindow();
        } else if (i == R.id.btn_rank) {
            openRankDialog();
        } else if (i == R.id.btn_coin) {
            dismiss();
            if (mActionListener != null) {
                mActionListener.onChargeClick();
            }
        } else if (i == R.id.btn_get_1) {
            playGame(1);
        } else if (i == R.id.btn_get_10) {
            playGame(10);
        } else if (i == R.id.btn_get_50) {
            playGame(50);
        } else if (i == R.id.btn_pack) {
            dismiss();
            if (mActionListener != null) {
                mActionListener.onPackClick();
            }
        }
    }

    private void chooseStar(final int index) {
        if (!mAnimRunning && mChooseStarIndex != index) {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    ViewGroup.LayoutParams lpA = mStars[index].getLayoutParams();
                    int wA = (int) (mDp100 + (mDp130 - mDp100) * value);
                    lpA.width = wA;
                    lpA.height = wA;
                    mStars[index].requestLayout();
                    ViewGroup.LayoutParams lpB = mStars[mChooseStarIndex].getLayoutParams();
                    int wB = (int) (mDp130 + (mDp100 - mDp130) * value);
                    lpB.width = wB;
                    lpB.height = wB;
                    mStars[mChooseStarIndex].requestLayout();
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mChooseStarIndex = index;
                    mAnimRunning = false;
                    if (index == 0) {
                        mStarType = 1;
                    } else if (index == 1) {
                        mStarType = 2;
                    } else if (index == 2) {
                        mStarType = 3;
                    }
                    mStars[mChooseStarIndex].startAnimation(mAnim);
                }
            });
            mStars[mChooseStarIndex].clearAnimation();
            mAnimRunning = true;
            animator.start();
            mIndicators[index].setVisibility(View.VISIBLE);
            mIndicators[mChooseStarIndex].setVisibility(View.INVISIBLE);
        }
    }

    private void showMorePopWindow() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_game_star_pop, null);
        v.measure(0, 0);
        int vw = v.getMeasuredWidth();
        int vh = v.getMeasuredHeight();
        final PopupWindow popupWindow = new PopupWindow(v, vw, vh, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        int[] arr = new int[2];
        mBtnMore.getLocationInWindow(arr);
        int offsetX = arr[0] + mBtnMore.getWidth() / 2 - vw / 2;
        int offsetY = arr[1] + mBtnMore.getHeight() + DpUtil.dp2px(5);
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
        bundle.putString(Constants.URL, HtmlConfig.GAME_RULE_XQTB);
        dialog.setArguments(bundle);
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "WebViewDialog");
    }

    /**
     * 中奖记录
     */
    private void openWinRecord() {
        StarRecordDialog dialog = new StarRecordDialog();
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveGameStarRecordDialog");
    }

    /**
     * 排行榜
     */
    private void openRankDialog() {
        StarRankDialog dialog = new StarRankDialog();
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveGameStarRankDialog");
    }

    private void playGame(final int num) {
        LiveHttpUtil.gameXqtbPlay(mLiveUid, mStream, mStarType, num, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mCoin != null) {
                        mCoin.setText(obj.getString("coin"));
                    }
                    List<StarResultBean> resultList = JSON.parseArray(obj.getString("gift_list"), StarResultBean.class);
                    if (resultList != null && resultList.size() > 0) {
                        StarResultDialog resultDialog = new StarResultDialog();
                        resultDialog.setResultList(resultList);
                        resultDialog.setActionListener(new StarResultDialog.ActionListener() {
                            @Override
                            public void againGame() {
                                playGame(num);
                            }

                        });
                        resultDialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveGameStarResultDialog");
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
                            for (StarResultBean bean : resultList) {
                                JSONObject item = new JSONObject();
                                item.put("gifticon", bean.getGiftIcon());
                                item.put("title", String.format(mWinTip, userName, bean.getGiftName(), bean.getCount()));
                                item.put("title_en", String.format(mWinTipEn, userName, bean.getGiftNameEn(), bean.getCount()));
                                array.add(item);
                            }
                            if (mActionListener != null) {
                                mActionListener.sendWinSocket(array.toJSONString());
                            }
                        }
                    } else {
                        StarResultEmptyDialog emptyDialog = new StarResultEmptyDialog();
                        emptyDialog.setActionListener(new StarResultEmptyDialog.ActionListener() {
                            @Override
                            public void againGame() {
                                playGame(num);
                            }

                        });
                        emptyDialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "StarResultEmptyDialog");
                    }
                } else if (code == 1004) {
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

    public void onWinResultChanged(List<StarWinMsgBean> list) {
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
        if (mActionListener != null) {
            mActionListener.onClose();
        }
        mActionListener = null;
        if (mBanner2 != null) {
            mBanner2.destroy();
            mBanner2 = null;
        }
        LiveHttpUtil.cancel(LiveHttpConsts.GAME_GET_XQTB_INFO);
        LiveHttpUtil.cancel(LiveHttpConsts.GAME_XQTB_PLAY);
        super.onDestroy();
    }


    public interface ActionListener {
        void onClose();

        void sendWinSocket(String json);

        void onPackClick();

        void onChargeClick();
    }

}
