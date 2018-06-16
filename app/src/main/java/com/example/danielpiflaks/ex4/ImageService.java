package com.example.danielpiflaks.ex4;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class ImageService extends Service {
    private BroadcastReceiver yourReceiver;
    private TCPClient tcpClient;
    private  PhotosDirectoryHandler photosDirectoryHandler;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service starting...", Toast.LENGTH_SHORT).show();

        this.photosDirectoryHandler = new PhotosDirectoryHandler();
        this.tcpClient = new TCPClient("10.0.2.2", 1102, this.photosDirectoryHandler.imagesList);
        broadcastOnEvent();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this,"Service ending...", Toast.LENGTH_SHORT).show();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void broadcastOnEvent(){
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        theFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.yourReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        //get the different network states
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            startTransfer();            // Starting the Transfer
                        }
                    }
                }
            }
        };
        registerReceiver(this.yourReceiver, theFilter);
    }


    public void startTransfer(){
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        final int notify_id = 1;
        final NotificationManager NM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Download Status...");
        builder.setContentText("Download in progress");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int icr;
                for (icr = 0; icr <=100; icr+=5){
                    builder.setProgress(100, icr, false);
                    NM.notify(notify_id, builder.build());
                    try {
                        Thread.sleep(2*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                builder.setProgress(0, 0, false);
                builder.setContentText("Download Complete...");
                NM.notify(notify_id, builder.build());
            }
        }).start();
        Thread thread = new Thread(this.tcpClient);
        thread.start();
    }
}
