# 📱 Guia de Verificação de Versão - KTAR

## 🎯 Como Saber qual Versão está no Tablet

### ✅ Método 1: Visualização Direta no App (MAIS FÁCIL)

**Abra o app no tablet e olhe na parte inferior da tela principal!**

Você verá um texto pequeno mostrando: `v1.0.1-viewmodel-fix`

### 📱 Método 2: Via ADB (Rápido)

```bash
# Verificar versão instalada
adb shell dumpsys package com.ktar | grep versionName

# Verificar código da versão
adb shell dumpsys package com.ktar | grep versionCode
```

### 🔍 Método 3: Verificar Timestamp de Instalação

```bash
# Quando o APK foi instalado pela última vez
adb shell dumpsys package com.ktar | grep -E "firstInstallTime|lastUpdateTime"
```

### 📦 Método 4: Comparar com APK Local

```bash
# Verificar versão do APK local (compilado)
./gradlew app:dependencies | head -20

# Ou verificar no build.gradle.kts
cat app/build.gradle.kts | grep -A 2 "versionName"
```

### 🔄 Método 5: Forçar Atualização e Verificar

```bash
# Desinstalar versão antiga
adb uninstall com.ktar

# Instalar versão nova
adb install app/build/outputs/apk/debug/app-debug.apk

# Verificar versão instalada
adb shell dumpsys package com.ktar | grep versionName
```

## 📊 Histórico de Versões

| Versão | Código | Descrição | Data |
|--------|--------|-----------|------|
| 1.0.0 | 1 | Versão inicial com Java 21 | 22/10/2025 |
| 1.0.1-viewmodel-fix | 2 | Correção de ViewModels com DI | 22/10/2025 |

## 🎨 Mudanças na v1.0.1

### Correções Aplicadas:
- ✅ Corrigido crash "Cannot create an instance of class HostListViewModel"
- ✅ Implementado ViewModelFactory corretamente
- ✅ Todas as telas agora recebem ViewModels via Dependency Injection
- ✅ Build bem-sucedido sem erros
- ✅ Informação de versão visível na tela principal

### Arquivos Modificados:
- `app/build.gradle.kts` - Versão incrementada + BuildConfig habilitado
- `HostListScreen.kt` - ViewModel como parâmetro obrigatório + versão no rodapé
- `ConnectionScreen.kt` - ViewModel como parâmetro obrigatório
- `TerminalScreen.kt` - ViewModel como parâmetro obrigatório
- `MainActivity.kt` - ViewModelFactory em todas as rotas

## 🚀 Próximas Versões Planejadas

### v1.0.2 (Melhorias de UI)
- [ ] Corrigir warnings de APIs deprecadas
- [ ] Melhorar tela "Sobre" com mais informações
- [ ] Adicionar tema escuro/claro

### v1.1.0 (Funcionalidades)
- [ ] Suporte a chaves SSH
- [ ] Histórico de comandos
- [ ] Auto-completar comandos

### v2.0.0 (Major Update)
- [ ] Múltiplas sessões SSH simultâneas
- [ ] Sincronização em nuvem
- [ ] Compartilhamento de configurações

## 🔧 Como Incrementar Versão para Próxima Release

1. **Editar `app/build.gradle.kts`:**
   ```kotlin
   versionCode = 3  // Incrementar em 1
   versionName = "1.0.2"  // Mudar conforme necessário
   ```

2. **Recompilar:**
   ```bash
   ./gradlew clean assembleDebug
   ```

3. **Instalar no dispositivo:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Verificar no app:**
   - Abra o app e veja o rodapé da tela principal

## 📝 Notas

- **versionCode**: Número inteiro que sempre aumenta (Android usa para updates)
- **versionName**: String legível para humanos (aparece no app)
- **BuildConfig**: Gerado automaticamente pelo Gradle com informações de build
- **Formato recomendado**: `MAJOR.MINOR.PATCH` (ex: 1.0.1)

## ✅ Checklist de Atualização

Antes de considerar que o tablet está com a versão atualizada:

- [ ] Build compilou sem erros
- [ ] APK foi instalado com sucesso (`adb install` retornou "Success")
- [ ] App abre sem crashes
- [ ] Versão mostrada no rodapé da tela corresponde ao esperado
- [ ] Funcionalidades da correção estão funcionando

---

**Versão Atual no Tablet:** `v1.0.1-viewmodel-fix` (versionCode: 2)

**Última Atualização:** 22 de Outubro de 2025

