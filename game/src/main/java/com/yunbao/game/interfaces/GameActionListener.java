package com.yunbao.game.interfaces;

/**
 * Created by cxf on 2019/3/22.
 */

public interface GameActionListener {
    void onGamePlayChanged(boolean playing);

    boolean isLinkMicIng();

    void showGameWindow(int gameAction);


    /**
     * 智勇三张 开启游戏窗口
     */
    void zjhShowGameWindow();

    /**
     * 智勇三张 主播创建游戏
     */
    void zjhAnchorCreateGame(String gameId);

    /**
     * 智勇三张 主播关闭游戏
     */
    void zjhAnchorCloseGame();

    /**
     * 智勇三张 主播通知所有人下注
     * 此时服务器收到该socket后，自动计时，30秒后自动发送显示游戏结果的socket
     */
    void zjhAnchorNotifyGameBet(String liveUid, String gameId, String token, int time);

    /**
     * 智勇三张 观众把自己的下注信息广播给所有人
     */
    void zjhAudienceBetGame(int coin, int index);


    /**
     * 幸运转盘 开启游戏窗口
     */
    void zpShowGameWindow();


    /**
     * 幸运转盘 主播关闭游戏
     */
    void zpAnchorCloseGame();

    /**
     * 幸运转盘 主播通知所有人下注
     * 此时服务器收到该socket后，自动计时，30秒后自动发送显示游戏结果的socket
     */
    void zpAnchorNotifyGameBet(String liveUid, String gameId, String token, int time);

    /**
     * 幸运转盘 观众把自己的下注信息广播给所有人
     */
    void zpAudienceBetGame(int coin, int index);




    void release();

}
