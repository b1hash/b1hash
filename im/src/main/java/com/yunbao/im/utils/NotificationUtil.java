package com.yunbao.im.utils;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationManagerCompat;

import com.yunbao.common.CommonAppContext;
import com.yunbao.common.R;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.WordUtil;


public class NotificationUtil {


//    public static void sendNotification(String title, String content, Intent intent) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(CommonAppContext.getInstance(), "default")
//                .setContentTitle(title)
//                .setContentText(content)
//                .setSmallIcon(CommonAppConfig.getInstance().getAppIconRes())
//                .setAutoCancel(true);
//
//        if (intent != null) {
//            PendingIntent pendingIntent = PendingIntent.getActivity(CommonAppContext.getInstance(), 1, intent,
//                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
//            builder.setContentIntent(pendingIntent);
//        }
//        Notification notification = builder.build();
//        notification.defaults = Notification.DEFAULT_ALL;
//        notification.priority = NotificationCompat.PRIORITY_HIGH;
//        NotificationManager manager = (NotificationManager) CommonAppContext.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel("default", "default", NotificationManager.IMPORTANCE_HIGH);
//            manager.createNotificationChannel(notificationChannel);
//        }
//        manager.notify(1, notification);
//    }

    /**
     * 检查是否开启通知权限
     */
    public static void checkNotificationsEnabled(Context context) {
        boolean isOpened = NotificationManagerCompat.from(CommonAppContext.getInstance()).areNotificationsEnabled();
        if (!isOpened) {
            DialogUitl.showSimpleDialog(context, WordUtil.getString(R.string.open_notification),true,  new DialogUitl.SimpleCallback() {
                @Override
                public void onConfirmClick(Dialog dialog, String content) {
                    try {
                        // 根据isOpened结果，判断是否需要提醒用户跳转AppInfo页面，去打开App通知权限
                        Intent intent = new Intent();
                        // 小米6 -MIUI9.6-8.0.0系统，是个特例，通知设置界面只能控制"允许使用通知圆点"
                        if ("MI 6".equals(Build.MODEL)) {
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", CommonAppContext.getInstance().getPackageName(), null);
                            intent.setData(uri);
                        } else {
                            //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                            if (Build.VERSION.SDK_INT >= 26) {
                                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                intent.putExtra(Settings.EXTRA_APP_PACKAGE, CommonAppContext.getInstance().getPackageName());
                                intent.putExtra(Settings.EXTRA_CHANNEL_ID, CommonAppContext.getInstance().getApplicationInfo().uid);
                            } else {
                                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                                intent.putExtra("app_package", CommonAppContext.getInstance().getPackageName());
                                intent.putExtra("app_uid", CommonAppContext.getInstance().getApplicationInfo().uid);
                            }
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        CommonAppContext.getInstance().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
                        Intent intent = new Intent();
                        //下面这种方案是直接跳转到当前应用的设置界面。
                        //https://blog.csdn.net/ysy950803/article/details/71910806
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", CommonAppContext.getInstance().getPackageName(), null);
                        intent.setData(uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        CommonAppContext.getInstance().startActivity(intent);
                    }
                }
            });
        }
    }
}