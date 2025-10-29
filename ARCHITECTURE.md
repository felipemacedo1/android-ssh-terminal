# KTAR Architecture

This document describes the architecture and design patterns used in KTAR.

## Architecture Overview

KTAR follows **MVVM (Model-View-ViewModel) + Clean Architecture** pattern:

```
┌─────────────────────────────────────────────┐
│           UI Layer (Compose)                │
│    • Screens, Components                    │
│    • ViewModels (State Management)          │
└────────────────────┬────────────────────────┘
                     │
┌─────────────────────────────────────────────┐
│          Domain Layer                       │
│    • Use Cases / Interactors                │
│    • Repository Interfaces                  │
│    • Business Models                        │
└────────────────────┬────────────────────────┘
                     │
┌─────────────────────────────────────────────┐
│          Data Layer                         │
│    • Repositories (Implementation)          │
│    • Local Data Sources (DataStore)         │
│    • Remote Data Sources (SSHJ)             │
└─────────────────────────────────────────────┘
```

## Layer Responsibilities

### UI Layer (`ui/`)

- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Design system with dark/light themes
- **ViewModels**: State management per screen
- **Navigation**: Compose Navigation
- **Screens**: Reusable composable screens

**Key Components**:
```
ui/
├── screens/
│   ├── terminal/          # Terminal emulator screen
│   ├── sftp/              # File transfer screen
│   ├── connections/       # Connection management
│   └── settings/          # Application settings
├── components/            # Reusable UI components
└── theme/                 # Material 3 theme
```

### Domain Layer (`domain/`)

- **Use Cases**: Encapsulate business logic
- **Repository Interfaces**: Data abstraction
- **Models**: Pure data classes (no Android dependencies)
- **Exceptions**: Domain-specific exceptions

**Key Components**:
```
domain/
├── usecase/
│   ├── CreateConnectionUseCase
│   ├── ExecuteCommandUseCase
│   ├── TransferFileUseCase
│   └── ...
├── repository/            # Repository interfaces
└── model/                 # Domain models
```

### Data Layer (`data/`)

- **Repository Implementations**: Concrete repository logic
- **Data Sources**: Local (DataStore) and remote (SSH)
- **Mappers**: Convert between models

**Key Components**:
```
data/
├── repository/
├── datasource/
│   ├── local/             # DataStore encrypted preferences
│   └── remote/            # SSHJ SSH client
└── mapper/                # Model conversions
```

## Key Technologies

### SSH Client: SSHJ

```kotlin
SSHClient().use { ssh ->
    ssh.addHostKeyVerifier(PromiscuousHostKeyVerifier())
    ssh.connect(host, port)
    ssh.authPublickey(username, provider)
    ssh.startSession().use { session ->
        val cmd = session.exec("ls -la")
        println(IOUtils.readFully(cmd.getInputStream()).toString())
    }
}
```

### State Management: ViewModel + Coroutines

```kotlin
@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val executeCommandUseCase: ExecuteCommandUseCase
) : ViewModel() {
    private val _terminalState = MutableStateFlow<TerminalState>(TerminalState.Idle)
    val terminalState = _terminalState.asStateFlow()
    
    fun executeCommand(cmd: String) = viewModelScope.launch {
        _terminalState.value = TerminalState.Loading
        _terminalState.value = executeCommandUseCase(cmd)
    }
}
```

### Secure Storage: Android Keystore + DataStore

```kotlin
// Credentials encrypted with Android Keystore (AES-GCM)
private val encryptedPreferences = EncryptedSharedPreferences.create(...)

// DataStore with proto-based encrypted serialization
val settingsDataStore: DataStore<Settings> = createDataStore(...)
```

## Data Flow

### Example: Execute SSH Command

```
1. User input (Terminal Screen)
   ↓
2. ViewModel.executeCommand(cmd)
   ↓
3. ExecuteCommandUseCase
   ↓
4. SSHRepository.executeCommand()
   ↓
5. SSHDataSource (SSHJ)
   ↓
6. SSH Server response
   ↓
7. Update TerminalState
   ↓
8. Recompose UI with result
```

## ViewModel State Pattern

```kotlin
sealed class TerminalState {
    object Idle : TerminalState()
    object Loading : TerminalState()
    data class Success(val output: String) : TerminalState()
    data class Error(val message: String) : TerminalState()
}
```

## Dependency Injection: Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SSHModule {
    
    @Provides
    @Singleton
    fun provideSSHRepository(
        dataSource: SSHDataSource
    ): SSHRepository = SSHRepositoryImpl(dataSource)
}
```

## Threading Model

- **Main Thread**: UI composition, user interactions
- **IO Thread**: SSH operations, file I/O
- **Default Thread**: Computation-heavy operations
- **Coroutines Dispatchers**:
  ```kotlin
  viewModelScope.launch(Dispatchers.IO) { ... }
  viewModelScope.launch(Dispatchers.Main) { ... }
  ```

## Error Handling

```kotlin
// Domain exceptions
class SSHConnectionException(msg: String) : Exception(msg)
class AuthenticationException(msg: String) : Exception(msg)

// UI state reflects errors
sealed class TerminalState {
    data class Error(val exception: Exception) : TerminalState()
}
```

## Testing Strategy

```
src/
├── test/                          # Unit tests
│   ├── domain/usecase/
│   └── data/repository/
└── androidTest/                   # Instrumentation tests
    └── ui/screens/
```

- **Unit Tests**: Business logic, use cases (no Android dependencies)
- **Integration Tests**: Repository + data sources
- **UI Tests**: Compose screen interactions

## Performance Considerations

### PTY (Pseudo-Terminal) Support

- Real terminal state management
- Streaming output for large commands
- Memory-efficient buffer handling

### SSH Connection Pooling

- Reuse SSH connections when possible
- Proper resource cleanup
- Timeout management

### UI Responsiveness

- Heavy operations on IO thread
- Smooth scrolling in terminal
- Lazy loading for large file lists

## Security Architecture

### Credential Handling

1. User enters credentials
2. Encrypted with Android Keystore
3. Stored in encrypted DataStore
4. Never kept in memory longer than needed
5. Cleared on logout

### SSH Implementation

- SSHJ library handles SSH protocol security
- Host key verification (TOFU - Trust On First Use)
- Support for modern key algorithms (ED25519, RSA)

## Future Improvements

- [ ] Connection multiplexing
- [ ] Advanced terminal emulation
- [ ] Local script execution
- [ ] Batch operations
- [ ] Terminal session recording
- [ ] Advanced auth methods (2FA, certificates)

---

For implementation details, see the source code with inline documentation.
