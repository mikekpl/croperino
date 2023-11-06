package com.mikelau.croperino

class CroperinoConfig(imageName: String, directory: String, rawDirectory: String) {

    init {
        sImageName = imageName
        sDirectory = directory
        sRawDirectory = rawDirectory
    }

    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_PICK_FILE = 2
        const val REQUEST_CROP_PHOTO = 3
        var sImageName = ""
        var sDirectory = ""
        var sRawDirectory = ""

        fun getsDirectory(): String = sDirectory
        fun getsRawDirectory(): String = sRawDirectory
        fun getsImageName(): String = sImageName
    }
}
