package com.example.danielpiflaks.ex4;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import android.widget.Toast;

public class ImageService extends Service {
    private BroadcastReceiver yourReceiver;
    private TCPClient tcpClient;
    private PhotosDirectoryHandler photosDirectoryHandler;

    @Override
    /*
     * On create of service.
     */
    public void onCreate() {
        super.onCreate();
    }

    @Override
    /*
     * On start command of service.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Set message that service is starting.
        Toast.makeText(this, "Service starting...", Toast.LENGTH_SHORT).show();
        //Create photo directory handler.
        this.photosDirectoryHandler = new PhotosDirectoryHandler();
        //Create tcp channel with wanted port and IP.
        this.tcpClient = new TCPClient("10.0.2.2", 1102, this.photosDirectoryHandler.imagesList);
        //Start the broadcast event.
        broadcastOnEvent();
        return START_STICKY;
    }

    @Override
    /**
     * On destroy command.
     */
    public void onDestroy() {
        //Mark that service ended.
        Toast.makeText(this, "Service ending...", Toast.LENGTH_SHORT).show();
        //Unregister receiver.
        this.unregisterReceiver(this.yourReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Broadcast on event.
     */
    public void broadcastOnEvent() {
        final IntentFilter theFilter = new IntentFilter();
        //Set filters.
        theFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        theFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.yourReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        //get the different network states
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            //If there is wifi then start transfer.
                            startTransfer(context);            // Starting the Transfer
                        }
                    }
                }
            }
        };
        //Register this receiver.
        registerReceiver(this.yourReceiver, theFilter);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    /**
     * Start the transfer.
     */
    public void startTransfer(Context context) {
        //Create progress bar.
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        final NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default", "Progress bar", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Progress bar for image transfer");
        NM.createNotificationChannel(channel);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        //Progress bar title.
        builder.setContentTitle("Image transfer");
        builder.setContentText("Transferring in progress...");
        //Give tcp client nm and builder so it will use it will transferring.
        this.tcpClient.setNm(NM);
        this.tcpClient.setBuilder(builder);
        Thread thread = new Thread(this.tcpClient);
        //Start run function of tcpclient.
        thread.start();
    }
}
