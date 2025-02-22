package com.yunbao.live.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.event.UpdateFieldEvent;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.http.UriDownloadCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.ImageResultCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.upload.UploadBean;
import com.yunbao.common.upload.UploadCallback;
import com.yunbao.common.upload.UploadStrategy;
import com.yunbao.common.upload.UploadUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DownloadUtil;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.live.R;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 云豹科技 on 2022/6/6.
 */
public class UserHomeBgActivity extends AbsActivity implements View.OnClickListener {

    private ImageView mImg;
    private String mImgUrl;

    private ImageResultCallback mImageResultCallback = new ImageResultCallback() {
        @Override
        public void beforeCamera() {

        }

        @Override
        public void onSuccess(File file) {
            if (file != null && file.exists()) {
                ImgLoader.display(mContext, file, mImg);

                final List<UploadBean> list = new ArrayList<>();
                list.add(new UploadBean(file, UploadBean.IMG));

                final Dialog loading = DialogUitl.loadingDialog(mContext);
                loading.show();

                UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
                    @Override
                    public void callback(UploadStrategy uploadStrategy) {
                        uploadStrategy.upload(list, true, new UploadCallback() {
                            @Override
                            public void onFinish(List<UploadBean> list, boolean success) {
                                if (success) {
                                    LiveHttpUtil.setUserHomeBgImg(list.get(0).getRemoteFileName(), new HttpCallback() {
                                        @Override
                                        public void onSuccess(int code, String msg, String[] info) {
                                            if (code == 0) {
                                                if (info.length > 0) {
                                                    mImgUrl = JSON.parseObject(info[0]).getString("bg_img");
                                                }
                                                EventBus.getDefault().post(new UpdateFieldEvent());
                                            }
                                            ToastUtil.show(msg);
                                        }

                                        @Override
                                        public void onFinish() {
                                            loading.dismiss();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });


            } else {
                ToastUtil.show(R.string.file_not_exist);
            }
        }


        @Override
        public void onFailure() {
        }
    };

    public static void forward(Context context, String url, boolean self) {
        Intent intent = new Intent(context, UserHomeBgActivity.class);
        intent.putExtra(Constants.URL, url);
        intent.putExtra("self", self);
        context.startActivity(intent);
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_home_bg;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        boolean self = intent.getBooleanExtra("self", false);
        if (self) {
            findViewById(R.id.btn_choose).setOnClickListener(this);
            findViewById(R.id.btn_download).setOnClickListener(this);
        } else {
            findViewById(R.id.btn_choose).setVisibility(View.INVISIBLE);
            findViewById(R.id.btn_download).setVisibility(View.INVISIBLE);
        }
        mImg = findViewById(R.id.img);
        mImg.setOnClickListener(this);
        mImgUrl = intent.getStringExtra(Constants.URL);
        ImgLoader.display(mContext, mImgUrl, mImg);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_choose) {
            chooseImage();
        } else if (i == R.id.btn_download) {
            download();
        } else if (i == R.id.img) {
            finish();
        }
    }

    /**
     * 选择图片
     */
    private void chooseImage() {
        DialogUitl.showStringArrayDialog(mContext, new Integer[]{R.string.alumb, R.string.camera}, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == R.string.camera) {
                    MediaUtil.getImageByCamera(UserHomeBgActivity.this, false, mImageResultCallback);
                } else if (tag == R.string.alumb) {
                    MediaUtil.getImageByAlumb(UserHomeBgActivity.this, false, mImageResultCallback);
                }
            }
        });
    }

    private void download() {
        PermissionUtil.request((AbsActivity) mContext,
                new PermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        long currentTimeMillis = SystemClock.uptimeMillis();
                        String fileName = StringUtil.generateFileName() + ".png";
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                        values.put(MediaStore.MediaColumns.TITLE, fileName);
                        values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
                        values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
                        } else {
                            values.put(MediaStore.MediaColumns.DATA, CommonAppConfig.IMAGE_DOWNLOAD_PATH + fileName);
                        }
                        Uri uri = CommonAppContext.getInstance().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        new DownloadUtil("save_img").download(uri, mImgUrl, new UriDownloadCallback() {
                            @Override
                            public void onSuccess() {
                                ToastUtil.show(R.string.save_success);
                            }
                        });
                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.SET_USER_HOME_BG_IMG);
        super.onDestroy();
    }
}
