package com.yunbao.im.adapter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.ClickUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.im.R;
import com.yunbao.im.bean.ImMessageBean;
import com.yunbao.im.custom.MyImageView2;
import com.yunbao.im.utils.ImMessageUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by cxf on 2018/11/28.
 */

public class ChatImagePreviewAdapter extends RecyclerView.Adapter<ChatImagePreviewAdapter.Vh> {

    private Context mContext;
    private List<ImMessageBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mLongClickListener;
    private ActionListener mActionListener;

    public ChatImagePreviewAdapter(Context context, List<ImMessageBean> list) {
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ClickUtil.canClick()) {
                    return;
                }
                if (mActionListener != null) {
                    mActionListener.onImageClick();
                }
            }
        };
        mLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImMessageBean bean = (ImMessageBean) v.getTag(R.string.a_001);
                ImMessageUtil.getInstance().getImageFile(bean, false, new CommonCallback<File>() {
                    @Override
                    public void callback(final File imageFile) {
                        DialogUitl.showStringArrayDialog(mContext, new Integer[]{R.string.save_image_album}, new DialogUitl.StringArrayDialogCallback() {
                            @Override
                            public void onItemClick(String text, int tag) {
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
                                ContentResolver contentResolver = CommonAppContext.getInstance().getContentResolver();
                                Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                if (uri == null) {
                                    return;
                                }
                                FileInputStream inputStream = null;
                                OutputStream outputStream = null;
                                try {
                                    inputStream = new FileInputStream(imageFile);
                                    outputStream = contentResolver.openOutputStream(uri);
                                    byte[] buf = new byte[4096];
                                    int len = 0;
                                    while ((len = inputStream.read(buf)) > 0) {
                                        outputStream.write(buf, 0, len);
                                    }
                                    ToastUtil.show(R.string.save_success);
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
                            }
                        });
                    }
                });
                return true;
            }
        };
    }


    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_im_chat_img, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
        vh.setData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerView);
    }

    class Vh extends RecyclerView.ViewHolder {

        MyImageView2 mImg;


        public Vh(View itemView) {
            super(itemView);
            mImg = (MyImageView2) itemView;
            mImg.setOnClickListener(mOnClickListener);
            mImg.setOnLongClickListener(mLongClickListener);
        }

        void setData(ImMessageBean bean) {
            mImg.setTag(R.string.a_001, bean);
            ImMessageBean.ImageMsgBean imageMsgBean = bean.getImageBean();
            if (imageMsgBean != null) {
                String localPath = imageMsgBean.getLocalPath();
                if (!TextUtils.isEmpty(localPath)) {
                    ImgLoader.display(mContext, new File(localPath), mImg);
                } else {
                    ImgLoader.display(mContext, imageMsgBean.getOriginImageUrl(), mImg);
                }
            }

        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void onImageClick();
    }
}
