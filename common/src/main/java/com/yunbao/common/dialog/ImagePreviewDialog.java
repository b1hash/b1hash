package com.yunbao.common.dialog;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.R;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.ImagePreviewAdapter;
import com.yunbao.common.http.UriDownloadCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DownloadUtil;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;

/**
 * Created by cxf on 2018/11/28.
 * 图片预览弹窗
 */

public class ImagePreviewDialog extends AbsDialogFragment implements View.OnClickListener {

    private View mBg;
    private RecyclerView mRecyclerView;
    private ValueAnimator mAnimator;
    private int mPosition;
    private int mPageCount;
    private ActionListener mActionListener;
    private TextView mCount;
    private ImagePreviewAdapter mAdapter;
    private boolean mNeedDelete;

    @Override
    protected int getLayoutId() {
        return R.layout.view_preview_image;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBg = mRootView.findViewById(R.id.bg);
        mCount = findViewById(R.id.count);
        findViewById(R.id.btn_close).setOnClickListener(this);
        if (mNeedDelete) {
            View btnDelete = findViewById(R.id.btn_delete);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(this);
        }
        mRecyclerView = mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(150);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                mBg.setAlpha(v);
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mRecyclerView != null && mPageCount > 0) {
                    ImagePreviewAdapter adapter = new ImagePreviewAdapter(mContext, mPageCount);
                    mAdapter = adapter;
                    adapter.setActionListener(new ImagePreviewAdapter.ActionListener() {

                        @Override
                        public void onPageChanged(int position) {
                            if (mCount != null) {
                                mCount.setText(StringUtil.contact(String.valueOf(position + 1), "/", String.valueOf(mPageCount)));
                            }
                        }

                        @Override
                        public void loadImage(ImageView imageView, int position) {
                            if (mActionListener != null) {
                                mActionListener.loadImage(imageView, position);
                            }
                        }

                        @Override
                        public void saveImage(int position) {
                            if (mActionListener != null) {
                                final String imgUrl = mActionListener.getImageUrl(position);
                                if (TextUtils.isEmpty(imgUrl) || mContext == null) {
                                    return;
                                }
                                DialogUitl.showStringArrayDialog(mContext, new Integer[]{R.string.save_image_album}, new DialogUitl.StringArrayDialogCallback() {
                                    @Override
                                    public void onItemClick(String text, int tag) {
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
                                                        new DownloadUtil("save_img").download(uri, imgUrl, new UriDownloadCallback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                ToastUtil.show(R.string.save_success);
                                                            }
                                                        });
                                                    }
                                                }, Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                    }
                                });
                            }
                        }
                    });
                    mRecyclerView.setAdapter(adapter);
                    if (mPosition >= 0 && mPosition < mPageCount) {
                        adapter.setCurPosition(mPosition);
                        mRecyclerView.scrollToPosition(mPosition);
                    }
                }
            }
        });
        mAnimator.start();
    }

    public void setImageInfo(int pageCount, int position, boolean needDelete, ActionListener actionListener) {
        mActionListener = actionListener;
        mPageCount = pageCount;
        mPosition = position;
        mNeedDelete = needDelete;
    }


    @Override
    public void onDestroy() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mContext = null;
        mActionListener = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_close) {
            dismiss();
        } else if (i == R.id.btn_delete) {
            delete();
        }
    }

    private void delete() {
        if (mAdapter != null && mActionListener != null) {
            mActionListener.onDeleteClick(mAdapter.getCurPosition());
        }
        dismiss();
    }


    public interface ActionListener {
        void loadImage(ImageView imageView, int position);

        void onDeleteClick(int position);

        String getImageUrl(int position);
    }

}
