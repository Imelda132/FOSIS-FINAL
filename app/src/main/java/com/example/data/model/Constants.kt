package com.example.data.model

/**
 * ============================================================================
 * OBJECT: FosisConstants
 * TUJUAN: Pusat konfigurasi master data dan daftar opsi dropdown untuk seluruh form
 * aplikasi FOSIS (Troubleshoot, Maintenance, Administrasi, Infrastruktur FO, dll.).
 *
 * PANDUAN UBAH DATA SECARA MANUAL:
 * - Jika ingin menambah/mengubah opsi pilihan dropdown pada dialog tambah tiket,
 *   filter, atau rekap, cukup ubah/tambahkan string di dalam listOf(...) yang sesuai.
 * ============================================================================
 */
object FosisConstants {
    // --- PILIHAN BULAN & TAHUN UNTUK FILTER DAN FORM ---
    val MONTHS = listOf(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    )

    val YEARS = listOf("2023", "2024", "2025", "2026", "2027")

    // --- MASTER OPSI REKAP MAINTENANCE (Image 2) ---
    // Opsi jenis kegiatan pemeliharaan
    val KEGIATAN_MAINTENANCE = listOf(
        "Maintenance",
        "Activation Backhaul",
        "Data core upgrade",
        "Validation",
        "Storing",
        "Support"
    )

    // Opsi requestor / pemohon kegiatan pemeliharaan
    val REQUESTOR_MAINTENANCE = listOf(
        "CLS", "PI", "NETPLAN", "SM", "SO", "SERDEL", "NAM", "POP", "DC", "NOC"
    )

    // Opsi status akhir pekerjaan maintenance
    val STATUS_MAINTENANCE = listOf(
        "DONE", "ON HOLD", "ON PROGRESS", "CONTINUE", "CANCEL"
    )

    // Opsi tipe perencanaan penugasan maintenance
    val TASK_TYPE_MAINTENANCE = listOf(
        "PLAN", "UNPLAN"
    )

    // --- MASTER OPSI REKAP ADMINISTRASI (Image 3) ---
    // Opsi jenis pekerjaan administrasi (Ticket, Surat Izin Kerja, Laporan Material, dll.)
    val TASK_ADMIN = listOf(
        "REPORT", "PERMIT", "REKONSILIASI", "TICKET"
    )

    // Opsi divisi sub-task pemohon
    val SUB_TASK_ADMIN = listOf(
        "FINANCE", "VENDOR", "FAM", "PROCUREMENT", "EXTERNAL",
        "CUSTOMER CARE", "CLS", "NOC", "SERDEL", "FIELD ENGINEER",
        "MONTHLY", "YEARLY", "MIDDLE YEAR"
    )

    // Opsi detail tindakan / dokumen administrasi
    val SUB_TASK_1_ADMIN = listOf(
        "FI TR", "FI CA", "FI PC", "VE MANAGED SERVICE", "VE MATERIAL",
        "VE STOCK OPNAME", "PROC BAK", "VE BAST", "VE BAK", "MATERIAL",
        "SURAT IJIN", "OPEN TICKET", "CLOSED TICKET"
    )

    // Opsi kanal komunikasi pengajuan
    val VIA_ADMIN = listOf(
        "WA", "TEAMS", "EMAIL", "PORTAL", "VISIT", "HARDCOPY", "SOFTCOPY"
    )

    // Opsi status administrasi
    val STATUS_ADMIN = listOf(
        "DONE", "CONTINUE", "ON HOLD", "CANCEL"
    )

    // Opsi kategori administrasi
    val KATEGORI_ADMIN = listOf(
        "REQUEST INTERNAL", "REQUEST EXTERNAL", "REFUNDS"
    )

    // Opsi aktivitas administrasi
    val ACTIVITY_ADMIN = listOf(
        "SERVICE OPERATION", "SERVICE MAINTENACE", "SERVICE MAINTENANCE", "VENDOR",
        "REG. SURABAYA", "REG. BANDUNG", "ADMIN"
    )

    // --- MASTER OPSI REKAP TROUBLESHOOT (Image 1) ---
    // Opsi akar penyebab gangguan jaringan
    val ROOT_CAUSE_TROUBLESHOOT = listOf(
        "VANDALISME", "GOVERNMENT PROJECT", "MAINTENANCE WINDOW", "CABLE",
        "DEVICE", "PORT", "ELECTRICITY", "FLAPPING", "SLOW CONNECTION",
        "PATH", "INTERMITTENT", "SUPPORT", "DIAL", "NATURAL DISASTERS",
        "HEAVY VEHICLES", "ANIMAL", "PACKET LOSS", "LOCAL ACTIVITY",
        "CUSTOMER SEGMENT", "NO ISSUE", "OTHER ISP PROJECT", "CONFIGURATION",
        "3RD PARTY ISSUE"
    )

    // Opsi dampak teknis / komponen perangkat yang terdampak
    val IMPACT_TROUBLESHOOT = listOf(
        "FO CUT", "PATCHCORE", "ONT", "UTP", "UPS", "CONVERTER", "ELECTRICITY",
        "BANDWIDTH", "BENDING", "ADAPTOR CONVERTER", "ROUTER", "ACCESS POINT",
        "NO IMPACT", "SFP", "SWITCH", "FLAPPING", "EXTENDER", "WALL JACK",
        "MIKROTIK", "CONNECTION", "ROSETTE", "BAD CORE", "RTO", "DPFO",
        "OTB", "PIGTAIL", "BARREL", "DEGRADE", "STB", "BAD SPLICE", "OLT"
    )

    // Opsi segmen layanan jaringan yang terkena dampak
    val SERVICE_IMPACT_TROUBLESHOOT = listOf(
        "BACKBONE", "LINE", "BROADBAND BUSINESS", "BROADBAND HOME",
        "DARK FIBER", "NET", "IPT", "CORE", "MANAGE SERVICE", "COLO",
        "NET FLEX", "IP PUBLIC", "IPLC", "MCIX", "IPT MAX", "DWDM",
        "TIE LINE", "EIPL", "DC TO DC", "IX"
    )

    // Opsi status target SLA gangguan
    val SLA_TROUBLESHOOT = listOf(
        "IN SLA", "OVER SLA", "ON PROGRESS", "ON HOLD", "CANCEL", "NO SLA", "SUPPORT"
    )

    // Opsi status Change Request
    val CR_TROUBLESHOOT = listOf(
        "DONE", "ON PROGRESS", "NO CR"
    )

    // Opsi mitra kontraktor / tim eksekutor
    val MITRA_TROUBLESHOOT = listOf(
        "GENTRI", "FAMIKA", "SCKP", "INTERNAL", "BBS", "BWANA", "JATEK",
        "OLT", "BLAO", "TRP"
    )

    // Opsi klasifikasi tiket gangguan
    val CATEGORY_TROUBLESHOOT = listOf(
        "DOWN", "SLOW CONNECTION", "UNSTABLE", "REQUEST INTERNAL", "REQUEST EXTERNAL"
    )

    // --- INFRASTRUKTUR & AKTIVITAS LAPANGAN FO ---
    // 5 Kategori utama infrastruktur Fiber Optik
    val INFRA_CATEGORIES = listOf(
        "Closure", "Kabel", "Tiang", "Handhole", "Mainhole"
    )

    val AKTIVITAS_CLOSURE = listOf(
        "Splicing Tray Core",
        "Pembersihan Tray & Sealing",
        "Perbaikan Closure Pecah / Bocor",
        "Re-jointing & Rekonfigurasi Core",
        "Pemasangan Joint Closure Baru"
    )

    val AKTIVITAS_KABEL = listOf(
        "Penarikan Kabel FO Baru",
        "Perbaikan Kabel Putus (FO Cut)",
        "OTDR Testing & Pengukuran Loss",
        "Splicing Sambungan Kabel",
        "Perbaikan Bending / Redaman Tinggi"
    )

    val AKTIVITAS_TIANG = listOf(
        "Perapihan Slack Kabel di Tiang",
        "Pemasangan Aksesoris & Suspension Clamp",
        "Penggantian / Penegakan Tiang Miring",
        "Relokasi Tiang FO (Proyek Jalan)",
        "Pemberian Labeling & Barcode Tiang"
    )

    val AKTIVITAS_HANDHOLE = listOf(
        "Pembersihan Lumpur & Genangan Air HH",
        "Penataan Slack Kabel & Loop di Handhole",
        "Inspeksi Jalur Subduct Handhole",
        "Pemasangan & Penguncian Cover Handhole",
        "Restorasi Handhole Rusak / Amblas"
    )

    val AKTIVITAS_MAINHOLE = listOf(
        "Inspeksi Saluran Mainhole (MH)",
        "Penyedotan Air & Pembersihan Lumpur MH",
        "Penempatan Joint Closure di Bracket MH",
        "Pemasangan Warning Sign & Jalur Subduct MH",
        "Pengecekan Gas Berbahaya & Pemeliharaan Cover MH"
    )
}

