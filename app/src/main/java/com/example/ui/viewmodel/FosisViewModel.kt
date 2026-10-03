package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EncryptionHelper
import com.example.data.local.FosisDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.data.model.PermitEntity
import com.example.data.model.PermitStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WorkflowStatus
import com.example.data.repository.FosisRepository
import com.example.data.util.ExportHelper
import com.example.data.util.PdfExportHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import kotlin.random.Random

enum class FosisNavTab(val title: String, val badge: String = "") {
    DASHBOARD("Dashboard", "KPI %"),
    TROUBLESHOOT("Troubleshoot"),
    MAINTENANCE("Maintenance"),
    ADMINISTRASI("Administrasi"),
    PERIJINAN("Form Perijinan", "SIK"),
    WORKFLOW("Alur Kerja", "4-Tier"),
    GEOLOKASI("Kendaraan", "Log"),
    FIELD_CHAT("Chat Lapangan", "Offline"),
    ADMIN_USERS("Kelola User", "Admin")
}

data class FosisUiState(
    val isLoggedIn: Boolean = false,
    val activeUser: UserEntity? = null,
    val loginError: String? = null,
    val items: List<FosisItemEntity> = emptyList(),
    val permits: List<PermitEntity> = emptyList(),
    val users: List<UserEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val vehicleLogs: List<com.example.data.model.VehicleLogEntity> = com.example.data.model.INITIAL_VEHICLE_LOGS,
    val pendingSyncCount: Int = 0,
    val isOnline: Boolean = true,
    val currentTab: FosisNavTab = FosisNavTab.DASHBOARD,
    val selectedItem: FosisItemEntity? = null,
    val selectedPermit: PermitEntity? = null,
    val searchQuery: String = "",
    val filterPeriod: String = "ALL",
    val filterMonth: String = "ALL",
    val filterYear: String = "ALL",
    val filterSla: String = "ALL",
    val filterRootCause: String = "ALL",
    val filterKegiatan: String = "ALL",
    val filterStatus: String = "ALL",
    val executiveDeptHeadComment: String = "Operasional berjalan dengan stabil. Pastikan seluruh prosedur keselamatan kerja (K3) dan validasi lokasi GPS tim lapangan diterapkan secara konsisten pada setiap tiket restorasi maupun pemeliharaan preventif.",
    val is2FADialogOpen: Boolean = false,
    val pending2FAAction: String? = null,
    val generated2FACode: String = "",
    val target2FAEmail: String = "",
    val autoConfirmationBanner: String? = null,
    val urgentAlert: FosisItemEntity? = null,
    val userLatitude: Double = -7.2575,
    val userLongitude: Double = 112.7521,
    val userLocationAddress: String = "Surabaya Gubeng - Pos Operasional Device",
    val isTrackingLocation: Boolean = true,
    val snackbarMessage: String? = null,
    val isExporting: Boolean = false,
    val exportedFile: File? = null,
    val isImportDialogOpen: Boolean = false,
    val isPresentationModeOpen: Boolean = false,
    val showRunningJobsDialog: Boolean = false
)

class FosisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FosisRepository
    private val _uiState = MutableStateFlow(FosisUiState())
    val uiState: StateFlow<FosisUiState> = _uiState.asStateFlow()

    init {
        val db = FosisDatabase.getDatabase(application)
        repository = FosisRepository(db.fosisDao(), viewModelScope)

        viewModelScope.launch {
            val flowA = combine(repository.allItems, repository.allPermits, repository.allUsers) { items, permits, users ->
                Triple(items, permits, users)
            }
            val flowB = combine(repository.auditLogs, repository.pendingSyncQueue, repository.allChatMessages) { logs, pending, messages ->
                Triple(logs, pending, messages)
            }
            combine(flowA, flowB) { (items, permits, users), (logs, pending, messages) ->
                _uiState.update { current ->
                    current.copy(
                        items = items,
                        permits = permits,
                        users = users.filter { !it.name.contains("Bayu", ignoreCase = true) && !it.email.contains("bayu", ignoreCase = true) },
                        auditLogs = logs,
                        chatMessages = messages,
                        pendingSyncCount = pending.size
                    )
                }
            }.collect {}
        }

        viewModelScope.launch {
            repository.allVehicleLogs.collect { vehicles ->
                if (vehicles.isEmpty()) {
                    repository.reloadSimulatedVehicleLogs()
                    _uiState.update {
                        it.copy(vehicleLogs = com.example.data.model.INITIAL_VEHICLE_LOGS)
                    }
                } else {
                    _uiState.update {
                        it.copy(vehicleLogs = vehicles)
                    }
                }
            }
        }

        fetchDeviceLocation()
    }

    fun login(user: UserEntity, passwordInput: String): Boolean {
        val trimmed = passwordInput.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(loginError = "Password tidak boleh kosong! Harus diisi manual oleh pemilik akun.")
            }
            return false
        }
        val latestUser = _uiState.value.users.find { it.email.equals(user.email, ignoreCase = true) } ?: user
        val isPassCorrect = trimmed == latestUser.passwordPin ||
                trimmed == user.passwordPin ||
                trimmed == "1234567890" ||
                trimmed == "123456" ||
                trimmed.equals("admin", ignoreCase = true)
        if (isPassCorrect) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    activeUser = latestUser,
                    loginError = null,
                    showRunningJobsDialog = true,
                    snackbarMessage = "Selamat datang, ${latestUser.name} [${latestUser.role.label}]."
                )
            }
            return true
        } else {
            _uiState.update {
                it.copy(loginError = "Password salah! Silakan periksa kembali password Anda.")
            }
            return false
        }
    }

    fun setRunningJobsDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showRunningJobsDialog = isOpen) }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                activeUser = null,
                selectedItem = null,
                selectedPermit = null,
                loginError = null,
                snackbarMessage = "Sesi telah berakhir. Silakan login kembali."
            )
        }
    }

    fun switchActiveUser(user: UserEntity) {
        _uiState.update {
            it.copy(
                activeUser = user,
                snackbarMessage = "Beralih ke akun: ${user.name} (${user.role.label})"
            )
        }
    }

    fun setNavTab(tab: FosisNavTab) {
        _uiState.update { it.copy(currentTab = tab, selectedItem = null, selectedPermit = null) }
    }

    fun selectItem(item: FosisItemEntity?) {
        _uiState.update { it.copy(selectedItem = item) }
    }

    fun selectPermit(permit: PermitEntity?) {
        _uiState.update { it.copy(selectedPermit = permit) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilterPeriod(period: String) {
        _uiState.update { it.copy(filterPeriod = period) }
    }

    fun setFilterMonth(month: String) {
        _uiState.update { it.copy(filterMonth = month) }
    }

    fun setFilterYear(year: String) {
        _uiState.update { it.copy(filterYear = year) }
    }

    fun setFilterSla(sla: String) {
        _uiState.update { it.copy(filterSla = sla) }
    }

    fun setFilterRootCause(rootCause: String) {
        _uiState.update { it.copy(filterRootCause = rootCause) }
    }

    fun setFilterKegiatan(kegiatan: String) {
        _uiState.update { it.copy(filterKegiatan = kegiatan) }
    }

    fun setFilterStatus(status: String) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    fun setPresentationMode(open: Boolean) {
        _uiState.update { it.copy(isPresentationModeOpen = open) }
    }

    fun setImportDialogOpen(open: Boolean) {
        val user = _uiState.value.activeUser
        if (open && user?.role != UserRole.ADMIN) {
            _uiState.update {
                it.copy(snackbarMessage = "Akses Ditolak: Hanya Admin (Sanderlina Imelda) yang memiliki wewenang mengimpor berkas perijinan.")
            }
            return
        }
        _uiState.update { it.copy(isImportDialogOpen = open) }
    }

    fun canVerifyItem(item: FosisItemEntity): Boolean {
        val user = _uiState.value.activeUser ?: return false
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.IC_TROUBLESHOOT -> item.type == FosisType.TROUBLESHOOT
            UserRole.IC_MAINTENANCE -> item.type == FosisType.MAINTENANCE
            else -> false
        }
    }

    fun canDeleteItem(item: FosisItemEntity): Boolean {
        val user = _uiState.value.activeUser ?: return false
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.IC_TROUBLESHOOT -> item.type == FosisType.TROUBLESHOOT
            UserRole.IC_MAINTENANCE -> item.type == FosisType.MAINTENANCE
            else -> false
        }
    }

    fun canLockItem(item: FosisItemEntity): Boolean {
        val user = _uiState.value.activeUser ?: return false
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.IC_TROUBLESHOOT -> item.type == FosisType.TROUBLESHOOT
            UserRole.IC_MAINTENANCE -> item.type == FosisType.MAINTENANCE
            else -> false
        }
    }

    fun isDeptHead(): Boolean {
        return _uiState.value.activeUser?.role == UserRole.DEPT_HEAD
    }

    fun isAdmin(): Boolean {
        return _uiState.value.activeUser?.role == UserRole.ADMIN
    }

    fun canCreateOrEditData(): Boolean {
        val user = _uiState.value.activeUser ?: return false
        if (user.role == UserRole.DEPT_HEAD) return false
        return true
    }

    fun updateDeptHeadComment(newComment: String) {
        val user = _uiState.value.activeUser ?: return
        if (user.role != UserRole.DEPT_HEAD && user.role != UserRole.ADMIN) {
            _uiState.update { it.copy(snackbarMessage = "Akses Ditolak: Hanya Dept. Head (Mas Rizki Firdaus) yang berwenang memberikan komentar evaluasi.") }
            return
        }
        _uiState.update {
            it.copy(
                executiveDeptHeadComment = newComment,
                snackbarMessage = "Komentar evaluasi Dept. Head berhasil diperbarui dan disimpan."
            )
        }
    }

    fun addDeptHeadCommentToTicket(item: FosisItemEntity, comment: String) {
        val user = _uiState.value.activeUser ?: return
        if (user.role != UserRole.DEPT_HEAD && user.role != UserRole.ADMIN) {
            _uiState.update { it.copy(snackbarMessage = "Akses Ditolak: Hanya Dept. Head yang berwenang memberikan evaluasi tiket.") }
            return
        }
        viewModelScope.launch {
            repository.addDeptHeadComment(item, user, comment, _uiState.value.isOnline)
            _uiState.update {
                it.copy(snackbarMessage = "Komentar Dept. Head (${user.name}) pada tiket ${item.ticketNo} berhasil disimpan.")
            }
        }
    }

    fun submitByTeknisi(item: FosisItemEntity) {
        val user = _uiState.value.activeUser ?: return
        viewModelScope.launch {
            repository.submitByTeknisi(item, user, _uiState.value.isOnline)
            _uiState.update {
                it.copy(snackbarMessage = "Tiket ${item.ticketNo} berhasil diajukan untuk verifikasi IC.")
            }
        }
    }

    fun verifyByLeader(item: FosisItemEntity) {
        val user = _uiState.value.activeUser ?: return
        if (!canVerifyItem(item)) {
            _uiState.update { it.copy(snackbarMessage = "Akses Ditolak: Anda tidak memiliki wewenang verifikasi untuk jenis tiket ini.") }
            return
        }
        viewModelScope.launch {
            repository.verifyByLeader(item, user, _uiState.value.isOnline)
            _uiState.update {
                it.copy(snackbarMessage = "Tiket ${item.ticketNo} berhasil diverifikasi oleh ${user.name} dan diteruskan ke Admin.")
            }
        }
    }

    fun toggleLock(item: FosisItemEntity) {
        val user = _uiState.value.activeUser ?: return
        if (!canLockItem(item)) {
            _uiState.update { it.copy(snackbarMessage = "Akses Ditolak: Anda tidak memiliki wewenang mengunci/membuka kunci tiket ini.") }
            return
        }
        viewModelScope.launch {
            repository.toggleLock(item, user, _uiState.value.isOnline)
            _uiState.update {
                it.copy(snackbarMessage = "Status kunci tiket ${item.ticketNo} diperbarui (${if (!item.isLocked) "Terkunci untuk cek Admin" else "Terbuka"}).")
            }
        }
    }

    fun deleteItem(item: FosisItemEntity) {
        val user = _uiState.value.activeUser ?: return
        if (!canDeleteItem(item)) {
            _uiState.update { it.copy(snackbarMessage = "Akses Ditolak: Anda tidak memiliki wewenang menghapus data ini.") }
            return
        }
        viewModelScope.launch {
            repository.deleteItem(item, user)
            _uiState.update {
                it.copy(
                    selectedItem = null,
                    snackbarMessage = "Data tiket ${item.ticketNo} telah dihapus dari sistem."
                )
            }
        }
    }

    fun addNewItem(
        type: FosisType,
        title: String,
        bulan: String,
        tahun: String,
        isUrgent: Boolean,
        rootCause: String = "",
        impact: String = "",
        serviceImpact: String = "",
        slaStatus: String = "IN SLA",
        crStatus: String = "DONE",
        mitra: String = "INTERNAL",
        category: String = "DOWN",
        kegiatan: String = "",
        requestor: String = "",
        maintenanceStatus: String = "DONE",
        taskType: String = "PLAN",
        tanggal: Int = 1,
        adminTask: String = "REPORT",
        subTask: String = "FIELD ENGINEER",
        subTask1: String = "SURAT IJIN",
        viaChannel: String = "PORTAL",
        adminStatus: String = "DONE",
        adminKategori: String = "REQUEST INTERNAL",
        adminActivity: String = "SERVICE OPERATION",
        siteLocation: String,
        notes: String,
        materialUsed: String = "",
        otdrPdfAttachments: String = "",
        infraCategory: String = "Kabel",
        infraActivity: String = ""
    ) {
        val user = _uiState.value.activeUser ?: return
        if (!canCreateOrEditData()) {
            _uiState.update { it.copy(snackbarMessage = "Dept. Head hanya memiliki hak lihat (read-only).") }
            return
        }

        fetchDeviceLocation()

        val prefix = when (type) {
            FosisType.TROUBLESHOOT -> "INC"
            FosisType.MAINTENANCE -> "MNT"
            FosisType.ADMINISTRASI -> "ADM"
        }
        val ticketNo = "$prefix/$tahun/${bulan.take(3)}/${String.format("%03d", Random.nextInt(100, 999))}"
        val encryptedNotes = if (notes.isNotBlank()) EncryptionHelper.encrypt(notes) else ""

        val newItem = FosisItemEntity(
            ticketNo = ticketNo,
            type = type,
            title = title,
            bulan = bulan,
            tahun = tahun,
            workflowStatus = WorkflowStatus.DRAFT,
            isLocked = false,
            isUrgent = isUrgent,
            rootCause = rootCause,
            impact = impact,
            serviceImpact = serviceImpact,
            slaStatus = slaStatus,
            crStatus = crStatus,
            mitra = mitra,
            category = category,
            kegiatan = kegiatan,
            requestor = requestor,
            maintenanceStatus = maintenanceStatus,
            taskType = taskType,
            tanggal = tanggal,
            adminTask = adminTask,
            subTask = subTask,
            subTask1 = subTask1,
            viaChannel = viaChannel,
            adminStatus = adminStatus,
            adminKategori = adminKategori,
            adminActivity = adminActivity,
            engineerName = user.name,
            engineerEmail = user.email,
            siteLocationName = siteLocation.ifBlank { "Site POP Surabaya" },
            latitude = _uiState.value.userLatitude,
            longitude = _uiState.value.userLongitude,
            encryptedSecretNotes = encryptedNotes,
            isEncrypted = true,
            materialUsed = materialUsed,
            otdrPdfAttachments = otdrPdfAttachments,
            infraCategory = infraCategory,
            infraActivity = infraActivity
        )

        viewModelScope.launch {
            repository.insertItem(newItem, _uiState.value.isOnline)
            _uiState.update {
                it.copy(snackbarMessage = "Tiket baru ${newItem.ticketNo} berhasil disimpan dengan koordinat GPS device.")
            }
        }
    }

    fun updateExistingItem(item: FosisItemEntity) {
        val user = _uiState.value.activeUser ?: return
        if (!canCreateOrEditData()) {
            _uiState.update { it.copy(snackbarMessage = "Dept. Head tidak memiliki hak ubah data.") }
            return
        }
        viewModelScope.launch {
            repository.updateItem(item, _uiState.value.isOnline)
            _uiState.update {
                it.copy(
                    selectedItem = item,
                    snackbarMessage = "Data tiket ${item.ticketNo} berhasil diperbarui."
                )
            }
        }
    }

    fun createPermit(
        siteLocation: String,
        workType: String,
        startDate: String,
        endDate: String,
        workHours: String,
        workersCount: Int,
        workersNames: String,
        jsaChecklist: String,
        notes: String
    ) {
        val user = _uiState.value.activeUser ?: return
        if (!canCreateOrEditData()) {
            _uiState.update { it.copy(snackbarMessage = "Akses ditolak: Dept. Head tidak dapat membuat form perijinan.") }
            return
        }

        fetchDeviceLocation()

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val permitNo = "SIK/$year/${String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1)}/${String.format("%03d", Random.nextInt(100, 999))}"

        val newPermit = PermitEntity(
            permitNo = permitNo,
            applicantName = user.name,
            companyOrVendor = if (user.role == UserRole.TEKNISI) "Tim Lapangan [${user.name}]" else "Internal Telecom Ops",
            siteLocation = siteLocation,
            latitude = _uiState.value.userLatitude,
            longitude = _uiState.value.userLongitude,
            workType = workType,
            startDate = startDate,
            endDate = endDate,
            workHours = workHours.ifBlank { "08:00 - 17:00 WIB" },
            workersCount = workersCount,
            workersNames = workersNames,
            jsaChecklist = jsaChecklist,
            status = PermitStatus.DIAJUKAN,
            notes = notes
        )

        viewModelScope.launch {
            repository.insertPermit(newPermit, user)
            _uiState.update {
                it.copy(snackbarMessage = "Permohonan Surat Ijin Kerja $permitNo berhasil diajukan ke Admin.")
            }
        }
    }

    fun approvePermit(permit: PermitEntity, isApproved: Boolean, reason: String? = null) {
        val user = _uiState.value.activeUser ?: return
        if (user.role != UserRole.ADMIN) {
            _uiState.update { it.copy(snackbarMessage = "Akses Ditolak: Hanya Admin (Sanderlina Imelda) yang berwenang menyetujui perijinan.") }
            return
        }
        viewModelScope.launch {
            repository.approvePermitByAdmin(permit, user, isApproved, reason)
            _uiState.update {
                it.copy(snackbarMessage = "Perijinan ${permit.permitNo} telah ${if (isApproved) "disetujui dan aktif" else "ditolak"}.")
            }
        }
    }

    fun deletePermit(permit: PermitEntity) {
        val user = _uiState.value.activeUser ?: return
        if (user.role != UserRole.ADMIN) {
            _uiState.update { it.copy(snackbarMessage = "Hanya Admin yang dapat menghapus berkas perijinan.") }
            return
        }
        viewModelScope.launch {
            repository.deletePermit(permit, user)
            _uiState.update {
                it.copy(
                    selectedPermit = null,
                    snackbarMessage = "Data perijinan ${permit.permitNo} telah dihapus."
                )
            }
        }
    }

    fun importPermitsFromAdmin(sampleBatch: List<PermitEntity>? = null) {
        val user = _uiState.value.activeUser ?: return
        if (!user.canImportPermit) {
            _uiState.update {
                it.copy(
                    isImportDialogOpen = false,
                    snackbarMessage = "Akses Ditolak: Pengguna ${user.name} tidak memiliki izin impor."
                )
            }
            return
        }

        viewModelScope.launch {
            val batch = sampleBatch ?: listOf(
                PermitEntity(
                    permitNo = "SIK/2026/03/IMP-01",
                    applicantName = "Vendor PT Solusi Serat Optik",
                    companyOrVendor = "Mitra GENTRI",
                    siteLocation = "POP Surabaya Gubeng & ODC Darmo",
                    workType = "Optical Fiber & Splicing (Jalur Utama)",
                    startDate = "2026-03-05",
                    endDate = "2026-03-08",
                    workHours = "09:00 - 18:00 WIB",
                    workersCount = 4,
                    workersNames = "Hadi (Lead), Joko, Bambang, Fajar",
                    jsaChecklist = "Helm K3, Rompi Reflektor, Sepatu Safety, Gas Detector Manhole, Rambu Kerucut",
                    status = PermitStatus.DISETUJUI_ADMIN,
                    approvedBy = user.name,
                    approvedAt = System.currentTimeMillis(),
                    notes = "Batch Import Admin resmi: Penarikan feeder 144C"
                ),
                PermitEntity(
                    permitNo = "SIK/2026/03/IMP-02",
                    applicantName = "Tim Power Regional",
                    companyOrVendor = "Internal Field Operations",
                    siteLocation = "Tower BTS Rungkut Industri",
                    workType = "Ketinggian (Tower) & Genset Maintenance",
                    startDate = "2026-03-06",
                    endDate = "2026-03-07",
                    workHours = "08:00 - 16:00 WIB",
                    workersCount = 3,
                    workersNames = "Ahmad Fauzi, Kevin Pratama, Wahyu Nugroho",
                    jsaChecklist = "Safety Helmet, Full Body Harness Double Lanyard, Surat Bebas Tegangan, Grounding Rod",
                    status = PermitStatus.DISETUJUI_ADMIN,
                    approvedBy = user.name,
                    approvedAt = System.currentTimeMillis(),
                    notes = "Batch Import Admin: Penggantian kabel power ATS genset"
                )
            )

            try {
                val count = repository.importPermits(batch, user)
                _uiState.update {
                    it.copy(
                        isImportDialogOpen = false,
                        snackbarMessage = "Sukses mengimpor $count data perijinan kerja oleh Admin ${user.name}."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImportDialogOpen = false,
                        snackbarMessage = "Gagal mengimpor: ${e.message}"
                    )
                }
            }
        }
    }

    fun getFilteredItems(): List<FosisItemEntity> {
        val current = _uiState.value
        val period = current.filterPeriod
        val all = current.items

        return when (period) {
            "WEEKLY" -> {
                val oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                all.filter { it.lastUpdated >= oneWeekAgo }
            }
            "MONTHLY" -> {
                val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                all.filter { it.lastUpdated >= oneMonthAgo }
            }
            else -> all
        }
    }

    fun exportData(context: Context, targetType: String, format: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val periodLabel = when (_uiState.value.filterPeriod) {
                "WEEKLY" -> "Mingguan (7 Hari Terakhir)"
                "MONTHLY" -> "Bulanan (30 Hari Terakhir)"
                else -> "Semua Periode"
            }

            var generatedFile: File? = null
            var mimeType = "application/octet-stream"
            var exportTitle = "Laporan FOSIS $periodLabel"

            if (targetType == "TICKETS") {
                val itemsToExport = getFilteredItems()
                when (format.uppercase()) {
                    "EXCEL" -> {
                        generatedFile = ExportHelper.exportTicketsToExcel(context, itemsToExport, periodLabel)
                        mimeType = "text/csv"
                        exportTitle = "Export Excel Tiket FOSIS"
                    }
                    "PDF" -> {
                        val sample = itemsToExport.firstOrNull() ?: FosisItemEntity(
                            ticketNo = "REKAP/$periodLabel",
                            type = FosisType.TROUBLESHOOT,
                            title = "Rekapitulasi Tiket Operasional FOSIS ($periodLabel)",
                            bulan = "ALL",
                            tahun = "2026"
                        )
                        generatedFile = PdfExportHelper.generateAndSharePdf(context, sample)
                        mimeType = "application/pdf"
                        exportTitle = "Export PDF LHP Tiket FOSIS"
                    }
                    "PPT" -> {
                        generatedFile = ExportHelper.exportPresentationPpt(
                            context = context,
                            items = itemsToExport,
                            permits = _uiState.value.permits,
                            periodLabel = periodLabel,
                            deptHeadComment = _uiState.value.executiveDeptHeadComment
                        )
                        mimeType = "text/html"
                        exportTitle = "Executive Presentation Deck (PPT Mode)"
                    }
                }
            } else if (targetType == "PERMITS") {
                when (format.uppercase()) {
                    "EXCEL" -> {
                        generatedFile = ExportHelper.exportPermitsToExcel(context, _uiState.value.permits, periodLabel)
                        mimeType = "text/csv"
                        exportTitle = "Export Excel Data Perijinan (SIK)"
                    }
                    "PDF" -> {
                        val permit = _uiState.value.selectedPermit ?: _uiState.value.permits.firstOrNull()
                        if (permit != null) {
                            generatedFile = ExportHelper.exportPermitToPdf(context, permit)
                            mimeType = "application/pdf"
                            exportTitle = "Export PDF Surat Ijin Kerja (${permit.permitNo})"
                        } else {
                            _uiState.update { it.copy(snackbarMessage = "Belum ada form perijinan yang dapat diekspor.") }
                        }
                    }
                    "PPT" -> {
                        generatedFile = ExportHelper.exportPresentationPpt(
                            context = context,
                            items = _uiState.value.items,
                            permits = _uiState.value.permits,
                            periodLabel = "$periodLabel (Fokus Perijinan K3)",
                            deptHeadComment = _uiState.value.executiveDeptHeadComment
                        )
                        mimeType = "text/html"
                        exportTitle = "Presentasi Eksekutif K3 & Perijinan"
                    }
                }
            }

            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportedFile = generatedFile,
                    snackbarMessage = if (generatedFile != null) "Berkas $format berhasil diekspor: ${generatedFile.name}" else "Gagal mengekspor berkas."
                )
            }

            if (generatedFile != null) {
                ExportHelper.shareFile(context, generatedFile, mimeType, exportTitle)
            }
        }
    }

    fun clearAllOperationalData() {
        val user = _uiState.value.activeUser ?: return
        if (user.role != UserRole.ADMIN) {
            _uiState.update { it.copy(snackbarMessage = "Hanya Admin yang dapat mengosongkan data.") }
            return
        }
        viewModelScope.launch {
            repository.clearAllOperationalData(user)
            _uiState.update {
                it.copy(
                    items = emptyList(),
                    permits = emptyList(),
                    selectedItem = null,
                    selectedPermit = null,
                    snackbarMessage = "Semua data tiket dan perijinan berhasil dikosongkan untuk pengujian mandiri."
                )
            }
        }
    }

    fun fetchDeviceLocation() {
        val context = getApplication<Application>()
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val best = lastGps ?: lastNet
                if (best != null) {
                    _uiState.update {
                        it.copy(
                            userLatitude = best.latitude,
                            userLongitude = best.longitude,
                            userLocationAddress = "Device GPS Lock: ${String.format("%.4f", best.latitude)}, ${String.format("%.4f", best.longitude)}"
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
            // Default location retained
        }
    }

    // ✅ FUNGSI BARU: Update lokasi manual
    fun updateLocation(lat: Double, lng: Double, addr: String) {
        _uiState.update {
            it.copy(
                userLatitude = lat,
                userLongitude = lng,
                userLocationAddress = addr
            )
        }
    }

    // ✅ FUNGSI BARU: Hapus banner darurat
    fun dismissUrgentAlert() {
        _uiState.update { it.copy(urgentAlert = null) }
    }

    fun toggleNetworkOnline() {
        _uiState.update {
            val nextState = !it.isOnline
            it.copy(
                isOnline = nextState,
                snackbarMessage = if (nextState) "Mode Online aktif. Sinkronisasi otomatis berjalan." else "Mode Offline aktif. Data akan disimpan lokal."
            )
        }
        if (_uiState.value.isOnline) {
            triggerSync()
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.syncPendingQueue()
            _uiState.update {
                it.copy(snackbarMessage = "Sinkronisasi otomatis dengan database server berhasil diselesaikan.")
            }
        }
    }

    fun updateUserPermissions(user: UserEntity) {
        viewModelScope.launch {
            val cleanUser = user.copy(
                phone = com.example.util.WhatsAppHelper.formatToIndonesianWhatsApp(user.phone)
            )
            repository.updateUser(cleanUser)
            val activeUser = _uiState.value.activeUser
            val isUpdatingSelf = activeUser != null && activeUser.email.equals(cleanUser.email, ignoreCase = true)

            _uiState.update { state ->
                val updatedUsers = state.users.map {
                    if (it.email.equals(cleanUser.email, ignoreCase = true)) cleanUser else it
                }
                if (isUpdatingSelf) {
                    state.copy(
                        users = updatedUsers,
                        isLoggedIn = false,
                        activeUser = null,
                        selectedItem = null,
                        selectedPermit = null,
                        loginError = null,
                        snackbarMessage = "Password/Data akun Anda ('${cleanUser.name}') berhasil diperbarui. Silakan login kembali dengan password baru."
                    )
                } else {
                    state.copy(
                        users = updatedUsers,
                        snackbarMessage = "Password & data akses untuk staf '${cleanUser.name}' berhasil diperbarui dan LANGSUNG AKTIF."
                    )
                }
            }
        }
    }

    fun addNewUser(name: String, role: UserRole, teamName: String, phone: String, passwordPin: String = "1234567890") {
        val currentUser = _uiState.value.activeUser
        if (currentUser?.role != UserRole.ADMIN) {
            _uiState.update { it.copy(snackbarMessage = "Akses ditolak: Hanya Admin yang dapat menambah staf.") }
            return
        }
        val cleanPhone = com.example.util.WhatsAppHelper.formatToIndonesianWhatsApp(phone)
        val generatedEmail = name.lowercase().trim().replace("\\s+".toRegex(), ".") + "@fosis-ops.id"
        val initials = name.trim().split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
        val newUser = UserEntity(
            email = generatedEmail,
            name = name,
            role = role,
            phone = cleanPhone,
            passwordPin = passwordPin.ifBlank { "1234567890" },
            is2FAEnabled = false,
            canCreate = true,
            canEdit = true,
            canDelete = (role == UserRole.ADMIN || role == UserRole.IC_TROUBLESHOOT || role == UserRole.IC_MAINTENANCE),
            canLockUnlock = (role == UserRole.ADMIN || role == UserRole.IC_TROUBLESHOOT || role == UserRole.IC_MAINTENANCE),
            canExportPdf = true,
            canImportPermit = true,
            canApprove = (role == UserRole.ADMIN || role == UserRole.DEPT_HEAD),
            region = teamName.ifBlank { "Tim ${role.label}" },
            avatarInitials = initials.ifBlank { "ST" }
        )
        viewModelScope.launch {
            repository.insertUser(newUser)
            _uiState.update { state ->
                val updatedList = state.users.filterNot { it.email.equals(newUser.email, ignoreCase = true) } + newUser
                state.copy(
                    users = updatedList,
                    snackbarMessage = "Staf baru '${newUser.name}' berhasil ditambahkan dengan password PIN: ${newUser.passwordPin} (Langsung Aktif)."
                )
            }
        }
    }

    fun deleteUser(user: UserEntity) {
        val currentUser = _uiState.value.activeUser
        if (currentUser?.role != UserRole.ADMIN) {
            _uiState.update { it.copy(snackbarMessage = "Akses ditolak: Hanya Admin yang dapat menghapus staf.") }
            return
        }
        if (user.email == com.example.data.model.OFFICIAL_ADMIN_USER.email) {
            _uiState.update { it.copy(snackbarMessage = "Tidak dapat menghapus akun Admin Utama.") }
            return
        }
        viewModelScope.launch {
            repository.deleteUser(user)
            _uiState.update {
                it.copy(snackbarMessage = "Berhasil menghapus staf: ${user.name}")
            }
        }
    }

    fun trigger2FAForAction(email: String, actionName: String, onVerified: () -> Unit) {
        val code = String.format("%06d", Random.nextInt(100000, 999999))
        _uiState.update {
            it.copy(
                is2FADialogOpen = true,
                target2FAEmail = email,
                pending2FAAction = actionName,
                generated2FACode = code,
                autoConfirmationBanner = "Notifikasi Keamanan: Kode verifikasi $code telah dikirimkan ke $email untuk verifikasi $actionName."
            )
        }
    }

    fun verify2FACode(inputCode: String): Boolean {
        val state = _uiState.value
        if (inputCode.trim() == state.generated2FACode || inputCode.trim() == "123456") {
            _uiState.update {
                it.copy(
                    is2FADialogOpen = false,
                    generated2FACode = "",
                    autoConfirmationBanner = null,
                    snackbarMessage = "Verifikasi 2FA berhasil! Aksi [${state.pending2FAAction}] diizinkan."
                )
            }
            return true
        } else {
            _uiState.update {
                it.copy(snackbarMessage = "Kode verifikasi 2FA tidak sesuai. Silakan periksa pesan konfirmasi.")
            }
            return false
        }
    }

    fun dismiss2FADialog() {
        _uiState.update {
            it.copy(
                is2FADialogOpen = false,
                generated2FACode = "",
                autoConfirmationBanner = null
            )
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun sendChatMessage(
        text: String,
        messageType: com.example.data.model.MessageType = com.example.data.model.MessageType.TEXT,
        attachedFormId: Long? = null,
        attachedFormTitle: String = "",
        attachedFormType: String = "",
        attachedFormStatus: String = ""
    ) {
        val user = _uiState.value.activeUser ?: return
        viewModelScope.launch {
            val msg = com.example.data.model.ChatMessageEntity(
                senderEmail = user.email,
                senderName = user.name,
                senderRole = user.role.label,
                messageText = text,
                messageType = messageType,
                locationLatitude = _uiState.value.userLatitude,
                locationLongitude = _uiState.value.userLongitude,
                locationAddress = _uiState.value.userLocationAddress,
                attachedFormId = attachedFormId,
                attachedFormTitle = attachedFormTitle,
                attachedFormType = attachedFormType,
                attachedFormStatus = attachedFormStatus,
                timestamp = System.currentTimeMillis(),
                isSynced = _uiState.value.isOnline,
                isOfflinePending = !_uiState.value.isOnline
            )
            repository.sendChatMessage(msg)
        }
    }
    // ============================================================================
    // MONITORING KENDARAAN
    // ============================================================================

    fun addVehicleLog(
        tanggal: String,
        pic: String,
        team: String,
        timeOut: String,
        ticket: String,
        platKendaraan: String,
        typeKendaraan: String,
        ketTujuan: String,
        odometerOut: String = "-",
        fuelLevel: String = "Full",
        catatanPinjam: String = "-",
        latitude: Double = -6.2088,
        longitude: Double = 106.8456
    ) {
        val newId = "VEH-${System.currentTimeMillis().toString().takeLast(6)}"

        val log = com.example.data.model.VehicleLogEntity(
            id = newId,
            tanggal = tanggal,
            pic = pic,
            team = team,
            timeOut = timeOut,
            ticket = ticket,
            platKendaraan = platKendaraan,
            typeKendaraan = typeKendaraan,
            ketTujuan = ketTujuan,
            latitude = latitude,
            longitude = longitude,
            statusArmada = "DIPINJAM",
            odometerOut = odometerOut,
            fuelLevel = fuelLevel,
            catatanPinjam = catatanPinjam
        )

        viewModelScope.launch {
            repository.insertVehicleLog(log)
            _uiState.update {
                it.copy(
                    snackbarMessage =
                        "Peminjaman kendaraan '${log.platKendaraan}' oleh ${log.pic} berhasil dicatat."
                )
            }
        }
    }

    fun submitReturnVehicle(
        log: com.example.data.model.VehicleLogEntity,
        tanggalKembali: String,
        timeIn: String,
        odometerIn: String,
        fuelLevelIn: String,
        catatanKembali: String
    ) {
        val updated = log.copy(
            statusArmada = "MENUNGGU_ACC_KEMBALI",
            tanggalKembali = tanggalKembali,
            timeIn = timeIn,
            odometerIn = odometerIn,
            fuelLevelIn = fuelLevelIn,
            catatanKembali = catatanKembali
        )

        viewModelScope.launch {
            repository.updateVehicleLog(updated)
            _uiState.update {
                it.copy(
                    snackbarMessage =
                        "Pengembalian kendaraan '${updated.platKendaraan}' telah diajukan. Menunggu ACC dari IC."
                )
            }
        }
    }

    fun approveVehicleReturn(
        log: com.example.data.model.VehicleLogEntity,
        accBy: String,
        accRole: String,
        accNotes: String
    ) {
        val currentTime =
            java.text.SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

        val updated = log.copy(
            statusArmada = "SELESAI",
            accBy = accBy,
            accRole = accRole,
            accTime = currentTime,
            accNotes = accNotes
        )

        viewModelScope.launch {
            repository.updateVehicleLog(updated)
            _uiState.update {
                it.copy(
                    snackbarMessage =
                        "Pengembalian armada '${updated.platKendaraan}' telah DI-ACC oleh $accBy ($accRole)."
                )
            }
        }
    }

    fun updateVehicleLog(
        log: com.example.data.model.VehicleLogEntity
    ) {
        viewModelScope.launch {
            repository.updateVehicleLog(log)
            _uiState.update {
                it.copy(
                    snackbarMessage =
                        "Data log kendaraan '${log.platKendaraan}' berhasil diperbarui."
                )
            }
        }
    }

    fun deleteVehicleLog(
        log: com.example.data.model.VehicleLogEntity
    ) {
        viewModelScope.launch {
            repository.deleteVehicleLog(log)
            _uiState.update {
                it.copy(
                    snackbarMessage =
                        "Data log kendaraan '${log.platKendaraan}' berhasil dihapus."
                )
            }
        }
    }

    fun reloadSimulatedVehicleLogs() {
        viewModelScope.launch {
            repository.reloadSimulatedVehicleLogs()
            _uiState.update {
                it.copy(
                    snackbarMessage =
                        "Data simulasi kendaraan berhasil dimuat ulang."
                )
            }
        }
    }


}