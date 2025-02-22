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

public class ChooseImageBean {
    public static final int CAMERA = 0;
    public static final int FILE = 1;

    private final Uri mImageUri;
    private final int mType;
    private boolean mChecked;

    public ChooseImageBean(int type, Uri uri) {
        mType = type;
        mImageUri = uri;
    }

    public int getType() {
        return mType;
    }

    public Uri getImageUri() {
        return mImageUri;
    }

    public File getImageFile() {
        File dir = new File(CommonAppConfig.IMAGE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File target = new File(dir, MD5Util.getMD5(mImageUri.toString())+".png");
        if (target.exists() && target.length() > 0) {
            return target;
        }
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = CommonAppContext.getInstance().getContentResolver().openInputStream(mImageUri);
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

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

}
