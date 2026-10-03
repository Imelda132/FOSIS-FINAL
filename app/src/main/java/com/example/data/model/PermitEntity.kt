package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PermitStatus(val label: String) {
    DRAFT("Draft Permohonan"),
    DIAJUKAN("Diajukan ke Admin"),
    DISETUJUI_ADMIN("Disetujui Admin (Aktif)"),
    DITOLAK("Ditolak Admin"),
    SELESAI("Pekerjaan Selesai")
}

@Entity(tableName = "permits")
data class PermitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val permitNo: String,
    val applicantName: String,
    val companyOrVendor: String = "Internal Field Operations",
    val siteLocation: String,
    val latitude: Double = -7.2575,
    val longitude: Double = 112.7521,
    val workType: String, // Ketinggian (Tower), Optical Fiber & Splicing, Kelistrikan/Genset, Server Datacenter, Ruang Terbatas
    val startDate: String,
    val endDate: String,
    val workHours: String = "08:00 - 17:00 WIB",
    val workersCount: Int = 2,
    val workersNames: String = "",
    val jsaChecklist: String = "Helm K3 (Safety Helmet), Rompi Reflektor, Sepatu Safety, Full Body Harness (Bila di Ketinggian), Surat Bebas Tegangan",
    val status: PermitStatus = PermitStatus.DRAFT,
    val approvedBy: String? = null,
    val approvedAt: Long? = null,
    val rejectionReason: String? = null,
    val notes: String = "",
    val importedByAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
