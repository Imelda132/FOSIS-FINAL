package com.example

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import kotlinx.coroutines.launch

import com.example.data.model.FosisType
import com.example.data.model.WorkflowStatus
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FosisNavTab
import com.example.ui.viewmodel.FosisUiState
import com.example.ui.viewmodel.FosisViewModel


// ============================================================================
// MAIN ACTIVITY
// ============================================================================

class MainActivity : ComponentActivity() {

    private val viewModel: FosisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    FosisAppRoot(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}


// ============================================================================
// FOSIS APP ROOT
// ============================================================================

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun FosisAppRoot(
    viewModel: FosisViewModel
) {

    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }


    // ========================================================================
    // ADD TICKET MODAL
    // ========================================================================

    var isAddModalOpen by remember {
        mutableStateOf(false)
    }

    var addModalInitialType by remember {
        mutableStateOf(FosisType.TROUBLESHOOT)
    }

    // Saat menekan "Tambah Administrasi", tampilkan halaman pemilihan
    // Troubleshoot / Maintenance / Administrasi terlebih dahulu.
    var showAddTypePicker by remember {
        mutableStateOf(false)
    }


    // ========================================================================
    // MOBILE NAVIGATION DRAWER
    // ========================================================================

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val drawerScope = rememberCoroutineScope()


    // ========================================================================
    // SNACKBAR
    // ========================================================================

    LaunchedEffect(uiState.snackbarMessage) {

        uiState.snackbarMessage?.let { msg ->

            snackbarHostState.showSnackbar(msg)

            viewModel.clearSnackbar()
        }
    }


    // ========================================================================
    // LOGIN
    // ========================================================================

    if (!uiState.isLoggedIn) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            LoginScreen(
                uiState = uiState,

                onLogin = { user, pwd ->
                    viewModel.login(
                        user,
                        pwd
                    )
                }
            )

            SnackbarHost(
                hostState = snackbarHostState,

                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        return
    }


    // ========================================================================
    // RESPONSIVE LAYOUT
    // ========================================================================

    // ========================================================================
// MENU NAVIGASI DRAWER
// Semua menu yang tersedia di drawer.
// Trouble, Mainten, dan Administrasi juga tetap muncul di bottom bar.
// ========================================================================

    val drawerMenuTabs = listOf(

        // UTAMA
        Triple(
            FosisNavTab.DASHBOARD,
            Icons.Default.Dashboard,
            "Dashboard"
        ),

        // OPERASIONAL
        Triple(
            FosisNavTab.PERIJINAN,
            Icons.Default.AssignmentTurnedIn,
            "Licensing (SIK)"
        ),

        Triple(
            FosisNavTab.WORKFLOW,
            Icons.Default.AccountTree,
            "Workflow"
        ),

        // TIM LAPANGAN
        Triple(
            FosisNavTab.GEOLOKASI,
            Icons.Default.DirectionsCar,
            "Vehicle"
        ),

        Triple(
            FosisNavTab.FIELD_CHAT,
            Icons.Default.Chat,
            "Chating"
        ),

        // ADMINISTRASI / AKSES
        Triple(
            FosisNavTab.ADMIN_USERS,
            Icons.Default.AdminPanelSettings,
            "Team Access"
        ),

        // ================================================================
        // MENU YANG JUGA ADA DI BOTTOM BAR
        // ================================================================

        Triple(
            FosisNavTab.TROUBLESHOOT,
            Icons.Default.Warning,
            "TroubleShoot"
        ),

        Triple(
            FosisNavTab.MAINTENANCE,
            Icons.Default.Build,
            "Maintenance"
        ),

        Triple(
            FosisNavTab.ADMINISTRASI,
            Icons.Default.Description,
            "Administration"
        )
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {

        val windowWidth = maxWidth

        val isExpanded =
            windowWidth >= 840.dp

        val isMedium =
            windowWidth in 600.dp..839.dp


        // ====================================================================
        // ALL NAVIGATION TABS
        // ====================================================================

        val navTabsList = listOf(

            Triple(
                FosisNavTab.DASHBOARD,
                Icons.Default.Dashboard,
                "Dashboard"
            ),

            Triple(
                FosisNavTab.TROUBLESHOOT,
                Icons.Default.Warning,
                "Troubleshoot"
            ),

            Triple(
                FosisNavTab.MAINTENANCE,
                Icons.Default.Build,
                "Maintenance"
            ),

            Triple(
                FosisNavTab.PERIJINAN,
                Icons.Default.AssignmentTurnedIn,
                "Perijinan (SIK)"
            ),

            Triple(
                FosisNavTab.ADMINISTRASI,
                Icons.Default.Description,
                "Administrasi"
            ),

            Triple(
                FosisNavTab.WORKFLOW,
                Icons.Default.AccountTree,
                "Alur Kerja"
            ),

            Triple(
                FosisNavTab.GEOLOKASI,
                Icons.Default.DirectionsCar,
                "Kendaraan"
            ),

            Triple(
                FosisNavTab.FIELD_CHAT,
                Icons.Default.Chat,
                "Chat Lapangan"
            ),

            Triple(
                FosisNavTab.ADMIN_USERS,
                Icons.Default.AdminPanelSettings,
                "Akses Tim"
            )
        )


        // ====================================================================
        // MOBILE BOTTOM NAVIGATION
        // ====================================================================

        // Hanya 3 menu di bawah sesuai desain: Trouble, Mainten, Administrasi.
        val mobileBottomNavTabs = listOf(
            Triple(FosisNavTab.TROUBLESHOOT, Icons.Default.Warning, "TroubleShoot"),
            Triple(FosisNavTab.MAINTENANCE, Icons.Default.Build, "Maintenance"),
            Triple(FosisNavTab.ADMINISTRASI, Icons.Default.Description, "Administration")
        )


        // ====================================================================
        // DESKTOP
        // ====================================================================

        if (isExpanded) {

            Row(
                modifier = Modifier.fillMaxSize()
            ) {

                // =================================================================
                // DESKTOP SIDEBAR
                // =================================================================

                Surface(
                    shape = RoundedCornerShape(
                        topEnd = 24.dp,
                        bottomEnd = 24.dp
                    ),

                    color = Color.White,

                    border = BorderStroke(
                        1.dp,
                        VibrantOutline
                    ),

                    shadowElevation = 2.dp,

                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),

                        verticalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Column {

                            // =================================================
                            // LOGO
                            // =================================================

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,

                                modifier = Modifier.padding(
                                    bottom = 20.dp,
                                    top = 8.dp
                                )
                            ) {

                                Surface(
                                    shape = CircleShape,

                                    color =
                                        VibrantPrimaryContainer,

                                    modifier =
                                        Modifier.size(42.dp)
                                ) {

                                    Box(
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Default.CellTower,

                                            contentDescription =
                                                "FOSIS",

                                            tint =
                                                VibrantPrimary,

                                            modifier =
                                                Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(
                                    modifier =
                                        Modifier.width(10.dp)
                                )

                                Column {

                                    Text(
                                        text =
                                            "FOSIS WEB OPS",

                                        fontWeight =
                                            FontWeight.Bold,

                                        fontSize =
                                            16.sp,

                                        color =
                                            VibrantOnBackground
                                    )

                                    Text(
                                        text =
                                            "Field Support Portal",

                                        fontSize =
                                            11.sp,

                                        color =
                                            VibrantOnSurfaceVariant
                                    )
                                }
                            }


                            HorizontalDivider(
                                color =
                                    Color(0xFFF3EDF7),

                                thickness =
                                    1.dp
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(14.dp)
                            )


                            // =================================================
                            // DESKTOP NAVIGATION
                            // =================================================

                            navTabsList.forEach {
                                    (tab, icon, label) ->

                                val isSelected =
                                    uiState.currentTab == tab

                                Surface(

                                    shape =
                                        RoundedCornerShape(14.dp),

                                    color =
                                        if (isSelected)
                                            VibrantPrimaryContainer
                                        else
                                            Color.Transparent,

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {

                                                viewModel.setNavTab(
                                                    tab
                                                )
                                            }
                                            .padding(
                                                vertical = 3.dp
                                            )
                                ) {

                                    Row(

                                        modifier =
                                            Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 10.dp
                                            ),

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Icon(
                                            imageVector = icon,

                                            contentDescription =
                                                label,

                                            tint =
                                                if (isSelected)
                                                    VibrantPrimary
                                                else
                                                    VibrantOnSurfaceVariant,

                                            modifier =
                                                Modifier.size(20.dp)
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.width(12.dp)
                                        )

                                        Text(
                                            text =
                                                label,

                                            fontSize =
                                                13.sp,

                                            fontWeight =
                                                if (isSelected)
                                                    FontWeight.Bold
                                                else
                                                    FontWeight.Medium,

                                            color =
                                                if (isSelected)
                                                    VibrantOnPrimaryContainer
                                                else
                                                    VibrantOnSurfaceVariant,

                                            modifier =
                                                Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }


                        // =================================================
                        // USER PROFILE
                        // =================================================

                        Surface(

                            shape =
                                RoundedCornerShape(16.dp),

                            color =
                                Color(0xFFF7F2FA),

                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Row(

                                modifier =
                                    Modifier.padding(10.dp),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Surface(

                                    shape =
                                        CircleShape,

                                    color =
                                        VibrantPrimary,

                                    modifier =
                                        Modifier.size(36.dp)
                                ) {

                                    Box(
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Text(
                                            text =
                                                uiState.activeUser
                                                    ?.avatarInitials
                                                    ?: "U",

                                            fontWeight =
                                                FontWeight.Bold,

                                            color =
                                                Color.White,

                                            fontSize =
                                                13.sp
                                        )
                                    }
                                }


                                Spacer(
                                    modifier =
                                        Modifier.width(10.dp)
                                )


                                Column(
                                    modifier =
                                        Modifier.weight(1f)
                                ) {

                                    Text(
                                        text =
                                            uiState.activeUser
                                                ?.name
                                                ?.take(16)
                                                ?: "-",

                                        fontSize =
                                            12.sp,

                                        fontWeight =
                                            FontWeight.Bold,

                                        color =
                                            VibrantOnBackground
                                    )

                                    Text(
                                        text =
                                            uiState.activeUser
                                                ?.role
                                                ?.label
                                                ?: "-",

                                        fontSize =
                                            10.sp,

                                        color =
                                            VibrantPrimary
                                    )
                                }


                                IconButton(
                                    onClick = {
                                        viewModel.logout()
                                    },

                                    modifier =
                                        Modifier.size(28.dp)
                                ) {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Logout,

                                        contentDescription =
                                            "Logout",

                                        tint =
                                            Color.Gray,

                                        modifier =
                                            Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }


                // =================================================================
                // DESKTOP CONTENT
                // =================================================================

                Scaffold(

                    topBar = {

                        FosisTopBar(

                            activeUser =
                                uiState.activeUser,

                            allUsers =
                                uiState.users,

                            isOnline =
                                uiState.isOnline,

                            pendingSyncCount =
                                uiState.pendingSyncCount,

                            runningJobsCount =
                                uiState.items.count {
                                    it.workflowStatus !=
                                            WorkflowStatus.APPROVED_DEPT_HEAD
                                },

                            onOpenRunningJobs = {
                                viewModel.setRunningJobsDialogOpen(
                                    true
                                )
                            },

                            onToggleNetwork = {
                                viewModel.toggleNetworkOnline()
                            },

                            onManualSync = {
                                viewModel.triggerSync()
                            },

                            onSwitchUser = {
                                viewModel.switchActiveUser(it)
                            },

                            onLogout = {
                                viewModel.logout()
                            },

                            onClearData = {
                                viewModel.clearAllOperationalData()
                            }
                        )
                    },


                    snackbarHost = {

                        SnackbarHost(
                            hostState =
                                snackbarHostState
                        )
                    },

                    modifier =
                        Modifier.weight(1f)

                ) { innerPadding ->

                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                    ) {

                        MainScreenContent(

                            uiState =
                                uiState,

                            viewModel =
                                viewModel,

                            onOpenAddModal = { type ->

                                addModalInitialType =
                                    type

                                showAddTypePicker =
                                    type == FosisType.ADMINISTRASI

                                isAddModalOpen =
                                    true
                            }
                        )
                    }
                }
            }
        }


        // ====================================================================
        // TABLET
        // ====================================================================

        else if (isMedium) {

            Row(
                modifier =
                    Modifier.fillMaxSize()
            ) {

                NavigationRail(

                    containerColor =
                        Color.White,

                    header = {

                        Column(

                            horizontalAlignment =
                                Alignment.CenterHorizontally,

                            modifier =
                                Modifier.padding(
                                    top = 12.dp,
                                    bottom = 12.dp
                                )
                        ) {

                            Surface(

                                shape =
                                    CircleShape,

                                color =
                                    VibrantPrimaryContainer,

                                modifier =
                                    Modifier.size(40.dp)
                            ) {

                                Box(
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Icon(

                                        imageVector =
                                            Icons.Default.CellTower,

                                        contentDescription =
                                            "FOSIS",

                                        tint =
                                            VibrantPrimary,

                                        modifier =
                                            Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    },

                    modifier =
                        Modifier.fillMaxHeight()

                ) {

                    navTabsList.forEach {
                            (tab, icon, label) ->

                        val isSelected =
                            uiState.currentTab == tab

                        NavigationRailItem(

                            selected =
                                isSelected,

                            onClick = {
                                viewModel.setNavTab(
                                    tab
                                )
                            },

                            icon = {

                                BadgedBox(

                                    badge = {

                                        if (
                                            tab ==
                                            FosisNavTab.PERIJINAN &&
                                            uiState.permits.isNotEmpty()
                                        ) {

                                            Badge(
                                                containerColor =
                                                    Color(0xFF166534)
                                            ) {

                                                Text(
                                                    text =
                                                        "${uiState.permits.size}"
                                                )
                                            }

                                        } else if (
                                            tab ==
                                            FosisNavTab.TROUBLESHOOT &&
                                            uiState.urgentAlert != null
                                        ) {

                                            Badge(
                                                containerColor =
                                                    VibrantError
                                            ) {

                                                Text(
                                                    text = "!"
                                                )
                                            }
                                        }
                                    }

                                ) {

                                    Icon(

                                        imageVector =
                                            icon,

                                        contentDescription =
                                            label,

                                        modifier =
                                            Modifier.size(20.dp)
                                    )
                                }
                            },

                            label = {

                                Text(

                                    text =
                                        label,

                                    fontSize =
                                        9.sp,

                                    fontWeight =
                                        if (isSelected)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Medium,

                                    maxLines =
                                        1
                                )
                            }
                        )
                    }
                }


                // =================================================================
                // TABLET CONTENT
                // =================================================================

                Scaffold(

                    topBar = {

                        FosisTopBar(

                            activeUser =
                                uiState.activeUser,

                            allUsers =
                                uiState.users,

                            isOnline =
                                uiState.isOnline,

                            pendingSyncCount =
                                uiState.pendingSyncCount,

                            runningJobsCount =
                                uiState.items.count {
                                    it.workflowStatus !=
                                            WorkflowStatus.APPROVED_DEPT_HEAD
                                },

                            onOpenRunningJobs = {
                                viewModel.setRunningJobsDialogOpen(
                                    true
                                )
                            },

                            onToggleNetwork = {
                                viewModel.toggleNetworkOnline()
                            },

                            onManualSync = {
                                viewModel.triggerSync()
                            },

                            onSwitchUser = {
                                viewModel.switchActiveUser(it)
                            },

                            onLogout = {
                                viewModel.logout()
                            },

                            onClearData = {
                                viewModel.clearAllOperationalData()
                            }
                        )
                    },

                    snackbarHost = {

                        SnackbarHost(
                            hostState =
                                snackbarHostState
                        )
                    },

                    modifier =
                        Modifier.weight(1f)

                ) { innerPadding ->

                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                    ) {

                        MainScreenContent(

                            uiState =
                                uiState,

                            viewModel =
                                viewModel,

                            onOpenAddModal = { type ->

                                addModalInitialType =
                                    type

                                showAddTypePicker =
                                    type == FosisType.ADMINISTRASI

                                isAddModalOpen =
                                    true
                            }
                        )
                    }
                }
            }
        }


        // ====================================================================
        // MOBILE
        // ====================================================================

        ModalNavigationDrawer(

            drawerState = drawerState,

            gesturesEnabled = true,

            drawerContent = {

                MobileNavigationDrawer(
                    activeUser = uiState.activeUser,
                    activeTab = uiState.currentTab,
                    menuTabs = drawerMenuTabs,

                    onSelectTab = { tab ->
                        viewModel.setNavTab(tab)
                        drawerScope.launch {
                            drawerState.close()
                        }
                    },

                    onClose = {
                        drawerScope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        ) {

            Scaffold(

                topBar = {

                    FosisTopBar(
                        activeUser = uiState.activeUser,
                        allUsers = uiState.users,
                        isOnline = uiState.isOnline,
                        pendingSyncCount = uiState.pendingSyncCount,

                        runningJobsCount = uiState.items.count {
                            it.workflowStatus != WorkflowStatus.APPROVED_DEPT_HEAD
                        },

                        onOpenRunningJobs = {
                            viewModel.setRunningJobsDialogOpen(true)
                        },

                        showMenuButton = true,

                        onOpenMenu = {
                            drawerScope.launch {
                                drawerState.open()
                            }
                        },

                        onToggleNetwork = {
                            viewModel.toggleNetworkOnline()
                        },

                        onManualSync = {
                            viewModel.triggerSync()
                        },

                        onSwitchUser = {
                            viewModel.switchActiveUser(it)
                        },

                        onLogout = {
                            viewModel.logout()
                        },

                        onClearData = {
                            viewModel.clearAllOperationalData()
                        }
                    )
                },

                bottomBar = {

                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, VibrantOutlineVariant)
                    ) {

                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 0.dp
                        ) {

                            mobileBottomNavTabs.forEach { (tab, icon, label) ->

                                val isSelected = uiState.currentTab == tab

                                NavigationBarItem(
                                    selected = isSelected,

                                    onClick = {
                                        viewModel.setNavTab(tab)
                                    },

                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (tab == FosisNavTab.TROUBLESHOOT && uiState.urgentAlert != null) {
                                                    Badge(containerColor = VibrantError) {
                                                        Text("!")
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },

                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    },

                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = VibrantPrimary,
                                        selectedTextColor = VibrantPrimary,
                                        indicatorColor = VibrantPrimaryContainer,
                                        unselectedIconColor = VibrantOnSurfaceVariant,
                                        unselectedTextColor = VibrantOnSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                },

                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },

                modifier = Modifier.fillMaxSize()

            ) { innerPadding ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {

                    MainScreenContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenAddModal = { type ->
                            addModalInitialType = type
                            showAddTypePicker = type == FosisType.ADMINISTRASI
                            isAddModalOpen = true
                        }
                    )
                }
            }
        }
    }


    // ========================================================================
    // TWO FACTOR DIALOG
    // ========================================================================

    TwoFactorDialog(

        isOpen =
            uiState.is2FADialogOpen,

        targetEmail =
            uiState.target2FAEmail,

        actionName =
            uiState.pending2FAAction,

        generatedCode =
            uiState.generated2FACode,

        autoConfirmationBanner =
            uiState.autoConfirmationBanner,

        onVerify = { code ->

            viewModel.verify2FACode(
                code
            )
        },

        onDismiss = {

            viewModel.dismiss2FADialog()
        }
    )


    // ========================================================================
    // DETAIL ITEM DIALOG
    // ========================================================================

    DetailItemDialog(

        item =
            uiState.selectedItem,

        activeUser =
            uiState.activeUser,

        onDismiss = {

            viewModel.selectItem(
                null
            )
        },

        onSubmitTeknisi = {

            viewModel.submitByTeknisi(
                it
            )
        },

        onVerifyLeader = {

            viewModel.verifyByLeader(
                it
            )
        },

        onToggleLockAdmin = {

            viewModel.toggleLock(
                it
            )
        },

        onApproveDeptHead = { item, comment ->

            viewModel.addDeptHeadCommentToTicket(
                item,
                comment
            )
        },


        onExportPdf = {

            viewModel.exportData(
                context,
                "TICKETS",
                "PDF"
            )
        }
    )


    // ========================================================================
    // ADD TICKET DIALOG
    // ========================================================================

    AddTicketDialog(

        isOpen =
            isAddModalOpen,

        initialType =
            addModalInitialType,

        startWithTypePicker =
            showAddTypePicker,

        onDismiss = {

            isAddModalOpen =
                false

            showAddTypePicker =
                false
        },

        onSubmit = {
                type,
                title,
                bulan,
                tahun,
                isUrgent,
                rootCause,
                impact,
                serviceImpact,
                slaStatus,
                crStatus,
                mitra,
                category,
                kegiatan,
                requestor,
                maintenanceStatus,
                taskType,
                tanggal,
                adminTask,
                subTask,
                subTask1,
                viaChannel,
                adminStatus,
                adminKategori,
                adminActivity,
                siteLocation,
                notes,
                materialUsed,
                otdrPdfAttachments,
                infraCategory,
                infraActivity ->

            viewModel.addNewItem(

                type =
                    type,

                title =
                    title,

                bulan =
                    bulan,

                tahun =
                    tahun,

                isUrgent =
                    isUrgent,

                rootCause =
                    rootCause,

                impact =
                    impact,

                serviceImpact =
                    serviceImpact,

                slaStatus =
                    slaStatus,

                crStatus =
                    crStatus,

                mitra =
                    mitra,

                category =
                    category,

                kegiatan =
                    kegiatan,

                requestor =
                    requestor,

                maintenanceStatus =
                    maintenanceStatus,

                taskType =
                    taskType,

                tanggal =
                    tanggal,

                adminTask =
                    adminTask,

                subTask =
                    subTask,

                subTask1 =
                    subTask1,

                viaChannel =
                    viaChannel,

                adminStatus =
                    adminStatus,

                adminKategori =
                    adminKategori,

                adminActivity =
                    adminActivity,

                siteLocation =
                    siteLocation,

                notes =
                    notes,

                materialUsed =
                    materialUsed,

                otdrPdfAttachments =
                    otdrPdfAttachments,

                infraCategory =
                    infraCategory,

                infraActivity =
                    infraActivity
            )
        }
    )


    // ========================================================================
    // RUNNING JOBS DIALOG
    // ========================================================================

    RunningJobsDialog(

        isOpen =
            uiState.showRunningJobsDialog,

        items =
            uiState.items,

        onDismiss = {

            viewModel.setRunningJobsDialogOpen(
                false
            )
        },

        onSelectItem = {

            viewModel.selectItem(
                it
            )
        }
    )
}


// ============================================================================
// MAIN SCREEN CONTENT
// ============================================================================

@Composable
fun MainScreenContent(

    uiState: FosisUiState,

    viewModel: FosisViewModel,

    onOpenAddModal: (FosisType) -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        // ====================================================================
        // URGENT ALERT
        // ====================================================================

        if (uiState.urgentAlert != null) {

            UrgentAlertBanner(

                urgentItem =
                    uiState.urgentAlert!!,

                onAccept = {

                    viewModel.dismissUrgentAlert()
                },

                onDismiss = {

                    viewModel.dismissUrgentAlert()
                },

                onViewDetail = {

                    viewModel.selectItem(
                        it
                    )
                }
            )
        }


        // ====================================================================
        // NAVIGATION CONTENT
        // ====================================================================

        when (uiState.currentTab) {


            // =================================================================
            // DASHBOARD
            // =================================================================

            FosisNavTab.DASHBOARD ->

                DashboardScreen(

                    uiState =
                        uiState,

                    viewModel =
                        viewModel,

                    onNavigateTab = {

                        viewModel.setNavTab(
                            it
                        )
                    },

                    onOpenAddModal = {

                        onOpenAddModal(
                            FosisType.TROUBLESHOOT
                        )
                    },

                    onSelectItem = {

                        viewModel.selectItem(
                            it
                        )
                    }
                )


            // =================================================================
            // TROUBLESHOOT
            // =================================================================

            FosisNavTab.TROUBLESHOOT ->

                TroubleshootScreen(

                    items =
                        uiState.items.filter {
                            it.type ==
                                    FosisType.TROUBLESHOOT
                        },

                    searchQuery =
                        uiState.searchQuery,

                    onSearchChange = {

                        viewModel.setSearchQuery(
                            it
                        )
                    },

                    selectedSla =
                        uiState.filterSla,

                    onSelectSla = {

                        viewModel.setFilterSla(
                            it
                        )
                    },

                    selectedRootCause =
                        uiState.filterRootCause,

                    onSelectRootCause = {

                        viewModel.setFilterRootCause(
                            it
                        )
                    },

                    onSelectItem = {

                        viewModel.selectItem(
                            it
                        )
                    },

                    onAddNew = {

                        onOpenAddModal(
                            FosisType.TROUBLESHOOT
                        )
                    }
                )


            // =================================================================
            // MAINTENANCE
            // =================================================================

            FosisNavTab.MAINTENANCE ->

                MaintenanceScreen(

                    items =
                        uiState.items.filter {
                            it.type ==
                                    FosisType.MAINTENANCE
                        },

                    searchQuery =
                        uiState.searchQuery,

                    onSearchChange = {

                        viewModel.setSearchQuery(
                            it
                        )
                    },

                    selectedKegiatan =
                        uiState.filterKegiatan,

                    onSelectKegiatan = {

                        viewModel.setFilterKegiatan(
                            it
                        )
                    },

                    selectedStatus =
                        uiState.filterStatus,

                    onSelectStatus = {

                        viewModel.setFilterStatus(
                            it
                        )
                    },

                    onSelectItem = {

                        viewModel.selectItem(
                            it
                        )
                    },

                    onAddNew = {

                        onOpenAddModal(
                            FosisType.MAINTENANCE
                        )
                    }
                )


            // =================================================================
            // PERIJINAN
            // =================================================================

            FosisNavTab.PERIJINAN ->

                PermitScreen(

                    uiState =
                        uiState,

                    viewModel =
                        viewModel
                )


            // =================================================================
            // ADMINISTRASI
            // =================================================================

            FosisNavTab.ADMINISTRASI ->

                AdminScreen(

                    items =
                        uiState.items.filter {
                            it.type ==
                                    FosisType.ADMINISTRASI
                        },

                    onSelectItem = {

                        viewModel.selectItem(
                            it
                        )
                    },

                    onAddNew = {

                        onOpenAddModal(
                            FosisType.ADMINISTRASI
                        )
                    }
                )


            // =================================================================
            // WORKFLOW
            // =================================================================

            FosisNavTab.WORKFLOW ->

                WorkflowVerificationScreen(

                    items =
                        uiState.items,

                    activeUser =
                        uiState.activeUser,

                    onSelectItem = {

                        viewModel.selectItem(
                            it
                        )
                    },

                    onSubmitTeknisi = {

                        viewModel.submitByTeknisi(
                            it
                        )
                    },

                    onVerifyLeader = {

                        viewModel.verifyByLeader(
                            it
                        )
                    },

                    onToggleLockAdmin = {

                        viewModel.toggleLock(
                            it
                        )
                    },

                    onApproveDeptHead = { item, comment ->

                        viewModel.addDeptHeadCommentToTicket(
                            item,
                            comment
                        )
                    }
                )


            // =================================================================
            // GEOLOCATION
            // =================================================================

            FosisNavTab.GEOLOKASI ->

                GeolocationMapView(

                    items =
                        uiState.items,

                    activeUser =
                        uiState.activeUser,

                    userLatitude =
                        uiState.userLatitude,

                    userLongitude =
                        uiState.userLongitude,

                    userAddress =
                        uiState.userLocationAddress,

                    vehicleLogs =
                        uiState.vehicleLogs,

                    onAddVehicleLog = { tanggal, pic, team, timeOut, ticket, plat, type, tujuan, odoOut, fuel, catatan, lat, lng ->
                        viewModel.addVehicleLog(
                            tanggal,
                            pic,
                            team,
                            timeOut,
                            ticket,
                            plat,
                            type,
                            tujuan,
                            odoOut,
                            fuel,
                            catatan,
                            lat,
                            lng
                        )
                    },

                    onSubmitReturn = { log, tanggalKembali, timeIn, odoIn, fuelIn, catatanKembali ->
                        viewModel.submitReturnVehicle(
                            log,
                            tanggalKembali,
                            timeIn,
                            odoIn,
                            fuelIn,
                            catatanKembali
                        )
                    },

                    onApproveReturn = { log, accBy, accRole, accNotes ->
                        viewModel.approveVehicleReturn(
                            log,
                            accBy,
                            accRole,
                            accNotes
                        )
                    },

                    onUpdateVehicleLog = {
                        viewModel.updateVehicleLog(it)
                    },

                    onDeleteVehicleLog = {
                        viewModel.deleteVehicleLog(it)
                    },

                    onReloadSimulatedData = {
                        viewModel.reloadSimulatedVehicleLogs()
                    },

                    onSelectItem = {
                        viewModel.selectItem(it)
                    },

                    onSimulateLocation = {
                            lat,
                            lng,
                            addr ->

                        viewModel.updateLocation(
                            lat,
                            lng,
                            addr
                        )
                    }
                )


            // =================================================================
            // FIELD CHAT
            // =================================================================

            FosisNavTab.FIELD_CHAT ->

                FieldChatScreen(

                    messages =
                        uiState.chatMessages,

                    activeUser =
                        uiState.activeUser,

                    userLatitude =
                        uiState.userLatitude,

                    userLongitude =
                        uiState.userLongitude,

                    userAddress =
                        uiState.userLocationAddress,

                    items =
                        uiState.items,

                    permits =
                        uiState.permits,

                    onSendMessage = {
                            text,
                            type,
                            formId,
                            formTitle,
                            formType,
                            formStatus ->

                        viewModel.sendChatMessage(

                            text,

                            type,

                            formId,

                            formTitle,

                            formType,

                            formStatus
                        )
                    },

                    onSelectItem = {

                        viewModel.selectItem(
                            it
                        )
                    }
                )


            // =================================================================
            // ADMIN USERS
            // =================================================================

            FosisNavTab.ADMIN_USERS ->

                UserManagementScreen(

                    activeUser =
                        uiState.activeUser,

                    users =
                        uiState.users,

                    onAddUser = {
                            name,
                            role,
                            teamName,
                            phone,
                            password ->

                        viewModel.addNewUser(

                            name,

                            role,

                            teamName,

                            phone,

                            password
                        )
                    },

                    onUpdateUser = { user ->

                        viewModel.updateUserPermissions(
                            user
                        )
                    },

                    onDeleteUser = { user ->

                        viewModel.deleteUser(
                            user
                        )
                    }
                )
        }
    }
}