package com.example.danielpiflaks.ex4;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TCPClient implements Runnable {
    private String ip;
    private int port;
    private List<File> fileList;
    private NotificationManager nm;
    private NotificationCompat.Builder builder;

    /**
     * Constructor
     *
     * @param ip       IP
     * @param port     Port
     * @param fileList All files to handle.
     */
    public TCPClient(String ip, int port, List<File> fileList) {
        this.ip = ip;
        this.port = port;
        this.fileList = fileList;
    }

    /**
     * Getter
     *
     * @param nm NotificationManager.
     */
    public void setNm(NotificationManager nm) {
        this.nm = nm;
    }

    /**
     * Getter
     *
     * @param builder Builder.
     */
    public void setBuilder(NotificationCompat.Builder builder) {
        this.builder = builder;
    }

    /**
     * Sends all files to image server.
     *
     * @param fileList
     * @param socket
     */
    public void SendFiles(List<File> fileList, Socket socket) {
        int barState = 0;
        final int notify_id = 1;
        //Get file size.
        int numberOfFiles = fileList.size();
        try {
            //Get stream.
            OutputStream output = socket.getOutputStream();
            //For each file.
            for (File file : fileList) {
                //Get file name in bytes array form.
                byte[] fileNameBytes = file.getName().getBytes();
                //Sends message with size of message in bytes.
                sendMessageWithSize(fileNameBytes, output);
                //Get file input stream.
                FileInputStream fis = new FileInputStream(file);
                //Decode stream to bitmap.
                Bitmap bm = BitmapFactory.decodeStream(fis);
                //Get image in bytes array.
                byte[] imgbyte = getBytesFromBitmap(bm);
                //Send image.
                sendMessageWithSize(imgbyte, output);
                //Update progress bar..
                barState = barState + 100 / numberOfFiles;
                this.builder.setProgress(100, barState, false);
                this.nm.notify(notify_id, builder.build());
            }
            output.flush();
            //Set progress bar to end.
            this.builder.setProgress(0, 0, false);
            builder.setContentText("Transfer Complete");
            this.nm.notify(notify_id, this.builder.build());
        } catch (Exception e) {
            Log.e("TCP", "S: Error", e);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            }
        }
    }

    /**
     * Send message to server with size of message before.
     * @param bytesArray Bytes array to send.
     * @param output Stream to send from.
     */
    public void sendMessageWithSize(byte[] bytesArray, OutputStream output) {
        byte[] messageSize = toByteArray(bytesArray.length);
        try {
            output.write(messageSize);
            output.write(bytesArray);
        } catch (Exception e) {
            Log.e("TCP", "S: Error", e);
        }
    }

    /**
     * Converts to bytes array.
     * @param value value to convert.
     * @return Bytes array.
     */
    byte[] toByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value};
    }

    /**
     * Gets bytes from bitmap.
     * @param bitmap
     * @return Bytes array.
     */
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    @Override
    /**
     * Override from interface runnable.
     */
    public void run() {
        try {
            InetAddress serverAddr = InetAddress.getByName(this.ip);
            Socket socket = new Socket(serverAddr, this.port);
            SendFiles(this.fileList, socket);
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }
}
