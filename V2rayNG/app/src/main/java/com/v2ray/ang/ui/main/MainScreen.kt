package com.v2ray.ang.ui.main

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.QRCodeDialog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.os.SystemClock
import java.util.Locale

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groups = uiState.groups
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val isRunning = uiState.isRunning
    val displayText = mainViewModel.formatStatus(uiState.status)
    val selectedGuid = uiState.selectedGuid
    val doubleColumnDisplay = uiState.doubleColumnDisplay
    val confirmRemove = uiState.confirmRemove
    val shareQRCodeBitmap = uiState.shareQRCodeBitmap

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by rememberSaveable(stateSaver = ServerDeleteTarget.Saver) {
        mutableStateOf<ServerDeleteTarget?>(null)
    }

    var shareTarget by remember { mutableStateOf<Triple<String, ProfileItem, Boolean>?>(null) }
    val removeServer: (String, String) -> Unit = { guid, profileName ->
        if (confirmRemove) {
            showRemoveConfirm = ServerDeleteTarget(guid, profileName)
        } else {
            onAction(MainAction.RemoveServer(guid))
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { groups.size.coerceAtLeast(1) }
    )

    val lazyListStates = remember { mutableStateMapOf<String, LazyListState>() }
    val lazyGridStates = remember { mutableStateMapOf<String, LazyGridState>() }

    LaunchedEffect(groups) {
        val validGroupIds = groups.map { it.id }.toSet()
        lazyListStates.keys.retainAll(validGroupIds)
        lazyGridStates.keys.retainAll(validGroupIds)
    }

    LaunchedEffect(groups, uiState.selectedGroupId) {
        if (groups.isEmpty()) return@LaunchedEffect
        val selectedIndex = groups.indexOfFirst { it.id == uiState.selectedGroupId }
            .takeIf { it >= 0 } ?: 0
        if (!pagerState.isScrollInProgress && pagerState.settledPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    val latestGroups by rememberUpdatedState(groups)
    // Height of the floating connection bar, so the server lists can scroll clear of it.
    var bottomBarHeightPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val currentGroups = latestGroups
                if (page in currentGroups.indices) {
                    onAction(MainAction.SelectGroup(currentGroups[page].id))
                }
            }
    }

    var confirmClipboardImport by rememberSaveable { mutableStateOf(false) }
    if (confirmClipboardImport) {
        AlertDialog(
            onDismissRequest = { confirmClipboardImport = false },
            title = { Text(stringResource(R.string.zaizy_confirm_paste)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClipboardImport = false
                    onAction(MainAction.ImportClipboard)
                }) {
                    Text(stringResource(R.string.zaizy_paste))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClipboardImport = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    MainDialogs(
        showDelAllConfirm = showDelAllConfirm,
        onDismissDelAll = { showDelAllConfirm = false },
        onConfirmDelAll = { showDelAllConfirm = false; onAction(MainAction.RemoveAllServers) },
        showDelDuplicateConfirm = showDelDuplicateConfirm,
        onDismissDelDuplicate = { showDelDuplicateConfirm = false },
        onConfirmDelDuplicate = { showDelDuplicateConfirm = false; onAction(MainAction.RemoveDuplicateServers) },
        showDelInvalidConfirm = showDelInvalidConfirm,
        onDismissDelInvalid = { showDelInvalidConfirm = false },
        onConfirmDelInvalid = { showDelInvalidConfirm = false; onAction(MainAction.RemoveInvalidServers) },
        showRemoveConfirm = showRemoveConfirm,
        onDismissRemove = { showRemoveConfirm = null },
        onConfirmRemove = { guid -> showRemoveConfirm = null; onAction(MainAction.RemoveServer(guid)) }
    )

    if (shareTarget != null) {
        val (guid, profile, more) = shareTarget!!
        ShareMethodDialog(
            guid = guid,
            profile = profile,
            more = more,
            onDismiss = { shareTarget = null },
            onAction = onAction,
            onRemove = removeServer,
        )
    }
    if (shareQRCodeBitmap != null) {
        QRCodeDialog(bitmap = shareQRCodeBitmap, onDismiss = { onAction(MainAction.DismissQRCodeDialog) })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerContent(
                drawerState = drawerState,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                }
            )
        }
    ) {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            topBar = {
                MainTopBar(
                    isLoading = isLoading,
                    showSearch = showSearch,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query: String ->
                        searchQuery = query
                        onAction(MainAction.Search(query))
                    },
                    onSearchClose = {
                        searchQuery = ""
                        onAction(MainAction.Search(""))
                        showSearch = false
                    },
                    onSearchToggle = { show: Boolean -> showSearch = show },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onAction = onAction,
                    onMoreMenuAction = { action ->
                        when (action) {
                            MainMoreMenuAction.RestartService -> onAction(MainAction.RestartService)
                            MainMoreMenuAction.DeleteAll -> showDelAllConfirm = true
                            MainMoreMenuAction.DeleteDuplicate -> showDelDuplicateConfirm = true
                            MainMoreMenuAction.DeleteInvalid -> showDelInvalidConfirm = true
                            MainMoreMenuAction.ExportAll -> onAction(MainAction.ExportAll)
                            MainMoreMenuAction.LocateSelected -> onAction(MainAction.LocateSelectedServer)
                            MainMoreMenuAction.SortByTestResults -> onAction(MainAction.SortByTestResults)
                            MainMoreMenuAction.TestAll -> onAction(MainAction.TestAllServers)
                            MainMoreMenuAction.TestAllRealPing -> onAction(MainAction.TestRealAllServers)
                            MainMoreMenuAction.UpdateSubscriptions -> onAction(MainAction.UpdateSubscriptions)
                        }
                    }
                )
            },
            floatingActionButton = {},
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val bottomBarHeight = with(LocalDensity.current) { bottomBarHeightPx.toDp() }

            Box(Modifier.fillMaxSize()) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(com.v2ray.ang.R.drawable.zaizy_city),
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            if (groups.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        // No bottom padding: the lists run behind the floating connection bar.
                        .padding(
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            top = innerPadding.calculateTopPadding(),
                            end = innerPadding.calculateEndPadding(layoutDirection)
                        )
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VpnShield(
                            isRunning = isRunning,
                            onClick = { onAction(MainAction.ToggleService) },
                            modifier = Modifier.size(
                                if (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 630) 142.dp else 204.dp
                            )
                        )
                        Text(
                            text = if (uiState.status == MainStatus.Connected) {
                                connectedStatusText(uiState.sessionStartedAt)
                            } else {
                                displayText
                            },
                            style = MaterialTheme.typography.bodyLarge.merge(ZaizyBrandTextStyle),
                            modifier = Modifier.background(androidx.compose.ui.graphics.Color(0xD9121932),
                                androidx.compose.foundation.shape.CircleShape).padding(horizontal = 12.dp, vertical = 3.dp))
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                        beyondViewportPageCount = 1,
                        key = { page -> groups.getOrNull(page)?.id ?: "group-page-$page" }
                    ) { page ->
                        val group = groups.getOrNull(page) ?: return@HorizontalPager

                        GroupPagerPage(
                            groupId = group.id,
                            mainViewModel = mainViewModel,
                            selectedGuid = selectedGuid,
                            locateTarget = uiState.locateTarget,
                            doubleColumnDisplay = doubleColumnDisplay,
                            searchQuery = searchQuery,
                            lazyListStates = lazyListStates,
                            lazyGridStates = lazyGridStates,
                            onSelectServer = { guid -> onAction(MainAction.SelectServer(guid)) },
                            onEditServer = { guid, profile -> onAction(MainAction.EditServer(guid, profile)) },
                            onShareServer = { guid, profile ->
                                shareTarget = Triple(guid, profile, false)
                            },
                            onMoreServer = { guid, profile ->
                                shareTarget = Triple(guid, profile, true)
                            },
                            onRemoveServer = removeServer,
                            usageInfo = group.usageInfo,
                            groupTitle = group.remarks,
                            onOpenLink = { url -> onAction(MainAction.OpenSubscriptionLink(url)) },
                            contentPadding = PaddingValues(
                                start = 0.dp,
                                top = 0.dp,
                                end = 0.dp,
                                bottom = bottomBarHeight + 8.dp
                            )
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .onSizeChanged { bottomBarHeightPx = it.height },
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val colors = ButtonDefaults.buttonColors(
                    containerColor = SubscriptionMint.copy(alpha = 0.34f),
                    contentColor = Color.White
                )
                val border = BorderStroke(0.5.dp, SubscriptionMint.copy(alpha = 0.70f))
                Button(
                    onClick = { confirmClipboardImport = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = colors,
                    border = border
                ) { Text("Из буфера") }
                Button(
                    onClick = { onAction(MainAction.ImportQRcode) },
                    shape = MaterialTheme.shapes.medium,
                    colors = colors,
                    border = border
                ) { Text("QR-код") }
            }
            }
        }
    }
}

@Composable
private fun connectedStatusText(startedAt: Long?): String {
    val label = androidx.compose.ui.res.stringResource(com.v2ray.ang.R.string.zaizy_connection_connected)
    if (startedAt == null) return label

    var now by remember(startedAt) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            val elapsedMillis = (now - startedAt).coerceAtLeast(0L)
            delay(1000L - elapsedMillis % 1000L)
        }
    }

    val seconds = (now - startedAt).coerceAtLeast(0L) / 1000L
    val duration = String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
    return "$label · $duration"
}