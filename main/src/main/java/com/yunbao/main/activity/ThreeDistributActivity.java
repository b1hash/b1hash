package com.yunbao.main.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.ImageResultCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.mob.MobShareUtil;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.live.dialog.LiveShareDialogFragment;
import com.yunbao.main.R;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by cxf on 2019/4/29.
 * 三级分销
 */

public class ThreeDistributActivity extends AbsActivity implements View.OnClickListener, LiveShareDialogFragment.ActionListener {

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private TextView mInviteCode;//邀请码
    private View mContainer;
    private ImageView mQrCode;//二维码
    private File mShareImageFile;//分享图片文件
    private MobShareUtil mMobShareUtil;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_three_distribut;
    }


    @Override
    protected void main() {
        Intent intent = getIntent();
        setTitle(intent.getStringExtra(Constants.TIP));
        mContainer = findViewById(R.id.share_container);
        ImageView mAppIcon = findViewById(R.id.app_icon);
        TextView mAppName = findViewById(R.id.app_name);
        CommonAppConfig appConfig = CommonAppConfig.getInstance();
        mAppIcon.setImageResource(appConfig.getAppIconRes());
        mAppName.setText(appConfig.getAppName());
        ImageView avatar = findViewById(R.id.avatar);
        TextView name = findViewById(R.id.name);
        TextView idVal = findViewById(R.id.id_val);
        mInviteCode = findViewById(R.id.invite_code);
        mQrCode = findViewById(R.id.qr_code);
        findViewById(R.id.btn_share).setOnClickListener(this);
        UserBean u = appConfig.getUserBean();
        if (u != null) {
            ImgLoader.displayAvatar(mContext, u.getAvatar(), avatar);
            name.setText(u.getUserNiceName());
            idVal.setText(StringUtil.contact("ID:", u.getId()));
        }


        String url = CommonAppConfig.getHtmlUrl(intent.getStringExtra(Constants.URL));
        L.e("H5--->" + url);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mWebView = new WebView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = DpUtil.dp2px(1);
        mWebView.setLayoutParams(params);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        container.addView(mWebView);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("H5-------->" + url);
                if (url.startsWith(Constants.COPY_PREFIX)) {
                    String content = url.substring(Constants.COPY_PREFIX.length());
                    if (!TextUtils.isEmpty(content)) {
                        copy(content);
                    }
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setTitle(view.getTitle());
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                MediaUtil.getImageByAlumb(ThreeDistributActivity.this, false, new ImageResultCallback() {
                    @Override
                    public void beforeCamera() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        filePathCallback.onReceiveValue(new Uri[]{Uri.fromFile(file)});
                    }

                    @Override
                    public void onFailure() {
                        filePathCallback.onReceiveValue(null);
                    }
                });
                return true;
            }

        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mWebView.loadUrl(url);

        MainHttpUtil.getQrCode(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mInviteCode != null) {
                        mInviteCode.setText(obj.getString("code"));
                    }
                    if (mQrCode != null) {
                        ImgLoader.display(mContext, obj.getString("qr"), mQrCode);
                    }
                }
            }
        });

    }


    protected boolean canGoBack() {
        return mWebView != null && mWebView.canGoBack();
    }

    @Override
    public void onBackPressed() {
        if (isNeedExitActivity()) {
            finish();
        } else {
            if (canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
        }
    }


    private boolean isNeedExitActivity() {
        if (mWebView != null) {
            String url = mWebView.getUrl();
            if (!TextUtils.isEmpty(url)) {
                return url.contains("g=appapi&m=Auth&a=success")//身份认证成功页面
                        || url.contains("g=appapi&m=Family&a=home");//家族申请提交成功页面

            }
        }
        return false;
    }


    public static void forward(Context context, String title, String url) {
        Intent intent = new Intent(context, ThreeDistributActivity.class);
        intent.putExtra(Constants.URL, url);
        intent.putExtra(Constants.TIP, title);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.GET_QR_CODE);
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
        }
        super.onDestroy();
    }

    /**
     * 复制到剪贴板
     */
    private void copy(String content) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", content);
        cm.setPrimaryClip(clipData);
        ToastUtil.show(R.string.copy_success);
    }

    /**
     * 生成分享图片
     */
    private void saveBitmapFile() {
        PermissionUtil.request(this, new PermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        if (mContainer == null) {
                            return;
                        }
                        mContainer.setDrawingCacheEnabled(true);
                        Bitmap bitmap = mContainer.getDrawingCache();
                        bitmap = Bitmap.createBitmap(bitmap);
                        mContainer.setDrawingCacheEnabled(false);
                        File dir = new File(CommonAppConfig.IMAGE_PATH);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        String fileName = StringUtil.generateFileName() + ".png";
                        mShareImageFile = new File(dir, fileName);

                        long currentTimeMillis = SystemClock.uptimeMillis();
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
                        ContentResolver contentResolver = CommonAppContext.getInstance().getContentResolver();
                        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        boolean success = false;
                        FileOutputStream fos = null;
                        OutputStream os = null;
                        try {
                            fos = new FileOutputStream(mShareImageFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                            success = true;
                            if (uri != null) {
                                os = contentResolver.openOutputStream(uri);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                                os.flush();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (success) {
                            if (!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                            LiveShareDialogFragment fragment = new LiveShareDialogFragment();
                            fragment.setNoLink(true);
                            fragment.setActionListener(ThreeDistributActivity.this);
                            fragment.show(getSupportFragmentManager(), "LiveShareDialogFragment");
                        }
                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_share) {
            saveBitmapFile();
        }
    }

    @Override
    public void onItemClick(String type) {
        if (mShareImageFile == null || !mShareImageFile.exists()) {
            return;
        }
        if (mMobShareUtil == null) {
            mMobShareUtil = new MobShareUtil();
        }
        File sendFile = new File(CommonAppConfig.DOWNLOAD_PATH + "/" + mShareImageFile.getName());
        if (sendFile.exists() && sendFile.length() > 0) {
            mMobShareUtil.shareImage(mContext, type, sendFile.getAbsolutePath(), null);
        } else {
            sendFile = new File(CommonAppConfig.IMAGE_DOWNLOAD_PATH + "/" + mShareImageFile.getName());
            if (sendFile.exists() && sendFile.length() > 0) {
                mMobShareUtil.shareImage(mContext, type, sendFile.getAbsolutePath(), null);
            } else {
                mMobShareUtil.shareImage(mContext, type, mShareImageFile.getAbsolutePath(), null);
            }
        }
    }
}
