package com.example.social_sharing_plus

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.social_sharing_plus.utils.MediaType
import com.example.social_sharing_plus.utils.SharePaths
import com.example.social_sharing_plus.utils.SocialConstants
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File

/** SocialSharingPlusPlugin */
class SocialSharingPlusPlugin : FlutterPlugin, MethodCallHandler {

    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, SocialConstants.CHANNEL_NAME)
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            SocialConstants.FACEBOOK ->
                    shareToSocialMedia(SocialConstants.FACEBOOK_PACKAGE_NAME, call, result)
            SocialConstants.TWITTER ->
                    shareToSocialMedia(SocialConstants.TWITTER_PACKAGE_NAME, call, result)
            SocialConstants.LINKEDIN ->
                    shareToSocialMedia(SocialConstants.LINKEDIN_PACKAGE_NAME, call, result)
            SocialConstants.WHATSAPP ->
                    shareToSocialMedia(SocialConstants.WHATSAPP_PACKAGE_NAME, call, result)
            SocialConstants.REDDIT ->
                    shareToSocialMedia(SocialConstants.REDDIT_PACKAGE_NAME, call, result)
            SocialConstants.TELEGRAM ->
                    shareToSocialMedia(SocialConstants.TELEGRAM_PACKAGE_NAME, call, result)
            SocialConstants.INSTAGRAM ->
                    shareToSocialMedia(SocialConstants.INSTAGRAM_PACKAGE_NAME, call, result)
            else -> result.notImplemented()
        }
    }

    private fun shareToSocialMedia(packageName: String, call: MethodCall, result: Result) {
        val content: String? = call.argument<String>("content")

        val media: Any? = call.argument<Any>("media")
        val isOpenBrowser: Boolean = call.argument<Boolean>("isOpenBrowser") ?: false

        val mediaUris: List<Uri?> =
                when (media) {
                    is String -> {
                      
                        val file = File(media)
                        listOf(
                                FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                )
                        )
                    }
                    is List<*> -> {
                      
                        media.mapNotNull { mediaUriString ->
                            if (mediaUriString is String) {
                                val file = File(mediaUriString)
                                FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                )
                            } else {
                                null
                            }
                        }
                    }
                    else -> emptyList()
                }

        if (mediaUris.isNotEmpty()) {
            shareMultipleMedia(packageName, content, mediaUris, result, isOpenBrowser)
        } else {
            shareSingleMedia(packageName, content, null, result, isOpenBrowser)
        }
    }

    private fun shareSingleMedia(
            packageName: String,
            content: String?,
            mediaUri: Uri?,
            result: Result,
            isOpenBrowser: Boolean
    ) {
        val intent: Intent =
                Intent(Intent.ACTION_SEND).apply {
                    type =
                            mediaUri?.let {
                                val extension: String = mediaUri.toString().substringAfterLast(".")
                                if (extension == SharePaths.MP4.reference) MediaType.VIDEO.reference
                                else MediaType.IMAGE.reference
                            }
                                    ?: SharePaths.TEXT_PLAIN.reference

                    putExtra(Intent.EXTRA_TEXT, content)
                    mediaUri?.let {
                        putExtra(Intent.EXTRA_STREAM, it)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    setPackage(packageName)
                }

        if (intent.resolveActivity(context.packageManager) != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            result.success(null)
        } else {
            if (isOpenBrowser) {
                openInBrowser(packageName, content ?: "", mediaUri)
                result.success(null)
            } else {
                result.error("APP_NOT_INSTALLED", "$packageName is not installed", null)
            }
        }
    }

    private fun shareMultipleMedia(
            packageName: String,
            content: String?,
            mediaUris: List<Uri?>,
            result: Result,
            isOpenBrowser: Boolean
    ) {
        val intent: Intent =
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type =
                            MediaType.IMAGE
                                    .reference
                    putExtra(Intent.EXTRA_TEXT, content)

                    if (mediaUris.isNotEmpty()) {
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(mediaUris))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    setPackage(packageName)
                }

        if (intent.resolveActivity(context.packageManager) != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            result.success(null)
        } else {
            if (isOpenBrowser) {
                openInBrowser(packageName, content ?: "", mediaUris.firstOrNull())
                result.success(null)
            } else {
                result.error("APP_NOT_INSTALLED", "$packageName is not installed", null)
            }
        }
    }

    private fun openInBrowser(packageName: String, content: String, mediaUri: Uri?) {
        val webIntent = Intent(Intent.ACTION_VIEW)
        val webUrlString: String? =
                when (packageName) {
                    SocialConstants.FACEBOOK_PACKAGE_NAME ->
                            "${SocialConstants.FACEBOOK_WEB_URL}$content"
                    SocialConstants.TWITTER_PACKAGE_NAME ->
                            "${SocialConstants.TWITTER_WEB_URL}$content"
                    SocialConstants.LINKEDIN_PACKAGE_NAME ->
                            "${SocialConstants.LINKEDIN_WEB_URL}$content"
                    SocialConstants.WHATSAPP_PACKAGE_NAME ->
                            "${SocialConstants.WHATSAPP_WEB_URL}$content"
                    SocialConstants.REDDIT_PACKAGE_NAME ->
                            "${SocialConstants.REDDIT_WEB_URL}$content"
                    SocialConstants.TELEGRAM_PACKAGE_NAME ->
                            "${SocialConstants.TELEGRAM_WEB_URL}$content"
                    SocialConstants.INSTAGRAM_PACKAGE_NAME ->
                            "${SocialConstants.INSTAGRAM_WEB_URL}"
                    else -> null
                }

        val mediaParam: String? =
                mediaUri?.let {
                    val extension: String = mediaUri.toString().substringAfterLast(".")
                    if (extension == SharePaths.MP4.reference)
                            "${SharePaths.VIDEO_URL.reference}${Uri.encode(mediaUri.toString())}"
                    else "${SharePaths.IMAGE_URL.reference}${Uri.encode(mediaUri.toString())}"
                }

        val finalUrlString: String? =
                webUrlString?.let { url -> if (mediaParam != null) "$url&$mediaParam" else url }

        finalUrlString?.let {
            webIntent.data = Uri.parse(it)
            webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(webIntent)
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
