package com.yunbao.live.socket;

import com.yunbao.game.GameConsts;
import com.yunbao.game.interfaces.GameActionListener;
import com.yunbao.live.activity.LiveActivity;

/**
 * Created by cxf on 2019/3/22.
 */

public class GameActionListenerImpl implements GameActionListener {

    private LiveActivity mLiveActivity;
    private SocketClient mSocketClient;

    public GameActionListenerImpl(LiveActivity liveActivity, SocketClient socketClient) {
        mLiveActivity = liveActivity;
        mSocketClient = socketClient;
    }


    @Override
    public void onGamePlayChanged(boolean playing) {
        if (mLiveActivity != null) {
            mLiveActivity.setGamePlaying(playing);
        }
    }

    @Override
    public boolean isLinkMicIng() {
        if (mLiveActivity != null) {
            return mLiveActivity.isLinkMic() || mLiveActivity.isLinkMicAnchor();
        }
        return false;
    }

    @Override
    public void showGameWindow(int gameAction) {
        switch (gameAction) {
            case GameConsts.GAME_ACTION_ZJH:
                SocketGameUtil.zjhShowGameWindow(mSocketClient);
                break;
            case GameConsts.GAME_ACTION_ZP:
                SocketGameUtil.zpShowGameWindow(mSocketClient);
                break;
        }
    }

    @Override
    public void zjhShowGameWindow() {
        SocketGameUtil.zjhShowGameWindow(mSocketClient);
    }

    @Override
    public void zjhAnchorCreateGame(String gameId) {
        SocketGameUtil.zjhAnchorCreateGame(mSocketClient, gameId);
    }

    @Override
    public void zjhAnchorCloseGame() {
        SocketGameUtil.zjhAnchorCloseGame(mSocketClient);
    }

    @Override
    public void zjhAnchorNotifyGameBet(String liveUid, String gameId, String token, int time) {
        SocketGameUtil.zjhAnchorNotifyGameBet(mSocketClient, liveUid, gameId, token, time);
    }

    @Override
    public void zjhAudienceBetGame(int coin, int index) {
        SocketGameUtil.zjhAudienceBetGame(mSocketClient, coin, index);
    }


    @Override
    public void zpShowGameWindow() {
        SocketGameUtil.zpShowGameWindow(mSocketClient);
    }

    @Override
    public void zpAnchorCloseGame() {
        SocketGameUtil.zpAnchorCloseGame(mSocketClient);
    }

    @Override
    public void zpAnchorNotifyGameBet(String liveUid, String gameId, String token, int time) {
        SocketGameUtil.zpAnchorNotifyGameBet(mSocketClient, liveUid, gameId, token, time);
    }

    @Override
    public void zpAudienceBetGame(int coin, int index) {
        SocketGameUtil.zpAudienceBetGame(mSocketClient, coin, index);
    }



    @Override
    public void release() {
        mSocketClient = null;
        mLiveActivity = null;
    }
}
