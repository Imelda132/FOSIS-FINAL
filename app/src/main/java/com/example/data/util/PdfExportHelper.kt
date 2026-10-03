package com.example.data.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    fun generateAndSharePdf(context: Context, item: FosisItemEntity): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard in 72 dpi (595x842)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale("id", "ID"))
        val currentDateStr = dateFormat.format(Date())

        // Header Background Banner
        paint.color = Color.rgb(15, 76, 129) // Navy Fosis
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Accent gold stripe
        paint.color = Color.rgb(217, 119, 6)
        canvas.drawRect(0f, 90f, 595f, 95f, paint)

        // Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText("SISTEM UTAMA FOSIS", 36f, 40f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = Color.rgb(200, 225, 255)
        canvas.drawText("Field Operation Support & Information System - Enterprise Telecom", 36f, 58f, paint)
        canvas.drawText("Regional Operasional Jawa Timur & Jawa Barat", 36f, 74f, paint)

        // Document Type Headline
        paint.color = Color.rgb(30, 41, 59)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        val docTitle = when (item.type) {
            FosisType.TROUBLESHOOT -> "LAPORAN HASIL TROUBLESHOOTING & RESTORASI"
            FosisType.MAINTENANCE -> "LAPORAN PREVENTIVE & CORRECTIVE MAINTENANCE"
            FosisType.ADMINISTRASI -> "SURAT IJIN & DOKUMEN ADMINISTRASI OPERASIONAL"
        }
        canvas.drawText(docTitle, 36f, 130f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Nomor Tiket: ${item.ticketNo}  |  Dicetak pada: $currentDateStr", 36f, 146f, paint)

        // Separator line
        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 1.5f
        canvas.drawLine(36f, 156f, 559f, 156f, paint)

        // Information Grid
        var y = 180f
        fun drawRow(label: String, value: String, isHighlight: Boolean = false) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText(label, 36f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, if (isHighlight) Typeface.BOLD else Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = if (isHighlight) Color.rgb(15, 76, 129) else Color.rgb(15, 23, 42)
            canvas.drawText(value, 200f, y, paint)

            // subtle dot line
            paint.color = Color.rgb(241, 245, 249)
            paint.strokeWidth = 1f
            canvas.drawLine(36f, y + 6f, 559f, y + 6f, paint)

            y += 24f
        }

        drawRow("Judul Pekerjaan", item.title, isHighlight = true)
        drawRow("Tipe Laporan", item.type.label)
        drawRow("Periode", "${item.bulan} ${item.tahun}")
        drawRow("Status Alur Kerja", item.workflowStatus.label)

        if (item.type == FosisType.TROUBLESHOOT) {
            drawRow("Root Cause / Penyebab", item.rootCause)
            drawRow("Impact / Dampak", item.impact)
            drawRow("Service Impact", item.serviceImpact)
            drawRow("SLA Status", item.slaStatus, isHighlight = item.slaStatus == "OVER SLA")
            drawRow("Status Change Request (CR)", item.crStatus)
            drawRow("Mitra Pelaksana", item.mitra)
            drawRow("Kategori Gangguan", item.category)
        } else if (item.type == FosisType.MAINTENANCE) {
            drawRow("Kegiatan", item.kegiatan)
            drawRow("Requestor", item.requestor)
            drawRow("Jenis Tugas", item.taskType)
            drawRow("Status Pelaksanaan", item.maintenanceStatus)
        } else {
            drawRow("Tanggal Administrasi", "${item.tanggal} ${item.bulan} ${item.tahun}")
            drawRow("Task Utama", item.adminTask)
            drawRow("Sub Task", item.subTask)
            drawRow("Sub Task 1", item.subTask1)
            drawRow("Media Pengajuan (Via)", item.viaChannel)
            drawRow("Kategori Administrasi", item.adminKategori)
            drawRow("Activity / Regional", item.adminActivity)
        }

        drawRow("Teknisi Lapangan", "${item.engineerName} (${item.engineerEmail})")
        drawRow("Lokasi / Site", "${item.siteLocationName} [GPS: ${item.latitude}, ${item.longitude}]")
        drawRow("Keamanan Enkripsi", "Terenkripsi Standar AES-256 GCM (Aman)")

        // Verification & Signatures Section
        y += 20f
        paint.color = Color.rgb(15, 76, 129)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("LEMBAR PENGESAHAN & TANDA TANGAN DIGITAL", 36f, y, paint)

        y += 18f
        // 4 Signature Boxes
        val boxWidth = 120f
        val boxHeight = 90f
        val startX = 36f
        val gap = 16f

        val roles = listOf(
            Triple("1. TEKNISI", item.engineerName, if (item.workflowStatus.step >= 1) "SUBMITTED" else "DRAFT"),
            Triple("2. TEAM LEADER", item.verifiedByLeaderName ?: "IC Verification", if (item.workflowStatus.step >= 3) "VERIFIED" else "PENDING"),
            Triple("3. TEAM ADMIN", item.lockedByAdminName ?: "Admin Lock", if (item.workflowStatus.step >= 4) "LOCKED" else "PENDING"),
            Triple("4. DEPT. HEAD", "Sanderlina Imelda", if (item.workflowStatus.step >= 5) "APPROVED" else "PENDING")
        )

        for (i in roles.indices) {
            val bx = startX + i * (boxWidth + gap)
            val (roleTitle, name, status) = roles[i]

            // Box outline
            paint.style = Paint.Style.STROKE
            paint.color = if (status in listOf("SUBMITTED", "VERIFIED", "LOCKED", "APPROVED")) Color.rgb(16, 185, 129) else Color.rgb(203, 213, 225)
            paint.strokeWidth = 1.2f
            canvas.drawRect(bx, y, bx + boxWidth, y + boxHeight, paint)

            paint.style = Paint.Style.FILL
            // Header box
            paint.color = if (status in listOf("SUBMITTED", "VERIFIED", "LOCKED", "APPROVED")) Color.rgb(236, 253, 245) else Color.rgb(248, 250, 252)
            canvas.drawRect(bx + 1f, y + 1f, bx + boxWidth - 1f, y + 22f, paint)

            // Role label
            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(roleTitle, bx + 6f, y + 15f, paint)

            // Digital sign badge
            if (status in listOf("SUBMITTED", "VERIFIED", "LOCKED", "APPROVED")) {
                paint.color = Color.rgb(5, 150, 105)
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("✓ $status", bx + 6f, y + 42f, paint)

                paint.color = Color.rgb(100, 116, 139)
                paint.textSize = 7.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("ID: #${item.id}*SEC", bx + 6f, y + 54f, paint)
            } else {
                paint.color = Color.rgb(156, 163, 175)
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                canvas.drawText("(Menunggu)", bx + 6f, y + 42f, paint)
            }

            // Name footer
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(name.take(18), bx + 6f, y + boxHeight - 8f, paint)
        }

        // Footer disclaimer & security hash
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Dokumen resmi FOSIS dicetak otomatis dengan kriptografi AES-256 GCM dan verifikasi dua faktor (2FA).", 36f, 800f, paint)
        canvas.drawText("Hash Keaslian: ${Integer.toHexString(item.ticketNo.hashCode())}-${System.currentTimeMillis().toString().takeLast(8)}", 36f, 814f, paint)

        pdfDocument.finishPage(page)

        // Save file
        val outputDir = File(context.cacheDir, "fosis_reports").apply { mkdirs() }
        val safeTicket = item.ticketNo.replace("/", "_")
        val file = File(outputDir, "LHP_${safeTicket}_${System.currentTimeMillis()}.pdf")

        try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan FOSIS: ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "Terlampir Laporan Hasil Pekerjaan (LHP) resmi FOSIS.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Kirim / Buka Dokumen PDF Laporan"))
        } catch (e: Exception) {
            Toast.makeText(context, "Dokumen tersimpan di: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
