package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FosisItemEntity
import com.example.data.model.INITIAL_VEHICLE_LOGS
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleLogEntity
import java.text.SimpleDateFormat
import java.util.*

enum class MapType {
    ROADMAP, SATELLITE, HYBRID
}

enum class VehicleViewMode(val label: String) {
    SPREADSHEET_TABLE("Tabel Matriks (Excel)"),
    MAP_SPLIT_VIEW("Google Maps Real & Radar"),
    CARD_LIST("Format Kartu Admin")
}

/**
 * Helper generator HTML Leaflet + Google Maps Layers untuk Real-Time Geolocation
 */
fun buildGoogleMapsLeafletHtml(
    vehicles: List<VehicleLogEntity>,
    focusedVehicleId: String? = null,
    defaultLat: Double = -6.2322,
    defaultLng: Double = 106.8317,
    defaultZoom: Int = 14
): String {
    val vehicleJsArray = vehicles.joinToString(",") { v ->
        val isMotor = v.typeKendaraan.contains("LEXY", ignoreCase = true) ||
                v.typeKendaraan.contains("NMAX", ignoreCase = true) ||
                v.typeKendaraan.contains("Vario", ignoreCase = true) ||
                v.typeKendaraan.contains("Beat", ignoreCase = true)
        val cleanPlat = v.platKendaraan.replace("\"", "")
        val cleanPic = v.pic.replace("\"", "")
        val cleanType = v.typeKendaraan.replace("\"", "")
        val cleanTujuan = v.ketTujuan.replace("\"", "")
        val cleanOdo = v.odometerOut.replace("\"", "")
        val cleanFuel = v.fuelLevel.replace("\"", "")
        val cleanTicket = v.ticket.replace("\"", "")
        val cleanTime = v.timeOut.replace("\"", "")

        """
        {
          "id": "${v.id}",
          "plat": "$cleanPlat",
          "type": "$cleanType",
          "pic": "$cleanPic",
          "team": "${v.team}",
          "ticket": "$cleanTicket",
          "tujuan": "$cleanTujuan",
          "lat": ${v.latitude},
          "lng": ${v.longitude},
          "status": "${v.statusArmada}",
          "timeOut": "$cleanTime",
          "fuel": "$cleanFuel",
          "odometer": "$cleanOdo",
          "isMotor": $isMotor
        }
        """.trimIndent()
    }

    val focused = vehicles.find { it.id == focusedVehicleId }
    val focusedLat = focused?.latitude ?: defaultLat
    val focusedLng = focused?.longitude ?: defaultLng
    val zoom = if (focusedVehicleId != null) 16 else defaultZoom

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #0f172a; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
            .vehicle-label {
                background: rgba(15, 23, 42, 0.95);
                color: #ffffff;
                border: 1px solid #38bdf8;
                border-radius: 6px;
                padding: 4px 8px;
                font-size: 11px;
                font-weight: 700;
                box-shadow: 0 4px 12px rgba(0,0,0,0.5);
                white-space: nowrap;
            }
            .custom-pin {
                display: flex;
                align-items: center;
                justify-content: center;
                width: 38px;
                height: 38px;
                border-radius: 50%;
                color: white;
                font-weight: bold;
                font-size: 18px;
                box-shadow: 0 4px 14px rgba(0,0,0,0.6);
                border: 2px solid #ffffff;
            }
            .pin-mobil { background: linear-gradient(135deg, #0284c7, #0369a1); }
            .pin-motor { background: linear-gradient(135deg, #7e22ce, #6b21a8); }
            .leaflet-popup-content-wrapper {
                background: #0f172a;
                color: #f8fafc;
                border: 1px solid #334155;
                border-radius: 12px;
                padding: 6px;
                box-shadow: 0 8px 24px rgba(0,0,0,0.6);
            }
            .leaflet-popup-tip { background: #0f172a; }
            .popup-title { font-size: 13px; font-weight: 800; color: #38bdf8; margin-bottom: 4px; }
            .popup-row { font-size: 11px; color: #cbd5e1; margin-bottom: 2px; }
            .popup-btn {
                display: block;
                width: 100%;
                text-align: center;
                background: #2563eb;
                color: #ffffff;
                padding: 6px 0;
                border-radius: 6px;
                text-decoration: none;
                font-weight: 700;
                font-size: 11px;
                margin-top: 8px;
            }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            var googleStreets = L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                maxZoom: 20,
                attribution: '© Google Maps'
            });

            var googleSat = L.tileLayer('https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', {
                maxZoom: 20,
                attribution: '© Google Maps Satelit'
            });

            var osmLayer = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '© OpenStreetMap'
            });

            var map = L.map('map', {
                center: [$focusedLat, $focusedLng],
                zoom: $zoom,
                layers: [googleStreets],
                zoomControl: true
            });

            var baseMaps = {
                "Google Maps Jalan": googleStreets,
                "Google Maps Satelit": googleSat,
                "OpenStreetMap": osmLayer
            };
            L.control.layers(baseMaps).addTo(map);

            // Titik Awal Kantor Pusat: Menara Kadin Indonesia
            var kadinIcon = L.divIcon({
                className: '',
                html: '<div class="custom-pin" style="background: linear-gradient(135deg, #1e3a8a, #2563eb); border: 2.5px solid #facc15; width: 42px; height: 42px; font-size: 20px;">🏢</div>',
                iconSize: [42, 42],
                iconAnchor: [21, 21],
                popupAnchor: [0, -21]
            });
            var kadinMarker = L.marker([-6.2322, 106.8317], { icon: kadinIcon }).addTo(map);

            kadinMarker.bindTooltip("🏢 Titik Awal: Kantor Menara Kadin", {
                permanent: true,
                direction: 'top',
                className: 'vehicle-label'
            });

            var kadinPopup = '<div style="min-width: 210px;">' +
                '<div class="popup-title" style="color: #facc15;">🏢 KANTOR PUSAT: MENARA KADIN</div>' +
                '<div class="popup-row"><b>Alamat:</b> Jl. H.R. Rasuna Said Blok X-5 Kav. 2-3, Kuningan</div>' +
                '<div class="popup-row"><b>Status:</b> Titik Awal Basecamp Peminjaman & Pengembalian Armada</div>' +
                '<div class="popup-row"><b>Koordinat:</b> -6.2322, 106.8317</div>' +
                '<a class="popup-btn" href="https://maps.google.com/?q=-6.2322,106.8317">🧭 Buka Google Maps Menara Kadin</a>' +
                '</div>';
            kadinMarker.bindPopup(kadinPopup);

            var vehicles = [$vehicleJsArray];
            var bounds = [[-6.2322, 106.8317]];

            vehicles.forEach(function(v) {
                var iconHtml = v.isMotor ? '🛵' : '🚗';
                var pinClass = v.isMotor ? 'pin-motor' : 'pin-mobil';
                var customIcon = L.divIcon({
                    className: '',
                    html: '<div class="custom-pin ' + pinClass + '">' + iconHtml + '</div>',
                    iconSize: [38, 38],
                    iconAnchor: [19, 19],
                    popupAnchor: [0, -19]
                });

                var marker = L.marker([v.lat, v.lng], { icon: customIcon }).addTo(map);
                bounds.push([v.lat, v.lng]);

                marker.bindTooltip(v.plat + ' • ' + v.pic, {
                    permanent: false,
                    direction: 'top',
                    className: 'vehicle-label'
                });

                var popupHtml = '<div style="min-width: 190px;">' +
                    '<div class="popup-title">' + v.plat + ' (' + v.type + ')</div>' +
                    '<div class="popup-row"><b>PIC:</b> ' + v.pic + ' (Team ' + v.team + ')</div>' +
                    '<div class="popup-row"><b>Tujuan:</b> ' + v.tujuan + '</div>' +
                    '<div class="popup-row"><b>Waktu Out:</b> ' + v.timeOut + '</div>' +
                    '<div class="popup-row"><b>Odo / BBM:</b> ' + v.odometer + ' • ' + v.fuel + '</div>' +
                    '<div class="popup-row"><b>Tiket:</b> ' + v.ticket + '</div>' +
                    '<a class="popup-btn" href="https://maps.google.com/?q=' + v.lat + ',' + v.lng + '">🧭 Buka Navigasi Google Maps</a>' +
                    '</div>';

                marker.bindPopup(popupHtml);

                if ("$focusedVehicleId" === v.id) {
                    marker.openPopup();
                }
            });

            if (!"$focusedVehicleId") {
                // Selalu letakkan awal titik camera di Menara Kadin
                map.setView([-6.2322, 106.8317], 15);
                kadinMarker.openPopup();
            }
        </script>
    </body>
    </html>
    """.trimIndent()
}

/**
 * Real Google Maps Interactive WebView Component
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RealGoogleMapsEmbedView(
    vehicles: List<VehicleLogEntity>,
    focusedVehicle: VehicleLogEntity? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val htmlContent = remember(vehicles, focusedVehicle?.id) {
        buildGoogleMapsLeafletHtml(
            vehicles = vehicles,
            focusedVehicleId = focusedVehicle?.id
        )
    }

    LaunchedEffect(htmlContent) {
        webViewRef?.loadDataWithBaseURL("https://maps.google.com", htmlContent, "text/html", "UTF-8", null)
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            if (url != null && (url.contains("maps.google.com") || url.startsWith("geo:"))) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    return true
                                } catch (e: Exception) {
                                    return false
                                }
                            }
                            return false
                        }
                    }
                    loadDataWithBaseURL("https://maps.google.com", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { wv ->
                wv.loadDataWithBaseURL("https://maps.google.com", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Indicator: GPS Google Maps Live Status
        val targetVeh = focusedVehicle
        val targetLat = targetVeh?.latitude ?: -6.2322
        val targetLng = targetVeh?.longitude ?: 106.8317
        val isMenaraKadin = targetVeh == null

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(8.dp)
                .fillMaxWidth(0.96f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF16A34A).copy(alpha = 0.25f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GPS Terhubung Google Maps",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF16A34A)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isMenaraKadin) "🏢 Titik Awal: Kantor Menara Kadin • -6.2322, 106.8317" else "${targetVeh?.platKendaraan} (${targetVeh?.pic}) • Lat: ${String.format(Locale.US, "%.4f", targetLat)}, Lng: ${String.format(Locale.US, "%.4f", targetLng)}",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:$targetLat,$targetLng?q=$targetLat,$targetLng(${Uri.encode("${targetVeh?.platKendaraan ?: "Armada"} - ${targetVeh?.pic ?: ""}")})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$targetLat,$targetLng"))
                            context.startActivity(browserIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buka App Maps", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


/**
 * ============================================================================
 * COMPOSABLE: GeolocationMapView (Monitoring, Peminjaman & Pengembalian Kendaraan + ACC IC)
 * ============================================================================
 * Format Resmi Kolom yang Ditampilkan Sesuai Rekap:
 * 1. Tanggal
 * 2. PIC
 * 3. Team (SO / SM)
 * 4. Time out
 * 5. Ticket
 * 6. Plat kendaraan
 * 7. Type Kendaraan
 * 8. Ket. Tujuan
 * + Status Peminjaman, Pengembalian & ACC IC
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeolocationMapView(
    userLatitude: Double,
    userLongitude: Double,
    userAddress: String,
    activeUser: UserEntity? = null,
    items: List<FosisItemEntity> = emptyList(),
    vehicleLogs: List<VehicleLogEntity> = emptyList(),
    onAddVehicleLog: (tanggal: String, pic: String, team: String, timeOut: String, ticket: String, plat: String, type: String, tujuan: String, odoOut: String, fuel: String, catatan: String, lat: Double, lng: Double) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onSubmitReturn: (log: VehicleLogEntity, tanggalKembali: String, timeIn: String, odoIn: String, fuelIn: String, catatanKembali: String) -> Unit = { _, _, _, _, _, _ -> },
    onApproveReturn: (log: VehicleLogEntity, accBy: String, accRole: String, accNotes: String) -> Unit = { _, _, _, _ -> },
    onUpdateVehicleLog: (VehicleLogEntity) -> Unit = {},
    onDeleteVehicleLog: (VehicleLogEntity) -> Unit = {},
    onReloadSimulatedData: () -> Unit = {},
    onSimulateLocation: (Double, Double, String) -> Unit = { _, _, _ -> },
    onSelectItem: (FosisItemEntity) -> Unit = {}
) {
    val context = LocalContext.current
    var viewMode by remember { mutableStateOf(VehicleViewMode.SPREADSHEET_TABLE) }
    var mapType by remember { mutableStateOf(MapType.ROADMAP) }
    var zoomLevel by remember { mutableStateOf(1.0f) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showStatsCards by remember { mutableStateOf(false) }

    // Dialog States
    var selectedVehicleLog by remember { mutableStateOf<VehicleLogEntity?>(null) }
    var liveTrackingVehicle by remember { mutableStateOf<VehicleLogEntity?>(null) }
    var showSimulationGuide by remember { mutableStateOf(false) }
    var showLoanDialog by remember { mutableStateOf(false) }
    var vehicleToReturn by remember { mutableStateOf<VehicleLogEntity?>(null) }
    var vehicleToAcc by remember { mutableStateOf<VehicleLogEntity?>(null) }
    var vehicleToEdit by remember { mutableStateOf<VehicleLogEntity?>(null) }
    var vehicleToDelete by remember { mutableStateOf<VehicleLogEntity?>(null) }

    val isAdminOrIC = activeUser?.role == UserRole.ADMIN ||
            activeUser?.role == UserRole.IC_TROUBLESHOOT ||
            activeUser?.role == UserRole.IC_MAINTENANCE ||
            activeUser?.role == UserRole.DEPT_HEAD

    // Ensure fallback to INITIAL_VEHICLE_LOGS if database is initially empty
    val effectiveLogs = remember(vehicleLogs) {
        if (vehicleLogs.isNotEmpty()) vehicleLogs else INITIAL_VEHICLE_LOGS
    }

    // Helper functions to categorize dates into weeks & months
    fun parseDateParts(dateStr: String): Pair<Int, String> {
        val clean = dateStr.replace("-", "/").trim()
        val parts = clean.split("/")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val month = parts.getOrNull(1) ?: "09"
        return Pair(day, month)
    }

    // Filter vehicle logs
    val filteredVehicleLogs = remember(effectiveLogs, searchQuery, selectedStatusFilter, selectedCategoryFilter) {
        effectiveLogs.filter { log ->
            val matchSearch = searchQuery.isBlank() ||
                    log.platKendaraan.contains(searchQuery, ignoreCase = true) ||
                    log.ticket.contains(searchQuery, ignoreCase = true) ||
                    log.pic.contains(searchQuery, ignoreCase = true) ||
                    log.team.contains(searchQuery, ignoreCase = true) ||
                    log.ketTujuan.contains(searchQuery, ignoreCase = true) ||
                    log.tanggal.contains(searchQuery, ignoreCase = true) ||
                    log.typeKendaraan.contains(searchQuery, ignoreCase = true) ||
                    log.accBy.contains(searchQuery, ignoreCase = true) ||
                    log.kategoriKendaraan.contains(searchQuery, ignoreCase = true)

            val (day, month) = parseDateParts(log.tanggal)
            val isThisWeek = month == "09" && day in 8..14
            val isLastWeek = month == "09" && day in 1..7
            val isThisMonth = month == "09"
            val isLastMonth = month == "08"

            val matchStatus = when (selectedStatusFilter) {
                "ALL" -> true
                "DIPINJAM" -> log.statusArmada.equals("DIPINJAM", ignoreCase = true) || log.statusArmada.equals("ON MISSION", ignoreCase = true)
                "MENUNGGU_ACC" -> log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true)
                "SELESAI" -> log.statusArmada.equals("SELESAI", ignoreCase = true)
                else -> true
            }

            val matchCategory = when (selectedCategoryFilter) {
                "ALL" -> true
                "MOBIL" -> !log.isMotor
                "MOTOR" -> log.isMotor
                "SO" -> log.team.equals("SO", ignoreCase = true)
                "SM" -> log.team.equals("SM", ignoreCase = true)
                "THIS_WEEK" -> isThisWeek
                "THIS_MONTH" -> isThisMonth
                else -> true
            }

            matchSearch && matchStatus && matchCategory
        }
    }

    // Vehicle statistics: Mobil vs Motor & Operational Status
    val totalArmada = effectiveLogs.size
    val mobilLogs = remember(effectiveLogs) { effectiveLogs.filter { !it.isMotor } }
    val motorLogs = remember(effectiveLogs) { effectiveLogs.filter { it.isMotor } }

    val totalMobil = mobilLogs.size
    val mobilDipinjam = mobilLogs.count { it.statusArmada.equals("DIPINJAM", true) || it.statusArmada.equals("ON MISSION", true) }
    val mobilMenungguAcc = mobilLogs.count { it.statusArmada.equals("MENUNGGU_ACC_KEMBALI", true) }
    val mobilSelesai = mobilLogs.count { it.statusArmada.equals("SELESAI", true) }

    val totalMotor = motorLogs.size
    val motorDipinjam = motorLogs.count { it.statusArmada.equals("DIPINJAM", true) || it.statusArmada.equals("ON MISSION", true) }
    val motorMenungguAcc = motorLogs.count { it.statusArmada.equals("MENUNGGU_ACC_KEMBALI", true) }
    val motorSelesai = motorLogs.count { it.statusArmada.equals("SELESAI", true) }

    val totalDipinjam = effectiveLogs.count { it.statusArmada.equals("DIPINJAM", ignoreCase = true) || it.statusArmada.equals("ON MISSION", ignoreCase = true) }
    val totalMenungguAcc = effectiveLogs.count { it.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) }
    val totalSelesai = effectiveLogs.count { it.statusArmada.equals("SELESAI", ignoreCase = true) }
    val totalSO = effectiveLogs.count { it.team.equals("SO", ignoreCase = true) }
    val totalSM = effectiveLogs.count { it.team.equals("SM", ignoreCase = true) }

    // Weekly Analytics: W2 Sep (Minggu Ini), W1 Sep (Minggu Lalu), W4 Agu, W3 Agu
    val weekThisLogs = remember(effectiveLogs) {
        effectiveLogs.filter {
            val (day, month) = parseDateParts(it.tanggal)
            month == "09" && day in 8..14
        }
    }
    val weekThisMobil = weekThisLogs.count { !it.isMotor }
    val weekThisMotor = weekThisLogs.count { it.isMotor }

    val weekLastLogs = remember(effectiveLogs) {
        effectiveLogs.filter {
            val (day, month) = parseDateParts(it.tanggal)
            month == "09" && day in 1..7
        }
    }
    val weekLastMobil = weekLastLogs.count { !it.isMotor }
    val weekLastMotor = weekLastLogs.count { it.isMotor }

    val week4AugLogs = remember(effectiveLogs) {
        effectiveLogs.filter {
            val (day, month) = parseDateParts(it.tanggal)
            month == "08" && day >= 22
        }
    }
    val week4AugMobil = week4AugLogs.count { !it.isMotor }
    val week4AugMotor = week4AugLogs.count { it.isMotor }

    val week3AugLogs = remember(effectiveLogs) {
        effectiveLogs.filter {
            val (day, month) = parseDateParts(it.tanggal)
            month == "08" && day in 15..21
        }
    }
    val week3AugMobil = week3AugLogs.count { !it.isMotor }
    val week3AugMotor = week3AugLogs.count { it.isMotor }

    // Monthly Analytics: September 2026, Agustus 2026, Juli 2026
    val monthSepLogs = remember(effectiveLogs) {
        effectiveLogs.filter {
            val (_, month) = parseDateParts(it.tanggal)
            month == "09"
        }
    }
    val monthSepMobil = monthSepLogs.count { !it.isMotor }
    val monthSepMotor = monthSepLogs.count { it.isMotor }

    val monthAugLogs = remember(effectiveLogs) {
        effectiveLogs.filter {
            val (_, month) = parseDateParts(it.tanggal)
            month == "08"
        }
    }
    val monthAugMobil = monthAugLogs.count { !it.isMotor }
    val monthAugMotor = monthAugLogs.count { it.isMotor }

    val monthJulLogs = remember(effectiveLogs) {
        effectiveLogs.filter {
            val (_, month) = parseDateParts(it.tanggal)
            month == "07"
        }
    }
    val monthJulMobil = monthJulLogs.count { !it.isMotor }
    val monthJulMotor = monthJulLogs.count { it.isMotor }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 0.dp)
            ) {
                // --- 1. BRAND HEADER BANNER (Ultra-Compact - Half Size) ---
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Logbook Kendaraan",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E3A8A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xFFDBEAFE)
                                        ) {
                                            Text(
                                                text = "${effectiveLogs.size}",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E40AF),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "🏢 Menara Kadin Indonesia",
                                        fontSize = 9.sp,
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Toggle Statistik Button
                                FilledTonalButton(
                                    onClick = { showStatsCards = !showStatsCards },
                                    shape = RoundedCornerShape(5.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (showStatsCards) Color(0xFF2563EB) else Color(0xFFDBEAFE)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 2.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 22.dp, minWidth = 1.dp)
                                ) {
                                    Icon(
                                        if (showStatsCards) Icons.Default.KeyboardArrowUp else Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = if (showStatsCards) Color.White else Color(0xFF1E40AF),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        if (showStatsCards) "Tutup" else "Statistik",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showStatsCards) Color.White else Color(0xFF1E40AF)
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showSimulationGuide = true },
                                    shape = RoundedCornerShape(5.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFF3E8FF)),
                                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 2.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 22.dp, minWidth = 1.dp)
                                ) {
                                    Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Simulasi", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7E22CE))
                                }

                                OutlinedButton(
                                    onClick = onReloadSimulatedData,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                                    shape = RoundedCornerShape(5.dp),
                                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 2.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 22.dp, minWidth = 1.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Draf", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                }
                            }
                        }

                        if (showStatsCards) {
                            Spacer(modifier = Modifier.height(6.dp))

                            // --- KOTAK-KOTAK STATISTIK UTAMA (Sleek light-themed cards matching Admin) ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // KOTAK 1: ARMADA MOBIL
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF0F9FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { selectedCategoryFilter = "MOBIL" }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("ARMADA MOBIL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                            }
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE0F2FE)) {
                                                Text("$totalMobil Unit", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF0284C7), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "$totalMobil Pemakaian",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0C4A6E)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🚗 Grandmax, Avanza, Luxio • SO & SM",
                                            fontSize = 9.sp,
                                            color = Color(0xFF0284C7)
                                        )
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp, color = Color(0xFFBAE6FD))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Dipinjam: $mobilDipinjam", fontSize = 9.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                            Text("ACC: $mobilMenungguAcc", fontSize = 9.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                            Text("Kembali: $mobilSelesai", fontSize = 9.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // KOTAK 2: SEPEDA MOTOR
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFAF5FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9D5FF)),
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { selectedCategoryFilter = "MOTOR" }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("SEPEDA MOTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8))
                                            }
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF3E8FF)) {
                                                Text("$totalMotor Unit", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF7E22CE), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "$totalMotor Pemakaian",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF581C87)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🛵 LEXY, NMAX, Vario, Beat • SO & SM",
                                            fontSize = 9.sp,
                                            color = Color(0xFF7E22CE)
                                        )
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp, color = Color(0xFFE9D5FF))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Dipinjam: $motorDipinjam", fontSize = 9.sp, color = Color(0xFF7E22CE), fontWeight = FontWeight.Bold)
                                            Text("ACC: $motorMenungguAcc", fontSize = 9.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                            Text("Kembali: $motorSelesai", fontSize = 9.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // KOTAK 3: TOTAL TIAP MINGGU
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier
                                        .width(250.dp)
                                        .clickable { selectedCategoryFilter = "THIS_WEEK" }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("TOTAL TIAP MINGGU", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                            }
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDCFCE7)) {
                                                Text("W2 Sep (Active)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Minggu Ini: ${weekThisLogs.size} Trip",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF14532D)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("🚗 Mobil: $weekThisMobil • 🛵 Motor: $weekThisMotor", fontSize = 10.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp), thickness = 0.5.dp, color = Color(0xFFBBF7D0))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("W1 Sep: ${weekLastLogs.size} (🚗$weekLastMobil 🛵$weekLastMotor)", fontSize = 8.5.sp, color = Color(0xFF475569))
                                            Text("W4 Agu: ${week4AugLogs.size} (🚗$week4AugMobil 🛵$week4AugMotor)", fontSize = 8.5.sp, color = Color(0xFF475569))
                                        }
                                    }
                                }

                                // KOTAK 4: TOTAL TIAP BULAN
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEFCE8),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEF08A)),
                                    modifier = Modifier
                                        .width(250.dp)
                                        .clickable { selectedCategoryFilter = "THIS_MONTH" }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("TOTAL TIAP BULAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                            }
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEF9C3)) {
                                                Text("Rekap 2026", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "September: ${monthSepLogs.size} Trip",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF713F12)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("🚗 Mobil: $monthSepMobil • 🛵 Motor: $monthSepMotor", fontSize = 10.sp, color = Color(0xFFB45309), fontWeight = FontWeight.SemiBold)
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp), thickness = 0.5.dp, color = Color(0xFFFEF08A))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Agustus: ${monthAugLogs.size} (🚗$monthAugMobil 🛵$monthAugMotor)", fontSize = 8.5.sp, color = Color(0xFF475569))
                                            Text("Juli: ${monthJulLogs.size} (🚗$monthJulMobil 🛵$monthJulMotor)", fontSize = 8.5.sp, color = Color(0xFF475569))
                                        }
                                    }
                                }

                                // KOTAK 5: STATUS OPERASIONAL & ACC IC
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.width(220.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("STATUS OPERASIONAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🚘 Dipinjam:", fontSize = 9.5.sp, color = Color(0xFF475569))
                                            Text("$totalDipinjam Unit", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("⏳ Menunggu ACC:", fontSize = 9.5.sp, color = Color(0xFF475569))
                                            Text("$totalMenungguAcc Form", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("✅ Selesai (Di-ACC):", fontSize = 9.5.sp, color = Color(0xFF475569))
                                            Text("$totalSelesai Unit", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("👥 Team SO / SM:", fontSize = 9.5.sp, color = Color(0xFF475569))
                                            Text("SO: $totalSO • SM: $totalSM", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // --- 2. VIEW MODE SWITCHER (Matching Admin Segmented Buttons - Compact) ---
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    VehicleViewMode.entries.forEachIndexed { index, mode ->
                        val isSelected = viewMode == mode
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = VehicleViewMode.entries.size),
                            onClick = { viewMode = mode },
                            selected = isSelected,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Color(0xFF2563EB),
                                activeContentColor = Color.White,
                                inactiveContainerColor = Color.White,
                                inactiveContentColor = Color(0xFF0F172A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    when (mode) {
                                        VehicleViewMode.SPREADSHEET_TABLE -> Icons.Default.TableChart
                                        VehicleViewMode.CARD_LIST -> Icons.Default.ViewList
                                        VehicleViewMode.MAP_SPLIT_VIEW -> Icons.Default.LocationOn
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) Color.White else Color(0xFF2563EB)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (mode) {
                                        VehicleViewMode.SPREADSHEET_TABLE -> "Tabel Matriks"
                                        VehicleViewMode.CARD_LIST -> "Daftar Berkas"
                                        VehicleViewMode.MAP_SPLIT_VIEW -> "Peta GPS"
                                    },
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // --- 3. SEARCH BAR (Compact Slim Style) ---
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari plat, PIC, tiket, tipe kendaraan, tujuan...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // --- 4. FILTER CHIPS (Single Scrollable Row to Elevate Content) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAllStatus = selectedStatusFilter == "ALL"
                    FilterChip(
                        selected = isAllStatus,
                        onClick = { selectedStatusFilter = "ALL" },
                        label = {
                            Text(
                                text = "Semua Status",
                                fontSize = 10.sp,
                                fontWeight = if (isAllStatus) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllStatus) Color.White else Color(0xFF1E293B)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = Color(0xFF1E293B),
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isAllStatus,
                            borderColor = if (isAllStatus) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                    listOf(
                        "DIPINJAM" to "Dipinjam",
                        "MENUNGGU_ACC" to "Menunggu ACC",
                        "SELESAI" to "Selesai"
                    ).forEach { (key, label) ->
                        val isChipSelected = selectedStatusFilter == key
                        FilterChip(
                            selected = isChipSelected,
                            onClick = { selectedStatusFilter = if (isChipSelected) "ALL" else key },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isChipSelected) Color.White else Color(0xFF1E293B)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Color(0xFF1E293B),
                                selectedContainerColor = when (key) {
                                    "DIPINJAM" -> Color(0xFF2563EB)
                                    "MENUNGGU_ACC" -> Color(0xFFD97706)
                                    else -> Color(0xFF16A34A)
                                },
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isChipSelected,
                                borderColor = if (isChipSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    // Small vertical divider between status and category
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color(0xFFCBD5E1))
                    )

                    val isAllCategory = selectedCategoryFilter == "ALL"
                    FilterChip(
                        selected = isAllCategory,
                        onClick = { selectedCategoryFilter = "ALL" },
                        label = {
                            Text(
                                text = "Semua Armada",
                                fontSize = 10.sp,
                                fontWeight = if (isAllCategory) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllCategory) Color.White else Color(0xFF1E293B)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = Color(0xFF1E293B),
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isAllCategory,
                            borderColor = if (isAllCategory) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                    listOf(
                        "MOBIL" to "🚗 Mobil",
                        "MOTOR" to "🛵 Motor",
                        "SO" to "Team SO",
                        "SM" to "Team SM",
                        "THIS_WEEK" to "📅 Minggu Ini",
                        "THIS_MONTH" to "📊 Bulan Ini"
                    ).forEach { (key, label) ->
                        val isCatSelected = selectedCategoryFilter == key
                        FilterChip(
                            selected = isCatSelected,
                            onClick = { selectedCategoryFilter = if (isCatSelected) "ALL" else key },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCatSelected) Color.White else Color(0xFF1E293B)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Color(0xFF1E293B),
                                selectedContainerColor = when (key) {
                                    "MOBIL" -> Color(0xFF0284C7)
                                    "MOTOR" -> Color(0xFF7E22CE)
                                    "SO", "SM" -> Color(0xFF1E293B)
                                    "THIS_WEEK" -> Color(0xFF059669)
                                    else -> Color(0xFFD97706)
                                },
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isCatSelected,
                                borderColor = if (isCatSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- 5. MAIN CONTENT: SPREADSHEET TABLE, CARD LIST, OR MAP SPLIT VIEW ---
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (viewMode) {
                        VehicleViewMode.SPREADSHEET_TABLE -> {
                            VehicleSpreadsheetView(
                                logs = filteredVehicleLogs,
                                onSelect = { selectedVehicleLog = it },
                                onOpenLiveMap = { liveTrackingVehicle = it },
                                onReturn = { vehicleToReturn = it },
                                onAcc = { vehicleToAcc = it },
                                onEdit = { vehicleToEdit = it },
                                onDelete = { vehicleToDelete = it },
                                onAddClick = { showLoanDialog = true },
                                isAdminOrIC = isAdminOrIC
                            )
                        }

                        VehicleViewMode.CARD_LIST -> {
                            VehicleCardListView(
                                logs = filteredVehicleLogs,
                                onSelect = { selectedVehicleLog = it },
                                onOpenLiveMap = { liveTrackingVehicle = it },
                                onReturn = { vehicleToReturn = it },
                                onAcc = { vehicleToAcc = it },
                                onEdit = { vehicleToEdit = it },
                                onDelete = { vehicleToDelete = it },
                                onAddClick = { showLoanDialog = true },
                                isAdminOrIC = isAdminOrIC
                            )
                        }

                        VehicleViewMode.MAP_SPLIT_VIEW -> {
                            VehicleMapSplitView(
                                logs = filteredVehicleLogs,
                                selectedLog = selectedVehicleLog,
                                onSelectLog = { selectedVehicleLog = it },
                                onOpenLiveMap = { liveTrackingVehicle = it },
                                onReturn = { vehicleToReturn = it },
                                onAcc = { vehicleToAcc = it },
                                mapType = mapType,
                                onChangeMapType = { mapType = it },
                                zoomLevel = zoomLevel,
                                onZoomIn = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(3.0f) },
                                onZoomOut = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.6f) },
                                userLatitude = userLatitude,
                                userLongitude = userLongitude,
                                userAddress = userAddress
                            )
                        }
                    }
                }
            }
        }
    }

    // --- MODAL DETAIL PEMINJAMAN & ACC ---
    selectedVehicleLog?.let { log ->
        VehicleDetailDialog(
            log = log,
            onDismiss = { selectedVehicleLog = null },
            onOpenLiveMap = {
                liveTrackingVehicle = log
                selectedVehicleLog = null
            },
            onReturn = {
                vehicleToReturn = log
                selectedVehicleLog = null
            },
            onAcc = {
                vehicleToAcc = log
                selectedVehicleLog = null
            },
            onOpenGoogleMaps = {
                val gmmIntentUri = Uri.parse("geo:${log.latitude},${log.longitude}?q=${log.latitude},${log.longitude}(${Uri.encode(log.platKendaraan + " - " + log.pic)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                try {
                    context.startActivity(mapIntent)
                } catch (e: Exception) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${log.latitude},${log.longitude}"))
                    context.startActivity(browserIntent)
                }
            },
            onEdit = {
                vehicleToEdit = log
                selectedVehicleLog = null
            },
            onDelete = {
                vehicleToDelete = log
                selectedVehicleLog = null
            },
            isAdminOrIC = isAdminOrIC
        )
    }

    // --- MODAL: LIVE TRACKING & GPS RADAR SIMULATION ---
    liveTrackingVehicle?.let { target ->
        VehicleLiveTrackingMapDialog(
            log = target,
            onDismiss = { liveTrackingVehicle = null },
            onReturn = {
                vehicleToReturn = target
                liveTrackingVehicle = null
            },
            onAcc = {
                vehicleToAcc = target
                liveTrackingVehicle = null
            },
            onOpenExternalMap = {
                val gmmIntentUri = Uri.parse("geo:${target.latitude},${target.longitude}?q=${target.latitude},${target.longitude}(${Uri.encode(target.platKendaraan + " - " + target.pic)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                try {
                    context.startActivity(mapIntent)
                } catch (e: Exception) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${target.latitude},${target.longitude}"))
                    context.startActivity(browserIntent)
                }
            }
        )
    }

    // --- MODAL: WORKFLOW SIMULATOR (Input -> Cek Peta -> Perjalanan -> Pengembalian -> ACC IC) ---
    if (showSimulationGuide) {
        VehicleSimulationWorkflowDialog(
            onDismiss = { showSimulationGuide = false },
            onStartInputFlow = {
                showSimulationGuide = false
                showLoanDialog = true
            },
            onStartLiveMapCheck = {
                showSimulationGuide = false
                liveTrackingVehicle = filteredVehicleLogs.firstOrNull() ?: INITIAL_VEHICLE_LOGS.first()
            },
            onQuickFullSimulation = {
                val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val todayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                onAddVehicleLog(
                    todayDate,
                    "Fadli & Kurniawan (Simulasi)",
                    "SO",
                    nowTime,
                    "E/SIMULASI/0911",
                    "B 9999 FOS",
                    "Grandmax",
                    "Cyber 1 (Mampang)",
                    "34,120 KM",
                    "Full (90%)",
                    "Simulasi perbaikan FO & pengecekan rute mobil",
                    -6.2383,
                    106.8210
                )
                showSimulationGuide = false
                viewMode = VehicleViewMode.MAP_SPLIT_VIEW
            }
        )
    }

    // --- MODAL 1: TAMBAH PEMINJAMAN KENDARAAN ---
    if (showLoanDialog) {
        AddVehicleLoanDialog(
            initialLat = userLatitude,
            initialLng = userLongitude,
            onDismiss = { showLoanDialog = false },
            onSubmit = { tanggal, pic, team, timeOut, ticket, plat, type, tujuan, odoOut, fuel, catatan, lat, lng ->
                onAddVehicleLog(tanggal, pic, team, timeOut, ticket, plat, type, tujuan, odoOut, fuel, catatan, lat, lng)
                showLoanDialog = false
            }
        )
    }

    // --- MODAL 2: FORM PENGEMBALIAN KENDARAAN OLEH PIC ---
    vehicleToReturn?.let { target ->
        ReturnVehicleDialog(
            log = target,
            onDismiss = { vehicleToReturn = null },
            onSubmit = { tglKembali, timeIn, odoIn, fuelIn, catatanKembali ->
                onSubmitReturn(target, tglKembali, timeIn, odoIn, fuelIn, catatanKembali)
                vehicleToReturn = null
            }
        )
    }

    // --- MODAL 3: PERSETUJUAN & ACC OLEH IC ---
    vehicleToAcc?.let { target ->
        AccReturnDialog(
            log = target,
            activeUser = activeUser,
            onDismiss = { vehicleToAcc = null },
            onSubmitAcc = { accBy, accRole, accNotes ->
                onApproveReturn(target, accBy, accRole, accNotes)
                vehicleToAcc = null
            }
        )
    }

    // --- MODAL 4: EDIT LOG KENDARAAN ---
    vehicleToEdit?.let { target ->
        EditVehicleDialog(
            log = target,
            onDismiss = { vehicleToEdit = null },
            onSubmit = { updated ->
                onUpdateVehicleLog(updated)
                vehicleToEdit = null
            }
        )
    }

    // --- MODAL 5: KONFIRMASI HAPUS LOG KENDARAAN ---
    vehicleToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
            title = { Text("Hapus Log Kendaraan", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus data peminjaman armada '${target.platKendaraan}' (${target.ticket} - ${target.pic})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVehicleLog(target)
                        vehicleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { vehicleToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Quick Statistic Badge
 */
@Composable
private fun VehicleStatBadge(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, fontSize = 9.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

/**
 * ============================================================================
 * VIEW 1: VEHICLE SPREADSHEET VIEW (Format Matriks Tabel Excel Seperti Admin)
 * Tanggal | PIC | Team | Time out | Time in | Ticket | Plat | Type | Ket. Tujuan | Odometer | BBM | Status | ACC IC | Aksi
 * ============================================================================
 */
@Composable
fun VehicleSpreadsheetView(
    logs: List<VehicleLogEntity>,
    onSelect: (VehicleLogEntity) -> Unit,
    onOpenLiveMap: (VehicleLogEntity) -> Unit,
    onReturn: (VehicleLogEntity) -> Unit,
    onAcc: (VehicleLogEntity) -> Unit,
    onEdit: (VehicleLogEntity) -> Unit,
    onDelete: (VehicleLogEntity) -> Unit,
    onAddClick: () -> Unit,
    isAdminOrIC: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Table Top Action & Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TABEL MATRIKS (${logs.size} Record)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = Color.White
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Geser kanan ➔",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 26.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Tambah", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tidak ada log kendaraan yang sesuai filter / pencarian.", color = Color(0xFF64748B), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Input Data Kendaraan Pertama", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Horizontal scrollable container for the spreadsheet
                val horizontalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Column(modifier = Modifier.width(1380.dp)) {
                        // Spreadsheet Column Headers (Styled like Excel with clean borders)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0))
                                .border(1.dp, Color(0xFFCBD5E1))
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VehicleHeaderCell("No", 45.dp, TextAlign.Center)
                            VehicleHeaderCell("Tanggal", 95.dp, TextAlign.Center)
                            VehicleHeaderCell("PIC", 130.dp, TextAlign.Start)
                            VehicleHeaderCell("Team", 65.dp, TextAlign.Center)
                            VehicleHeaderCell("Time out", 80.dp, TextAlign.Center)
                            VehicleHeaderCell("Ticket", 140.dp, TextAlign.Center)
                            VehicleHeaderCell("Plat kendaraan", 115.dp, TextAlign.Center)
                            VehicleHeaderCell("Type Kendaraan", 115.dp, TextAlign.Center)
                            VehicleHeaderCell("Ket. Tujuan", 170.dp, TextAlign.Start)
                            VehicleHeaderCell("Time In", 80.dp, TextAlign.Center)
                            VehicleHeaderCell("Odometer & BBM", 135.dp, TextAlign.Start)
                            VehicleHeaderCell("Status & ACC IC", 185.dp, TextAlign.Center)
                            VehicleHeaderCell("Aksi & GPS", 145.dp, TextAlign.Center)
                        }

                        // Spreadsheet Data Rows
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            itemsIndexed(logs, key = { _, item -> item.id }) { index, log ->
                                VehicleSpreadsheetRow(
                                    index = index + 1,
                                    log = log,
                                    onClick = { onSelect(log) },
                                    onOpenLiveMap = { onOpenLiveMap(log) },
                                    onReturn = { onReturn(log) },
                                    onAcc = { onAcc(log) },
                                    onEdit = { onEdit(log) },
                                    onDelete = { onDelete(log) },
                                    isAdminOrIC = isAdminOrIC
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleHeaderCell(title: String, width: Dp, align: TextAlign = TextAlign.Start) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = when (align) {
            TextAlign.Center -> Alignment.Center
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF0F172A),
            textAlign = align
        )
    }
}

/**
 * Single Row inside Vehicle Spreadsheet Matrix
 */
@Composable
private fun VehicleSpreadsheetRow(
    index: Int,
    log: VehicleLogEntity,
    onClick: () -> Unit,
    onOpenLiveMap: () -> Unit,
    onReturn: () -> Unit,
    onAcc: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isAdminOrIC: Boolean
) {
    val isEven = index % 2 == 0
    val rowBg = if (isEven) Color(0xFFF8FAFC) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .border(0.5.dp, Color(0xFFE2E8F0))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. No
        Text(
            text = index.toString(),
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(45.dp)
        )

        // 2. Tanggal
        Text(
            text = log.tanggal,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(95.dp)
        )

        // 3. PIC
        Column(
            modifier = Modifier
                .width(130.dp)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = log.pic,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 4. Team (SO / SM)
        Box(modifier = Modifier.width(65.dp), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (log.team.equals("SO", ignoreCase = true)) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
            ) {
                Text(
                    text = log.team,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (log.team.equals("SO", ignoreCase = true)) Color(0xFF166534) else Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 5. Time Out
        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFEFF6FF)
            ) {
                Text(
                    text = log.timeOut,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // 6. Ticket
        Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = when {
                    log.ticket.startsWith("E/") -> Color(0xFFFEE2E2)
                    log.ticket.equals("Storing", ignoreCase = true) -> Color(0xFFFEF08A)
                    else -> Color(0xFFE0F2FE)
                }
            ) {
                Text(
                    text = log.ticket,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        log.ticket.startsWith("E/") -> Color(0xFF991B1B)
                        log.ticket.equals("Storing", ignoreCase = true) -> Color(0xFF854D0E)
                        else -> Color(0xFF075985)
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 7. Plat Kendaraan
        Box(modifier = Modifier.width(115.dp), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0F172A)
            ) {
                Text(
                    text = log.platKendaraan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFCD34D),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // 8. Type Kendaraan (Mobil / Motor)
        Box(modifier = Modifier.width(115.dp), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (log.isMotor) Color(0xFFF3E8FF) else Color(0xFFEFF6FF),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (log.isMotor) Color(0xFFA855F7) else Color(0xFF38BDF8)
                )
            ) {
                Text(
                    text = "${if (log.isMotor) "🛵 " else "🚗 "}${log.typeKendaraan}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (log.isMotor) Color(0xFF7E22CE) else Color(0xFF1D4ED8),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 9. Ket. Tujuan
        Column(
            modifier = Modifier
                .width(170.dp)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = log.ketTujuan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 10. Time In
        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (log.timeIn != "-") Color(0xFFF0FDF4) else Color(0xFFF1F5F9)
            ) {
                Text(
                    text = log.timeIn,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (log.timeIn != "-") Color(0xFF15803D) else Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // 11. Odometer & BBM
        Column(
            modifier = Modifier
                .width(135.dp)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = "Odo: ${log.odometerOut}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Text(
                text = "BBM: ${log.fuelLevel}",
                fontSize = 9.sp,
                color = Color(0xFF64748B)
            )
        }

        // 12. Status & ACC IC
        Box(modifier = Modifier.width(185.dp), contentAlignment = Alignment.Center) {
            when {
                log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> {
                    Button(
                        onClick = onAcc,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth(0.92f).height(30.dp)
                    ) {
                        Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ACC IC (Verifikasi)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                log.statusArmada.equals("SELESAI", ignoreCase = true) -> {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDCFCE7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("DI-ACC SELESAI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                if (log.accBy.isNotBlank() && log.accBy != "-") {
                                    Text("Oleh: ${log.accBy}", fontSize = 8.sp, color = Color(0xFF15803D), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onReturn,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth(0.92f).height(30.dp)
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kembalikan Mobil", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 13. Aksi & GPS (Peta GPS, Edit & Delete)
        Row(
            modifier = Modifier.width(145.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onOpenLiveMap,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE0F2FE)),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Google Maps", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(15.dp))
            }
            if (isAdminOrIC) {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

/**
 * ============================================================================
 * VIEW 2: VEHICLE CARD LIST VIEW (Format Kartu Admin Seperti AdminScreen)
 * ============================================================================
 */
@Composable
private fun VehicleCardListView(
    logs: List<VehicleLogEntity>,
    onSelect: (VehicleLogEntity) -> Unit,
    onOpenLiveMap: (VehicleLogEntity) -> Unit,
    onReturn: (VehicleLogEntity) -> Unit,
    onAcc: (VehicleLogEntity) -> Unit,
    onEdit: (VehicleLogEntity) -> Unit,
    onDelete: (VehicleLogEntity) -> Unit,
    onAddClick: () -> Unit,
    isAdminOrIC: Boolean
) {
    if (logs.isEmpty()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tidak ada log kendaraan yang sesuai filter.", color = Color(0xFF64748B), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Input Data Kendaraan Baru", fontSize = 12.sp)
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Action header with green Tambah button matching spreadsheet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BERKAS LOGBOOK (${logs.size} Record)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 26.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Tambah", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    VehicleAdminCard(
                        log = log,
                        onClick = { onSelect(log) },
                        onOpenLiveMap = { onOpenLiveMap(log) },
                        onReturn = { onReturn(log) },
                        onAcc = { onAcc(log) },
                        onEdit = { onEdit(log) },
                        onDelete = { onDelete(log) },
                        isAdminOrIC = isAdminOrIC
                    )
                }
            }
        }
    }
}

/**
 * Admin Card Item for Vehicle Data (Matching AdminScreen card aesthetic)
 */
@Composable
private fun VehicleAdminCard(
    log: VehicleLogEntity,
    onClick: () -> Unit,
    onOpenLiveMap: () -> Unit,
    onReturn: () -> Unit,
    onAcc: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isAdminOrIC: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Plat + Type Badge, Team & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = log.platKendaraan,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color(0xFFFCD34D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (log.isMotor) Color(0xFFF3E8FF) else Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (log.isMotor) Color(0xFFA855F7) else Color(0xFF38BDF8)
                        )
                    ) {
                        Text(
                            text = "${if (log.isMotor) "🛵 " else "🚗 "}${log.typeKendaraan}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (log.isMotor) Color(0xFF7E22CE) else Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (log.team.equals("SO", ignoreCase = true)) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "Team ${log.team}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (log.team.equals("SO", ignoreCase = true)) Color(0xFF166534) else Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // Status Badge
                when {
                    log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Text(
                                text = "MENUNGGU ACC IC",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    log.statusArmada.equals("SELESAI", ignoreCase = true) -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                text = "DI-ACC SELESAI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDBEAFE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD))
                        ) {
                            Text(
                                text = "SEDANG DIPINJAM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Ticket & Tujuan + PIC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when {
                                log.ticket.startsWith("E/") -> Color(0xFFFEE2E2)
                                log.ticket.equals("Storing", ignoreCase = true) -> Color(0xFFFEF08A)
                                else -> Color(0xFFE0F2FE)
                            }
                        ) {
                            Text(
                                text = "Tiket: ${log.ticket}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    log.ticket.startsWith("E/") -> Color(0xFF991B1B)
                                    log.ticket.equals("Storing", ignoreCase = true) -> Color(0xFF854D0E)
                                    else -> Color(0xFF075985)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tgl: ${log.tanggal}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tujuan: ${log.ketTujuan}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "PIC Driver: ${log.pic}",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Time Metrics
                Column(
                    modifier = Modifier.weight(0.8f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Time Out: ${log.timeOut}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                    Text(
                        text = "Time In: ${log.timeIn}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.timeIn != "-") Color(0xFF16A34A) else Color(0xFF94A3B8)
                    )
                    if (log.accBy.isNotBlank() && log.accBy != "-") {
                        Text(
                            text = "ACC: ${log.accBy}",
                            fontSize = 10.sp,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Telemetry: Odometer, BBM, and Coordinates
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Odo: ${log.odometerOut}", fontSize = 10.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                    Text("BBM: ${log.fuelLevel}", fontSize = 10.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                    Text("GPS: ${String.format(Locale.US, "%.4f", log.latitude)}, ${String.format(Locale.US, "%.4f", log.longitude)}", fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Map Google Maps Button
                FilledTonalButton(
                    onClick = onOpenLiveMap,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE0F2FE)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Peta Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                }

                // Right: Return / ACC and Edit / Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> {
                            Button(
                                onClick = onAcc,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ACC IC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        log.statusArmada.equals("SELESAI", ignoreCase = true) -> {
                            OutlinedButton(
                                onClick = onClick,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Lihat Berkas", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Button(
                                onClick = onReturn,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kembalikan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                    }

                    if (isAdminOrIC) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * ============================================================================
 * VIEW 2: MAP SPLIT VIEW (Peta Lokasi GPS + Kartu Kendaraan)
 * ============================================================================
 */
@Composable
private fun VehicleMapSplitView(
    logs: List<VehicleLogEntity>,
    selectedLog: VehicleLogEntity?,
    onSelectLog: (VehicleLogEntity) -> Unit,
    onOpenLiveMap: (VehicleLogEntity) -> Unit,
    onReturn: (VehicleLogEntity) -> Unit,
    onAcc: (VehicleLogEntity) -> Unit,
    mapType: MapType,
    onChangeMapType: (MapType) -> Unit,
    zoomLevel: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    userLatitude: Double,
    userLongitude: Double,
    userAddress: String
) {
    var useRealGoogleMaps by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val mapPane: @Composable (Modifier) -> Unit = { paneModifier ->
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = paneModifier,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (useRealGoogleMaps) {
                    RealGoogleMapsEmbedView(
                        vehicles = logs,
                        focusedVehicle = selectedLog,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Canvas Vector Map with Vehicle Pins
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val bgColor = when (mapType) {
                            MapType.ROADMAP -> Color(0xFFF8FAFC)
                            MapType.SATELLITE -> Color(0xFF0F172A)
                            MapType.HYBRID -> Color(0xFF1E293B)
                        }
                        drawRect(color = bgColor)

                        val gridColor = when (mapType) {
                            MapType.ROADMAP -> Color(0xFFE2E8F0)
                            MapType.SATELLITE -> Color(0xFF334155).copy(alpha = 0.4f)
                            MapType.HYBRID -> Color(0xFF475569).copy(alpha = 0.5f)
                        }

                        val step = 40.dp.toPx() * zoomLevel
                        var x = 0f
                        while (x < w) {
                            drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, h), strokeWidth = 1f)
                            x += step
                        }
                        var y = 0f
                        while (y < h) {
                            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
                            y += step
                        }

                        val roadColor = when (mapType) {
                            MapType.ROADMAP -> Color(0xFFCBD5E1)
                            MapType.SATELLITE -> Color(0xFF64748B).copy(alpha = 0.6f)
                            MapType.HYBRID -> Color(0xFF94A3B8).copy(alpha = 0.7f)
                        }
                        val pathA = Path().apply {
                            moveTo(0f, h * 0.4f)
                            lineTo(w * 0.3f, h * 0.45f)
                            lineTo(w * 0.7f, h * 0.35f)
                            lineTo(w, h * 0.6f)
                        }
                        drawPath(pathA, color = roadColor, style = Stroke(width = 6f * zoomLevel))

                        val pathB = Path().apply {
                            moveTo(w * 0.45f, 0f)
                            lineTo(w * 0.5f, h * 0.5f)
                            lineTo(w * 0.55f, h)
                        }
                        drawPath(pathB, color = roadColor, style = Stroke(width = 5f * zoomLevel))

                        logs.forEachIndexed { idx, log ->
                            val offsetX = (w * 0.18f + ((idx * 110) % (w * 0.65f).toInt()))
                            val offsetY = (h * 0.18f + ((idx * 85) % (h * 0.65f).toInt()))

                            val isSelected = selectedLog?.id == log.id

                            if (isSelected) {
                                drawCircle(
                                    color = Color(0xFF2563EB).copy(alpha = 0.3f),
                                    radius = 22.dp.toPx(),
                                    center = Offset(offsetX, offsetY)
                                )
                            }

                            val pinColor = when {
                                log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> Color(0xFFD97706)
                                log.statusArmada.equals("SELESAI", ignoreCase = true) -> Color(0xFF16A34A)
                                log.typeKendaraan.contains("Grandmax", ignoreCase = true) -> Color(0xFFEA580C)
                                log.typeKendaraan.contains("LEXY", ignoreCase = true) -> Color(0xFF9333EA)
                                else -> Color(0xFF2563EB)
                            }

                            drawCircle(
                                color = Color.White,
                                radius = 12.dp.toPx(),
                                center = Offset(offsetX, offsetY)
                            )
                            drawCircle(
                                color = pinColor,
                                radius = 9.dp.toPx(),
                                center = Offset(offsetX, offsetY)
                            )
                        }
                    }
                }

                // Map Top Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Layer Toggle: Real Google Maps vs Radar
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.92f),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (useRealGoogleMaps) Color(0xFF0284C7) else Color.Transparent,
                                modifier = Modifier.clickable { useRealGoogleMaps = true }
                            ) {
                                Text(
                                    text = "🌐 Google Maps Real",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (!useRealGoogleMaps) Color(0xFF334155) else Color.Transparent,
                                modifier = Modifier.clickable { useRealGoogleMaps = false }
                            ) {
                                Text(
                                    text = "📡 Vektor",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // External Google Maps App Launch Button
                    selectedLog?.let { target ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF16A34A).copy(alpha = 0.95f),
                            shadowElevation = 3.dp,
                            modifier = Modifier.clickable {
                                val gmmIntentUri = Uri.parse("geo:${target.latitude},${target.longitude}?q=${target.latitude},${target.longitude}(${Uri.encode(target.platKendaraan + " - " + target.pic)})")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${target.latitude},${target.longitude}"))
                                    context.startActivity(browserIntent)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buka Google Maps App", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    val listPane: @Composable (Modifier) -> Unit = { paneModifier ->
        // Right: Vehicle Cards List
        Column(
            modifier = paneModifier
        ) {
            Text(
                text = "Daftar Armada (${logs.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    val isSelected = selectedLog?.id == log.id
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLog(log) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = log.platKendaraan,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFCD34D),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when {
                                        log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> Color(0xFFFEF3C7)
                                        log.statusArmada.equals("SELESAI", ignoreCase = true) -> Color(0xFFDCFCE7)
                                        else -> Color(0xFFE0F2FE)
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> "MENUNGGU ACC"
                                            log.statusArmada.equals("SELESAI", ignoreCase = true) -> "SELESAI (ACC)"
                                            else -> "DIPINJAM"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> Color(0xFF92400E)
                                            log.statusArmada.equals("SELESAI", ignoreCase = true) -> Color(0xFF166534)
                                            else -> Color(0xFF0369A1)
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${log.typeKendaraan} - PIC: ${log.pic} (Team ${log.team})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Tiket: ${log.ticket} • Out: ${log.timeOut}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2563EB)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Tujuan: ${log.ketTujuan} (${log.tanggal})",
                                fontSize = 10.sp,
                                color = Color(0xFF334155),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons per card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { onOpenLiveMap(log) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(28.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Radar GPS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                if (log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true)) {
                                    Button(
                                        onClick = { onAcc(log) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(28.dp)
                                    ) {
                                        Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ACC IC", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (!log.statusArmada.equals("SELESAI", ignoreCase = true)) {
                                    Button(
                                        onClick = { onReturn(log) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(28.dp)
                                    ) {
                                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kembali", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 640.dp
        if (isCompact) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                mapPane(Modifier.fillMaxWidth().height(230.dp))
                listPane(Modifier.fillMaxWidth().weight(1f))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                mapPane(Modifier.weight(1.3f).fillMaxHeight())
                listPane(Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

/**
 * ============================================================================
 * MODAL DETAIL LENGKAP & BUKTI ACC KENDARAAN
 * ============================================================================
 */
@Composable
private fun VehicleDetailDialog(
    log: VehicleLogEntity,
    onDismiss: () -> Unit,
    onOpenLiveMap: () -> Unit,
    onReturn: () -> Unit,
    onAcc: () -> Unit,
    onOpenGoogleMaps: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isAdminOrIC: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF2563EB))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Detail Logbook & ACC Kendaraan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Plat Kendaraan & Status Header
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("NOMOR POLISI / PLAT", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text(log.platKendaraan, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFFCD34D))
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when {
                                    log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> Color(0xFFD97706)
                                    log.statusArmada.equals("SELESAI", ignoreCase = true) -> Color(0xFF16A34A)
                                    else -> Color(0xFF2563EB)
                                }
                            ) {
                                Text(
                                    text = log.statusArmada,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // 8 Format Kolom Standar
                item { DetailFieldItem("1. Tanggal Peminjaman", log.tanggal, Icons.Default.CalendarToday) }
                item { DetailFieldItem("2. PIC Lapangan", log.pic, Icons.Default.Person) }
                item { DetailFieldItem("3. Team", "Team ${log.team}", Icons.Default.Group) }
                item { DetailFieldItem("4. Time out (Waktu Pinjam)", log.timeOut, Icons.Default.Schedule) }
                item { DetailFieldItem("5. Ticket", log.ticket, Icons.Default.ConfirmationNumber) }
                item { DetailFieldItem("6. Plat kendaraan", log.platKendaraan, Icons.Default.DirectionsCar) }
                item { DetailFieldItem("7. Type Kendaraan", log.typeKendaraan, Icons.Default.AirportShuttle) }
                item { DetailFieldItem("8. Ket. Tujuan", log.ketTujuan, Icons.Default.Place) }

                // Info Tambahan Peminjaman
                item {
                    DetailFieldItem("KM Berangkat & BBM", "${log.odometerOut} • BBM Awal: ${log.fuelLevel}", Icons.Default.Speed)
                }

                if (log.catatanPinjam.isNotBlank() && log.catatanPinjam != "-") {
                    item { DetailFieldItem("Catatan Peminjaman", log.catatanPinjam, Icons.Default.Notes) }
                }

                // Info Pengembalian jika ada
                if (log.timeIn != "-" || log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) || log.statusArmada.equals("SELESAI", ignoreCase = true)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("DATA PENGEMBALIAN KENDARAAN", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Waktu Kembali: ${log.tanggalKembali} ${log.timeIn}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("KM Kembali: ${log.odometerIn} • BBM: ${log.fuelLevelIn}", fontSize = 11.sp)
                                if (log.catatanKembali.isNotBlank() && log.catatanKembali != "-") {
                                    Text("Catatan Fisik/Kondisi: ${log.catatanKembali}", fontSize = 10.sp, color = Color(0xFF475569))
                                }
                            }
                        }
                    }
                }

                // Info Bukti ACC oleh IC jika sudah di-ACC
                if (log.statusArmada.equals("SELESAI", ignoreCase = true)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("DISETUJUI & DI-ACC OLEH IC", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF166534))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Pejabat IC: ${log.accBy} (${log.accRole})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                                Text("Waktu ACC: ${log.accTime}", fontSize = 10.sp, color = Color(0xFF166534))
                                if (log.accNotes.isNotBlank() && log.accNotes != "-") {
                                    Text("Catatan Verifikasi IC: ${log.accNotes}", fontSize = 10.sp, color = Color(0xFF166534))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true)) {
                    Button(
                        onClick = onAcc,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ACC IC")
                    }
                } else if (!log.statusArmada.equals("SELESAI", ignoreCase = true)) {
                    Button(
                        onClick = onReturn,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pengembalian")
                    }
                }

                Button(
                    onClick = onOpenLiveMap,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Google Maps")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Text("Edit")
                }
                if (isAdminOrIC) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Hapus")
                    }
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Tutup")
                }
            }
        }
    )
}

@Composable
private fun DetailFieldItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * ============================================================================
 * MODAL 1: FORM PEMINJAMAN KENDARAAN (Add Vehicle Loan Dialog)
 * Sesuai Desain & Struktur Screenshot Gambar 1 & Gambar 2
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVehicleLoanDialog(
    initialLat: Double,
    initialLng: Double,
    onDismiss: () -> Unit,
    onSubmit: (tanggal: String, pic: String, team: String, timeOut: String, ticket: String, plat: String, type: String, tujuan: String, odoOut: String, fuel: String, catatan: String, lat: Double, lng: Double) -> Unit
) {
    val currentDate = remember { SimpleDateFormat("10/09/2026", Locale.getDefault()).format(Date()) }
    val currentTime = remember { SimpleDateFormat("10:38", Locale.getDefault()).format(Date()) }

    var judulPekerjaan by remember { mutableStateOf("Operasional Perbaikan & Peminjaman Kendaraan Lapangan") }
    var selectedMonth by remember { mutableStateOf("SEPTEMBER") }
    var selectedYear by remember { mutableStateOf("2026") }

    var tanggal by remember { mutableStateOf("10/09/2026") }
    var pic by remember { mutableStateOf("Hadi & Qori") }
    var team by remember { mutableStateOf("SO") }
    var timeOut by remember { mutableStateOf("10:38") }
    var ticket by remember { mutableStateOf("E/260909/0028") }
    var platKendaraan by remember { mutableStateOf("B 2924 SKN") }
    var typeKendaraan by remember { mutableStateOf("Grandmax") }
    var ketTujuan by remember { mutableStateOf("Cilandak") }
    var odometerOut by remember { mutableStateOf("45,210 KM") }
    var fuelLevel by remember { mutableStateOf("Full (90%)") }
    var catatanPinjam by remember { mutableStateOf("Peminjaman armada mobil untuk penanganan lapangan") }

    val vehiclePresets = listOf(
        Pair("B 2924 SKN", "Grandmax"),
        Pair("B 4605 SOU", "LEXY"),
        Pair("B 1241 U", "AVANZA"),
        Pair("B 4616 SOU", "LEXY"),
        Pair("B 9872 KBA", "Grandmax"),
        Pair("B 3321 TZA", "AVANZA")
    )

    val teamMemberPresets = listOf(
        Pair("Hadi & Qori", "SO"),
        Pair("Carlos & Syahril", "SO"),
        Pair("Yosep", "SM"),
        Pair("Anil", "SM"),
        Pair("M.Rusli", "SM"),
        Pair("Alqori Dewantara & Ahmad Ibrahim", "SO"),
        Pair("Dimas Wahyu Pratama & Muhamad Fadli", "SO"),
        Pair("Budi Mulya & Carolus Borromeus", "SM")
    )

    val ticketPresets = listOf("E/260909/0028", "E/260909/0033", "Storing", "Aktifasi Switch Ring 7", "E/260909/0019")
    val tujuanPresets = listOf("Cilandak", "Mid Plaza", "DurenTiga", "Sudirman", "Cyber 1", "TB Simatupang", "Kuningan Barat", "Kelapa Gading")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 8.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header (Sesuai Gambar 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Input Data Operasional FOSIS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Pelaporan Otomatis ke Database Terenkripsi",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF94A3B8))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented buttons / Tab (Sesuai Gambar 2)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFF1E293B))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        Triple("Troubleshoot", "SO", false),
                        Triple("Maintenance", "SM", false),
                        Triple("Log Kendaraan", team, true)
                    ).forEach { (label, tVal, isSelected) ->
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) Color(0xFF334155) else Color.Transparent,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (label == "Troubleshoot") team = "SO"
                                    if (label == "Maintenance") team = "SM"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Judul / Deskripsi Ringkas Pekerjaan * (Sesuai Gambar 2)
                    OutlinedTextField(
                        value = judulPekerjaan,
                        onValueChange = { judulPekerjaan = it },
                        label = { Text("Judul / Deskripsi Ringkas Pekerjaan *", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                        placeholder = { Text("Deskripsi pekerjaan / peminjaman armada", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bulan & Tahun (Sesuai Gambar 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = { selectedMonth = it },
                            label = { Text("Bulan", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = selectedYear,
                            onValueChange = { selectedYear = it },
                            label = { Text("Tahun", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Section Title: Parameter Administrasi / Logbook Kendaraan (Sesuai Gambar 2)
                    Text(
                        text = "Parameter Administrasi (Sesuai Struktur Tabel Kendaraan):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )

                    // Row 1: Tanggal & Time out (Sesuai Format 2 Kolom Gambar 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tanggal,
                            onValueChange = { tanggal = it },
                            label = { Text("Tanggal", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("10/09/2026", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = timeOut,
                            onValueChange = { timeOut = it },
                            label = { Text("Time out", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("10:38", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Row 2: PIC & Team (Sesuai Format 2 Kolom Gambar 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pic,
                            onValueChange = { pic = it },
                            label = { Text("PIC", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("Hadi & Qori / Yosep", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = team,
                            onValueChange = { team = it },
                            label = { Text("Team", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("SO / SM", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.7f),
                            singleLine = true
                        )
                    }

                    // Pilihan Cepat PIC Draf
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        teamMemberPresets.forEach { preset ->
                            FilterChip(
                                selected = pic == preset.first,
                                onClick = {
                                    pic = preset.first
                                    team = preset.second
                                },
                                label = { Text("${preset.first} (${preset.second})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFFCBD5E1),
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Row 3: Ticket & Ket. Tujuan (Sesuai Format 2 Kolom Gambar 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ticket,
                            onValueChange = { ticket = it },
                            label = { Text("Ticket", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("E/260909/0028 / Storing", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ketTujuan,
                            onValueChange = { ketTujuan = it },
                            label = { Text("Ket. Tujuan", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("Cilandak / Mid Plaza", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Pilihan Cepat Ticket & Tujuan
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ticketPresets.forEach { tPreset ->
                            FilterChip(
                                selected = ticket == tPreset,
                                onClick = { ticket = tPreset },
                                label = { Text(tPreset, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFFCBD5E1),
                                    selectedContainerColor = Color(0xFF0D9488),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Row 4: Plat kendaraan & Type Kendaraan (Sesuai Format 2 Kolom Gambar 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = platKendaraan,
                            onValueChange = { platKendaraan = it },
                            label = { Text("Plat kendaraan", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("B 2924 SKN", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = typeKendaraan,
                            onValueChange = { typeKendaraan = it },
                            label = { Text("Type Kendaraan", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("Grandmax / LEXY / AVANZA", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Pilihan Cepat Armada
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        vehiclePresets.forEach { vPreset ->
                            FilterChip(
                                selected = platKendaraan == vPreset.first,
                                onClick = {
                                    platKendaraan = vPreset.first
                                    typeKendaraan = vPreset.second
                                },
                                label = { Text("${vPreset.first} (${vPreset.second})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFFCBD5E1),
                                    selectedContainerColor = Color(0xFF16A34A),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Row 5: Odometer & BBM
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = odometerOut,
                            onValueChange = { odometerOut = it },
                            label = { Text("Odometer Awal", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("45,210 KM", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = fuelLevel,
                            onValueChange = { fuelLevel = it },
                            label = { Text("BBM Awal", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            placeholder = { Text("Full (90%)", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Catatan Peminjaman
                    OutlinedTextField(
                        value = catatanPinjam,
                        onValueChange = { catatanPinjam = it },
                        label = { Text("Catatan Peminjaman Lapangan", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    // GPS LOCK Card (Sesuai Gambar 2)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GPS LOCK : LOKASI TERKUNCI OTOMATIS SENSOR GPS AKTIF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                            Text(
                                text = "Koordinat terunci saat input data: ${String.format(Locale.US, "%.4f", initialLat)}, ${String.format(Locale.US, "%.4f", initialLng)} (Akurasi Presisi Device)",
                                fontSize = 11.sp,
                                color = Color(0xFF166534)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF38BDF8))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions: Batal (Outlined pill) & Simpan Data (Filled Pill) (Sesuai Gambar 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF93C5FD)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Batal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (platKendaraan.isNotBlank() && pic.isNotBlank()) {
                                onSubmit(
                                    tanggal, pic, team, timeOut, ticket, platKendaraan, typeKendaraan,
                                    ketTujuan, odometerOut, fuelLevel, catatanPinjam, initialLat, initialLng
                                )
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Data", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * ============================================================================
 * MODAL 2: FORM PENGEMBALIAN KENDARAAN OLEH PIC (Return Vehicle Dialog)
 * ============================================================================
 */
@Composable
private fun ReturnVehicleDialog(
    log: VehicleLogEntity,
    onDismiss: () -> Unit,
    onSubmit: (tanggalKembali: String, timeIn: String, odoIn: String, fuelIn: String, catatanKembali: String) -> Unit
) {
    val currentDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    var tanggalKembali by remember { mutableStateOf(currentDate) }
    var timeIn by remember { mutableStateOf(currentTime) }
    var odometerIn by remember { mutableStateOf(if (log.odometerOut.contains("KM")) log.odometerOut else "45,280 KM") }
    var fuelLevelIn by remember { mutableStateOf("Full (80%)") }
    var catatanKembali by remember { mutableStateOf("Kendaraan telah kembali di parkiran kantor, kunci dan STNK sudah dikembalikan ke pos/IC.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = Color(0xFF2563EB))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Form Pengembalian Kendaraan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info Ringkas Armada
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ARMADA YANG DIKEMBALIKAN", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text("${log.platKendaraan} (${log.typeKendaraan})", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFFCD34D))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("PIC: ${log.pic} • Tiket: ${log.ticket} • Berangkat: ${log.tanggal} ${log.timeOut}", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }

                // 1. Tanggal Kembali
                item {
                    OutlinedTextField(
                        value = tanggalKembali,
                        onValueChange = { tanggalKembali = it },
                        label = { Text("Tanggal Kembali (dd/MM/yyyy)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 2. Jam Pengembalian (Time In)
                item {
                    OutlinedTextField(
                        value = timeIn,
                        onValueChange = { timeIn = it },
                        label = { Text("Jam Kembali / Time In (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 3. Odometer Akhir (KM In)
                item {
                    OutlinedTextField(
                        value = odometerIn,
                        onValueChange = { odometerIn = it },
                        label = { Text("KM Akhir / Odometer Masuk (contoh: 45,260 KM)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 4. Kondisi BBM saat Masuk
                item {
                    OutlinedTextField(
                        value = fuelLevelIn,
                        onValueChange = { fuelLevelIn = it },
                        label = { Text("Level BBM saat Kembali (contoh: Full / 3/4 / 1/2)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 5. Catatan Kondisi Fisik Kendaraan
                item {
                    OutlinedTextField(
                        value = catatanKembali,
                        onValueChange = { catatanKembali = it },
                        label = { Text("Catatan Fisik, Kebersihan, Rem & Kunci") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Setelah form diajukan, status berubah menjadi 'MENUNGGU ACC IC' untuk diverifikasi oleh IC SO / IC SM.",
                                fontSize = 10.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (timeIn.isNotBlank()) {
                        onSubmit(tanggalKembali, timeIn, odometerIn, fuelLevelIn, catatanKembali)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ajukan Pengembalian")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

/**
 * ============================================================================
 * MODAL 3: VERIFIKASI & ACC OLEH IC (Approval Dialog)
 * ============================================================================
 */
@Composable
private fun AccReturnDialog(
    log: VehicleLogEntity,
    activeUser: UserEntity?,
    onDismiss: () -> Unit,
    onSubmitAcc: (accBy: String, accRole: String, accNotes: String) -> Unit
) {
    // Default IC name and role based on active user or Team
    val defaultIcName = when {
        activeUser?.name?.isNotBlank() == true -> activeUser.name
        log.team.equals("SO", ignoreCase = true) -> "Muhamad Fadli"
        else -> "Nemi BR Ginting"
    }

    val defaultIcRole = when {
        activeUser?.role == UserRole.IC_TROUBLESHOOT -> "IC Service Operation (Troubleshoot)"
        activeUser?.role == UserRole.IC_MAINTENANCE -> "IC Maintenance"
        activeUser?.role == UserRole.DEPT_HEAD -> "Dept. Head Field Engineer"
        activeUser?.role == UserRole.ADMIN -> "Admin Field Operations"
        log.team.equals("SO", ignoreCase = true) -> "IC Service Operation (Troubleshoot)"
        else -> "IC Maintenance"
    }

    var accBy by remember { mutableStateOf(defaultIcName) }
    var accRole by remember { mutableStateOf(defaultIcRole) }
    var accNotes by remember { mutableStateOf("Kendaraan telah dicek fisik, kunci dan STNK lengkap di pos armada. Disetujui.") }

    val icPresets = listOf(
        Pair("Muhamad Fadli", "IC Service Operation (Troubleshoot)"),
        Pair("Nemi BR Ginting", "IC Maintenance"),
        Pair("Mas Rizki Firdaus", "Dept. Head Field Engineer"),
        Pair("Sanderlina Imelda", "Admin Field Operations")
    )
    var showIcDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFFD97706))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verifikasi & ACC Pengembalian IC", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info Kendaraan & Pengembalian
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.platKendaraan, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFFCD34D))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (log.team.equals("SO", ignoreCase = true)) Color(0xFF16A34A) else Color(0xFFD97706)
                                ) {
                                    Text("TEAM ${log.team}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("PIC Peminjam: ${log.pic} • Tiket: ${log.ticket}", fontSize = 11.sp, color = Color.White)
                            Text("Waktu Pinjam: ${log.tanggal} ${log.timeOut} → Kembali: ${log.tanggalKembali} ${log.timeIn}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("KM Berangkat: ${log.odometerOut} → KM Masuk: ${log.odometerIn}", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            Text("Catatan PIC: ${log.catatanKembali}", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }

                // 1. Nama IC Penyetuju
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = accBy,
                            onValueChange = { accBy = it },
                            label = { Text("Nama Penanggung Jawab IC") },
                            trailingIcon = {
                                IconButton(onClick = { showIcDropdown = !showIcDropdown }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        DropdownMenu(
                            expanded = showIcDropdown,
                            onDismissRequest = { showIcDropdown = false }
                        ) {
                            icPresets.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text("${preset.first} (${preset.second})", fontSize = 12.sp) },
                                    onClick = {
                                        accBy = preset.first
                                        accRole = preset.second
                                        showIcDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Jabatan / Role IC
                item {
                    OutlinedTextField(
                        value = accRole,
                        onValueChange = { accRole = it },
                        label = { Text("Jabatan / IC Divisi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 3. Catatan Pemeriksaan IC
                item {
                    OutlinedTextField(
                        value = accNotes,
                        onValueChange = { accNotes = it },
                        label = { Text("Catatan Hasil Pemeriksaan & ACC") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (accBy.isNotBlank()) {
                        onSubmitAcc(accBy, accRole, accNotes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Setujui & ACC Pengembalian")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

/**
 * ============================================================================
 * MODAL 4: EDIT LOG KENDARAAN (Form Edit Lengkap)
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditVehicleDialog(
    log: VehicleLogEntity,
    onDismiss: () -> Unit,
    onSubmit: (VehicleLogEntity) -> Unit
) {
    var tanggal by remember { mutableStateOf(log.tanggal) }
    var pic by remember { mutableStateOf(log.pic) }
    var team by remember { mutableStateOf(log.team) }
    var timeOut by remember { mutableStateOf(log.timeOut) }
    var ticket by remember { mutableStateOf(log.ticket) }
    var platKendaraan by remember { mutableStateOf(log.platKendaraan) }
    var typeKendaraan by remember { mutableStateOf(log.typeKendaraan) }
    var ketTujuan by remember { mutableStateOf(log.ketTujuan) }
    var statusArmada by remember { mutableStateOf(log.statusArmada) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Log: ${log.platKendaraan}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(value = tanggal, onValueChange = { tanggal = it }, label = { Text("1. Tanggal") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = pic, onValueChange = { pic = it }, label = { Text("2. PIC") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = team, onValueChange = { team = it }, label = { Text("3. Team (SO/SM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = timeOut, onValueChange = { timeOut = it }, label = { Text("4. Time out") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = ticket, onValueChange = { ticket = it }, label = { Text("5. Ticket") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = platKendaraan, onValueChange = { platKendaraan = it }, label = { Text("6. Plat Kendaraan") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = typeKendaraan, onValueChange = { typeKendaraan = it }, label = { Text("7. Type Kendaraan") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = ketTujuan, onValueChange = { ketTujuan = it }, label = { Text("8. Ket. Tujuan") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        log.copy(
                            tanggal = tanggal,
                            pic = pic,
                            team = team,
                            timeOut = timeOut,
                            ticket = ticket,
                            platKendaraan = platKendaraan,
                            typeKendaraan = typeKendaraan,
                            ketTujuan = ketTujuan,
                            statusArmada = statusArmada
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

/**
 * ============================================================================
 * MODAL: LIVE RADAR GPS & VEHICLE TRACKING SIMULATION
 * Menampilkan Peta Interaktif Digital, Rute Perjalanan, Telemetri Real-Time & Navigasi
 * ============================================================================
 */
@Composable
private fun VehicleLiveTrackingMapDialog(
    log: VehicleLogEntity,
    onDismiss: () -> Unit,
    onReturn: () -> Unit,
    onAcc: () -> Unit,
    onOpenExternalMap: () -> Unit
) {
    var isSimulating by remember { mutableStateOf(true) }
    var currentSpeed by remember { mutableIntStateOf(48) }
    var useRealGoogleMaps by remember { mutableStateOf(true) }

    // Smooth progress animation along the trajectory
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTracking")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 36f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1600, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "PulseRing"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1600, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    val simulatedProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.88f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(12000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "SimulatedProgress"
    )

    val activeProgress = if (isSimulating) simulatedProgress else 0.55f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // HUD Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LIVE GPS GOOGLE MAPS: ${log.platKendaraan}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFFF8FAFC)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF16A34A).copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4ADE80))
                                ) {
                                    Text(
                                        text = "LIVE GPS LOCK",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4ADE80),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${log.typeKendaraan} • PIC: ${log.pic} (Team ${log.team}) • Tiket: ${log.ticket}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8))
                    }
                }

                // Interactive Map Viewport (Real Google Maps + Tech Radar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF030712))
                ) {
                    if (useRealGoogleMaps) {
                        RealGoogleMapsEmbedView(
                            vehicles = listOf(log),
                            focusedVehicle = log,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            // Draw Tech Grid Lines
                            val gridSpacing = 48.dp.toPx()
                            for (x in 0..(canvasWidth / gridSpacing).toInt()) {
                                val lineX = x * gridSpacing
                                drawLine(
                                    color = Color(0xFF1E293B).copy(alpha = 0.4f),
                                    start = Offset(lineX, 0f),
                                    end = Offset(lineX, canvasHeight),
                                    strokeWidth = 1f
                                )
                            }
                            for (y in 0..(canvasHeight / gridSpacing).toInt()) {
                                val lineY = y * gridSpacing
                                drawLine(
                                    color = Color(0xFF1E293B).copy(alpha = 0.4f),
                                    start = Offset(0f, lineY),
                                    end = Offset(canvasWidth, lineY),
                                    strokeWidth = 1f
                                )
                            }

                            // Coordinates for Basecamp and Destination
                            val basecampX = canvasWidth * 0.18f
                            val basecampY = canvasHeight * 0.72f
                            val destX = canvasWidth * 0.82f
                            val destY = canvasHeight * 0.28f

                            // Draw Route Trajectory (Curved S-Path)
                            val path = Path().apply {
                                moveTo(basecampX, basecampY)
                                cubicTo(
                                    canvasWidth * 0.35f, canvasHeight * 0.85f,
                                    canvasWidth * 0.65f, canvasHeight * 0.15f,
                                    destX, destY
                                )
                            }

                            // Background Glow Route
                            drawPath(
                                path = path,
                                color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                style = Stroke(width = 10f)
                            )
                            // Active Route Dashed Line
                            drawPath(
                                path = path,
                                color = Color(0xFF38BDF8),
                                style = Stroke(
                                    width = 3.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                                )
                            )

                            // Current Animated Vehicle Position along trajectory
                            val vehicleX = basecampX + (destX - basecampX) * activeProgress
                            // Approximate cubic Y
                            val t = activeProgress
                            val vehicleY = (1 - t) * (1 - t) * (1 - t) * basecampY +
                                    3 * (1 - t) * (1 - t) * t * (canvasHeight * 0.85f) +
                                    3 * (1 - t) * t * t * (canvasHeight * 0.15f) +
                                    t * t * t * destY

                            // 1. Draw Basecamp Pin (Origin)
                            drawCircle(
                                color = Color(0xFF10B981).copy(alpha = 0.3f),
                                radius = 16.dp.toPx(),
                                center = Offset(basecampX, basecampY)
                            )
                            drawCircle(
                                color = Color(0xFF10B981),
                                radius = 7.dp.toPx(),
                                center = Offset(basecampX, basecampY)
                            )

                            // 2. Draw Destination Pin (Target)
                            drawCircle(
                                color = Color(0xFFEF4444).copy(alpha = 0.3f),
                                radius = 18.dp.toPx(),
                                center = Offset(destX, destY)
                            )
                            drawCircle(
                                color = Color(0xFFEF4444),
                                radius = 8.dp.toPx(),
                                center = Offset(destX, destY)
                            )

                            // 3. Draw Vehicle Animated Radar Wave
                            drawCircle(
                                color = Color(0xFF38BDF8).copy(alpha = radarAlpha),
                                radius = radarPulse * density,
                                center = Offset(vehicleX, vehicleY)
                            )
                            drawCircle(
                                color = Color(0xFF0284C7),
                                radius = 12.dp.toPx(),
                                center = Offset(vehicleX, vehicleY)
                            )
                            drawCircle(
                                color = Color(0xFFFCD34D),
                                radius = 6.dp.toPx(),
                                center = Offset(vehicleX, vehicleY)
                            )
                        }

                        // Floating GPS Overlay HUD Badges
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF064E3B).copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34D399)),
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 24.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("🏢 ASAL: KANTOR MENARA KADIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7))
                                Text("Waktu Out: ${log.timeOut} • Odo: ${log.odometerOut}", fontSize = 8.sp, color = Color(0xFFD1FAE5))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF7F1D1D).copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
                            modifier = Modifier
                                .padding(end = 16.dp, top = 16.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("🏁 TUJUAN: ${log.ketTujuan}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                                Text("Lat: ${log.latitude} | Lng: ${log.longitude}", fontSize = 8.sp, color = Color(0xFFFEE2E2))
                            }
                        }
                    }

                    // Map Mode Switcher & Simulation Controls in Top-Left
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (useRealGoogleMaps) Color(0xFF0284C7) else Color.Transparent,
                                    modifier = Modifier.clickable { useRealGoogleMaps = true }
                                ) {
                                    Text(
                                        text = "🌐 Google Maps Real",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (!useRealGoogleMaps) Color(0xFF334155) else Color.Transparent,
                                    modifier = Modifier.clickable { useRealGoogleMaps = false }
                                ) {
                                    Text(
                                        text = "📡 Vektor",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (!useRealGoogleMaps) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { isSimulating = !isSimulating }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSimulating) "Jeda" else "Mulai",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF1F5F9)
                                    )
                                }
                            }
                        }
                    }
                }

                // Telemetry & Control Dashboard Bottom Panel
                Surface(
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // 4 Metric Telemetry Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Speed
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("KECEPATAN", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (isSimulating) "$currentSpeed km/h" else "0 km/h (Parkir)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }

                            // BBM
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("BBM AWAL", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                    Text(
                                        text = log.fuelLevel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4ADE80)
                                    )
                                }
                            }

                            // Odometer
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("KM BERANGKAT", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                    Text(
                                        text = log.odometerOut,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFCD34D)
                                    )
                                }
                            }

                            // Status Armada
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("STATUS ARMADA", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                    Text(
                                        text = when {
                                            log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> "MENUNGGU ACC"
                                            log.statusArmada.equals("SELESAI", ignoreCase = true) -> "SELESAI"
                                            else -> "DALAM TUGAS"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when {
                                            log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true) -> Color(0xFFF59E0B)
                                            log.statusArmada.equals("SELESAI", ignoreCase = true) -> Color(0xFF4ADE80)
                                            else -> Color(0xFF60A5FA)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (log.statusArmada.equals("MENUNGGU_ACC_KEMBALI", ignoreCase = true)) {
                                Button(
                                    onClick = onAcc,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verifikasi ACC IC", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else if (!log.statusArmada.equals("SELESAI", ignoreCase = true)) {
                                Button(
                                    onClick = onReturn,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Proses Pengembalian", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = onOpenExternalMap,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buka Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Tutup", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ============================================================================
 * MODAL: SIMULASI ALUR KENDARAAN (Dari Input Data Sampai Cek Lokasi & ACC)
 * Memberikan Panduan Interaktif & 1-Klik Simulasi End-to-End
 * ============================================================================
 */
@Composable
private fun VehicleSimulationWorkflowDialog(
    onDismiss: () -> Unit,
    onStartInputFlow: () -> Unit,
    onStartLiveMapCheck: () -> Unit,
    onQuickFullSimulation: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4C1D95))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = Color(0xFFDDD6FE), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Simulasi Alur Logistik Kendaraan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Alur Kerja: Input Data ➔ GPS Radar ➔ Pengembalian ➔ ACC IC",
                                fontSize = 11.sp,
                                color = Color(0xFFDDD6FE)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                // Workflow Steps Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Highlight Box: 1-Click Auto Simulation
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8B5CF6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Uji Coba Cepat (1-Klik Simulasi Lengkap)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF5B21B6)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Sistem akan membuat 1 record armada aktif baru ('B 9999 FOS' - Cyber 1 Mampang) dan langsung membuka Radar Peta GPS interaktif.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6D28D9)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onQuickFullSimulation,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("⚡ Jalankan 1-Klik Simulasi & Buka Peta", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Tahapan Alur Operasional Kendaraan:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )
                    }

                    item {
                        WorkflowStepCard(
                            stepNumber = "1",
                            title = "Input Data Peminjaman Armada",
                            description = "Teknisi atau PIC mengisi Tanggal, PIC, Team (SO/SM), Waktu Keluar, Nomor Tiket, Plat Nomor, Type Mobil, Tujuan, KM awal, BBM awal, dan titik GPS koordinat keberangkatan.",
                            icon = Icons.Default.AddCircle,
                            color = Color(0xFF16A34A)
                        )
                    }

                    item {
                        WorkflowStepCard(
                            stepNumber = "2",
                            title = "Pengecekan Lokasi & Radar GPS Peta",
                            description = "Buka tab 'PETA LOKASI ARMADA' atau klik tombol 'Peta' di tabel untuk melihat radar armada, lintasan rute perjalanan dari Basecamp ke tujuan, dan telemetri kecepatan secara live.",
                            icon = Icons.Default.Place,
                            color = Color(0xFF0284C7)
                        )
                    }

                    item {
                        WorkflowStepCard(
                            stepNumber = "3",
                            title = "Pengembalian Kendaraan oleh PIC",
                            description = "Setelah pekerjaan di lapangan selesai, PIC menekan tombol 'Kembalikan Mobil' untuk menginput Waktu Masuk, KM Kembali, BBM Kembali, dan catatan fisik armada.",
                            icon = Icons.Default.AssignmentReturn,
                            color = Color(0xFF2563EB)
                        )
                    }

                    item {
                        WorkflowStepCard(
                            stepNumber = "4",
                            title = "Verifikasi & Persetujuan (ACC) Pejabat IC",
                            description = "Pejabat IC memeriksa kesesuaian fisik kendaraan, odometer, serta catatan pengembalian, kemudian menekan tombol 'ACC IC' untuk menyelesaikan status logbook.",
                            icon = Icons.Default.Verified,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // Footer Actions
                Surface(
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onStartInputFlow,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Input Manual", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onStartLiveMapCheck,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("📍 Buka Radar Peta", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step Card Component for the Simulation Workflow
 */
@Composable
private fun WorkflowStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, color),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stepNumber,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
