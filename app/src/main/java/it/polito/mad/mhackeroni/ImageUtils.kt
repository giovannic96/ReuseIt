package it.polito.mad.mhackeroni

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Images
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream


class ImageUtils {
    companion object{

        private val PIC_TITLE = "profile_pic"
        private val PIC_DESCRIPTION ="Profile picture"

            fun getBitmap(path: String, context: Context): Bitmap?{
                var bitmap: Bitmap?

                if(!canDisplayBitmap(path, context)) return getPlaceholder(context)

                try{
                    bitmap = BitmapFactory.decodeFile(path)
                    if(bitmap == null)
                        throw FileNotFoundException() // Try to use content resolver
                } catch (e:FileNotFoundException){
                    try {
                        val input = context.contentResolver.openInputStream(Uri.parse(path))
                        bitmap = BitmapFactory.decodeStream(input, null, null)!!

                    }catch (e:Exception){
                        return getPlaceholder(context)
                    }
                }

                return bitmap
            }

            fun checkOrientation(path: String, context: Context): Bitmap {
                var bitmap: Bitmap? = getBitmap(path, context)

                try {
                    val ei = ExifInterface(path)
                    val orientation: Int = ei.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_FLIP_VERTICAL
                    )
                    lateinit var rotatedBitmap: Bitmap

                    rotatedBitmap = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap!!, 90.0F)
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap!!, 180.0F)
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap!!, 270.0F)
                        ExifInterface.ORIENTATION_NORMAL -> bitmap!!
                        else -> bitmap!!
                    }

                    return rotatedBitmap

                }catch (e:FileNotFoundException){
                    return bitmap!!
                }
            }

            fun rotateImage(source: Bitmap, angle: Float): Bitmap {
                val matrix = Matrix()
                matrix.postRotate(angle)
                return Bitmap.createBitmap(
                    source, 0, 0, source.width, source.height,
                    matrix, true)
            }

        fun rotateImageFromUri(uri: Uri, angle: Float, context: Context): Bitmap? {
            val source = getBitmap(uri.toString(), context)
            val matrix = Matrix()

            matrix.postRotate(angle)
            return source?.let {
                Bitmap.createBitmap(
                    it, 0, 0, source.width, source.height,
                    matrix, true)
            }
        }

            private fun getPlaceholder(context: Context):Bitmap{
                val bitmap:Bitmap
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    val vectorDrawable = context.getDrawable(R.drawable.ic_avatar)

                    bitmap = Bitmap.createBitmap(
                        vectorDrawable!!.intrinsicWidth,
                        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
                    )

                    vectorDrawable.setBounds(0, 0, Canvas(bitmap).getWidth(), Canvas(bitmap).getHeight())
                    vectorDrawable.draw(Canvas(bitmap))
                } else {
                    bitmap = BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.ic_avatar
                    )
                }

                return bitmap
            }

            fun canDisplayBitmap(path: String, context: Context):Boolean{
                var bitmap:Bitmap?

                try{
                    bitmap = BitmapFactory.decodeFile(path)
                    if(bitmap == null) {
                        throw FileNotFoundException() // Try to use content resolver
                    }
                } catch (e:FileNotFoundException){
                    try {
                        val input = context.contentResolver.openInputStream(Uri.parse(path))
                        BitmapFactory.decodeStream(input, null, null)!!

                    }catch (e:Exception){
                        return false
                    }
                }

                return true
            }

            fun insertImage(
                cr: ContentResolver,
                source: Bitmap
            ): String? {
                val values = ContentValues()

                values.put(Images.Media.TITLE, PIC_TITLE)
                values.put(Images.Media.DISPLAY_NAME, PIC_TITLE)
                values.put(Images.Media.DESCRIPTION, PIC_DESCRIPTION)
                values.put(Images.Media.MIME_TYPE, "image/jpeg")
                values.put(Images.Media.DATE_ADDED, System.currentTimeMillis())
                // values.put(Images.Media.SIZE,  source.rowBytes* source.height)

                var url: Uri? = null
                var stringUrl: String? = null
                try {
                    url = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values)
                    val imageOut: OutputStream? = cr.openOutputStream(url!!)
                    try {
                        source.compress(Bitmap.CompressFormat.JPEG, 30, imageOut)
                    } finally {
                        imageOut?.close()
                    }
                } catch (e: java.lang.Exception) {
                    if (url != null) {
                        cr.delete(url, null, null)
                        url = null
                    }
                }
                if (url != null) {
                    stringUrl = url.toString()
                }
                return stringUrl
            }
    }
}