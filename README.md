# Croperino

A simple image cropping tool for your android applications (v1.1.8)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Croperino-green.svg?style=popout-square)](https://android-arsenal.com/details/1/4374)
[![Android Arsenal](https://img.shields.io/badge/Twitter-mike14u-blue.svg?style=popout-square)](https://www.twitter.com/mike14u)
[![Android Arsenal](https://img.shields.io/badge/Github-mike14u-ff69b4.svg?style=popout-square)](https://github.com/mike14u)

Supported SDK and Gradle Version:
* Minimum SDK Version 14
* Target SDK Version 28
* Gradle 3.2.0

Features:
* Camera and/or gallery calls.
* Face recoginition
* Cropping of Image based on Scale (Aspect Ratio)
* Customizing button and background
* Performance and compression improvements

![croperino_screenshot1](https://user-images.githubusercontent.com/16832215/36243160-2477012a-125b-11e8-9daf-3eb734e401d0.png =200x500)

## Getting Started

Make sure to have Android Studio

**Gradle**

```
repositories {
    maven { url "https://jitpack.io" }
}
```

```
compile 'com.github.mike14u:croperino:1.1.8'
```

## Usage

**Android Manifest**

```
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

**Sample Usage**

```
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

**onActivityResult**

- Aspect Ratio X = 1 / Y = 1 will produce fixed square view
- Aspect Ratio X = 0 / Y = 0 will produce customizable square view width or height can be customized
- Color and Background Color should be XML format e.g. R.color.gray, place 0 if no change is meant to colors

```
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
        case CroperinoConfig.REQUEST_TAKE_PHOTO:
            if (resultCode == Activity.RESULT_OK) {
                /* Parameters of runCropImage = File, Activity Context, Image is Scalable or Not, Aspect Ratio X, Aspect Ratio Y, Button Bar Color, Background Color */
                Croperino.runCropImage(CroperinoFileUtil.getTempFile(), MainActivity.this, true, 1, 1, R.color.gray, R.color.gray_variant);
            }
            break;
        case CroperinoConfig.REQUEST_PICK_FILE:
            if (resultCode == Activity.RESULT_OK) {
                CroperinoFileUtil.newGalleryFile(data, MainActivity.this);
                Croperino.runCropImage(CroperinoFileUtil.getTempFile(), MainActivity.this, true, 0, 0, R.color.gray, R.color.gray_variant);
            }
            break;
        case CroperinoConfig.REQUEST_CROP_PHOTO:
            if (resultCode == Activity.RESULT_OK) {
                Uri i = Uri.fromFile(CroperinoFileUtil.getTempFile());
                ivMain.setImageURI(i);
                //Do saving / uploading of photo method here.
                //The image file can always be retrieved via CroperinoFileUtil.getTempFile()
            }
            break;
        default:
            break;
    }
}
```

**Proguard**

```
-dontwarn com.mikelau.croperino.**
-keep class com.mikelau.croperino.** { *; }
-keep interface com.mikelau.croperino.** { *; }
```

## Meta

Distributed under the Apache License. See ``LICENSE`` for more information.

## Contributing

1. Fork it (<https://github.com/mike14u/croperino/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Commit your changes (`git commit -am 'Add some fooBar'`)
4. Push to the branch (`git push origin feature/fooBar`)
5. Create a new Pull Request

## License

```
The MIT License (MIT)

Copyright (c) 2016 Mike Lau

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
