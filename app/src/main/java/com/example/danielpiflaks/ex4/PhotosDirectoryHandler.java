package com.example.danielpiflaks.ex4;

import android.Manifest;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotosDirectoryHandler {

    List<File> imagesList;

    public PhotosDirectoryHandler() {
        this.imagesList = new ArrayList<>();

        // Getting the Camera Folder
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        listAllFiles(dcim, this.imagesList);
    }

    private void listAllFiles(File directory, List<File> files) {
        // Get all the files from a directory.
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listAllFiles(file, files);
            }
        }
    }
}
