package com.example.danielpiflaks.ex4;

import android.Manifest;
import android.media.Image;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotosDirectoryHandler {

    List<File> imagesList;

    /**
     * Constructor.
     */
    public PhotosDirectoryHandler() {
        //Create new array list.
        this.imagesList = new ArrayList<>();

        //Getting the Camera Folder
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //Get all images from DCIM.
        listAllFiles(dcim, this.imagesList);
    }

    /**
     * Puts all images from given directory into list.
     * @param directory Wanted directory to scan.
     * @param files List to add to.
     */
    private void listAllFiles(File directory, List<File> files) {
        // Get all the files from a directory.
        File[] fList = directory.listFiles();
        for (File file : fList) {
            //Add entry if it's a file and it's image.
            if (file.isFile()) {
                String extention = getFileExtension(file);
                if (extention.equals("jpg") || (extention.equals("bmp") || extention.equals("jpeg")))
                    files.add(file);
            } else if (file.isDirectory()) {
                listAllFiles(file, files);
            }
        }
    }

    /**
     * Gets files extension.
     * @param file File to get it's extension.
     * @return Extension.
     */
    private String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}
