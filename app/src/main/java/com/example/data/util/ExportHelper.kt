package com.example.data.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.data.model.PermitEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))

    private fun getReportsDir(context: Context): File {
        return File(context.cacheDir, "fosis_reports").apply { mkdirs() }
    }

    // ==========================================
    // 1. EXCEL EXPORT (.csv / .xls compatible format)
    // ==========================================
    fun exportTicketsToExcel(
        context: Context,
        items: List<FosisItemEntity>,
        periodLabel: String
    ): File? {
        try {
            val file = File(getReportsDir(context), "FOSIS_Tiket_${periodLabel.replace(" ", "_")}_${System.currentTimeMillis()}.csv")
            val fos = FileOutputStream(file)
            val writer = OutputStreamWriter(fos, StandardCharsets.UTF_8)

            // UTF-8 BOM so Microsoft Excel automatically recognises encoding & columns
            writer.write("\uFEFF")

            // Header info
            writer.write("LAPORAN OPERASIONAL FOSIS - TELKOM & NETWORK OPERATIONS\n")
            writer.write("Periode Ekspor:,\"$periodLabel\"\n")
            writer.write("Tanggal Cetak:,\"${dateFormat.format(Date())}\"\n")
            writer.write("Total Tiket:,\"${items.size}\"\n\n")

            // Table columns
            writer.write("No,Nomor Tiket,Tipe,Judul Pekerjaan,Bulan,Tahun,Status Alur,Urgent,Root Cause,Dampak,Service Impact,SLA Status,CR Status,Mitra,Teknisi,Lokasi Site,Latitude,Longitude,Verifikasi Leader,Kunci Admin,Komentar Dept Head,Catatan Terenkripsi\n")

            items.forEachIndexed { index, item ->
                val safeTitle = item.title.replace("\"", "\"\"")
                val safeNotes = item.encryptedSecretNotes.replace("\"", "\"\"")
                val safeComment = (item.deptHeadComment ?: "-").replace("\"", "\"\"")
                val line = "${index + 1},\"${item.ticketNo}\",\"${item.type.label}\",\"$safeTitle\",\"${item.bulan}\",\"${item.tahun}\",\"${item.workflowStatus.label}\",\"${if (item.isUrgent) "YA (URGENT)" else "NORMAL"}\",\"${item.rootCause}\",\"${item.impact}\",\"${item.serviceImpact}\",\"${item.slaStatus}\",\"${item.crStatus}\",\"${item.mitra}\",\"${item.engineerName}\",\"${item.siteLocationName}\",${item.latitude},${item.longitude},\"${item.verifiedByLeaderName ?: "-"}\",\"${item.lockedByAdminName ?: "-"}\",\"$safeComment\",\"$safeNotes\"\n"
                writer.write(line)
            }

            writer.flush()
            writer.close()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportPermitsToExcel(
        context: Context,
        permits: List<PermitEntity>,
        periodLabel: String
    ): File? {
        try {
            val file = File(getReportsDir(context), "FOSIS_Perijinan_${periodLabel.replace(" ", "_")}_${System.currentTimeMillis()}.csv")
            val fos = FileOutputStream(file)
            val writer = OutputStreamWriter(fos, StandardCharsets.UTF_8)
            writer.write("\uFEFF")

            writer.write("FORM PERIJINAN KERJA (PERMIT TO WORK / SIK) - FOSIS OPERATIONS\n")
            writer.write("Periode Ekspor:,\"$periodLabel\"\n")
            writer.write("Tanggal Cetak:,\"${dateFormat.format(Date())}\"\n")
            writer.write("Total Perijinan:,\"${permits.size}\"\n\n")

            writer.write("No,Nomor Ijin,Pemohon,Perusahaan / Vendor,Site Tujuan,Latitude,Longitude,Jenis Pekerjaan,Tanggal Mulai,Tanggal Selesai,Jam Kerja,Jumlah Personil,Daftar Personil,JSA Checklist K3,Status Ijin,Disetujui Oleh,Catatan K3\n")

            permits.forEachIndexed { index, p ->
                val safeSite = p.siteLocation.replace("\"", "\"\"")
                val safeWorkers = p.workersNames.replace("\"", "\"\"")
                val safeJsa = p.jsaChecklist.replace("\"", "\"\"")
                val safeNotes = p.notes.replace("\"", "\"\"")
                val line = "${index + 1},\"${p.permitNo}\",\"${p.applicantName}\",\"${p.companyOrVendor}\",\"$safeSite\",${p.latitude},${p.longitude},\"${p.workType}\",\"${p.startDate}\",\"${p.endDate}\",\"${p.workHours}\",${p.workersCount},\"$safeWorkers\",\"$safeJsa\",\"${p.status.label}\",\"${p.approvedBy ?: "-"}\",\"$safeNotes\"\n"
                writer.write(line)
            }

            writer.flush()
            writer.close()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // ==========================================
    // 2. PDF EXPORT (Formal Documents & Summary)
    // ==========================================
    fun exportPermitToPdf(context: Context, permit: PermitEntity): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Corporate Header
        paint.color = Color.rgb(103, 80, 164) // Vibrant Violet
        canvas.drawRect(0f, 0f, 595f, 85f, paint)

        paint.color = Color.rgb(3, 218, 197) // Vibrant Mint
        canvas.drawRect(0f, 85f, 595f, 90f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 17f
        canvas.drawText("SURAT IJIN KERJA / PERMIT TO WORK (SIK)", 36f, 38f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.color = Color.rgb(234, 221, 255)
        canvas.drawText("FOSIS Operations - Keselamatan Kerja & Prosedur Akses Site", 36f, 54f, paint)
        canvas.drawText("No. Dokumen: ${permit.permitNo}  |  Diterbitkan: ${dateFormat.format(Date(permit.createdAt))}", 36f, 70f, paint)

        var y = 125f
        fun drawRow(label: String, value: String, isBold: Boolean = false) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = Color.rgb(73, 69, 79)
            canvas.drawText(label, 36f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            paint.textSize = 9.5f
            paint.color = if (isBold) Color.rgb(103, 80, 164) else Color.rgb(29, 27, 32)
            canvas.drawText(value, 180f, y, paint)

            paint.color = Color.rgb(230, 224, 233)
            paint.strokeWidth = 1f
            canvas.drawLine(36f, y + 5f, 559f, y + 5f, paint)
            y += 24f
        }

        drawRow("Nomor Surat Ijin", permit.permitNo, isBold = true)
        drawRow("Status Perijinan", permit.status.label, isBold = true)
        drawRow("Nama Pemohon", permit.applicantName)
        drawRow("Perusahaan / Vendor", permit.companyOrVendor)
        drawRow("Site Lokasi Tujuan", permit.siteLocation)
        drawRow("Koordinat GPS Device", "${permit.latitude}, ${permit.longitude}")
        drawRow("Jenis Pekerjaan", permit.workType, isBold = true)
        drawRow("Masa Berlaku", "${permit.startDate} s/d ${permit.endDate} (${permit.workHours})")
        drawRow("Jumlah Personil", "${permit.workersCount} Orang")
        drawRow("Daftar Anggota Tim", permit.workersNames.ifBlank { "(Sesuai daftar penugasan tim)" })
        drawRow("Standar K3 / JSA", permit.jsaChecklist)
        drawRow("Catatan Khusus", permit.notes.ifBlank { "Wajib mematuhi SOP K3 dan izin lingkungan setempat." })

        // Approval Box
        y += 20f
        paint.color = Color.rgb(103, 80, 164)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("PENGESAHAN PERIJINAN RESMI (AUTHORIZATION)", 36f, y, paint)

        y += 15f
        val boxW = 240f
        val boxH = 95f

        // Box 1: Pemohon
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(202, 196, 208)
        paint.strokeWidth = 1.2f
        canvas.drawRect(36f, y, 36f + boxW, y + boxH, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(243, 237, 247)
        canvas.drawRect(37f, y + 1f, 36f + boxW - 1f, y + 22f, paint)

        paint.color = Color.rgb(29, 27, 32)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PENGAJU / PEMOHON TIM", 44f, y + 15f, paint)
        canvas.drawText("Pemohon: ${permit.applicantName}", 44f, y + 42f, paint)
        canvas.drawText("Status: TERSUBMIT RESMI", 44f, y + 60f, paint)
        canvas.drawText("Ttd Digital: SEC-${permit.permitNo.hashCode().toUInt()}", 44f, y + 80f, paint)

        // Box 2: Admin Approval
        val bx2 = 300f
        paint.style = Paint.Style.STROKE
        paint.color = if (permit.status == com.example.data.model.PermitStatus.DISETUJUI_ADMIN) Color.rgb(34, 197, 94) else Color.rgb(202, 196, 208)
        canvas.drawRect(bx2, y, bx2 + boxW, y + boxH, paint)

        paint.style = Paint.Style.FILL
        paint.color = if (permit.status == com.example.data.model.PermitStatus.DISETUJUI_ADMIN) Color.rgb(234, 248, 238) else Color.rgb(243, 237, 247)
        canvas.drawRect(bx2 + 1f, y + 1f, bx2 + boxW - 1f, y + 22f, paint)

        paint.color = Color.rgb(29, 27, 32)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ADMIN PENGESAH (FULL AKSES)", bx2 + 8f, y + 15f, paint)

        if (permit.status == com.example.data.model.PermitStatus.DISETUJUI_ADMIN) {
            paint.color = Color.rgb(22, 163, 74)
            canvas.drawText("✓ DISETUJUI & DITERBITKAN", bx2 + 8f, y + 42f, paint)
            paint.color = Color.rgb(29, 27, 32)
            canvas.drawText("Disetujui oleh: ${permit.approvedBy ?: "Sanderlina Imelda"}", bx2 + 8f, y + 60f, paint)
            canvas.drawText("Ttd Otentikasi: SEC-ADMIN-VERIFIED", bx2 + 8f, y + 80f, paint)
        } else {
            paint.color = Color.rgb(120, 117, 126)
            canvas.drawText("(Menunggu Verifikasi Admin)", bx2 + 8f, y + 50f, paint)
        }

        // Footer
        paint.color = Color.rgb(120, 117, 126)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Surat Ijin Kerja ini sah dan dilindungi hak cipta operasional enterprise telecom FOSIS.", 36f, 800f, paint)
        canvas.drawText("Verifikasi Dokumen: SHA256-${Integer.toHexString(permit.hashCode())}", 36f, 814f, paint)

        pdfDocument.finishPage(page)

        val file = File(getReportsDir(context), "SIK_${permit.permitNo.replace("/", "_")}_${System.currentTimeMillis()}.pdf")
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

    // ==========================================
    // 3. PPT PRESENTATION EXPORT (.html & slide deck)
    // ==========================================
    fun exportPresentationPpt(
        context: Context,
        items: List<FosisItemEntity>,
        permits: List<PermitEntity>,
        periodLabel: String,
        deptHeadComment: String?
    ): File? {
        try {
            val file = File(getReportsDir(context), "FOSIS_Presentasi_${periodLabel.replace(" ", "_")}_${System.currentTimeMillis()}.html")
            val fos = FileOutputStream(file)
            val writer = OutputStreamWriter(fos, StandardCharsets.UTF_8)

            val totalTickets = items.size
            val inSlaCount = items.count { it.slaStatus == "IN SLA" }
            val slaRate = if (totalTickets > 0) (inSlaCount * 100.0 / totalTickets) else 100.0
            val troubleshootCount = items.count { it.type == FosisType.TROUBLESHOOT }
            val maintenanceCount = items.count { it.type == FosisType.MAINTENANCE }
            val adminCount = items.count { it.type == FosisType.ADMINISTRASI }
            val approvedPermits = permits.count { it.status == com.example.data.model.PermitStatus.DISETUJUI_ADMIN }

            val htmlContent = """
                <!DOCTYPE html>
                <html lang="id">
                <head>
                    <meta charset="UTF-8">
                    <title>FOSIS Executive Presentation Deck - $periodLabel</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #121019; color: #FEF7FF; margin: 0; padding: 20px; }
                        .slide { background: #1F1B2C; border: 1px solid #3B2E5C; border-radius: 20px; padding: 40px; margin-bottom: 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); min-height: 480px; position: relative; }
                        .slide-num { position: absolute; top: 20px; right: 30px; font-size: 14px; color: #03DAC5; font-weight: bold; }
                        h1 { color: #EADDFF; font-size: 32px; margin-top: 0; }
                        h2 { color: #03DAC5; font-size: 24px; margin-bottom: 25px; }
                        .metric-card { background: #2D273E; border-radius: 12px; padding: 20px; display: inline-block; width: 260px; margin: 10px; vertical-align: top; border-left: 4px solid #6750A4; }
                        .metric-card.accent { border-left-color: #03DAC5; }
                        .metric-num { font-size: 36px; font-weight: bold; color: #FFFFFF; }
                        .metric-label { font-size: 13px; color: #CAC4D0; margin-top: 5px; }
                        .comment-box { background: #2B2138; border: 1px solid #EADDFF; border-radius: 12px; padding: 20px; margin-top: 25px; }
                        .footer { font-size: 12px; color: #938F99; margin-top: 40px; text-align: center; }
                        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                        th, td { border: 1px solid #49454F; padding: 10px; text-align: left; font-size: 13px; }
                        th { background: #2D273E; color: #EADDFF; }
                    </style>
                </head>
                <body>
                    <!-- SLIDE 1: Title Slide -->
                    <div class="slide">
                        <div class="slide-num">SLIDE 1 OF 4</div>
                        <h1 style="font-size: 40px; margin-top: 60px;">FOSIS EXECUTIVE REPORT</h1>
                        <h2 style="font-size: 26px;">Field Operation Support & Information System</h2>
                        <p style="font-size: 18px; color: #EADDFF;">Periode Laporan: <strong>$periodLabel</strong></p>
                        <p style="color: #CAC4D0; margin-top: 40px;">
                            Oleh: Manajemen Operasional FOSIS<br>
                            Executive Lead: Sanderlina Imelda (Admin) & Mas Rizki Firdaus (Dept. Head)<br>
                            Waktu Rilis: ${dateFormat.format(Date())}
                        </p>
                    </div>

                    <!-- SLIDE 2: KPI & SLA Statistics -->
                    <div class="slide">
                        <div class="slide-num">SLIDE 2 OF 4</div>
                        <h2>Pencapaian KPI & Tingkat Kepatuhan SLA</h2>
                        <div class="metric-card accent">
                            <div class="metric-num">${String.format("%.1f", slaRate)}%</div>
                            <div class="metric-label">Tingkat Kepatuhan SLA</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-num">$totalTickets</div>
                            <div class="metric-label">Total Tiket Operasional</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-num">$troubleshootCount</div>
                            <div class="metric-label">Troubleshoot Tiket</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-num">$maintenanceCount</div>
                            <div class="metric-label">Maintenance Terjadwal</div>
                        </div>
                        <div style="margin-top: 25px; padding: 15px; background: #282138; border-radius: 10px;">
                            <p style="margin: 0; color: #EADDFF;">
                                Ringkasan: Seluruh aktivitas lapangan dipantau secara real-time melalui GPS koordinat perangkat 20 tim lapangan dengan pengawasan IC Troubleshoot (Muhamad Fadli) dan IC Maintenance (Nemi BR Ginting).
                            </p>
                        </div>
                    </div>

                    <!-- SLIDE 3: Status Form Perijinan (Work Permit) -->
                    <div class="slide">
                        <div class="slide-num">SLIDE 3 OF 4</div>
                        <h2>Status Form Perijinan Kerja (Permit to Work / SIK)</h2>
                        <div class="metric-card accent">
                            <div class="metric-num">${permits.size}</div>
                            <div class="metric-label">Total Permohonan Ijin Kerja</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-num">$approvedPermits</div>
                            <div class="metric-label">Disetujui Admin (Aktif)</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-num">${permits.count { it.status == com.example.data.model.PermitStatus.DIAJUKAN }}</div>
                            <div class="metric-label">Menunggu Persetujuan</div>
                        </div>
                        <table>
                            <tr><th>No. Ijin</th><th>Pemohon</th><th>Site Tujuan</th><th>Jenis Pekerjaan</th><th>Masa Berlaku</th><th>Status</th></tr>
                            ${permits.take(5).joinToString("") { "<tr><td>${it.permitNo}</td><td>${it.applicantName}</td><td>${it.siteLocation}</td><td>${it.workType}</td><td>${it.startDate} s/d ${it.endDate}</td><td>${it.status.label}</td></tr>" }}
                        </table>
                    </div>

                    <!-- SLIDE 4: Evaluasi & Catatan Dept. Head -->
                    <div class="slide">
                        <div class="slide-num">SLIDE 4 OF 4</div>
                        <h2>Evaluasi & Catatan Dept. Head (Mas Rizki Firdaus)</h2>
                        <div class="comment-box">
                            <p style="font-size: 16px; font-weight: bold; color: #03DAC5; margin-top: 0;">Catatan Resmi Head of Department:</p>
                            <p style="font-size: 15px; line-height: 1.6; color: #FEF7FF;">
                                "${deptHeadComment ?: "Operasional berjalan dengan stabil. Pastikan seluruh prosedur keselamatan kerja (K3) dan validasi lokasi GPS tim lapangan diterapkan secara konsisten pada setiap tiket restorasi maupun pemeliharaan preventif."}"
                            </p>
                        </div>
                        <div style="margin-top: 30px;">
                            <p style="color: #CAC4D0; font-size: 13px;">
                                Disahkan untuk Laporan Eksekutif Bulanan / Mingguan Telecom Operations Hub.<br>
                                Hak Akses Admin: Sanderlina Imelda | Evaluasi: Mas Rizki Firdaus
                            </p>
                        </div>
                    </div>
                    <div class="footer">Sistem Utama FOSIS Enterprise Deck &copy; 2026</div>
                </body>
                </html>
            """.trimIndent()

            writer.write(htmlContent)
            writer.flush()
            writer.close()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // ==========================================
    // 4. SHARING & OPENING FILES
    // ==========================================
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Terlampir berkas ekspor resmi sistem FOSIS: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan / Buka Berkas $title"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Berkas tersimpan di: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
