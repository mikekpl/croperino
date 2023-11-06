package com.mikelau.croperinosample

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.checkPermissions
import com.lorenzofelletti.permissions.dispatcher.dsl.doOnDenied
import com.lorenzofelletti.permissions.dispatcher.dsl.doOnGranted
import com.lorenzofelletti.permissions.dispatcher.dsl.showRationaleDialog
import com.lorenzofelletti.permissions.dispatcher.dsl.withRequestCode
import com.mikelau.croperino.Croperino
import com.mikelau.croperino.CroperinoConfig
import com.mikelau.croperino.CroperinoFileUtil

class MainActivity : AppCompatActivity() {

    lateinit var btnCamera: Button
    lateinit var btnGallery: Button
    lateinit var ivMain: ImageView
    lateinit var permissionManager: PermissionManager

    companion object {
        private const val POSITION_REQUEST_CODE = 1
        private val POSITION_REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnCamera = findViewById(R.id.btn_camera)
        btnGallery = findViewById(R.id.btn_gallery)
        ivMain = findViewById(R.id.iv_main)

        permissionManager = PermissionManager(this)

        permissionManager buildRequestResultsDispatcher {
            withRequestCode(POSITION_REQUEST_CODE) {
                checkPermissions(POSITION_REQUIRED_PERMISSIONS)
                showRationaleDialog(message = "Please accept all permission to use Croperino Library")
                doOnGranted {
                }
                doOnDenied {
                }
            }
        }

        permissionManager checkRequestAndDispatch POSITION_REQUEST_CODE
        permissionManager.checkRequestAndDispatch(
            POSITION_REQUEST_CODE
        )

        CroperinoConfig(
            "IMG_" + System.currentTimeMillis() + ".jpg",
            "/MikeLau/Pictures/",
            "/sdcard/MikeLau/Pictures/"
        )
        CroperinoFileUtil.setupDirectory(this)

        btnCamera.setOnClickListener {
            Croperino.prepareCamera(this)
        }

        btnGallery.setOnClickListener {
            Croperino.prepareGallery(this)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CroperinoConfig.REQUEST_TAKE_PHOTO -> if (resultCode == RESULT_OK) {
                Croperino.runCropImage(CroperinoFileUtil.tempFile!!, this, true, 1, 1,
                    com.mikelau.croperino.R.color.gray,
                    com.mikelau.croperino.R.color.gray_variant
                )
            }

            CroperinoConfig.REQUEST_PICK_FILE -> if (resultCode == RESULT_OK) {
                CroperinoFileUtil.newGalleryFile(data!!, this@MainActivity)
                Croperino.runCropImage(
                    CroperinoFileUtil.tempFile!!,
                    this@MainActivity,
                    true,
                    1,
                    1,
                    com.mikelau.croperino.R.color.gray,
                    com.mikelau.croperino.R.color.gray_variant
                )
            }

            CroperinoConfig.REQUEST_CROP_PHOTO -> if (resultCode == RESULT_OK) {
                val i = Uri.fromFile(CroperinoFileUtil.tempFile)
                ivMain.setImageURI(i)
            }

            else -> {}
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.dispatchOnRequestPermissionsResult(requestCode, grantResults)
    }
}
