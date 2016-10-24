Croperino
=========

Simple image cropping tool derived from [Crop Image](https://github.com/biokys/cropimage)

Features:
* Camera or Gallery calls.
* Facial Recoginition
* Cropping of Image based on Scale (Aspect Ratio)
* Customizing button and background
* Performance and compression improvments


![device-2016-09-15-163009](https://cloud.githubusercontent.com/assets/16832215/18544278/855d9aae-7b66-11e6-8236-ba1bc89a8e44.png)

Gradle

```java
	repositories {
    	maven { url "https://jitpack.io" }
    }
```

```java
	compile 'com.github.ekimual:croperino:1.0.2'
```

Make sure to have this in your manifest

```java
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
Sample Usage

```java
	//Initialize on every usage
	new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/MikeLau/Pictures", "/sdcard/MikeLau/Pictures");
        CroperinoFileUtil.verifyStoragePermissions(MainActivity.this);
        CroperinoFileUtil.setupDirectory(MainActivity.this);

	//Prepare Chooser (Gallery or Camera)
	Croperino.prepareChooser(MainActivity.this, "Capture photo...", ContextCompat.getColor(MainActivity.this, android.R.color.background_dark));
	 
	//Prepare Camera
	try {
	    Croperino.prepareCamera(MainActivity.this);
	} catch(Exception e) { 
	    e.printStackTrace;
	}
	
	//Prepare Gallery
	Croperino.prepareGallery(MainActivity.this);
	
```

onActivityResult

```java
	@Override
    	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CroperinoConfig.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    /* Parameters of runCropImage = File, Activity Context, Image is Scalable or Not, Aspect Ratio X, Aspect Ratio Y, Button Bar Color, Background Color */
                    Croperino.runCropImage(CroperinoFileUtil.getmFileTemp(), MainActivity.this, true, 1, 1, 0, 0);
                }
                break;
            case CroperinoConfig.REQUEST_PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    CroperinoFileUtil.newGalleryFile(data, MainActivity.this);
                    Croperino.runCropImage(CroperinoFileUtil.getmFileTemp(), MainActivity.this, true, 1, 1, 0, 0);
                }
                break;
            case CroperinoConfig.REQUEST_CROP_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri i = Uri.fromFile(CroperinoFileUtil.getmFileTemp());
                    ivMain.setImageURI(i);
                    //Do saving / uploading of photo method here.
                    //The image file can always be retrieved via CroperinoFileUtil.getmFileTemp()
                }
                break;
            default:
                break;
        }
    }
	
```

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Croperino-green.svg?style=true)](https://android-arsenal.com/details/1/4374)
