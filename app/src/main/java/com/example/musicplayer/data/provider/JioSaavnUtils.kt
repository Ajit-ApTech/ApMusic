package com.example.musicplayer.data.provider

import android.text.Html
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object JioSaavnUtils {
    private const val DES_KEY = "38346591"

    fun decryptMediaUrl(encryptedUrl: String?): String? {
        if (encryptedUrl.isNullOrBlank()) return null
        return try {
            val keySpec = SecretKeySpec(DES_KEY.toByteArray(Charsets.UTF_8), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)

            val decodedBytes = Base64.decode(encryptedUrl.trim(), Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            val rawUrl = String(decryptedBytes, Charsets.UTF_8)

            // Convert to high quality 320kbps stream
            rawUrl.replace("_96.mp4", "_320.mp4")
                .replace("_96_p.mp4", "_320.mp4")
                .replace("_160.mp4", "_320.mp4")
        } catch (e: Exception) {
            null
        }
    }

    fun formatImageUrl(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null
        return imageUrl.replace("150x150.jpg", "500x500.jpg")
            .replace("50x50.jpg", "500x500.jpg")
    }

    fun cleanText(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return try {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } catch (e: Exception) {
            text.replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&#039;", "'")
                .trim()
        }
    }
}
