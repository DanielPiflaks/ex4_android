package com.example.danielpiflaks.ex4;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
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

    public TCPClient(String ip, int port, List<File> fileList) {
        this.ip = ip;
        this.port = port;
        this.fileList = fileList;
    }

    public void SendFiles(List<File> fileList, Socket socket) {
        try {
            //sends the message to the server
            OutputStream output = socket.getOutputStream();
            for (File file : fileList) {
                if (file.isFile()) {
                    byte[] fileNameBytes = file.getName().getBytes();
                    sendMessageWithSize(fileNameBytes, output);
                    FileInputStream fis = new FileInputStream(file);
                    Bitmap bm = BitmapFactory.decodeStream(fis);
                    byte[] imgbyte = getBytesFromBitmap(bm);
                    sendMessageWithSize(imgbyte, output);
                }
            }
            output.flush();
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

    public void sendMessageWithSize(byte[] bytesArray, OutputStream output){
        byte[] messageSize = toByteArray(bytesArray.length);
        try {
            output.write(messageSize);
            output.write(bytesArray);
        } catch (Exception e){
            Log.e("TCP", "S: Error", e);
        }
    }

    byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    @Override
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
