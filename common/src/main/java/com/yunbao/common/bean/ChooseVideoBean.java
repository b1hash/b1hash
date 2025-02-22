package com.yunbao.common.bean;

import android.net.Uri;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.utils.MD5Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cxf on 2018/6/20.
 * 选择视频的实体类
 */

public class ChooseVideoBean {

    public static final int CAMERA = 0;
    public static final int FILE = 1;

    private final Uri videoUri;
    private long duration;
    private String durationString;
    private final int mType;


    public ChooseVideoBean(int type, Uri uri) {
        mType = type;
        videoUri = uri;
    }

    public File getVideoFile() {
        File dir = new File(CommonAppConfig.VIDEO_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File target = new File(dir, MD5Util.getMD5(videoUri.toString()) + ".mp4");
        if (target.exists() && target.length() > 0) {
            return target;
        }
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = CommonAppContext.getInstance().getContentResolver().openInputStream(videoUri);
            outputStream = new FileOutputStream(target);
            byte[] buf = new byte[4096];
            int len = 0;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return target;
    }

    public Uri getVideoUri() {
        return this.videoUri;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDurationString() {
        return durationString;
    }

    public void setDurationString(String durationString) {
        this.durationString = durationString;
    }


    public int getType() {
        return mType;
    }
}
