package com.yunbao.game.http;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.http.HttpClient;

/**
 * Created by cxf on 2018/9/17.
 */

public class GameHttpUtil {

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }
    
    /**
     * 创建炸金花游戏
     */
    public static void gameJinhuaCreate(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Jinhua", GameHttpConsts.GAME_JINHUA_CREATE)
                .params("liveuid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 炸金花游戏下注
     */
    public static void gameJinhuaBet(String gameid, int coin, int grade, HttpCallback callback) {
        HttpClient.getInstance().get("Game.JinhuaBet", GameHttpConsts.GAME_JINHUA_BET)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .execute(callback);
    }

    /**
     * 游戏结果出来后，观众获取自己赢到的金额
     */
    public static void gameSettle(String gameid, HttpCallback callback) {
        HttpClient.getInstance().get("Game.settleGame", GameHttpConsts.GAME_SETTLE)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("gameid", gameid)
                .execute(callback);
    }



    /**
     * 创建幸运转盘游戏
     */
    public static void gameLuckPanCreate(String stream, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Dial", GameHttpConsts.GAME_LUCK_PAN_CREATE)
                .params("liveuid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("stream", stream)
                .execute(callback);
    }

    /**
     * 幸运转盘游戏下注
     */
    public static void gameLuckPanBet(String gameid, int coin, int grade, HttpCallback callback) {
        HttpClient.getInstance().get("Game.Dial_Bet", GameHttpConsts.GAME_LUCK_PAN_BET)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .execute(callback);
    }



    /**
     * 获取用户余额
     */
    public static void getCoin(HttpCallback callback) {
        HttpClient.getInstance().get("Live.getCoin", GameHttpConsts.GET_COIN)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .execute(callback);
    }

}




