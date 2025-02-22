package com.yunbao.common.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.yunbao.common.CommonAppContext;
import com.yunbao.common.bean.ChooseImageBean;
import com.yunbao.common.interfaces.CommonCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/6/20.
 */

public class ChooseImageUtil {

    private ContentResolver mContentResolver;
    private CommonCallback<List<ChooseImageBean>> mCallback;
    private boolean mStop;

    public ChooseImageUtil() {
        mContentResolver = CommonAppContext.getInstance().getContentResolver();
    }

    public void getLocalImageList(CommonCallback<List<ChooseImageBean>> callback) {
        if (callback == null) {
            return;
        }
        mCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    final List<ChooseImageBean> imageList = getAllImage();
                    CommonAppContext.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                mCallback.callback(imageList);
                            }
                        }
                    });
                }
            }
        }).start();
    }


    private List<ChooseImageBean> getAllImage() {
        List<ChooseImageBean> imageList = new ArrayList<>();
        Cursor cursor = null;
        try {
            //只查询jpeg和png的图片
            cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    "mime_type=? or mime_type=?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED + " desc");
            if (cursor != null) {
                while (!mStop && cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    if (id <= 0) {
                        continue;
                    }
                    Uri uri= ContentUris.withAppendedId( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    imageList.add(new ChooseImageBean(ChooseImageBean.FILE, uri));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imageList;
    }


    public void release() {
        mStop = true;
        mCallback = null;
    }


}
