package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk Monitoring Penggunaan Kendaraan Lapangan.
 * Kolom Sesuai Format Tabel:
 * 1. Tanggal
 * 2. PIC
 * 3. Team (SO / SM)
 * 4. Time out
 * 5. Ticket
 * 6. Plat kendaraan
 * 7. Type Kendaraan
 * 8. Ket. Tujuan
 */
@Entity(tableName = "vehicle_logs")
data class VehicleLogEntity(
    @PrimaryKey
    val id: String,
    val tanggal: String,          // Tanggal Pinjam (e.g. "10/09/2026")
    val pic: String,              // PIC (e.g. "Hadi & Qori", "Carlos & Syahril", "Yosep", "Anil", "M.Rusli")
    val team: String = "SO",      // Team (e.g. "SO", "SM")
    val timeOut: String,          // Time out (e.g. "10:38")
    val ticket: String,           // Ticket (e.g. "E/260909/0028", "Storing", "Aktifasi Switch Ring 7")
    val platKendaraan: String,    // Plat kendaraan (e.g. "B 2924 SKN", "B 4605 SOU", "B 1241 U", "B 4616 SOU")
    val typeKendaraan: String,    // Type Kendaraan (e.g. "Grandmax", "LEXY", "AVANZA", "NMAX", "Vario 160", "Innova")
    val ketTujuan: String,        // Ket. Tujuan (e.g. "Cilandak", "Mid Plaza", "DurenTiga", "Sudirman", "Cyber 1")
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val statusArmada: String = "DIPINJAM", // DIPINJAM, MENUNGGU_ACC_KEMBALI, SELESAI
    val timeIn: String = "-",               // Waktu Pengembalian
    val tanggalKembali: String = "-",       // Tanggal Pengembalian
    val fuelLevel: String = "Normal",       // Level BBM saat berangkat
    val fuelLevelIn: String = "Normal",     // Level BBM saat kembali
    val odometerOut: String = "-",          // KM Berangkat
    val odometerIn: String = "-",           // KM Kembali
    val catatanPinjam: String = "-",        // Catatan saat peminjaman
    val catatanKembali: String = "-",       // Catatan kondisi kendaraan saat pengembalian (ban, rem, kebersihan, dll.)
    val accBy: String = "-",                // Nama IC / Pejabat yang ACC (e.g., Muhamad Fadli, Nemi BR Ginting)
    val accRole: String = "-",              // Role IC yang ACC (IC Service Operation / IC Maintenance)
    val accTime: String = "-",              // Waktu ACC oleh IC
    val accNotes: String = "-"              // Catatan verifikasi dari IC
) {
    val isMotor: Boolean
        get() = typeKendaraan.contains("LEXY", ignoreCase = true) ||
                typeKendaraan.contains("NMAX", ignoreCase = true) ||
                typeKendaraan.contains("Vario", ignoreCase = true) ||
                typeKendaraan.contains("Beat", ignoreCase = true) ||
                typeKendaraan.contains("PCX", ignoreCase = true) ||
                typeKendaraan.contains("Aerox", ignoreCase = true) ||
                typeKendaraan.contains("Motor", ignoreCase = true) ||
                platKendaraan.contains("SOU", ignoreCase = true) ||
                platKendaraan.contains("SOX", ignoreCase = true)

    val kategoriKendaraan: String
        get() = if (isMotor) "Motor" else "Mobil"
}

/**
 * Dataset Dummy Kendaraan Sesuai Rekap Logbook Lapangan (Mobil & Motor Lintas Minggu & Bulan)
 */
val INITIAL_VEHICLE_LOGS = listOf(
    // === DATA LOGBOOK DARI EXCEL FORMAT KENDARAAN KELUAR ===
    VehicleLogEntity(
        id = "VEH-2026-001",
        tanggal = "10/09/2026",
        pic = "Hadi & Qori",
        team = "SO",
        timeOut = "10:38",
        ticket = "E/260909/0028",
        platKendaraan = "B 2924 SKN",
        typeKendaraan = "Grandmax",
        ketTujuan = "Cilandak",
        latitude = -6.2905,
        longitude = 106.7975,
        statusArmada = "DIPINJAM",
        timeIn = "-",
        tanggalKembali = "-",
        fuelLevel = "Full (85%)",
        odometerOut = "45,210 KM",
        catatanPinjam = "Peminjaman mobil Grandmax untuk perbaikan FO drop di Cilandak"
    ),
    VehicleLogEntity(
        id = "VEH-2026-002",
        tanggal = "10/09/2026",
        pic = "Carlos & Syahril",
        team = "SO",
        timeOut = "14:30",
        ticket = "E/260909/0033",
        platKendaraan = "B 4605 SOU",
        typeKendaraan = "LEXY",
        ketTujuan = "Mid Plaza",
        latitude = -6.2088,
        longitude = 106.8225,
        statusArmada = "MENUNGGU_ACC_KEMBALI",
        timeIn = "17:45",
        tanggalKembali = "10/09/2026",
        fuelLevel = "Full (90%)",
        fuelLevelIn = "3/4 Tank",
        odometerOut = "18,340 KM",
        odometerIn = "18,368 KM",
        catatanPinjam = "Pekerjaan splicing OTB Mid Plaza menggunakan motor Lexy",
        catatanKembali = "Kendaraan sudah kembali di parkiran kantor, kunci dan STNK sudah di pos. Kondisi fisik prima."
    ),
    VehicleLogEntity(
        id = "VEH-2026-003",
        tanggal = "10/09/2026",
        pic = "Yosep",
        team = "SM",
        timeOut = "10:59",
        ticket = "Storing",
        platKendaraan = "B 1241 U",
        typeKendaraan = "AVANZA",
        ketTujuan = "DurenTiga",
        latitude = -6.2550,
        longitude = 106.8375,
        statusArmada = "SELESAI",
        timeIn = "15:20",
        tanggalKembali = "10/09/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Full (80%)",
        odometerOut = "62,110 KM",
        odometerIn = "62,135 KM",
        catatanPinjam = "Storing material kabel FO Duren Tiga",
        catatanKembali = "Kendaraan dikembalikan dalam kondisi bersih dan bahan bakar aman.",
        accBy = "Nemi BR Ginting",
        accRole = "IC Maintenance",
        accTime = "10/09/2026 15:35",
        accNotes = "Fisik mobil dicek lengkap, STNK & kunci sudah diterima di laci armada SM. Disetujui."
    ),
    VehicleLogEntity(
        id = "VEH-2026-004",
        tanggal = "10/09/2026",
        pic = "Anil",
        team = "SM",
        timeOut = "11:00",
        ticket = "Storing",
        platKendaraan = "B 4616 SOU",
        typeKendaraan = "LEXY",
        ketTujuan = "Sudirman",
        latitude = -6.2230,
        longitude = 106.8110,
        statusArmada = "DIPINJAM",
        timeIn = "-",
        tanggalKembali = "-",
        fuelLevel = "Full",
        odometerOut = "14,890 KM",
        catatanPinjam = "Pengantaran alat ukur OTDR ke Sudirman via motor operasional"
    ),
    VehicleLogEntity(
        id = "VEH-2026-005",
        tanggal = "11/10/2026",
        pic = "M.Rusli",
        team = "SM",
        timeOut = "11:00",
        ticket = "Aktifasi Switch Ring 7",
        platKendaraan = "B 1241 U",
        typeKendaraan = "AVANZA",
        ketTujuan = "Cyber 1",
        latitude = -6.2383,
        longitude = 106.8310,
        statusArmada = "DIPINJAM",
        timeIn = "-",
        tanggalKembali = "-",
        fuelLevel = "Full",
        odometerOut = "62,135 KM",
        catatanPinjam = "Aktifasi switch & router Ring 7 Cyber 1"
    ),
    VehicleLogEntity(
        id = "VEH-2026-006",
        tanggal = "11/10/2026",
        pic = "Carlos & Syahril",
        team = "SO",
        timeOut = "14:30",
        ticket = "-",
        platKendaraan = "B 4605 SOU",
        typeKendaraan = "LEXY",
        ketTujuan = "Mid Plaza",
        latitude = -6.2088,
        longitude = 106.8225,
        statusArmada = "DIPINJAM",
        timeIn = "-",
        tanggalKembali = "-",
        fuelLevel = "Full",
        odometerOut = "18,368 KM",
        catatanPinjam = "Perjalanan dinas maintenance Mid Plaza"
    ),
    VehicleLogEntity(
        id = "VEH-2026-006B",
        tanggal = "10/09/2026",
        pic = "Alqori Dewantara & Ahmad Ibrahim",
        team = "SO",
        timeOut = "09:15",
        ticket = "E/260909/0019",
        platKendaraan = "B 2924 SKN",
        typeKendaraan = "Grandmax",
        ketTujuan = "TB Simatupang",
        latitude = -6.2990,
        longitude = 106.8150,
        statusArmada = "SELESAI",
        timeIn = "14:10",
        tanggalKembali = "10/09/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Full (85%)",
        odometerOut = "45,150 KM",
        odometerIn = "45,190 KM",
        catatanPinjam = "Pergantian kabel feeder TB Simatupang Tower",
        catatanKembali = "Armada kembali aman, kebersihan terjaga.",
        accBy = "Muhamad Fadli",
        accRole = "IC Service Operation",
        accTime = "10/09/2026 14:30",
        accNotes = "Disetujui. Kondisi fisik dan logbook sesuai."
    ),
    VehicleLogEntity(
        id = "VEH-2026-007",
        tanggal = "11/09/2026",
        pic = "Dimas Wahyu Pratama",
        team = "SO",
        timeOut = "08:45",
        ticket = "E/260910/0042",
        platKendaraan = "B 3892 SOX",
        typeKendaraan = "NMAX 155",
        ketTujuan = "Kuningan Barat",
        latitude = -6.2350,
        longitude = 106.8280,
        statusArmada = "DIPINJAM",
        timeIn = "-",
        tanggalKembali = "-",
        fuelLevel = "Full (95%)",
        odometerOut = "12,110 KM",
        catatanPinjam = "Inspeksi kabel backbone Kuningan Barat via Motor NMAX"
    ),
    VehicleLogEntity(
        id = "VEH-2026-008",
        tanggal = "11/09/2026",
        pic = "Budi Mulya & Carolus Borromeus",
        team = "SM",
        timeOut = "13:15",
        ticket = "Storing",
        platKendaraan = "B 1241 U",
        typeKendaraan = "AVANZA",
        ketTujuan = "Kelapa Gading",
        latitude = -6.1600,
        longitude = 106.9050,
        statusArmada = "MENUNGGU_ACC_KEMBALI",
        timeIn = "17:30",
        tanggalKembali = "11/09/2026",
        fuelLevel = "Full",
        fuelLevelIn = "3/4 Tank",
        odometerOut = "62,140 KM",
        odometerIn = "62,175 KM",
        catatanPinjam = "Storing joint closure Kelapa Gading Mall",
        catatanKembali = "Kunci dan STNK sudah diserahkan di pos IC SM."
    ),
    VehicleLogEntity(
        id = "VEH-2026-009",
        tanggal = "12/09/2026",
        pic = "Rian Hidayat",
        team = "SO",
        timeOut = "08:15",
        ticket = "E/260912/0008",
        platKendaraan = "B 5521 SOU",
        typeKendaraan = "Vario 160",
        ketTujuan = "Grogol Petamburan",
        latitude = -6.1670,
        longitude = 106.7890,
        statusArmada = "SELESAI",
        timeIn = "12:45",
        tanggalKembali = "12/09/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Full (80%)",
        odometerOut = "9,420 KM",
        odometerIn = "9,445 KM",
        catatanPinjam = "Patroli kabel optik Grogol Petamburan",
        catatanKembali = "Unit motor sudah diparkir kembali di basement.",
        accBy = "Muhamad Fadli",
        accRole = "IC Service Operation",
        accTime = "12/09/2026 13:00",
        accNotes = "Logbook KM & fisik motor terverifikasi lengkap."
    ),
    VehicleLogEntity(
        id = "VEH-2026-010",
        tanggal = "12/09/2026",
        pic = "Denny & Farhan",
        team = "SM",
        timeOut = "09:30",
        ticket = "Preventive Maintenance Ring 3",
        platKendaraan = "B 1982 TPO",
        typeKendaraan = "Innova Reborn",
        ketTujuan = "Bekasi Barat",
        latitude = -6.2380,
        longitude = 106.9950,
        statusArmada = "DIPINJAM",
        timeIn = "-",
        tanggalKembali = "-",
        fuelLevel = "Full (100%)",
        odometerOut = "78,320 KM",
        catatanPinjam = "PM berkala POP Bekasi Barat membawa genset portable"
    ),

    // === MINGGU LALU (MINGGU 1 SEP 2026: 01-07 SEP 2026) ===
    VehicleLogEntity(
        id = "VEH-2026-011",
        tanggal = "03/09/2026",
        pic = "Hadi & Qori",
        team = "SO",
        timeOut = "08:30",
        ticket = "E/260903/0014",
        platKendaraan = "B 2924 SKN",
        typeKendaraan = "Grandmax",
        ketTujuan = "Mampang Prapatan",
        latitude = -6.2480,
        longitude = 106.8280,
        statusArmada = "SELESAI",
        timeIn = "16:15",
        tanggalKembali = "03/09/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Half Tank",
        odometerOut = "44,980 KM",
        odometerIn = "45,035 KM",
        catatanPinjam = "Relokasi tiang FO Mampang",
        catatanKembali = "Semua peralatan diturunkan, mobil aman.",
        accBy = "Muhamad Fadli",
        accRole = "IC Service Operation",
        accTime = "03/09/2026 16:30",
        accNotes = "ACC Selesai."
    ),
    VehicleLogEntity(
        id = "VEH-2026-012",
        tanggal = "05/09/2026",
        pic = "Carlos & Syahril",
        team = "SO",
        timeOut = "10:00",
        ticket = "E/260905/0022",
        platKendaraan = "B 4605 SOU",
        typeKendaraan = "LEXY",
        ketTujuan = "Harmoni Central",
        latitude = -6.1620,
        longitude = 106.8190,
        statusArmada = "SELESAI",
        timeIn = "14:20",
        tanggalKembali = "05/09/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Full (85%)",
        odometerOut = "18,290 KM",
        odometerIn = "18,315 KM",
        catatanPinjam = "Pemeriksaan core fiber Harmoni",
        catatanKembali = "Kondisi motor prima.",
        accBy = "Muhamad Fadli",
        accRole = "IC Service Operation",
        accTime = "05/09/2026 14:40",
        accNotes = "ACC selesai."
    ),
    VehicleLogEntity(
        id = "VEH-2026-013",
        tanggal = "06/09/2026",
        pic = "Anil",
        team = "SM",
        timeOut = "13:00",
        ticket = "Storing",
        platKendaraan = "B 4616 SOU",
        typeKendaraan = "LEXY",
        ketTujuan = "Tanah Abang",
        latitude = -6.1880,
        longitude = 106.8120,
        statusArmada = "SELESAI",
        timeIn = "17:10",
        tanggalKembali = "06/09/2026",
        fuelLevel = "Full",
        fuelLevelIn = "3/4 Tank",
        odometerOut = "14,830 KM",
        odometerIn = "14,860 KM",
        catatanPinjam = "Pengantaran jumper cable Tanah Abang Blok A",
        catatanKembali = "Motor selesai digunakan.",
        accBy = "Nemi BR Ginting",
        accRole = "IC Maintenance",
        accTime = "06/09/2026 17:25",
        accNotes = "Verifikasi OK."
    ),

    // === BULAN AGUSTUS 2026 (MINGGU 1-4 AGU 2026) ===
    VehicleLogEntity(
        id = "VEH-2026-014",
        tanggal = "28/08/2026",
        pic = "Yosep & Budi",
        team = "SM",
        timeOut = "09:00",
        ticket = "Cut Over Fiber",
        platKendaraan = "B 1241 U",
        typeKendaraan = "AVANZA",
        ketTujuan = "Serpong BSD",
        latitude = -6.3010,
        longitude = 106.6520,
        statusArmada = "SELESAI",
        timeIn = "18:00",
        tanggalKembali = "28/08/2026",
        fuelLevel = "Full",
        fuelLevelIn = "1/2 Tank",
        odometerOut = "61,900 KM",
        odometerIn = "61,985 KM",
        catatanPinjam = "Pekerjaan Cut Over Fiber BSD City",
        catatanKembali = "Kembali tepat waktu, kondisi baik.",
        accBy = "Nemi BR Ginting",
        accRole = "IC Maintenance",
        accTime = "28/08/2026 18:15",
        accNotes = "Logbook terverifikasi."
    ),
    VehicleLogEntity(
        id = "VEH-2026-015",
        tanggal = "22/08/2026",
        pic = "Rian Hidayat",
        team = "SO",
        timeOut = "08:00",
        ticket = "E/260822/0009",
        platKendaraan = "B 5521 SOU",
        typeKendaraan = "Vario 160",
        ketTujuan = "Pluit Junction",
        latitude = -6.1260,
        longitude = 106.7900,
        statusArmada = "SELESAI",
        timeIn = "13:30",
        tanggalKembali = "22/08/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Full (75%)",
        odometerOut = "9,280 KM",
        odometerIn = "9,315 KM",
        catatanPinjam = "Troubleshoot ONT Pluit",
        catatanKembali = "Unit parkir aman.",
        accBy = "Muhamad Fadli",
        accRole = "IC Service Operation",
        accTime = "22/08/2026 13:45",
        accNotes = "Selesai di-ACC."
    ),
    VehicleLogEntity(
        id = "VEH-2026-016",
        tanggal = "15/08/2026",
        pic = "Ahmad Ibrahim & Alqori",
        team = "SO",
        timeOut = "09:30",
        ticket = "E/260815/0011",
        platKendaraan = "B 2924 SKN",
        typeKendaraan = "Grandmax",
        ketTujuan = "Bogor Kota",
        latitude = -6.5950,
        longitude = 106.7970,
        statusArmada = "SELESAI",
        timeIn = "17:40",
        tanggalKembali = "15/08/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Full (60%)",
        odometerOut = "44,650 KM",
        odometerIn = "44,760 KM",
        catatanPinjam = "Tarik kabel FO Bogor Ring",
        catatanKembali = "Kondisi armada bersih, BBM aman.",
        accBy = "Muhamad Fadli",
        accRole = "IC Service Operation",
        accTime = "15/08/2026 18:00",
        accNotes = "ACC disetujui."
    ),
    VehicleLogEntity(
        id = "VEH-2026-017",
        tanggal = "08/08/2026",
        pic = "Dimas Wahyu Pratama",
        team = "SO",
        timeOut = "11:15",
        ticket = "E/260808/0004",
        platKendaraan = "B 3892 SOX",
        typeKendaraan = "NMAX 155",
        ketTujuan = "Gambir Station",
        latitude = -6.1760,
        longitude = 106.8300,
        statusArmada = "SELESAI",
        timeIn = "15:00",
        tanggalKembali = "08/08/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Full (85%)",
        odometerOut = "11,850 KM",
        odometerIn = "11,878 KM",
        catatanPinjam = "Instalasi media converter Gambir",
        catatanKembali = "Selesai tanpa kendala.",
        accBy = "Muhamad Fadli",
        accRole = "IC Service Operation",
        accTime = "08/08/2026 15:15",
        accNotes = "Verifikasi lengkap."
    ),

    // === BULAN JULI 2026 ===
    VehicleLogEntity(
        id = "VEH-2026-018",
        tanggal = "25/07/2026",
        pic = "Denny & Farhan",
        team = "SM",
        timeOut = "10:00",
        ticket = "Maintenance POP",
        platKendaraan = "B 1982 TPO",
        typeKendaraan = "Innova Reborn",
        ketTujuan = "Cibubur Junction",
        latitude = -6.3720,
        longitude = 106.8980,
        statusArmada = "SELESAI",
        timeIn = "16:30",
        tanggalKembali = "25/07/2026",
        fuelLevel = "Full",
        fuelLevelIn = "3/4 Tank",
        odometerOut = "77,810 KM",
        odometerIn = "77,880 KM",
        catatanPinjam = "Maintenance baterai UPS POP Cibubur",
        catatanKembali = "Kunci dan STNK disimpan di laci SM.",
        accBy = "Nemi BR Ginting",
        accRole = "IC Maintenance",
        accTime = "25/07/2026 16:45",
        accNotes = "Disetujui."
    ),
    VehicleLogEntity(
        id = "VEH-2026-019",
        tanggal = "18/07/2026",
        pic = "Carlos & Syahril",
        team = "SO",
        timeOut = "08:45",
        ticket = "E/260718/0007",
        platKendaraan = "B 4605 SOU",
        typeKendaraan = "LEXY",
        ketTujuan = "Cempaka Putih",
        latitude = -6.1800,
        longitude = 106.8720,
        statusArmada = "SELESAI",
        timeIn = "13:00",
        tanggalKembali = "18/07/2026",
        fuelLevel = "Full",
        fuelLevelIn = "Full (80%)",
        odometerOut = "17,920 KM",
        odometerIn = "17,950 KM",
        catatanPinjam = "Penyambungan core FO putus Cempaka Putih",
        catatanKembali = "Motor kembali aman di pos.",
        accBy = "Muhamad Fadli",
        accRole = "IC Service Operation",
        accTime = "18/07/2026 13:15",
        accNotes = "ACC disetujui."
    )
)