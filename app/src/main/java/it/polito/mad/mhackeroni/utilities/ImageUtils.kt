package it.polito.mad.mhackeroni.utilities

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import it.polito.mad.mhackeroni.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.ref.WeakReference


class ImageUtils {
    companion object{

        private val PIC_TITLE = "profile_pic"
        private val PIC_DESCRIPTION ="Profile picture"

            fun getBitmap(path: String, context: Context): Bitmap?{
                var bitmap: Bitmap?
                var input: InputStream? = null

                try{
                    input = context.contentResolver.openInputStream(Uri.parse(path))
                    bitmap = BitmapFactory.decodeStream(input, null, null)!!
                } catch (e1: FileNotFoundException){
                    bitmap = BitmapFactory.decodeFile(path)
                } catch (e: Exception) {
                    return getPlaceholder(
                        context
                    )
                } finally {
                    if(input != null)
                        input.close()
                }

                return bitmap
            }

        fun rotateImageFromUri(uri: Uri, angle: Float, context: Context): Bitmap? {
            val source =
                getBitmap(
                    uri.toString(),
                    context
                )
            val matrix = Matrix()

            matrix.postRotate(angle)
            return source?.let {
                Bitmap.createBitmap(
                    it, 0, 0, source.width, source.height,
                    matrix, true)
            }
        }

            private fun getPlaceholder(context: Context):Bitmap?{
                val bitmap:Bitmap
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    val vectorDrawable = context.getDrawable(R.drawable.ic_avatar)

                    bitmap = Bitmap.createBitmap(
                            vectorDrawable!!.intrinsicWidth,
                            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
                    )

                    vectorDrawable.setBounds(0, 0, Canvas(bitmap).width, Canvas(bitmap).height)
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
                var input: InputStream? = null
                var canDisplay = false

                try{
                    input = context.contentResolver.openInputStream(Uri.parse(path))
                    BitmapFactory.decodeStream(input, null, null)!!
                    input?.close()
                    canDisplay =  true
                } catch (e1: FileNotFoundException){
                    BitmapFactory.decodeFile(path)
                    canDisplay = true
                } catch (e: Exception) {
                     canDisplay =  false
                } finally {
                    if(input != null)
                        input.close()
                    return canDisplay
                }
            }

            fun insertImage(
                cr: ContentResolver,
                source: Bitmap
            ): String? {
                val values = ContentValues()

                values.put(Images.Media.TITLE,
                    PIC_TITLE
                )
                values.put(Images.Media.DISPLAY_NAME,
                    PIC_TITLE
                )
                values.put(Images.Media.DESCRIPTION,
                    PIC_DESCRIPTION
                )
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

        suspend fun downloadAndSaveImageTask(context: Context, url : String) : String?  =
            withContext(Dispatchers.IO){
                var path : String? = null
                var mContext: WeakReference<Context> = WeakReference(context)
                val requestOptions = RequestOptions()
                    .downsample(DownsampleStrategy.CENTER_INSIDE)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)

                Log.d("MAG", "Downloading: ${url}")

                mContext.get()?.let {
                    val bitmap = async {
                        Glide.with(it)
                            .asBitmap()
                            .load(url)
                            .apply(requestOptions)
                            .submit()
                            .get()
                    }

                    try {
                        var file = it.getDir("Pictures", Context.MODE_PRIVATE)
                        file = File(file, "itemImage.jpg")
                        val out = FileOutputStream(file)
                        bitmap.await().compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.flush()
                        out.close()
                        path = file.absolutePath
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                return@withContext path
            }

    }
}