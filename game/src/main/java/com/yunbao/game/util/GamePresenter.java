package com.yunbao.game.util;

import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.game.GameConsts;
import com.yunbao.game.R;
import com.yunbao.game.bean.GameParam;
import com.yunbao.game.interfaces.GameActionListener;
import com.yunbao.game.views.AbsGameViewHolder;
import com.yunbao.game.views.GameZjhViewHolder;
import com.yunbao.game.views.GameZpViewHolder;

import java.util.List;


/**
 * Created by cxf on 2018/10/31.
 */

public class GamePresenter {
    private GameParam mGameParam;
    private List<Integer> mGameList;
    private AbsGameViewHolder mGameViewHolder;
    private GameSoundPool mGameSoundPool;
    private boolean mEnd;
    private GameActionListener mGameActionListener;

    public GamePresenter() {
        mGameSoundPool = new GameSoundPool();
    }

    public GamePresenter(GameParam param) {
        this();
        setGameParam(param);
    }

    public void setGameParam(GameParam param) {
        mGameParam = param;
        mGameActionListener=param.getGameActionListener();
        boolean anchor = param.isAnchor();
        JSONObject obj = param.getObj();
        if (!anchor) {
            int gameAction = obj.getIntValue("gameaction");
            int betTime = obj.getIntValue("gametime");
            int[] totalBet = obj.getObject("game", int[].class);
            int[] myBet = obj.getObject("gamebet", int[].class);
            if (gameAction != 0 && betTime > 0 && totalBet.length > 0 && myBet.length > 0) {
                createGameViewHolder(gameAction);
                if (mGameViewHolder != null) {
                    mGameViewHolder.setGameID(obj.getString("gameid"));
                    mGameViewHolder.setBetTime(betTime);
                    mGameViewHolder.setTotalBet(totalBet);
                    mGameViewHolder.setMyBet(myBet);
                    mGameViewHolder.enterRoomOpenGameWindow();
                }
            }
        }
    }

    public void setGameList(List<Integer> gameList) {
        mGameList = gameList;
    }

    public List<Integer> getGameList() {
        return mGameList;
    }

    /**
     * 开始游戏
     */
    public void startGame(int gameAction) {
        if (mGameActionListener == null) {
            return;
        }
        if (mGameActionListener.isLinkMicIng()) {
            ToastUtil.show(R.string.live_link_mic_cannot_game);
            return;
        }
        if (mGameViewHolder != null && mGameViewHolder.isBetStarted()) {
            ToastUtil.show(R.string.game_wait_end);
            return;
        }
        mGameActionListener.showGameWindow(gameAction);
    }

    /**
     * 关闭游戏
     */
    public void closeGame() {
        if (mGameViewHolder != null) {
            mGameViewHolder.anchorCloseGame();
        }
    }

    private void createGameViewHolder(int gameAction) {
        AbsGameViewHolder gameViewHolder = null;
        switch (gameAction) {
            case GameConsts.GAME_ACTION_ZJH:
                gameViewHolder = new GameZjhViewHolder(mGameParam, mGameSoundPool);
                break;
            case GameConsts.GAME_ACTION_ZP:
                gameViewHolder = new GameZpViewHolder(mGameParam, mGameSoundPool);
                break;
        }
        if (gameViewHolder != null) {
            mGameViewHolder = gameViewHolder;
            if (mGameActionListener != null) {
                mGameActionListener.onGamePlayChanged(true);
            }
        }
    }


    /**
     * 收到 智勇三张 socket回调
     */
    public void onGameZjhSocket(JSONObject obj) {
        if (mEnd) {
            return;
        }
        int action = obj.getIntValue("action");
        if (action == GameConsts.GAME_ACTION_OPEN_WINDOW) {
            if (mGameViewHolder != null) {
                mGameViewHolder.removeFromParent();
                mGameViewHolder.release();
                mGameViewHolder = null;
            }
            createGameViewHolder(GameConsts.GAME_ACTION_ZJH);
        } else if (action == GameConsts.GAME_ACTION_CREATE) {
            if (mGameViewHolder != null) {
                if (!(mGameViewHolder instanceof GameZjhViewHolder)) {
                    mGameViewHolder.removeFromParent();
                    mGameViewHolder.release();
                    mGameViewHolder = null;
                    createGameViewHolder(GameConsts.GAME_ACTION_ZJH);
                }
            } else {
                createGameViewHolder(GameConsts.GAME_ACTION_ZJH);
            }
        }
        if (mGameViewHolder != null && mGameViewHolder instanceof GameZjhViewHolder) {
            mGameViewHolder.handleSocket(action, obj);
        }
        if (action == GameConsts.GAME_ACTION_CLOSE) {
            mGameViewHolder = null;
            if (mGameActionListener != null) {
                mGameActionListener.onGamePlayChanged(false);
            }
        }
    }


    /**
     * 收到 幸运转盘 socket回调
     */
    public void onGameZpSocket(JSONObject obj) {
        if (mEnd) {
            return;
        }
        int action = obj.getIntValue("action");
        if (action == GameConsts.GAME_ACTION_OPEN_WINDOW) {
            if (mGameViewHolder != null) {
                mGameViewHolder.removeFromParent();
                mGameViewHolder.release();
                mGameViewHolder = null;
            }
            createGameViewHolder(GameConsts.GAME_ACTION_ZP);
        } else if (action == GameConsts.GAME_ACTION_NOTIFY_BET) {
            if (mGameViewHolder != null) {
                if (!(mGameViewHolder instanceof GameZpViewHolder)) {
                    mGameViewHolder.removeFromParent();
                    mGameViewHolder.release();
                    mGameViewHolder = null;
                    createGameViewHolder(GameConsts.GAME_ACTION_ZP);
                }
            } else {
                createGameViewHolder(GameConsts.GAME_ACTION_ZP);
            }
        }
        if (mGameViewHolder != null && mGameViewHolder instanceof GameZpViewHolder) {
            mGameViewHolder.handleSocket(action, obj);
        }
        if (action == GameConsts.GAME_ACTION_CLOSE) {
            mGameViewHolder = null;
            if (mGameActionListener != null) {
                mGameActionListener.onGamePlayChanged(false);
            }
        }
    }



    public void setLastCoin(String coin) {
        if (mGameViewHolder != null) {
            mGameViewHolder.setLastCoin(coin);
        }
    }

    public void clearGame() {
        if (mGameViewHolder != null) {
            mGameViewHolder.hideGameWindow();
            mGameViewHolder.release();
        }
        mGameViewHolder = null;
        if (mGameActionListener != null) {
            mGameActionListener.onGamePlayChanged(false);
        }
    }


    public void release() {
        mEnd = true;
        if (mGameSoundPool != null) {
            mGameSoundPool.release();
        }
        if (mGameActionListener != null) {
            mGameActionListener.onGamePlayChanged(false);
            mGameActionListener.release();
        }
        mGameActionListener = null;
        mGameList = null;
        if (mGameViewHolder != null) {
            mGameViewHolder.release();
        }
        mGameViewHolder = null;
    }

}
