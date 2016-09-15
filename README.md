The MIT License (MIT)

Copyright (c) 2012 Jan Muller

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Croperino
=========

Simple image cropping tool derived from Jan Muler's Crop Image with improvments and simplifications.

![device-2016-09-15-163009](https://cloud.githubusercontent.com/assets/16832215/18544278/855d9aae-7b66-11e6-8236-ba1bc89a8e44.png)

Gradle

```java
	repositories {
    	maven { url "https://jitpack.io" }
    }
```

```java
	compile 'com.github.ekimual:croperino:1.0.0'
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
