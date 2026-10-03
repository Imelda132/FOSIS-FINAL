package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AuditLogEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.data.model.PermitEntity
import com.example.data.model.SyncQueueEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * ============================================================================
 * INTERFACE: FosisDao (Data Access Object)
 * TUJUAN: Menangani seluruh query & manipulasi data Room SQLite lokal untuk aplikasi FOSIS.
 * Meliputi operasi CRUD pada Tiket Operasional, SIK (Permit), Pengguna, Log Audit, Antrian Sinkronisasi, dan Chat Lapangan.
 *
 * PANDUAN UBAH QUERY SECARA MANUAL:
 * - Sesuaikan klausa SQL @Query(...) jika ingin mengubah pengurutan, filter kolom, atau batas limit query.
 * ============================================================================
 */
@Dao
interface FosisDao {
    // ==========================================
    // 1. OPERASI TIKET OPERASIONAL (Fosis Items)
    // ==========================================

    /** Mengambil seluruh tiket (Troubleshoot, Maintenance, Administrasi) diurutkan dari pembaruan terbaru */
    @Query("SELECT * FROM fosis_items ORDER BY lastUpdated DESC")
    fun getAllItems(): Flow<List<FosisItemEntity>>

    /** Mengambil daftar tiket berdasarkan tipe modul tertentu (TROUBLESHOOT / MAINTENANCE / ADMINISTRASI) */
    @Query("SELECT * FROM fosis_items WHERE type = :type ORDER BY lastUpdated DESC")
    fun getItemsByType(type: FosisType): Flow<List<FosisItemEntity>>

    /** Mengambil tiket darurat (Over SLA / Status Mendesak) untuk ditampilkan pada banner peringatan */
    @Query("SELECT * FROM fosis_items WHERE isUrgent = 1 ORDER BY lastUpdated DESC")
    fun getUrgentItems(): Flow<List<FosisItemEntity>>

    /** Mengambil detail lengkap 1 tiket berdasarkan ID database */
    @Query("SELECT * FROM fosis_items WHERE id = :id")
    suspend fun getItemById(id: Long): FosisItemEntity?

    /** Menyimpan 1 tiket baru ke database lokal (mengembalikan generated ID) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FosisItemEntity): Long

    /** Menyimpan banyak tiket sekaligus (digunakan saat inisialisasi / import dataset) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<FosisItemEntity>)

    /** Memperbarui data tiket yang sudah ada (status, catatan, workflow, otorisasi) */
    @Update
    suspend fun updateItem(item: FosisItemEntity)

    /** Menghapus 1 tiket dari database */
    @Delete
    suspend fun deleteItem(item: FosisItemEntity)

    /** Menghapus tiket berdasarkan ID spesifik */
    @Query("DELETE FROM fosis_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    /** Mengosongkan seluruh tabel tiket operasional */
    @Query("DELETE FROM fosis_items")
    suspend fun clearAllItems()

    /** Menandai seluruh tiket sebagai tersinkronisasi ke server (isSynced = 1) */
    @Query("UPDATE fosis_items SET isSynced = 1")
    suspend fun markAllSynced()

    // ==========================================
    // 2. OPERASI SURAT IZIN KERJA (Permits / SIK)
    // ==========================================

    /** Mengambil semua daftar permohonan Surat Izin Kerja (SIK) */
    @Query("SELECT * FROM permits ORDER BY createdAt DESC")
    fun getAllPermits(): Flow<List<PermitEntity>>

    /** Mengambil detail 1 izin kerja berdasarkan ID */
    @Query("SELECT * FROM permits WHERE id = :id")
    suspend fun getPermitById(id: Long): PermitEntity?

    /** Mengajukan / menyimpan surat izin kerja baru */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermit(permit: PermitEntity): Long

    /** Menyimpan banyak data perizinan kerja sekaligus */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermits(permits: List<PermitEntity>)

    /** Memperbarui status persetujuan atau catatan permit */
    @Update
    suspend fun updatePermit(permit: PermitEntity)

    /** Menghapus surat izin kerja dari database */
    @Delete
    suspend fun deletePermit(permit: PermitEntity)

    /** Menghapus surat izin kerja berdasarkan ID */
    @Query("DELETE FROM permits WHERE id = :id")
    suspend fun deletePermitById(id: Long)

    /** Mengosongkan tabel perizinan kerja */
    @Query("DELETE FROM permits")
    suspend fun clearAllPermits()

    // ==========================================
    // 3. OPERASI PENGGUNA & HAK AKSES (Users)
    // ==========================================

    /** Mengambil daftar seluruh personel operasional diurutkan berdasarkan level jabatan/role */
    @Query("SELECT * FROM users ORDER BY role ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    /** Mencari akun pengguna berdasarkan alamat email */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    /** Menyimpan roster daftar seluruh pengguna sistem */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /** Menambahkan 1 pengguna / teknisi baru */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /** Memperbarui data profil pengguna */
    @Update
    suspend fun updateUser(user: UserEntity)

    /** Menghapus pengguna */
    @Delete
    suspend fun deleteUser(user: UserEntity)

    /** Menghapus pengguna berdasarkan email */
    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)

    /** Mengosongkan data pengguna */
    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    /** Menghapus akun berdasarkan email atau pola nama (untuk pembersihan data tidak valid) */
    @Query("DELETE FROM users WHERE email = :email OR name LIKE '%' || :namePattern || '%'")
    suspend fun deleteUserByEmailOrPattern(email: String, namePattern: String)

    // ==========================================
    // 4. LOG AUDIT & JEJAK REKAM KEAMANAN
    // ==========================================

    /** Mengambil 100 catatan log aktivitas operasional terbaru untuk transparansi keamanan */
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    /** Mencatat riwayat aksi baru (login, submit, approval, perubahan data, export) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    /** Mengosongkan riwayat audit log */
    @Query("DELETE FROM audit_logs")
    suspend fun clearAllAuditLogs()

    // ==========================================
    // 5. ANTRIAN SINKRONISASI OFFLINE (Sync Queue)
    // ==========================================

    /** Mengambil antrian aksi offline yang menunggu koneksi internet */
    @Query("SELECT * FROM sync_queue WHERE isPending = 1 ORDER BY timestamp ASC")
    fun getPendingSyncQueue(): Flow<List<SyncQueueEntity>>

    /** Memasukkan perubahan data ke antrian sinkronisasi lokal saat sedang offline */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueue(entry: SyncQueueEntity)

    /** Mengosongkan antrian sinkronisasi setelah proses sync selesai */
    @Query("DELETE FROM sync_queue")
    suspend fun clearSyncQueue()

    // ==========================================
    // 6. CHAT LAPANGAN & KOORDINASI TIM (Chat Messages)
    // ==========================================

    /** Mengambil riwayat pesan koordinasi teknisi lapangan */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    /** Mengirim pesan chat koordinasi baru */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    /** Mengosongkan riwayat pesan chat */
    @Query("DELETE FROM chat_messages")
    suspend fun clearAllChatMessages()
    // ============================================================================
    // 7. MONITORING KENDARAAN
    // ============================================================================

    @Query("SELECT * FROM vehicle_logs ORDER BY tanggal DESC, timeOut DESC")
    fun getAllVehicleLogs(): Flow<List<com.example.data.model.VehicleLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleLog(
        vehicleLog: com.example.data.model.VehicleLogEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleLogs(
        logs: List<com.example.data.model.VehicleLogEntity>
    )

    @Update
    suspend fun updateVehicleLog(
        vehicleLog: com.example.data.model.VehicleLogEntity
    )

    @Delete
    suspend fun deleteVehicleLog(
        vehicleLog: com.example.data.model.VehicleLogEntity
    )

    @Query("SELECT COUNT(*) FROM vehicle_logs")
    suspend fun countVehicleLogs(): Int

    @Query("DELETE FROM vehicle_logs")
    suspend fun clearAllVehicleLogs()


}