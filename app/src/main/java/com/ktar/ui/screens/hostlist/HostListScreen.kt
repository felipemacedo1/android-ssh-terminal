package com.ktar.ui.screens.hostlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktar.R
import com.ktar.data.model.Host
import com.ktar.ui.components.ConfirmDialog
import com.ktar.ui.components.ErrorDialog
import com.ktar.ui.components.HostCard
import com.ktar.ui.components.KTARHeader

/**
 * Host list screen wrapper for navigation.
 */
@Composable
fun HostListScreen(
    viewModel: HostListViewModel,
    onNavigateToConnection: (String?) -> Unit,
    onNavigateToTerminal: (String) -> Unit
) {
    HostListScreenContent(
        viewModel = viewModel,
        onHostClick = { host ->
            // For now, navigate to connection screen with host ID
            onNavigateToConnection(host.id)
        },
        onAddHost = {
            onNavigateToConnection(null)
        },
        onEditHost = { host ->
            onNavigateToConnection(host.id)
        }
    )
}

/**
 * Host list screen displaying all saved SSH connections.
 *
 * @param viewModel ViewModel for managing hosts
 * @param onHostClick Callback when a host is clicked to connect
 * @param onAddHost Callback to navigate to add host screen
 * @param onEditHost Callback to navigate to edit host screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostListScreenContent(
    viewModel: HostListViewModel,
    onHostClick: (Host) -> Unit,
    onAddHost: () -> Unit,
    onEditHost: (Host) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var hostToDelete by remember { mutableStateOf<Host?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.host_list_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            // Version info at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "v${com.ktar.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHost,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.host_list_add_connection)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HostListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HostListUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        KTARHeader()
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.host_list_empty),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onAddHost) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.host_list_add_connection))
                            }
                        }
                    }
                }

                is HostListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            KTARHeader()
                        }
                        items(state.hosts, key = { it.id }) { host ->
                            HostCard(
                                host = host,
                                onClick = {
                                    viewModel.updateLastUsed(host.id)
                                    onHostClick(host)
                                },
                                onEdit = { onEditHost(host) },
                                onDelete = { hostToDelete = host },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                is HostListUiState.Error -> {
                    errorMessage = state.message
                }
            }
        }
    }

    // Delete confirmation dialog
    hostToDelete?.let { host ->
        ConfirmDialog(
            title = stringResource(R.string.delete),
            message = "Delete connection to ${host.name}?",
            onConfirm = {
                viewModel.deleteHost(host.id)
                hostToDelete = null
            },
            onDismiss = { hostToDelete = null }
        )
    }

    // Error dialog
    errorMessage?.let { message ->
        ErrorDialog(
            title = "Error",
            message = message,
            onDismiss = { errorMessage = null }
        )
    }
}
