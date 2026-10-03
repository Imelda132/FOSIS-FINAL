package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val label: String, val level: Int, val description: String) {
    ADMIN("Administrasi", 4, "Full akses: import perijinan, kelola semua tiket, manage users, lock/unlock, delete, export"),
    IC_TROUBLESHOOT("IC Service Operation", 3, "Verifikasi & hapus tiket troubleshoot yang tidak perlu, lalu lock untuk dicek Admin"),
    IC_MAINTENANCE("IC Service Maintenance", 3, "Verifikasi & hapus tiket maintenance yang tidak perlu, lalu lock untuk dicek Admin"),
    DEPT_HEAD("Departemen Head", 2, "Hanya melihat hasil, grafik akumulatif KPI, dan memberikan komentar pada kolom evaluasi"),
    TEKNISI("Tim Lapangan", 1, "Input data & edit data di lapangan sesuai lokasi GPS perangkat terkini")
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val email: String,
    val name: String,
    val role: UserRole,
    val phone: String = "+6281234567890",
    val passwordPin: String = "1234567890",
    val is2FAEnabled: Boolean = false,
    val canCreate: Boolean = true,
    val canEdit: Boolean = true,
    val canDelete: Boolean = false,
    val canLockUnlock: Boolean = false,
    val canExportPdf: Boolean = true,
    val canImportPermit: Boolean = false,
    val canApprove: Boolean = false,
    val region: String = "Regional Operasional",
    val avatarInitials: String = ""
)

data class AuthSession(
    val currentUser: UserEntity,
    val is2FAVerified: Boolean = true,
    val pending2FACode: String? = null,
    val isSessionActive: Boolean = true
)

// 1. ADMIN
val OFFICIAL_ADMIN_USER = UserEntity(
    email = "sanderlina.imelda@gmail.com",
    name = "Sanderlina Imelda",
    role = UserRole.ADMIN,
    phone = "+6289665805758",
    is2FAEnabled = false,
    canCreate = true,
    canEdit = true,
    canDelete = true,
    canLockUnlock = true,
    canExportPdf = true,
    canImportPermit = true,
    canApprove = true,
    region = "Admin",
    avatarInitials = "SI"
)

// 2. DEPARTEMENT HEAD
val OFFICIAL_DEPT_HEAD = UserEntity(
    email = "rizki.firdaus@fosis-ops.id",
    name = "Mas Rizki Firdaus",
    role = UserRole.DEPT_HEAD,
    phone = "+6281155556666",
    is2FAEnabled = false,
    canCreate = true,
    canEdit = true,
    canDelete = false,
    canLockUnlock = false,
    canExportPdf = true,
    canImportPermit = true,
    canApprove = true,
    region = "Departement Head",
    avatarInitials = "RF"
)

// 3. DAFTAR LENGKAP PENGGUNA RESMI SISTEM FOSIS (TIDAK BOLEH DIRUBAH)
val PRESET_SYSTEM_USERS: List<UserEntity> = listOf(
    // --- ADMIN ---
    OFFICIAL_ADMIN_USER,

    // --- DEPARTEMENT HEAD ---
    OFFICIAL_DEPT_HEAD,

    // --- IC SERVICE OPERATION (13 PERSONEL) ---
    UserEntity(
        email = "alqori.dewantara@fosis-ops.id",
        name = "Alqori Dewantara",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010001",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "AD"
    ),
    UserEntity(
        email = "ahmad.ibrahim@fosis-ops.id",
        name = "Ahmad Ibrahim",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010002",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "AI"
    ),
    UserEntity(
        email = "budi.mulya@fosis-ops.id",
        name = "Budi Mulya",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010004",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "BM"
    ),
    UserEntity(
        email = "carolus.borromeus@fosis-ops.id",
        name = "Carolus Borromeus",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010005",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "CB"
    ),
    UserEntity(
        email = "dimas.wahyu.pratama@fosis-ops.id",
        name = "Dimas Wahyu Pratama",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010006",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "DW"
    ),
    UserEntity(
        email = "muhamad.fadli@fosis-ops.id",
        name = "Muhamad Fadli",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281322223333",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "MF"
    ),
    UserEntity(
        email = "muhammad.hadi.hendarsyah@fosis-ops.id",
        name = "Muhammad Hadi Hendarsyah",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010007",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "MH"
    ),
    UserEntity(
        email = "muhammad.sandi.pratama@fosis-ops.id",
        name = "Muhammad Sandi Pratama",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010008",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "MS"
    ),
    UserEntity(
        email = "irwan.sumarwan@fosis-ops.id",
        name = "Irwan Sumarwan",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010009",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "IS"
    ),
    UserEntity(
        email = "syahril.affan@fosis-ops.id",
        name = "Syahril Affan",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281210010010",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "SA"
    ),
    UserEntity(
        email = "gergorius.galus.gala.toron@fosis-ops.id",
        name = "Gergorius Galus Gala Toron",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281530010001",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "GG"
    ),
    UserEntity(
        email = "arry.dwi.putranto@fosis-ops.id",
        name = "Arry Dwi Putranto",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281530010002",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "AD"
    ),
    UserEntity(
        email = "deni.herwanto@fosis-ops.id",
        name = "Deni Herwanto",
        role = UserRole.IC_TROUBLESHOOT,
        phone = "+6281530010003",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Operation",
        avatarInitials = "DH"
    ),

    // --- IC SERVICE MAINTENANCE (6 PERSONEL) ---
    UserEntity(
        email = "nemi.ginting@fosis-ops.id",
        name = "Nemi BR Ginting",
        role = UserRole.IC_MAINTENANCE,
        phone = "+6281333334444",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Maintenance",
        avatarInitials = "NG"
    ),
    UserEntity(
        email = "depri.alnopen@fosis-ops.id",
        name = "Depri Alnopen",
        role = UserRole.IC_MAINTENANCE,
        phone = "+6281320010001",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Maintenance",
        avatarInitials = "DA"
    ),
    UserEntity(
        email = "hadzy.anil.hakim@fosis-ops.id",
        name = "Hadzy Anil Hakim",
        role = UserRole.IC_MAINTENANCE,
        phone = "+6281320010002",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Maintenance",
        avatarInitials = "HA"
    ),
    UserEntity(
        email = "mohammad.rusli@fosis-ops.id",
        name = "Mohammad Rusli",
        role = UserRole.IC_MAINTENANCE,
        phone = "+6281320010003",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Maintenance",
        avatarInitials = "MR"
    ),
    UserEntity(
        email = "toto.daifalahi@fosis-ops.id",
        name = "Toto Daifalahi",
        role = UserRole.IC_MAINTENANCE,
        phone = "+6281320010004",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Maintenance",
        avatarInitials = "TD"
    ),
    UserEntity(
        email = "yosep.damianus@fosis-ops.id",
        name = "Yosep Damianus",
        role = UserRole.IC_MAINTENANCE,
        phone = "+6281320010005",
        canCreate = true,
        canEdit = true,
        canDelete = true,
        canLockUnlock = true,
        canExportPdf = true,
        canImportPermit = true,
        region = "IC Service Maintenance",
        avatarInitials = "YD"
    )
)
