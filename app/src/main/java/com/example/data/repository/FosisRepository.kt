package com.example.data.repository

import com.example.data.local.FosisDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.data.model.PermitEntity
import com.example.data.model.PermitStatus
import com.example.data.model.SyncQueueEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WorkflowStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FosisRepository(
    private val dao: FosisDao,
    private val appScope: CoroutineScope
) {
    val allItems: Flow<List<FosisItemEntity>> = dao.getAllItems()
    val allPermits: Flow<List<PermitEntity>> = dao.getAllPermits()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    val pendingSyncQueue: Flow<List<SyncQueueEntity>> = dao.getPendingSyncQueue()
    val urgentItems: Flow<List<FosisItemEntity>> = dao.getUrgentItems()
    val allChatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()
    val allVehicleLogs: Flow<List<com.example.data.model.VehicleLogEntity>> = dao.getAllVehicleLogs()

    suspend fun sendChatMessage(message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        dao.insertChatMessage(message)
    }

    suspend fun insertVehicleLog(
        log: com.example.data.model.VehicleLogEntity
    ) = withContext(Dispatchers.IO) {
        dao.insertVehicleLog(log)
    }

    suspend fun updateVehicleLog(
        log: com.example.data.model.VehicleLogEntity
    ) = withContext(Dispatchers.IO) {
        dao.updateVehicleLog(log)
    }

    suspend fun deleteVehicleLog(
        log: com.example.data.model.VehicleLogEntity
    ) = withContext(Dispatchers.IO) {
        dao.deleteVehicleLog(log)
    }

    suspend fun reloadSimulatedVehicleLogs() = withContext(Dispatchers.IO) {
        dao.clearAllVehicleLogs()
        dao.insertVehicleLogs(
            com.example.data.model.INITIAL_VEHICLE_LOGS
        )
    }

    init {
        appScope.launch(Dispatchers.IO) {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        // Sync preset users roster to database
        dao.clearAllUsers()
        dao.insertUsers(com.example.data.model.PRESET_SYSTEM_USERS)

        // Masukkan data simulasi lengkap persis sesuai sheet user
        dao.clearAllItems()
        dao.clearAllPermits()
        dao.clearAllVehicleLogs()
        dao.insertItems(com.example.data.model.FosisSeedData.ALL_SIMULATED_ITEMS)
        dao.insertPermits(com.example.data.model.FosisSeedData.SIMULATED_PERMITS)
        dao.insertVehicleLogs(com.example.data.model.INITIAL_VEHICLE_LOGS)
    }

    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        dao.insertUser(user)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "INSERT_USER",
                userEmail = user.email,
                userName = user.name,
                details = "Menambahkan staf baru: ${user.name} (${user.region})"
            )
        )
    }

    suspend fun deleteUser(user: UserEntity) = withContext(Dispatchers.IO) {
        dao.deleteUser(user)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "DELETE_USER",
                userEmail = user.email,
                userName = user.name,
                details = "Menghapus staf: ${user.name} (${user.region})"
            )
        )
    }

    // ==========================================
    // TICKET OPERATIONS
    // ==========================================
    suspend fun insertItem(item: FosisItemEntity, isOnline: Boolean): Long = withContext(Dispatchers.IO) {
        val insertedId = dao.insertItem(item.copy(isSynced = isOnline, lastUpdated = System.currentTimeMillis()))
        if (!isOnline) {
            dao.insertSyncQueue(
                SyncQueueEntity(
                    itemId = insertedId,
                    operation = "CREATE",
                    payloadJson = "Item: ${item.ticketNo} - ${item.title}"
                )
            )
        }
        dao.insertAuditLog(
            AuditLogEntity(
                action = "INSERT_${item.type.name}",
                userEmail = item.engineerEmail,
                userName = item.engineerName,
                details = "Membuat tiket ${item.type.label}: ${item.ticketNo} (${if (isOnline) "Online" else "Offline Queue"})"
            )
        )
        insertedId
    }

    suspend fun updateItem(item: FosisItemEntity, isOnline: Boolean) = withContext(Dispatchers.IO) {
        dao.updateItem(item.copy(isSynced = isOnline, lastUpdated = System.currentTimeMillis()))
        if (!isOnline) {
            dao.insertSyncQueue(
                SyncQueueEntity(
                    itemId = item.id,
                    operation = "UPDATE",
                    payloadJson = "Update: ${item.ticketNo}"
                )
            )
        }
    }

    suspend fun deleteItem(item: FosisItemEntity, user: UserEntity) = withContext(Dispatchers.IO) {
        dao.deleteItem(item)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "DELETE_ITEM",
                userEmail = user.email,
                userName = user.name,
                details = "Menghapus item tiket ${item.ticketNo} (${item.title}) oleh ${user.name} [${user.role.label}]"
            )
        )
    }

    // Workflow Transitions
    suspend fun submitByTeknisi(item: FosisItemEntity, user: UserEntity, isOnline: Boolean) = withContext(Dispatchers.IO) {
        val updated = item.copy(
            workflowStatus = WorkflowStatus.SUBMITTED_TEKNISI,
            submittedByTeknisiAt = System.currentTimeMillis(),
            isSynced = isOnline,
            lastUpdated = System.currentTimeMillis()
        )
        dao.updateItem(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "WORKFLOW_SUBMIT",
                userEmail = user.email,
                userName = user.name,
                details = "Tim Lapangan (${user.name}) mengajukan tiket ${item.ticketNo} ke IC Verifikator."
            )
        )
    }

    suspend fun verifyByLeader(item: FosisItemEntity, leader: UserEntity, isOnline: Boolean) = withContext(Dispatchers.IO) {
        val updated = item.copy(
            workflowStatus = WorkflowStatus.VERIFIED_LEADER,
            verifiedByLeaderAt = System.currentTimeMillis(),
            verifiedByLeaderName = leader.name,
            isSynced = isOnline,
            lastUpdated = System.currentTimeMillis()
        )
        dao.updateItem(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "WORKFLOW_VERIFY",
                userEmail = leader.email,
                userName = leader.name,
                details = "${leader.name} [${leader.role.label}] memverifikasi tiket ${item.ticketNo}."
            )
        )
    }

    suspend fun toggleLock(item: FosisItemEntity, user: UserEntity, isOnline: Boolean) = withContext(Dispatchers.IO) {
        val newLockState = !item.isLocked
        val newStatus = if (newLockState) WorkflowStatus.LOCKED_ADMIN else WorkflowStatus.VERIFIED_LEADER
        val updated = item.copy(
            isLocked = newLockState,
            workflowStatus = newStatus,
            lockedByAdminAt = if (newLockState) System.currentTimeMillis() else null,
            lockedByAdminName = if (newLockState) user.name else null,
            isSynced = isOnline,
            lastUpdated = System.currentTimeMillis()
        )
        dao.updateItem(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                action = if (newLockState) "WORKFLOW_LOCK" else "WORKFLOW_UNLOCK",
                userEmail = user.email,
                userName = user.name,
                details = "${user.name} [${user.role.label}] ${if (newLockState) "mengunci (Lock)" else "membuka kunci (Unlock)"} data tiket ${item.ticketNo}."
            )
        )
    }

    suspend fun addDeptHeadComment(item: FosisItemEntity, deptHead: UserEntity, comment: String, isOnline: Boolean) = withContext(Dispatchers.IO) {
        val updated = item.copy(
            workflowStatus = WorkflowStatus.APPROVED_DEPT_HEAD,
            approvedByDeptHeadAt = System.currentTimeMillis(),
            deptHeadComment = comment.ifBlank { "Evaluasi & catatan Dept Head dicatat." },
            isSynced = isOnline,
            lastUpdated = System.currentTimeMillis()
        )
        dao.updateItem(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "DEPT_HEAD_COMMENT",
                userEmail = deptHead.email,
                userName = deptHead.name,
                details = "Dept. Head (${deptHead.name}) memberikan komentar pada tiket ${item.ticketNo}: $comment"
            )
        )
    }

    // ==========================================
    // PERMIT OPERATIONS
    // ==========================================
    suspend fun insertPermit(permit: PermitEntity, user: UserEntity): Long = withContext(Dispatchers.IO) {
        val id = dao.insertPermit(permit)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "CREATE_PERMIT",
                userEmail = user.email,
                userName = user.name,
                details = "Mengajukan Surat Ijin Kerja baru: ${permit.permitNo} (${permit.siteLocation})"
            )
        )
        id
    }

    suspend fun updatePermit(permit: PermitEntity, user: UserEntity) = withContext(Dispatchers.IO) {
        dao.updatePermit(permit)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "UPDATE_PERMIT",
                userEmail = user.email,
                userName = user.name,
                details = "Memperbarui data perijinan ${permit.permitNo} oleh ${user.name}"
            )
        )
    }

    suspend fun deletePermit(permit: PermitEntity, user: UserEntity) = withContext(Dispatchers.IO) {
        dao.deletePermit(permit)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "DELETE_PERMIT",
                userEmail = user.email,
                userName = user.name,
                details = "Menghapus data perijinan ${permit.permitNo} oleh ${user.name}"
            )
        )
    }

    suspend fun approvePermitByAdmin(permit: PermitEntity, admin: UserEntity, isApproved: Boolean, reason: String? = null) = withContext(Dispatchers.IO) {
        val updated = permit.copy(
            status = if (isApproved) PermitStatus.DISETUJUI_ADMIN else PermitStatus.DITOLAK,
            approvedBy = if (isApproved) admin.name else null,
            approvedAt = if (isApproved) System.currentTimeMillis() else null,
            rejectionReason = if (!isApproved) reason else null
        )
        dao.updatePermit(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                action = if (isApproved) "APPROVE_PERMIT" else "REJECT_PERMIT",
                userEmail = admin.email,
                userName = admin.name,
                details = "Admin ${admin.name} ${if (isApproved) "menyetujui" else "menolak"} perijinan ${permit.permitNo}."
            )
        )
    }

    suspend fun importPermits(permits: List<PermitEntity>, admin: UserEntity): Int = withContext(Dispatchers.IO) {
        if (admin.role != UserRole.ADMIN) {
            throw IllegalAccessException("Hanya Admin (Sanderlina Imelda) yang berwenang mengimpor data perijinan!")
        }
        dao.insertPermits(permits.map { it.copy(importedByAdmin = true) })
        dao.insertAuditLog(
            AuditLogEntity(
                action = "IMPORT_PERMITS_ADMIN",
                userEmail = admin.email,
                userName = admin.name,
                details = "Admin ${admin.name} berhasil mengimpor ${permits.size} data perijinan ke sistem."
            )
        )
        permits.size
    }

    suspend fun clearAllOperationalData(admin: UserEntity) = withContext(Dispatchers.IO) {
        dao.clearAllItems()
        dao.clearAllPermits()
        dao.clearAllVehicleLogs()
        dao.insertAuditLog(
            AuditLogEntity(
                action = "PURGE_OPERATIONAL_DATA",
                userEmail = admin.email,
                userName = admin.name,
                details = "Admin ${admin.name} mengosongkan seluruh data operasional (tiket dan perijinan) untuk pengujian bersih."
            )
        )
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        dao.updateUser(user)
    }

    suspend fun syncPendingQueue(): Int = withContext(Dispatchers.IO) {
        val pending = dao.getPendingSyncQueue().firstOrNull() ?: emptyList()
        val count = pending.size
        dao.markAllSynced()
        dao.clearSyncQueue()
        dao.insertAuditLog(
            AuditLogEntity(
                action = "AUTO_SYNC",
                userEmail = "system@fosis-ops.id",
                userName = "Auto-Sync Engine",
                details = "Berhasil mensinkronisasi $count antrean offline ke server."
            )
        )
        count
    }
}

val DEFAULT_ADMIN_ITEMS: List<FosisItemEntity> = listOf(
    FosisItemEntity(
        ticketNo = "TKT-ADM-001",
        type = FosisType.ADMINISTRASI,
        title = "Laporan Finance Bulanan FI TR via WA",
        bulan = "JANUARY",
        tahun = "2025",
        tanggal = 1,
        adminTask = "REPORT",
        subTask = "FINANCE",
        subTask1 = "FI TR",
        viaChannel = "WA",
        adminStatus = "DONE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "SERVICE OPERATION",
        workflowStatus = WorkflowStatus.APPROVED_DEPT_HEAD,
        engineerName = "Gergorius Galus Gala Toron",
        engineerEmail = "gergorius.galus.gala.toron@fosis-ops.id",
        siteLocationName = "Regional Jawa Timur (Head Office)",
        materialUsed = "Laporan Rekap Bulanan Terlampir",
        deptHeadComment = "Laporan keuangan bulanan valid dan terverifikasi."
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-002",
        type = FosisType.ADMINISTRASI,
        title = "Permit Kerja Rekanan Vendor FI CA via Teams",
        bulan = "FEBRUARY",
        tahun = "2026",
        tanggal = 2,
        adminTask = "PERMIT",
        subTask = "VENDOR",
        subTask1 = "FI CA",
        viaChannel = "TEAMS",
        adminStatus = "CONTINUE",
        adminKategori = "REQUEST EXTERNAL",
        adminActivity = "SERVICE MAINTENACE",
        workflowStatus = WorkflowStatus.VERIFIED_LEADER,
        engineerName = "Arry Dwi Putranto",
        engineerEmail = "arry.dwi.putranto@fosis-ops.id",
        siteLocationName = "POP Surabaya Gubeng",
        materialUsed = "Dokumen SIK Rekanan Vendor"
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-003",
        type = FosisType.ADMINISTRASI,
        title = "Rekonsiliasi Aset FAM FI PC via Email",
        bulan = "MARCH",
        tahun = "2026",
        tanggal = 3,
        adminTask = "REKONSILIASI",
        subTask = "FAM",
        subTask1 = "FI PC",
        viaChannel = "EMAIL",
        adminStatus = "ON HOLD",
        adminKategori = "REFUNDS",
        adminActivity = "VENDOR",
        workflowStatus = WorkflowStatus.SUBMITTED_TEKNISI,
        engineerName = "Deni Herwanto",
        engineerEmail = "deni.herwanto@fosis-ops.id",
        siteLocationName = "Warehouse Surabaya",
        materialUsed = "Form Berita Acara Rekonsiliasi Aset"
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-004",
        type = FosisType.ADMINISTRASI,
        title = "Pengadaan Ticket VE Managed Service Regional Surabaya",
        bulan = "APRIL",
        tahun = "2026",
        tanggal = 4,
        adminTask = "TICKET",
        subTask = "PROCUREMENT",
        subTask1 = "VE MANAGED SERVICE",
        viaChannel = "PORTAL",
        adminStatus = "CANCEL",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "REG. SURABAYA",
        workflowStatus = WorkflowStatus.DRAFT,
        engineerName = "Gergorius Galus Gala Toron",
        engineerEmail = "gergorius.galus.gala.toron@fosis-ops.id",
        siteLocationName = "Regional Surabaya",
        materialUsed = "Draft Permohonan Managed Service"
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-005",
        type = FosisType.ADMINISTRASI,
        title = "Pengadaan Material Eksternal VE Material Regional Bandung",
        bulan = "MAY",
        tahun = "2026",
        tanggal = 5,
        adminTask = "REPORT",
        subTask = "EXTERNAL",
        subTask1 = "VE MATERIAL",
        viaChannel = "VISIT",
        adminStatus = "DONE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "REG. BANDUNG",
        workflowStatus = WorkflowStatus.APPROVED_DEPT_HEAD,
        engineerName = "Arry Dwi Putranto",
        engineerEmail = "arry.dwi.putranto@fosis-ops.id",
        siteLocationName = "Regional Bandung",
        materialUsed = "Patchcord SC-LC 50 pcs, OTB 24C 2 unit",
        deptHeadComment = "Pengiriman material telah dikonfirmasi tim Bandung."
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-006",
        type = FosisType.ADMINISTRASI,
        title = "Laporan Stock Opname Customer Care Hardcopy",
        bulan = "JUNE",
        tahun = "2026",
        tanggal = 6,
        adminTask = "REPORT",
        subTask = "CUSTOMER CARE",
        subTask1 = "VE STOCK OPNAME",
        viaChannel = "HARDCOPY",
        adminStatus = "DONE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "ADMIN",
        workflowStatus = WorkflowStatus.LOCKED_ADMIN,
        engineerName = "Deni Herwanto",
        engineerEmail = "deni.herwanto@fosis-ops.id",
        siteLocationName = "Kantor Operasional FOSIS",
        materialUsed = "Berkas Fisik Stock Opname Q2"
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-007",
        type = FosisType.ADMINISTRASI,
        title = "Permit Berita Acara Kesepakatan (PROC BAK) CLS",
        bulan = "JULY",
        tahun = "2026",
        tanggal = 7,
        adminTask = "PERMIT",
        subTask = "CLS",
        subTask1 = "PROC BAK",
        viaChannel = "SOFTCOPY",
        adminStatus = "DONE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "SERVICE OPERATION",
        workflowStatus = WorkflowStatus.APPROVED_DEPT_HEAD,
        engineerName = "Gergorius Galus Gala Toron",
        engineerEmail = "gergorius.galus.gala.toron@fosis-ops.id",
        siteLocationName = "CLS Station",
        materialUsed = "Softcopy BAK Digital Sign"
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-008",
        type = FosisType.ADMINISTRASI,
        title = "Rekonsiliasi VE BAST dengan Tim NOC",
        bulan = "AUGUST",
        tahun = "2026",
        tanggal = 8,
        adminTask = "REKONSILIASI",
        subTask = "NOC",
        subTask1 = "VE BAST",
        viaChannel = "EMAIL",
        adminStatus = "DONE",
        adminKategori = "REQUEST EXTERNAL",
        adminActivity = "SERVICE MAINTENACE",
        workflowStatus = WorkflowStatus.APPROVED_DEPT_HEAD,
        engineerName = "Arry Dwi Putranto",
        engineerEmail = "arry.dwi.putranto@fosis-ops.id",
        siteLocationName = "NOC Center",
        materialUsed = "BAST Serah Terima Perangkat"
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-009",
        type = FosisType.ADMINISTRASI,
        title = "Rekonsiliasi VE BAK Serdel Service Operation",
        bulan = "SEPTEMBER",
        tahun = "2026",
        tanggal = 9,
        adminTask = "REKONSILIASI",
        subTask = "SERDEL",
        subTask1 = "VE BAK",
        viaChannel = "PORTAL",
        adminStatus = "CONTINUE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "SERVICE OPERATION",
        workflowStatus = WorkflowStatus.SUBMITTED_TEKNISI,
        engineerName = "Deni Herwanto",
        engineerEmail = "deni.herwanto@fosis-ops.id",
        siteLocationName = "Hub Serdel Surabaya",
        materialUsed = "Dokumen Penyerahan Layanan Serdel"
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-010",
        type = FosisType.ADMINISTRASI,
        title = "Ticket Material Lapangan Field Engineer",
        bulan = "OCTOBER",
        tahun = "2026",
        tanggal = 10,
        adminTask = "TICKET",
        subTask = "FIELD ENGINEER",
        subTask1 = "MATERIAL",
        viaChannel = "WA",
        adminStatus = "CONTINUE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "SERVICE MAINTENACE",
        workflowStatus = WorkflowStatus.VERIFIED_LEADER,
        engineerName = "Gergorius Galus Gala Toron",
        engineerEmail = "gergorius.galus.gala.toron@fosis-ops.id",
        siteLocationName = "Field Rute Surabaya - Malang",
        materialUsed = "Kabel Fiber Optik ADSS 24C 200m, Closure 24C 1 set"
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-011",
        type = FosisType.ADMINISTRASI,
        title = "Surat Ijin Kerja Bulanan Operasional",
        bulan = "NOVEMBER",
        tahun = "2026",
        tanggal = 11,
        adminTask = "PERMIT",
        subTask = "MONTHLY",
        subTask1 = "SURAT IJIN",
        viaChannel = "TEAMS",
        adminStatus = "DONE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "ADMIN",
        workflowStatus = WorkflowStatus.APPROVED_DEPT_HEAD,
        engineerName = "Arry Dwi Putranto",
        engineerEmail = "arry.dwi.putranto@fosis-ops.id",
        siteLocationName = "Seluruh POP Area Jatim",
        materialUsed = "Surat Ijin Akses POP & Jalan Protokol",
        deptHeadComment = "SIK operasional bulanan telah diperpanjang."
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-012",
        type = FosisType.ADMINISTRASI,
        title = "Open Ticket Operasional Tahunan FOSIS",
        bulan = "DECEMBER",
        tahun = "2026",
        tanggal = 12,
        adminTask = "TICKET",
        subTask = "YEARLY",
        subTask1 = "OPEN TICKET",
        viaChannel = "PORTAL",
        adminStatus = "DONE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "SERVICE OPERATION",
        workflowStatus = WorkflowStatus.APPROVED_DEPT_HEAD,
        engineerName = "Deni Herwanto",
        engineerEmail = "deni.herwanto@fosis-ops.id",
        siteLocationName = "Kantor Pusat FOSIS",
        materialUsed = "Annual Operational Checklist",
        deptHeadComment = "Tiket audit tahunan telah diselesaikan dengan baik."
    ),
    FosisItemEntity(
        ticketNo = "TKT-ADM-013",
        type = FosisType.ADMINISTRASI,
        title = "Closed Ticket Evaluasi Tengah Tahun FOSIS",
        bulan = "APRIL",
        tahun = "2026",
        tanggal = 13,
        adminTask = "TICKET",
        subTask = "MIDDLE YEAR",
        subTask1 = "CLOSED TICKET",
        viaChannel = "EMAIL",
        adminStatus = "DONE",
        adminKategori = "REQUEST INTERNAL",
        adminActivity = "ADMIN",
        workflowStatus = WorkflowStatus.LOCKED_ADMIN,
        engineerName = "Gergorius Galus Gala Toron",
        engineerEmail = "gergorius.galus.gala.toron@fosis-ops.id",
        siteLocationName = "Regional Surabaya",
        materialUsed = "Laporan Penutupan Tiket Middle Year"
    )
)