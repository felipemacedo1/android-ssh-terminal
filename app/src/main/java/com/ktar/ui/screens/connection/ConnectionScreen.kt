package com.ktar.ui.screens.connection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktar.data.model.AuthMethod
import com.ktar.ui.components.ErrorDialog
import com.ktar.ui.components.LoadingDialog

/**
 * Connection screen for creating/editing SSH connections.
 *
 * @param viewModel ViewModel instance created with factory
 * @param hostId Optional host ID for editing existing connection
 * @param onNavigateBack Callback when user navigates back
 * @param onNavigateToTerminal Callback when connection is successful
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel,
    hostId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToTerminal: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(hostId) {
        hostId?.let { viewModel.loadHost(it) }
    }

    // Handle navigation to terminal on successful connection
    LaunchedEffect(uiState.connectionSuccessful) {
        if (uiState.connectionSuccessful && uiState.sessionId != null) {
            onNavigateToTerminal(uiState.sessionId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (hostId == null) "Nova Conexão" else "Editar Conexão") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Host name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Nome da Conexão") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )

            // Host address
            OutlinedTextField(
                value = uiState.host,
                onValueChange = { viewModel.updateHost(it) },
                label = { Text("Host") },
                placeholder = { Text("exemplo.com ou 192.168.1.100") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.hostError != null,
                supportingText = uiState.hostError?.let { { Text(it) } }
            )

            // Port
            OutlinedTextField(
                value = uiState.port,
                onValueChange = { viewModel.updatePort(it) },
                label = { Text("Porta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.portError != null,
                supportingText = uiState.portError?.let { { Text(it) } }
            )

            // Username
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Usuário") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.usernameError != null,
                supportingText = uiState.usernameError?.let { { Text(it) } }
            )

            // Auth method selection
            Text(
                text = "Método de Autenticação",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.authMethod == AuthMethod.PASSWORD,
                    onClick = { viewModel.updateAuthMethod(AuthMethod.PASSWORD) },
                    label = { Text("Senha") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.authMethod == AuthMethod.PRIVATE_KEY,
                    onClick = { viewModel.updateAuthMethod(AuthMethod.PRIVATE_KEY) },
                    label = { Text("Chave Pública") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Password field (if password auth)
            if (uiState.authMethod == AuthMethod.PASSWORD) {
                var passwordVisible by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Senha") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) 
                                    Icons.Default.Visibility 
                                else 
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) 
                                    "Ocultar senha" 
                                else 
                                    "Mostrar senha"
                            )
                        }
                    },
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError?.let { { Text(it) } }
                )
            }

            // Private key field (if key auth)
            if (uiState.authMethod == AuthMethod.PRIVATE_KEY) {
                OutlinedTextField(
                    value = uiState.privateKey,
                    onValueChange = { viewModel.updatePrivateKey(it) },
                    label = { Text("Chave Privada") },
                    placeholder = { Text("Cole aqui o conteúdo da chave privada (PEM)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10,
                    isError = uiState.privateKeyError != null,
                    supportingText = uiState.privateKeyError?.let { { Text(it) } }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Connect button
            Button(
                onClick = { viewModel.connect(onNavigateToTerminal) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isConnecting
            ) {
                Text(if (uiState.isConnecting) "Conectando..." else "Conectar")
            }

            // Save connection button (without connecting)
            if (hostId == null) {
                OutlinedButton(
                    onClick = { viewModel.saveHost(onSuccess = onNavigateBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isConnecting
                ) {
                    Text("Salvar Sem Conectar")
                }
            }
        }
    }

    // Loading dialog
    if (uiState.isConnecting) {
        LoadingDialog(message = "Conectando ao servidor...")
    }

    // Error dialog
    uiState.errorMessage?.let { error ->
        ErrorDialog(
            title = "Erro de Conexão",
            message = error,
            onDismiss = { viewModel.clearError() }
        )
    }
}
