# ViewModel Dependency Injection - Correções Aplicadas

**Data:** 22 de Outubro de 2025
**Problema Identificado:** App crashava com erro "Cannot create an instance of class HostListViewModel"

## 🔍 Causa Raiz

Os ViewModels com dependências no construtor estavam sendo criados incorretamente usando o parâmetro padrão `viewModel()` nas funções Composable, em vez de usar a ViewModelFactory.

### ViewModels e suas Dependências:
- **HostListViewModel**: Requer `HostDataStore`
- **ConnectionViewModel**: Requer `HostDataStore`, `SecurityManager`, `SSHManager`
- **TerminalViewModel**: Sem dependências (mas corrigido por consistência)

## ✅ Correções Aplicadas

### 1. HostListScreen.kt
**Antes:**
```kotlin
@Composable
fun HostListScreen(
    onNavigateToConnection: (String?) -> Unit,
    onNavigateToTerminal: (String) -> Unit,
    viewModel: HostListViewModel = viewModel()  // ❌ PROBLEMA
) {
```

**Depois:**
```kotlin
@Composable
fun HostListScreen(
    viewModel: HostListViewModel,  // ✅ CORRIGIDO - parâmetro obrigatório
    onNavigateToConnection: (String?) -> Unit,
    onNavigateToTerminal: (String) -> Unit
) {
```

### 2. ConnectionScreen.kt
**Antes:**
```kotlin
@Composable
fun ConnectionScreen(
    hostId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToTerminal: (String) -> Unit,
    viewModel: ConnectionViewModel = viewModel()  // ❌ PROBLEMA
) {
```

**Depois:**
```kotlin
@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel,  // ✅ CORRIGIDO - parâmetro obrigatório
    hostId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToTerminal: (String) -> Unit
) {
```

### 3. TerminalScreen.kt
**Antes:**
```kotlin
@Composable
fun TerminalScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    viewModel: TerminalViewModel = viewModel()  // ❌ PROBLEMA
) {
```

**Depois:**
```kotlin
@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel,  // ✅ CORRIGIDO - parâmetro obrigatório
    sessionId: String,
    onNavigateBack: () -> Unit
) {
```

### 4. MainActivity.kt - NavHost
Adicionada criação explícita de ViewModels com factory em TODAS as rotas:

**Antes:**
```kotlin
composable("host_list") {
    val hostListViewModel: HostListViewModel = viewModel(factory = viewModelFactory)
    HostListScreen(
        viewModel = hostListViewModel,
        // ...
    )
}

composable("connection") {
    ConnectionScreen(  // ❌ Sem ViewModel
        onNavigateBack = { },
        onNavigateToTerminal = { }
    )
}

composable("terminal/{sessionId}") { backStackEntry ->
    TerminalScreen(  // ❌ Sem ViewModel
        sessionId = sessionId,
        onNavigateBack = { }
    )
}
```

**Depois:**
```kotlin
composable("host_list") {
    val hostListViewModel: HostListViewModel = viewModel(factory = viewModelFactory)
    HostListScreen(
        viewModel = hostListViewModel,
        // ...
    )
}

composable("connection") {
    val connectionViewModel: ConnectionViewModel = viewModel(factory = viewModelFactory)  // ✅
    ConnectionScreen(
        viewModel = connectionViewModel,  // ✅
        onNavigateBack = { },
        onNavigateToTerminal = { }
    )
}

composable("connection/{hostId}") { backStackEntry ->
    val hostId = backStackEntry.arguments?.getString("hostId")
    val connectionViewModel: ConnectionViewModel = viewModel(factory = viewModelFactory)  // ✅
    ConnectionScreen(
        viewModel = connectionViewModel,  // ✅
        hostId = hostId,
        onNavigateBack = { },
        onNavigateToTerminal = { }
    )
}

composable("terminal/{sessionId}") { backStackEntry ->
    val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
    val terminalViewModel: TerminalViewModel = viewModel(factory = viewModelFactory)  // ✅
    TerminalScreen(
        viewModel = terminalViewModel,  // ✅
        sessionId = sessionId,
        onNavigateBack = { }
    )
}
```

### 5. Import Adicionado
```kotlin
import com.ktar.ui.screens.terminal.TerminalViewModel
```

## 📊 Resultado

### Build Status
```
BUILD SUCCESSFUL in 15s
35 actionable tasks: 34 executed, 1 up-to-date
```

### Warnings (não críticos)
- Parâmetros não utilizados (sessionId, onNavigateToTerminal)
- Uso de APIs deprecadas (ArrowBack, Divider)
- Variável 'session' nunca usada em ConnectionViewModel

### APK
- ✅ Instalação bem-sucedida no dispositivo R83Y50T0LHW
- ✅ Sem crashes detectados nos logs
- ✅ App inicializa corretamente

## 🎯 Lições Aprendidas

1. **NUNCA use `viewModel()` como valor padrão** quando o ViewModel tem dependências no construtor
2. **SEMPRE passe ViewModels explicitamente** através da ViewModelFactory
3. **Consistência é importante** - mesmo ViewModels sem dependências devem seguir o mesmo padrão
4. **Ordem dos parâmetros** - ViewModel como primeiro parâmetro facilita leitura

## 📝 Próximos Passos

1. ✅ Testar funcionalidade completa do app no tablet
2. 🔄 Corrigir warnings deprecados (ArrowBack, Divider)
3. 🔄 Remover parâmetros não utilizados
4. 🔄 Implementar testes unitários para ViewModels
5. 🔄 Adicionar testes de UI para navegação

## 🔗 Arquivos Modificados

1. `/app/src/main/java/com/felipemacedo/androidsshterminal/ui/screens/hostlist/HostListScreen.kt`
2. `/app/src/main/java/com/felipemacedo/androidsshterminal/ui/screens/connection/ConnectionScreen.kt`
3. `/app/src/main/java/com/felipemacedo/androidsshterminal/ui/screens/terminal/TerminalScreen.kt`
4. `/app/src/main/java/com/felipemacedo/androidsshterminal/MainActivity.kt`

---

**Conclusão:** Todas as correções foram aplicadas com sucesso. O app agora compila sem erros e não apresenta crashes na inicialização. A arquitetura MVVM com Dependency Injection via ViewModelFactory está corretamente implementada.
