package com.yunbao.game.util;

import android.util.SparseIntArray;

import com.yunbao.game.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cxf on 2018/10/31.
 */

public class GameIconUtil {
    private static SparseIntArray sGameIconMap;//游戏图标
    private static SparseIntArray sGameNameMap;//游戏名称
    private static Map<String, Integer> sPokerMap;//扑克牌
    private static Map<String, Integer> sJinHuaResult;//炸金花开牌结果
    private static SparseIntArray sZpResult;//转盘结果

    static {
        sGameIconMap = new SparseIntArray();
        sGameIconMap.put(1, R.mipmap.icon_zjh);
        sGameIconMap.put(3, R.mipmap.icon_zp);
        sGameIconMap.put(4, R.mipmap.icon_nz);

        sGameNameMap = new SparseIntArray();
        sGameNameMap.put(1, R.string.game_zjh);
        sGameNameMap.put(3, R.string.game_zp);

        sPokerMap = new HashMap<>();
        sPokerMap.put("1-1", R.mipmap.p1_01);
        sPokerMap.put("1-2", R.mipmap.p1_02);
        sPokerMap.put("1-3", R.mipmap.p1_03);
        sPokerMap.put("1-4", R.mipmap.p1_04);
        sPokerMap.put("1-5", R.mipmap.p1_05);
        sPokerMap.put("1-6", R.mipmap.p1_06);
        sPokerMap.put("1-7", R.mipmap.p1_07);
        sPokerMap.put("1-8", R.mipmap.p1_08);
        sPokerMap.put("1-9", R.mipmap.p1_09);
        sPokerMap.put("1-10", R.mipmap.p1_10);
        sPokerMap.put("1-11", R.mipmap.p1_11);
        sPokerMap.put("1-12", R.mipmap.p1_12);
        sPokerMap.put("1-13", R.mipmap.p1_13);
        sPokerMap.put("1-14", R.mipmap.p1_01);

        sPokerMap.put("2-1", R.mipmap.p2_01);
        sPokerMap.put("2-2", R.mipmap.p2_02);
        sPokerMap.put("2-3", R.mipmap.p2_03);
        sPokerMap.put("2-4", R.mipmap.p2_04);
        sPokerMap.put("2-5", R.mipmap.p2_05);
        sPokerMap.put("2-6", R.mipmap.p2_06);
        sPokerMap.put("2-7", R.mipmap.p2_07);
        sPokerMap.put("2-8", R.mipmap.p2_08);
        sPokerMap.put("2-9", R.mipmap.p2_09);
        sPokerMap.put("2-10", R.mipmap.p2_10);
        sPokerMap.put("2-11", R.mipmap.p2_11);
        sPokerMap.put("2-12", R.mipmap.p2_12);
        sPokerMap.put("2-13", R.mipmap.p2_13);
        sPokerMap.put("2-14", R.mipmap.p2_01);

        sPokerMap.put("3-1", R.mipmap.p3_01);
        sPokerMap.put("3-2", R.mipmap.p3_02);
        sPokerMap.put("3-3", R.mipmap.p3_03);
        sPokerMap.put("3-4", R.mipmap.p3_04);
        sPokerMap.put("3-5", R.mipmap.p3_05);
        sPokerMap.put("3-6", R.mipmap.p3_06);
        sPokerMap.put("3-7", R.mipmap.p3_07);
        sPokerMap.put("3-8", R.mipmap.p3_08);
        sPokerMap.put("3-9", R.mipmap.p3_09);
        sPokerMap.put("3-10", R.mipmap.p3_10);
        sPokerMap.put("3-11", R.mipmap.p3_11);
        sPokerMap.put("3-12", R.mipmap.p3_12);
        sPokerMap.put("3-13", R.mipmap.p3_13);
        sPokerMap.put("3-14", R.mipmap.p3_01);

        sPokerMap.put("4-1", R.mipmap.p4_01);
        sPokerMap.put("4-2", R.mipmap.p4_02);
        sPokerMap.put("4-3", R.mipmap.p4_03);
        sPokerMap.put("4-4", R.mipmap.p4_04);
        sPokerMap.put("4-5", R.mipmap.p4_05);
        sPokerMap.put("4-6", R.mipmap.p4_06);
        sPokerMap.put("4-7", R.mipmap.p4_07);
        sPokerMap.put("4-8", R.mipmap.p4_08);
        sPokerMap.put("4-9", R.mipmap.p4_09);
        sPokerMap.put("4-10", R.mipmap.p4_10);
        sPokerMap.put("4-11", R.mipmap.p4_11);
        sPokerMap.put("4-12", R.mipmap.p4_12);
        sPokerMap.put("4-13", R.mipmap.p4_13);
        sPokerMap.put("4-14", R.mipmap.p4_01);


        sJinHuaResult = new HashMap<>();
        sJinHuaResult.put("1", R.mipmap.icon_zjh_result_dp);//单牌
        sJinHuaResult.put("2", R.mipmap.icon_zjh_result_dz);//对子
        sJinHuaResult.put("3", R.mipmap.icon_zjh_result_sz);//顺子
        sJinHuaResult.put("4", R.mipmap.icon_zjh_result_th);//同花
        sJinHuaResult.put("5", R.mipmap.icon_zjh_result_ths);//同花顺
        sJinHuaResult.put("6", R.mipmap.icon_zjh_result_bz);//豹子


        sZpResult = new SparseIntArray();
        sZpResult.put(1, R.mipmap.icon_zp_1);
        sZpResult.put(2, R.mipmap.icon_zp_2);
        sZpResult.put(3, R.mipmap.icon_zp_3);
        sZpResult.put(4, R.mipmap.icon_zp_4);

    }

    public static int getGameIcon(int key) {
        return sGameIconMap.get(key);
    }

    public static int getGameName(int key) {
        return sGameNameMap.get(key);
    }

    /**
     * 获取扑克牌
     */
    public static int getPoker(String key) {
        return sPokerMap.get(key);
    }

    /**
     * 炸金花游戏的结果
     */
    public static int getJinHuaResult(String key) {
        return sJinHuaResult.get(key);
    }


    /**
     * 转盘的结果
     */
    public static int getLuckPanResult(int key) {
        return sZpResult.get(key);
    }
}
