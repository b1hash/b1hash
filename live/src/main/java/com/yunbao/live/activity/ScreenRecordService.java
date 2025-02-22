package com.yunbao.live.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.live.R;

public class ScreenRecordService extends Service {

    private final static String NOTIFICATION_CHANNEL_ID = "com.tencent.liteav.demo.TestService";
    private final static String NOTIFICATION_CHANNEL_NAME = "com.tencent.liteav.demo.channel_name";
    private final static String NOTIFICATION_CHANNEL_DESC = "com.tencent.liteav.demo.channel_desc";

    @Override
    public void onCreate() {
        super.onCreate();
        startNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private void startNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        Intent notificationIntent = new Intent(this, ScreenRecordService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID).setLargeIcon(
                        BitmapFactory.decodeResource(getResources(), CommonAppConfig.getInstance().getAppIconRes()))
                .setSmallIcon(CommonAppConfig.getInstance().getAppIconRes())
                .setContentTitle(CommonAppConfig.getInstance().getAppName())
                .setContentText(WordUtil.getString(R.string.开始录屏))
                .setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(NOTIFICATION_CHANNEL_DESC);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        //必须使用此方法显示通知，不能使用notificationManager.notify，否则还是会报上面的错误
        startForeground(1, notification);
    }

    private class MyBinder extends Binder {

    }
}