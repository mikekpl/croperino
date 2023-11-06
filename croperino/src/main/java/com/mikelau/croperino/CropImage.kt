package com.mikelau.croperino

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.media.FaceDetector
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Parcelable
import android.os.StatFs
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import com.mikelau.croperino.BitmapManager.Companion.instance
import com.mikelau.croperino.BitmapManager.ThreadSet
import com.mikelau.croperino.CropUtil.closeSilently
import com.mikelau.croperino.CropUtil.getOrientationInDegree
import com.mikelau.croperino.CropUtil.rotateImage
import com.mikelau.croperino.CropUtil.startBackgroundJob
import com.mikelau.croperino.CropUtil.transform
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class CropImage : MonitoredActivity() {
    val IMAGE_MAX_SIZE = 1024
    private val mDoFaceDetection = true
    private var mCircleCrop = false
    private var mScaleUp = true
    private val mOutputFormat = CompressFormat.JPEG
    private val mHandler = Handler()
    private val mDecodingThreads = ThreadSet()
    private var mAspectX = 0
    private var mAspectY = 0
    private var mOutputX = 0
    private var mOutputY = 0
    private var mScale = false
    private var mImageView: CropImageView? = null
    private var mContentResolver: ContentResolver? = null
    private var mBitmap: Bitmap? = null
    private var mImagePath: String? = null
    var mWaitingToPick = false
    var mSaving = false
    var mCrop: HighlightView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContentResolver = contentResolver
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.cropimage)
        mImageView = findViewById(R.id.image)
        showStorageToast(this)
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            if (extras.getString(CIRCLE_CROP) != null) {
                mImageView?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                mCircleCrop = true
                mAspectX = 1
                mAspectY = 1
            }
            mImagePath = extras.getString(IMAGE_PATH)
            mSaveUri = getImageUri(mImagePath)
            mBitmap = getBitmap(mImagePath)
            mAspectX = if (extras.containsKey(ASPECT_X) && extras[ASPECT_X] is Int) {
                extras.getInt(ASPECT_X)
            } else {
                throw IllegalArgumentException("aspect_x must be integer")
            }
            mAspectY = if (extras.containsKey(ASPECT_Y) && extras[ASPECT_Y] is Int) {
                extras.getInt(ASPECT_Y)
            } else {
                throw IllegalArgumentException("aspect_y must be integer")
            }
            mOutputX = extras.getInt(OUTPUT_X)
            mOutputY = extras.getInt(OUTPUT_Y)
            mScale = extras.getBoolean(SCALE, true)
            mScaleUp = extras.getBoolean(SCALE_UP_IF_NEEDED, true)
        }
        if (mBitmap == null) {
            finish()
            return
        }
        assert(extras != null)
        if (extras!!.getInt("color") != 0) {
            findViewById<View>(R.id.rl_main).setBackgroundColor(extras.getInt("color"))
        }
        if (extras.getInt("bgColor") != 0) {
            mImageView?.setBackgroundColor(extras.getInt("bgColor"))
        }

        // Make UI fullscreen.
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        findViewById<View>(R.id.discard).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        findViewById<View>(R.id.save).setOnClickListener {
            try {
                onSaveClicked()
            } catch (e: Exception) {
                finish()
            }
        }
        findViewById<View>(R.id.rotateLeft).setOnClickListener {
            mBitmap = rotateImage(mBitmap!!, -90f)
            val rotateBitmap = RotateBitmap(mBitmap!!)
            mImageView?.setImageRotateBitmapResetBase(rotateBitmap, true)
            mRunFaceDetection.run()
        }
        findViewById<View>(R.id.rotateRight).setOnClickListener {
            mBitmap = rotateImage(mBitmap!!, 90f)
            val rotateBitmap = RotateBitmap(mBitmap!!)
            mImageView?.setImageRotateBitmapResetBase(rotateBitmap, true)
            mRunFaceDetection.run()
        }

        //fix image rotated
        rotateImageIfNecessary()
        startFaceDetection()
    }

    private fun getImageUri(path: String?): Uri {
        return Uri.fromFile(File(path))
    }

    private fun getBitmap(path: String?): Bitmap? {
        val uri = getImageUri(path)
        var inputStream: InputStream? = null
        try {
            inputStream = mContentResolver?.openInputStream(uri)

            // Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream?.close()
            var scale = 1
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = 2.0.pow(
                    Math.round(
                        ln(
                            IMAGE_MAX_SIZE / max(
                                o.outHeight.toDouble(),
                                o.outWidth.toDouble()
                            )
                        ) / ln(0.5)
                    ).toInt().toDouble()
                ).toInt()
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = mContentResolver?.openInputStream(uri)
            val b = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream?.close()
            return b
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "file $path not found")
        } catch (e: IOException) {
            Log.e(TAG, "file $path has problem")
        }
        return null
    }

    private fun startFaceDetection() {
        if (isFinishing) {
            return
        }
        mImageView!!.setImageBitmapResetBase(mBitmap, true)
        startBackgroundJob(this, null, "Please wait\u2026", {
            val latch = CountDownLatch(1)
            val b = mBitmap
            mHandler.post {
                if (b != mBitmap && b != null) {
                    mImageView!!.setImageBitmapResetBase(b, true)
                    mBitmap!!.recycle()
                    mBitmap = b
                }
                if (mImageView!!.scale == 1f) {
                    mImageView!!.center(true, true)
                }
                latch.countDown()
            }
            try {
                latch.await()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            mRunFaceDetection.run()
        }, mHandler)
    }

    @Throws(Exception::class)
    private fun onSaveClicked() {
        if (mSaving) return
        if (mCrop == null) {
            return
        }
        mSaving = true
        val r = mCrop!!.cropRect
        val width = r.width()
        val height = r.height()
        var croppedImage: Bitmap?
        croppedImage = try {
            Bitmap.createBitmap(
                width, height,
                if (mCircleCrop) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            )
        } catch (e: Exception) {
            throw e
        }
        if (croppedImage == null) {
            return
        }
        run {
            val canvas = Canvas(croppedImage!!)
            val dstRect = Rect(0, 0, width, height)
            canvas.drawBitmap(mBitmap!!, r, dstRect, null)
        }
        if (mCircleCrop) {
            val c = Canvas(croppedImage)
            val p = Path()
            p.addCircle(
                width / 2f, height / 2f, width / 2f,
                Path.Direction.CW
            )
            c.clipPath(p, Region.Op.DIFFERENCE)
            c.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
        }
        if (mOutputX != 0 && mOutputY != 0) {
            if (mScale) {
                val old: Bitmap = croppedImage
                croppedImage = transform(
                    Matrix(),
                    croppedImage, mOutputX, mOutputY, mScaleUp
                )
                if (old != croppedImage) {
                    old.recycle()
                }
            } else {
                val b = Bitmap.createBitmap(
                    mOutputX, mOutputY,
                    Bitmap.Config.RGB_565
                )
                val canvas = Canvas(b)
                val srcRect = mCrop!!.cropRect
                val dstRect = Rect(0, 0, mOutputX, mOutputY)
                val dx = (srcRect.width() - dstRect.width()) / 2
                val dy = (srcRect.height() - dstRect.height()) / 2
                srcRect.inset(
                    max(0.0, dx.toDouble()).toInt(), max(0.0, dy.toDouble())
                        .toInt()
                )
                dstRect.inset(
                    max(0.0, -dx.toDouble()).toInt(), max(0.0, -dy.toDouble())
                        .toInt()
                )
                canvas.drawBitmap(mBitmap!!, srcRect, dstRect, null)
                croppedImage.recycle()
                croppedImage = b
            }
        }
        val myExtras = intent.extras
        if (myExtras != null && (myExtras.getParcelable<Parcelable?>("data") != null || myExtras.getBoolean(
                RETURN_DATA
            ))
        ) {
            val extras = Bundle()
            extras.putParcelable(RETURN_DATA_AS_BITMAP, croppedImage)
            setResult(RESULT_OK, Intent().setAction(ACTION_INLINE_DATA).putExtras(extras))
            finish()
        } else {
            val b = croppedImage
            startBackgroundJob(
                this,
                null,
                getString(R.string.croperino_saving_image),
                { saveOutput(b) },
                mHandler
            )
        }
    }

    private fun saveOutput(croppedImage: Bitmap?) {
        Log.e("FILE", "CROPPED IMAGE BITMAP ${croppedImage.toString()}")
        if (mSaveUri != null) {
            Log.e("FILE", "SAVE URI ${mSaveUri.toString()}")
            var outputStream: OutputStream? = null
            try {
                outputStream = mContentResolver?.openOutputStream(mSaveUri!!)
                if (outputStream != null) {
                    croppedImage?.compress(mOutputFormat, 80, outputStream)
                }
            } catch (ex: IOException) {
                Log.e(TAG, "Cannot open file: " + mSaveUri, ex)
                runOnUiThread {
                    Toast.makeText(
                        this@CropImage,
                        "Cannot access file due to app storage encryption, Please use camera or other apps to open gallery",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                setResult(RESULT_CANCELED)
                finish()
                return
            } finally {
                closeSilently(outputStream)
            }
            val extras = Bundle()
            val intent = Intent(mSaveUri.toString())
            intent.putExtras(extras)
            intent.putExtra(IMAGE_PATH, mImagePath)
            intent.putExtra(ORIENTATION_IN_DEGREES, getOrientationInDegree(this))
            setResult(RESULT_OK, intent)
        } else {
            runOnUiThread {
                Toast.makeText(
                    this@CropImage,
                    "Image URL does not exist please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        croppedImage!!.recycle()
        finish()
    }

    override fun onPause() {
        super.onPause()
        instance()!!.cancelThreadDecoding(mDecodingThreads)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBitmap != null) {
            mBitmap!!.recycle()
        }
    }

    var mRunFaceDetection: Runnable = object : Runnable {
        var mScale = 1f
        var mImageMatrix: Matrix? = null
        var mFaces = arrayOfNulls<FaceDetector.Face>(3)
        var mNumFaces = 0
        private fun handleFace(f: FaceDetector.Face?) {
            val midPoint = PointF()
            val r = (f!!.eyesDistance() * mScale).toInt() * 2
            f.getMidPoint(midPoint)
            midPoint.x *= mScale
            midPoint.y *= mScale
            val midX = midPoint.x.toInt()
            val midY = midPoint.y.toInt()
            val hv = HighlightView(mImageView!!)
            val width = mBitmap!!.width
            val height = mBitmap!!.height
            val imageRect = Rect(0, 0, width, height)
            val faceRect = RectF(midX.toFloat(), midY.toFloat(), midX.toFloat(), midY.toFloat())
            faceRect.inset(-r.toFloat(), -r.toFloat())
            if (faceRect.left < 0) {
                faceRect.inset(-faceRect.left, -faceRect.left)
            }
            if (faceRect.top < 0) {
                faceRect.inset(-faceRect.top, -faceRect.top)
            }
            if (faceRect.right > imageRect.right) {
                faceRect.inset(
                    faceRect.right - imageRect.right,
                    faceRect.right - imageRect.right
                )
            }
            if (faceRect.bottom > imageRect.bottom) {
                faceRect.inset(
                    faceRect.bottom - imageRect.bottom,
                    faceRect.bottom - imageRect.bottom
                )
            }
            hv.setup(
                mImageMatrix, imageRect, faceRect, mCircleCrop,
                mAspectX != 0 && mAspectY != 0
            )
            mImageView!!.add(hv)
        }

        private fun makeDefault() {
            val hv = HighlightView(mImageView!!)
            val width = mBitmap!!.width
            val height = mBitmap!!.height
            val imageRect = Rect(0, 0, width, height)
            var cropWidth = (min(width.toDouble(), height.toDouble()) * 4 / 5).toInt()
            var cropHeight = cropWidth
            if (mAspectX != 0 && mAspectY != 0) {
                if (mAspectX > mAspectY) {
                    cropHeight = cropWidth * mAspectY / mAspectX
                } else {
                    cropWidth = cropHeight * mAspectX / mAspectY
                }
            }
            val x = (width - cropWidth) / 2
            val y = (height - cropHeight) / 2
            val cropRect = RectF(
                x.toFloat(),
                y.toFloat(),
                (x + cropWidth).toFloat(),
                (y + cropHeight).toFloat()
            )
            hv.setup(
                mImageMatrix, imageRect, cropRect, mCircleCrop,
                mAspectX != 0 && mAspectY != 0
            )
            mImageView!!.mHighlightViews.clear()
            mImageView!!.add(hv)
        }

        private fun prepareBitmap(): Bitmap? {
            if (mBitmap == null) {
                return null
            }
            if (mBitmap!!.width > 256) {
                mScale = 256.0f / mBitmap!!.width
            }
            val matrix = Matrix()
            matrix.setScale(mScale, mScale)
            return Bitmap.createBitmap(
                mBitmap!!,
                0,
                0,
                mBitmap!!.width,
                mBitmap!!.height,
                matrix,
                true
            )
        }

        override fun run() {
            mImageMatrix = mImageView!!.imageMatrix
            val faceBitmap = prepareBitmap()
            mScale = 1.0f / mScale
            if (faceBitmap != null && mDoFaceDetection) {
                val detector = FaceDetector(
                    faceBitmap.width,
                    faceBitmap.height, mFaces.size
                )
                mNumFaces = detector.findFaces(faceBitmap, mFaces)
            }
            if (faceBitmap != null && faceBitmap != mBitmap) {
                faceBitmap.recycle()
            }
            mHandler.post {
                mWaitingToPick = mNumFaces > 1
                if (mNumFaces > 0) {
                    for (i in 0 until mNumFaces) {
                        handleFace(mFaces[i])
                    }
                } else {
                    makeDefault()
                }
                mImageView!!.invalidate()
                if (mImageView!!.mHighlightViews.size == 1) {
                    mCrop = mImageView!!.mHighlightViews[0]
                    mCrop!!.setFocus(true)
                }
                if (mNumFaces > 1) {
                    Toast.makeText(this@CropImage, "Multi face crop help", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun rotateImageIfNecessary() {
        try {
            val exif = ExifInterface(mImagePath!!)
            val orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION)
            val orientation = orientString?.toInt() ?: ExifInterface.ORIENTATION_NORMAL
            var rotationAngle = 0
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270
            val matrix = Matrix()
            matrix.postRotate(rotationAngle.toFloat())
            mBitmap = Bitmap.createBitmap(
                mBitmap!!,
                mOutputX,
                mOutputY,
                mBitmap!!.width,
                mBitmap!!.height,
                matrix,
                true
            )
        } catch (e: Exception) {
            Log.e(TAG, "cannot rotate file")
        }
    }

    companion object {
        private const val TAG = "CropImage"
        const val IMAGE_PATH = "image-path"
        const val SCALE = "scale"
        const val ORIENTATION_IN_DEGREES = "orientation_in_degrees"
        const val ASPECT_X = "aspectX"
        const val ASPECT_Y = "aspectY"
        const val OUTPUT_X = "outputX"
        const val OUTPUT_Y = "outputY"
        const val SCALE_UP_IF_NEEDED = "scaleUpIfNeeded"
        const val CIRCLE_CROP = "circleCrop"
        const val RETURN_DATA = "return-data"
        const val RETURN_DATA_AS_BITMAP = "data"
        const val ACTION_INLINE_DATA = "inline-data"
        const val NO_STORAGE_ERROR = -1
        const val CANNOT_STAT_ERROR = -2
        var mSaveUri: Uri? = null
        @JvmOverloads
        fun showStorageToast(
            activity: Activity,
            remaining: Int = calculatePicturesRemaining(activity)
        ) {
            var noStorageText: String? = null
            if (remaining == NO_STORAGE_ERROR) {
                val state = Environment.getExternalStorageState()
                noStorageText = if (state == Environment.MEDIA_CHECKING) {
                    activity.getString(R.string.croperino_preparing_card)
                } else {
                    activity.getString(R.string.croperino_no_storage_card)
                }
            } else if (remaining < 1) {
                noStorageText = activity.getString(R.string.croperino_not_enough_space)
            }
            if (noStorageText != null) {
                Toast.makeText(activity, noStorageText, Toast.LENGTH_SHORT).show()
            }
        }

        fun calculatePicturesRemaining(activity: Activity): Int {
            return try {
                var storageDirectory = ""
                val state = Environment.getExternalStorageState()
                storageDirectory = if (Environment.MEDIA_MOUNTED == state) {
                    Environment.getExternalStorageDirectory().toString()
                } else {
                    activity.filesDir.toString()
                }
                val stat = StatFs(storageDirectory)
                val remaining = stat.availableBlocks.toFloat() * stat.blockSize.toFloat() / 400000f
                remaining.toInt()
            } catch (ex: Exception) {
                CANNOT_STAT_ERROR
            }
        }
    }
}
