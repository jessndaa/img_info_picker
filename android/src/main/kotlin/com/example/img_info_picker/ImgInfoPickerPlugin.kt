package com.example.img_info_picker

import android.Manifest
import android.R.attr.phoneNumber
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.tahamalas.custom_image_picker.PhoneAlbum
import com.tahamalas.custom_image_picker.PhonePhoto
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*


/** ImgInfoPickerPlugin */
class ImgInfoPickerPlugin: FlutterPlugin, MethodCallHandler, ActivityAware{
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private var appActivity: Activity? = null
  private var result: Result? = null
  private val callbackById: MutableMap<Int, Runnable> = mutableMapOf()
  private var arguments: Map<*, *>? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "img_info_picker")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    this.result = result
    when (call.method) {
      "startListening" -> getPermissionResult(result, appActivity!!, call.arguments)
      "cancelListening" -> cancelListening(call.arguments, result)
        else -> result.notImplemented()
    }
  }
  private fun getPermissionResult(result: Result, activity: Activity, arguments: Any?) {
    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val callIntent = Intent(Intent.ACTION_VIEW)
    callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    this.appActivity!!.intent = callIntent
    Permissions.check(this.appActivity!!/*context*/, permissions, "holla", null/*options*/, object : PermissionHandler() {
      override fun onGranted() {
        val argsMap = arguments as Map<*, *>
        when (argsMap["id"] as Int) {
          CallbacksEnum.GET_IMAGES.ordinal -> startListening(argsMap, result, "getAllImages")
          CallbacksEnum.GET_GALLERY.ordinal -> startListening(argsMap, result, "getAlbumList")
          CallbacksEnum.GET_IMAGES_OF_GALLERY.ordinal -> startListening(argsMap, result, "getPhotosOfAlbum")
        }
      }

      override fun onDenied(context: Context?, deniedPermissions: ArrayList<String?>?) {
        Log.i("ERROR", "onDenied: ")
      }
    })
  }
  private fun cancelListening(args: Any, result: Result) {
    // Get callback id
    val currentListenerId = args as Int
    // Remove callback
    callbackById.remove(currentListenerId)
    // Do additional stuff if required to cancel the listener
    result.success(null)
  }
  private fun getPhotosOfAlbum(activity: Activity, albumID: String, pageNumber: Int): String {

    val phonePhotos = mutableListOf<PhonePhoto>()

    val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)

    val images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val cur = activity.contentResolver.query(
            images,
            projection,
            "${MediaStore.Images.ImageColumns.BUCKET_ID} == ?",
            arrayOf(albumID),
            MediaStore.Images.Media.DATE_MODIFIED + " DESC " + " LIMIT ${(pageNumber - 1) * 50}, 50 "
    )

    if (cur != null && cur!!.count > 0) {
      Log.i("DeviceImageManager", " query count=" + cur!!.count)

      if (cur!!.moveToFirst()) {
        var bucketName: String
        var data: String
        var imageId: String
        val bucketNameColumn = cur!!.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        val imageUriColumn = cur!!.getColumnIndex(MediaStore.Images.Media.DATA)

        val imageIdColumn = cur!!.getColumnIndex(MediaStore.Images.Media._ID)

        do {
          bucketName = cur!!.getString(bucketNameColumn)
          data = cur!!.getString(imageUriColumn)
          imageId = cur!!.getString(imageIdColumn)
          // phonePhotos.add(PhonePhoto(imageId, bucketName, data, ))

        } while (cur!!.moveToNext())
      }

      cur!!.close()
      var string = "[ "
      for (phonePhoto in phonePhotos) {
        string += phonePhoto.toJson()
        if (phonePhotos.indexOf(phonePhoto) != phonePhotos.size - 1)
          string += ", "
      }
      string += "]"
      return string
    } else {
      return "[]"
    }

  }

  private fun getAlbumList(mediaType: Int, contentResolver: ContentResolver): String {

    val phoneAlbums = mutableListOf<PhoneAlbum>()

    var contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
      contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(MediaStore.Images.ImageColumns.BUCKET_ID, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.DATA)
    val bucketGroupBy = "1) GROUP BY ${MediaStore.Images.ImageColumns.BUCKET_ID}, (${MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME}"
    val bucketOrderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC"

    val cursor = contentResolver.query(contentUri, projection, bucketGroupBy, null, bucketOrderBy)


    if (cursor != null) {
      while (cursor.moveToNext()) {
        val bucketId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
        val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)) // Thumb image path

        val selection = MediaStore.Images.Media.BUCKET_ID + "='" + bucketId + "'"

        val countCursor = contentResolver.query(contentUri, arrayOf("count(" + MediaStore.Images.ImageColumns._ID + ")"), selection, null, bucketOrderBy)

        var count = 0
        if (countCursor != null) {
          countCursor.moveToFirst()
          count = countCursor.getInt(0)
          countCursor.close()
        }

        Log.d("AlbumScanner", "bucketId : $bucketId | name : $name | count : $count | path : $path")

        phoneAlbums.add(PhoneAlbum(bucketId, name, path, count))
      }
      cursor.close()
      var string = "[ "
      for (phoneAlbum in phoneAlbums) {
        string += phoneAlbum.toJson()
        if (phoneAlbums.indexOf(phoneAlbum) != phoneAlbums.size - 1)
          string += ", "
      }
      string += "]"
      return string
    } else {
      return "[]"
    }
  }

  private fun getAllImageList(activity: Activity): List<String> {
    print("all images")
    val allImageList = ArrayList<String>()
    val allVideoList = ArrayList<String>()
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    var videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_ADDED, MediaStore.Images.ImageColumns.TITLE, MediaStore.Images.ImageColumns.DATE_MODIFIED, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
    val c = activity.contentResolver.query(uri, projection, null, null, null)
    if (c != null) {
      while (c.moveToNext()) {
        var id = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns._ID))
        var photoUri = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
        var albumName = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
        var createdDate = c.getString(c.getColumnIndex( MediaStore.Images.ImageColumns.DATE_ADDED))
        var lastUpdateDate = c.getString(c.getColumnIndex( MediaStore.Images.ImageColumns.DATE_ADDED))
        var image = PhonePhoto(id, photoUri, albumName, videoTumbUrl = "", fileType =  "0", createdDate =  lastUpdateDate, lastModifiedDate =  createdDate)
        allImageList.add(image.toJson())
      }
      c.close()
    }

    val projectionVideo = arrayOf( MediaStore.Video.VideoColumns._ID,  MediaStore.Video.Media.DATA, MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME, MediaStore.Video.VideoColumns.DATE_ADDED, MediaStore.Video.VideoColumns.TITLE, MediaStore.Video.VideoColumns.DATE_MODIFIED)
    val videoCursor = activity.contentResolver.query(videoUri, projectionVideo, null, null, null)

    if (videoCursor != null) {
      while (videoCursor.moveToNext()) {
        var id = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.Media._ID))
        var photoUri = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.Media.DATA))
        var albumName = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME))
        var createdDate = videoCursor.getString(videoCursor.getColumnIndex( MediaStore.Images.ImageColumns.DATE_ADDED))
        var lastUpdateDate = videoCursor.getString(videoCursor.getColumnIndex( MediaStore.Images.ImageColumns.DATE_ADDED))
        var image = PhonePhoto(id, photoUri, albumName, videoTumbUrl = "", fileType =  "1", createdDate =  lastUpdateDate, lastModifiedDate =  createdDate)
        allVideoList.add(image.toJson())
      }
      videoCursor.close()
    }
    allImageList.addAll(allVideoList);
    return allImageList.reversed().toList()
  }

  private fun startListening(args: Any, result: Result?, methodName: String) {
    // Get callback id
    println("the args are $args")
    val argsFromFlutter = args as Map<*, *>
    val currentListenerId = argsFromFlutter["id"] as Int
    val runnable = Runnable {
      if (callbackById.containsKey(currentListenerId)) {
        val argsMap: MutableMap<String, Any> = mutableMapOf()
        argsMap["id"] = currentListenerId

        when (methodName) {
          "getAllImages" -> {
            argsMap["args"] = getAllImageList(appActivity!!)
          }
          "getAlbumList" -> {
            argsMap["args"] = getAlbumList(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, appActivity!!.contentResolver)
          }
          "getPhotosOfAlbum" -> {
            val callArgs = argsFromFlutter["args"] as Map<*, *>
            argsMap["args"] = getPhotosOfAlbum(appActivity!!, callArgs["albumID"] as String, callArgs["page"] as Int)
          }
        }
        // Send some value to callback
        appActivity!!.runOnUiThread {
          channel.invokeMethod("callListener", argsMap)
        }
      }
    }
    val thread = Thread(runnable)
    callbackById[currentListenerId] = runnable
    thread.start()
    // Return immediately
    result?.success(null)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    this.appActivity = binding.activity
  }


  override fun onDetachedFromActivityForConfigChanges() {
    this.appActivity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    getPermissionResult(result!!, this.appActivity!!, {})
  }

  override fun onDetachedFromActivity() {this.appActivity = null}
}
