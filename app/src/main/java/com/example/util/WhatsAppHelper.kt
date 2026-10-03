package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {
    /**
     * Formats any phone number string into international WhatsApp format replacing leading 0 with +62.
     * Examples:
     * - "08123456789" -> "+628123456789"
     * - "628123456789" -> "+628123456789"
     * - "8123456789" -> "+628123456789"
     * - "+62 812-3456-7890" -> "+6281234567890"
     */
    fun formatToIndonesianWhatsApp(phone: String): String {
        val cleanDigits = phone.replace("-", "").replace(" ", "").replace(Regex("[^0-9+]"), "")
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return when {
            cleanDigits.startsWith("+62") -> "+62" + digitsOnly.substring(if (digitsOnly.startsWith("62")) 2 else 0)
            cleanDigits.startsWith("62") -> "+62" + digitsOnly.substring(2)
            cleanDigits.startsWith("0") -> "+62" + digitsOnly.substring(1)
            cleanDigits.startsWith("8") -> "+62$digitsOnly"
            cleanDigits.isBlank() -> "+6289665805758"
            else -> "+62$digitsOnly"
        }
    }

    /**
     * Opens WhatsApp application or web browser with pre-filled message sent to formatted +62 phone number.
     */
    fun sendWhatsAppMessage(
        context: Context,
        rawPhone: String,
        subject: String,
        bodyMessage: String
    ) {
        val formattedPhone = formatToIndonesianWhatsApp(rawPhone)
        val numberOnly = formattedPhone.replace("+", "")
        val fullText = "📌 *NOTIFIKASI FOSIS OPERATIONAL SYSTEM*\n*Subject:* $subject\n\n$bodyMessage"

        try {
            val encodedText = URLEncoder.encode(fullText, "UTF-8")
            val whatsappUrl = "https://api.whatsapp.com/send?phone=$numberOnly&text=$encodedText"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal membuka WhatsApp ke $formattedPhone: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
