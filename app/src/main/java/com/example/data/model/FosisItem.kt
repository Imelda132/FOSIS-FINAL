package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ============================================================================
 * ENUM: FosisType
 * TUJUAN: Menentukan kategori/tipe modul utama dari setiap tiket atau catatan kerja.
 * - TROUBLESHOOT : Modul penanganan gangguan jaringan & perbaikan (Image 1)
 * - MAINTENANCE  : Modul pemeliharaan preventif / terjadwal & inspeksi OSP (Image 2)
 * - ADMINISTRASI : Modul rekap administrasi (Surat Izin Kerja, Tiket CC, Report Material) (Image 3)
 * ============================================================================
 */
enum class FosisType(val label: String) {
    TROUBLESHOOT("Troubleshoot"),
    MAINTENANCE("Maintenance"),
    ADMINISTRASI("Administrasi")
}

/**
 * ============================================================================
 * ENUM: WorkflowStatus
 * TUJUAN: Mengatur alur otorisasi berjenjang (4 Level Approval):
 * 1. DRAFT              : Dibuat awal oleh tim teknisi lapangan
 * 2. SUBMITTED_TEKNISI  : Diajukan ke Incident Commander / Tim Leader untuk verifikasi
 * 3. VERIFIED_LEADER    : Disetujui oleh Team Leader, siap diverifikasi Admin
 * 4. LOCKED_ADMIN       : Dikunci oleh Admin Operasional (data resmi terkunci)
 * 5. APPROVED_DEPT_HEAD : Disahkan secara final oleh Department Head dengan tanda tangan digital
 * 0. REJECTED           : Dikembalikan ke teknisi untuk revisi
 * ============================================================================
 */
enum class WorkflowStatus(val label: String, val step: Int) {
    DRAFT("Draft Teknisi", 1),
    SUBMITTED_TEKNISI("Menunggu Verifikasi IC", 2),
    VERIFIED_LEADER("Diverifikasi Team Leader", 3),
    LOCKED_ADMIN("Terkunci Admin", 4),
    APPROVED_DEPT_HEAD("Disahkan Dept. Head", 5),
    REJECTED("Perlu Revisi", 0)
}

/**
 * ============================================================================
 * ENTITY: FosisItemEntity
 * TUJUAN: Tabel utama database Room ("fosis_items") yang menyimpan seluruh
 * data operasional lapangan, baik Troubleshoot, Maintenance, maupun Administrasi.
 *
 * PANDUAN UBAH DATA SECARA MANUAL:
 * - Ubah nilai default atau tipe data jika ada perubahan spesifikasi format.
 * - Tambahkan kolom baru di sini jika ada parameter form tambahan.
 * ============================================================================
 */
@Entity(tableName = "fosis_items")
data class FosisItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                         // ID unik auto-increment database
    val ticketNo: String,                     // Nomor Tiket (cth: [E/260101/0002] atau MNT-260105-001)
    val type: FosisType,                      // Tipe Item (TROUBLESHOOT, MAINTENANCE, ADMINISTRASI)
    val title: String,                        // Judul Tiket / Deskripsi Pelanggan / Detail Pekerjaan
    val bulan: String,                        // Bulan Operasional (JANUARY, FEBRUARY, dst.)
    val tahun: String,                        // Tahun Operasional (2025, 2026, 2027)
    
    // --- STATUS & WORKFLOW OTORISASI ---
    val workflowStatus: WorkflowStatus = WorkflowStatus.DRAFT, // Status tahapan persetujuan
    val isLocked: Boolean = false,            // Flag apakah tiket sudah dikunci (read-only)
    val isUrgent: Boolean = false,            // Flag prioritas tinggi / darurat (Over SLA)
    
    // --- PARAMETER MODUL MAINTENANCE (Image 2) ---
    val kegiatan: String = "",                // Jenis kegiatan (Maintenance, Activation Backhaul, Storing, dll.)
    val requestor: String = "",               // Pihak pemohon (SM, SERDEL, CLS, NETPLAN, NOC, dll.)
    val maintenanceStatus: String = "DONE",   // Status pekerjaan (DONE, ON HOLD, ON PROGRESS, CANCEL)
    val taskType: String = "PLAN",            // Tipe penugasan (PLAN atau UNPLAN)
    
    // --- PENGGUNAAN MATERIAL & DOKUMEN OTDR ---
    val materialUsed: String = "",            // Rincian material yang terpakai (Closure, Patchcore, ONT, dll.)
    val otdrPdfAttachments: String = "",      // Lampiran hasil uji ukur OTDR dalam format PDF (maks. 50 file)
    
    // --- KATEGORI INFRASTRUKTUR FIBER OPTIK ---
    val infraCategory: String = "Kabel",      // Kategori aset FO (Closure, Kabel, Tiang, Handhole, Mainhole)
    val infraActivity: String = "",           // Aktivitas pada infrastruktur (Validasi, Sambung, Tarik, dll.)
    
    // --- PARAMETER MODUL TROUBLESHOOT (Image 1) ---
    val rootCause: String = "",               // Akar masalah (CABLE, DEVICE, GOVERNMENT PROJECT, HEAVY VEHICLES, dll.)
    val impact: String = "",                  // Dampak teknis (BAD CORE, FO CUT, SFP, MIKROTIK, ONT, dll.)
    val serviceImpact: String = "",           // Dampak layanan (BACKBONE, DARK FIBER, BROADBAND HOME, NET, dll.)
    val slaStatus: String = "IN SLA",         // Pemenuhan target SLA (IN SLA atau OVER SLA)
    val crStatus: String = "DONE",            // Status Change Request (NO CR, DONE, ON PROGRESS)
    val mitra: String = "INTERNAL",           // Pelaksana pekerjaan (INTERNAL, SCKP, FAMIKA, GENTRI, dll.)
    val category: String = "DOWN",            // Kategori gangguan (DOWN, UNSTABLE, REQUEST EXTERNAL)
    
    // --- PARAMETER MODUL ADMINISTRASI (Image 3) ---
    val tanggal: Int = 1,                     // Tanggal pelaksanaan (1 s/d 31)
    val adminTask: String = "",               // Jenis Task Administrasi (PERMIT, TICKET, REPORT, REKONSILIASI)
    val subTask: String = "",                 // Divisi Sub Task (EXTERNAL, CUSTOMER CARE, FAM, VENDOR, dll.)
    val subTask1: String = "",                // Detail Sub Task 1 (SURAT IJIN, CLOSED TICKET, MATERIAL, dll.)
    val viaChannel: String = "PORTAL",        // Kanal pengajuan (PORTAL, WA, TEAMS, EMAIL, HARDCOPY)
    val adminStatus: String = "DONE",         // Status administrasi (DONE, CONTINUE, ON HOLD, CANCEL)
    val adminKategori: String = "REQUEST INTERNAL", // Klasifikasi permintaan (REQUEST INTERNAL, REQUEST EXTERNAL)
    val adminActivity: String = "SERVICE OPERATION", // Bidang aktivitas (SERVICE OPERATION, SERVICE MAINTENANCE, VENDOR)
    
    // --- LOKASI GPS & PERSONEL LAPANGAN ---
    val engineerName: String = "Alqori Dewantara",      // Nama personil yang menangani tiket
    val engineerEmail: String = "alqori.dewantara@fosis-ops.id", // Email personil
    val siteLocationName: String = "POP Jakarta",       // Nama site/gedung/titik koordinat
    val latitude: Double = -7.2575,                  // Titik latitude GPS untuk peta pemantauan
    val longitude: Double = 112.7521,                // Titik longitude GPS untuk peta pemantauan
    
    // --- KEAMANAN & ENKRIPSI AES-256 ---
    val encryptedSecretNotes: String = "",    // Catatan teknis terenkripsi (AES-256 GCM)
    val isEncrypted: Boolean = true,          // Status keamanan data catatan
    
    // --- AUDIT TRAIL, TANGGAL & DIGITAL SIGNATURE ---
    val submittedByTeknisiAt: Long? = null,   // Timestamp teknisi submit laporan
    val verifiedByLeaderAt: Long? = null,     // Timestamp verifikasi oleh Team Leader
    val verifiedByLeaderName: String? = null, // Nama Team Leader pemverifikasi
    val lockedByAdminAt: Long? = null,        // Timestamp penguncian oleh Admin Operasional
    val lockedByAdminName: String? = null,    // Nama Admin yang mengunci tiket
    val approvedByDeptHeadAt: Long? = null,   // Timestamp pengesahan oleh Dept. Head
    val deptHeadComment: String? = null,      // Catatan evaluasi/disposisi Dept. Head
    val digitalSignatureHash: String? = null, // Hash verifikasi integritas tanda tangan digital
    
    // --- SINKRONISASI OFFLINE-FIRST ---
    val isSynced: Boolean = true,             // Status sinkronisasi ke server (true = online, false = offline queue)
    val lastUpdated: Long = System.currentTimeMillis() // Waktu pembaruan data terakhir
) {
    /**
     * Mengembalikan ikon emoji visual sesuai jenis infrastruktur FO untuk memudahkan identifikasi pada daftar UI.
     */
    fun getInfraEmoji(): String = when (infraCategory.lowercase()) {
        "closure" -> "🔌"
        "kabel" -> "🧵"
        "tiang" -> "🗼"
        "handhole" -> "🕳️"
        "mainhole", "manhole" -> "🏢"
        else -> "⚡"
    }

    /**
     * Otomatis mendeteksi kategori infrastruktur berdasarkan kata kunci pada judul/lokasi jika belum terisi eksplisit.
     */
    fun getEffectiveInfraCategory(): String {
        if (infraCategory.isNotBlank()) return infraCategory
        return when {
            title.contains("closure", ignoreCase = true) || rootCause.contains("closure", ignoreCase = true) -> "Closure"
            title.contains("tiang", ignoreCase = true) || rootCause.contains("tiang", ignoreCase = true) -> "Tiang"
            title.contains("handhole", ignoreCase = true) || siteLocationName.contains("handhole", ignoreCase = true) || siteLocationName.contains("HH", ignoreCase = true) -> "Handhole"
            title.contains("mainhole", ignoreCase = true) || title.contains("manhole", ignoreCase = true) || siteLocationName.contains("MH", ignoreCase = true) -> "Mainhole"
            else -> "Kabel"
        }
    }
}

