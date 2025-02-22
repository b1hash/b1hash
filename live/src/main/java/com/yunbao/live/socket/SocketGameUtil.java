package com.yunbao.live.socket;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.bean.UserBean;
import com.yunbao.game.GameConsts;

/**
 * Created by cxf on 2018/10/31.
 */

public class SocketGameUtil {


    /**
     * 智勇三张 开启游戏窗口
     */
    public static void zjhShowGameWindow(SocketClient client) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZJH)
                .param("action", GameConsts.GAME_ACTION_OPEN_WINDOW)
                .param("msgtype", 15)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("ct", "")
        );
    }

    /**
     * 智勇三张 主播创建游戏
     */
    public static void zjhAnchorCreateGame(SocketClient client, String gameId) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZJH)
                .param("action", GameConsts.GAME_ACTION_CREATE)
                .param("msgtype", 15)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("gameid", gameId)
                .param("ct", "")
        );
    }

    /**
     * 智勇三张 主播关闭游戏
     */
    public static void zjhAnchorCloseGame(SocketClient client) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZJH)
                .param("action", GameConsts.GAME_ACTION_CLOSE)
                .param("msgtype", 15)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("ct", "")
        );
    }

    /**
     * 智勇三张 主播通知所有人下注
     * 此时服务器收到该socket后，自动计时，30秒后自动发送显示游戏结果的socket
     */
    public static void zjhAnchorNotifyGameBet(SocketClient client, String liveUid, String gameId, String token, int time) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZJH)
                .param("action", GameConsts.GAME_ACTION_NOTIFY_BET)
                .param("msgtype", 15)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("liveuid", liveUid)
                .param("gameid", gameId)
                .param("token", token)
                .param("time", time)
                .param("ct", "")
        );
    }

    /**
     * 智勇三张 观众把自己的下注信息广播给所有人
     */
    public static void zjhAudienceBetGame(SocketClient client, int coin, int index) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZJH)
                .param("action", GameConsts.GAME_ACTION_BROADCAST_BET)
                .param("msgtype", 15)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("money", coin)
                .param("type", index)
                .param("ct", "")
        );
    }




    /**
     * 幸运转盘 开启游戏窗口
     */
    public static void zpShowGameWindow(SocketClient client) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZP)
                .param("action", 1)
                .param("msgtype", 16)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("ct", "")
        );
    }


    /**
     * 幸运转盘 主播关闭游戏
     */
    public static void zpAnchorCloseGame(SocketClient client) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZP)
                .param("action", GameConsts.GAME_ACTION_CLOSE)
                .param("msgtype", 16)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("ct", "")
        );
    }

    /**
     * 幸运转盘 主播通知所有人下注
     * 此时服务器收到该socket后，自动计时，30秒后自动发送显示游戏结果的socket
     */
    public static void zpAnchorNotifyGameBet(SocketClient client, String liveUid, String gameId, String token, int time) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZP)
                .param("action", GameConsts.GAME_ACTION_NOTIFY_BET)
                .param("msgtype", 16)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("liveuid", liveUid)
                .param("gameid", gameId)
                .param("token", token)
                .param("time", time)
                .param("ct", "")
        );
    }

    /**
     * 幸运转盘 观众把自己的下注信息广播给所有人
     */
    public static void zpAudienceBetGame(SocketClient client, int coin, int index) {
        if (client == null) {
            return;
        }
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        client.send(new SocketSendBean()
                .param("_method_", Constants.SOCKET_GAME_ZP)
                .param("action", GameConsts.GAME_ACTION_BROADCAST_BET)
                .param("msgtype", 16)
                .param("level", u.getLevel())
                .param("uname", u.getUserNiceName())
                .param("uid", u.getId())
                .param("money", coin)
                .param("type", index)
                .param("ct", "")
        );
    }



}
