package com.mikelau.croperino;

public class CroperinoConfig {

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_PICK_FILE = 2;
    public static final int REQUEST_CROP_PHOTO = 3;

    public static String sImageName = "";
    public static String sDirectory = "";
    public static String sRawDirectory = "";

    public CroperinoConfig(String imageName, String directory, String rawDirectory) {
        sImageName = imageName;
        sDirectory = directory;
        sRawDirectory = rawDirectory;
    }

    public static String getsDirectory() {
        return sDirectory;
    }

    public static String getsRawDirectory() {
        return sRawDirectory;
    }

    public static String getsImageName() {
        return sImageName;
    }
}
