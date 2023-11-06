# Croperino

> A simple image cropping tool for your android applications (v2.0.0)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Croperino-blue.svg?style=popout-square)](https://android-arsenal.com/details/1/4374)

Supported SDK and Gradle Version:
* Minimum SDK Version 24
* Gradle 8.4

Features:
* Camera and/or gallery calls.
* Face recognition
* Cropping of Image based on Scale (Aspect Ratio)
* Customizing button and background
* Performance and compression improvements
* Removed permission support (still has issues and different per Device OS)
* Chooser capability is no longer recommended

<img src="https://user-images.githubusercontent.com/16832215/36243160-2477012a-125b-11e8-9daf-3eb734e401d0.png" width="200" height="400">

## Getting Started

Make sure to have Android Studio

**Gradle**

In your root build.gradle, add at the end of the repositories:

```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

If you are using newer Android Studio Project structure that is using build.gradle.kts, you can instead declare inside your settings.gradle.kts:

```
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
      google()
      mavenCentral()
      maven (url = "https://jitpack.io")
    }
}
```

Add the dependency

```
implementation 'com.github.mikekpl:croperino:2.0.0'
```

## Usage

**Permissions**

IMPORTANT!! Please manage permissions accordingly (the library's permission management is bugged at the moment)
* Camera permission is still being checked, but it would be better if you're able to handle permission prompts
* Gallery access should have read and write access as well as ACTION_PICK capability

**Sample Usage**

Initialization:
```
CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/MikeLau/Pictures", "/sdcard/MikeLau/Pictures")
CroperinoFileUtil.setupDirectory(this)
```

Make sure corresponding permissions are already granted prior to calling these functions:
```
// Prepare Camera - Make sure Camera permission is already allowed otherwise it won't work
Croperino.prepareCamera(this)

// Prepare Gallery - Make sure Read / Write / Manage External Storage is already allowed otherwise it won't work
Croperino.prepareGallery(this)
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

## Contributing

1. Fork it (<https://github.com/mikekpl/croperino/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Commit your changes (`git commit -am 'Add some fooBar'`)
4. Push to the branch (`git push origin feature/fooBar`)
5. Create a new Pull Request

## License

```
The MIT License (MIT)

Copyright (c) 2013 Mike Lau

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
