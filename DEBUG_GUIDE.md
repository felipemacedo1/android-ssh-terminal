# 🐛 Guia de Debug - KTAR

## 🔍 Debug no Tablet Conectado

### 1. Capturar Logs em Tempo Real

```bash
# Logs gerais do app (recomendado para começar)
adb logcat | grep -E "androidsshterminal|AndroidRuntime|FATAL"

# Logs apenas de crashes
adb logcat *:E | grep -A 50 "FATAL EXCEPTION"

# Logs específicos do SSH
adb logcat | grep -E "SSHTerminal|SSH_CONNECTION|SSH_COMMAND|SECURITY"

# Logs com timestamps
adb logcat -v time | grep -E "androidsshterminal"
```

### 2. Capturar Crash Report

Se o app crashou, execute:
```bash
# Ver último crash
adb logcat -d | grep -A 100 "FATAL EXCEPTION"

# Salvar crash em arquivo
adb logcat -d | grep -A 100 "FATAL EXCEPTION" > crash_report.txt
```

### 3. Reinstalar App com Debug

```bash
# Desinstalar versão antiga
adb uninstall com.ktar

# Instalar nova versão
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Ou compilar e instalar direto
./gradlew installDebug
```

### 4. Executar App e Ver Logs

```bash
# Iniciar app
adb shell am start -n com.ktar/.MainActivity

# Ver logs em tempo real
adb logcat -v time | grep -i "ssh\|terminal\|error\|exception"
```

---

## 🐞 Bugs Comuns e Soluções

### Bug 1: App Crasha ao Abrir
**Sintoma**: App fecha imediatamente

**Debug**:
```bash
adb logcat *:E | head -50
```

**Possíveis Causas**:
- ViewModel sem Factory
- Context null
- DataStore não inicializado

**Solução**: Ver stack trace completo

---

### Bug 2: Crash ao Adicionar Host
**Sintoma**: App crasha ao clicar no botão `+`

**Possível Causa**: ViewModel não está sendo criado com Factory

**Solução**: Precisamos implementar ViewModelFactory no MainActivity

---

### Bug 3: Crash ao Conectar
**Sintoma**: Erro ao tentar conectar SSH

**Debug**:
```bash
adb logcat | grep -E "SSH|Connection|Auth"
```

**Possíveis Causas**:
- Credenciais inválidas
- Host inacessível
- Timeout de rede
- Problema de criptografia

---

### Bug 4: DataStore Error
**Sintoma**: `Unable to create DataStore`

**Solução**:
```bash
# Limpar dados do app
adb shell pm clear com.ktar
```

---

## 🔧 Ferramentas de Debug

### 1. Logcat em Tempo Real (Filtrado)
```bash
# Terminal 1: Monitor de erros
adb logcat *:E *:W | grep -v "chatty"

# Terminal 2: Monitor do app
adb logcat | grep "felipemacedo"
```

### 2. Ver Processos do App
```bash
# Ver se app está rodando
adb shell ps | grep androidsshterminal

# Matar processo (se travado)
adb shell am force-stop com.ktar
```

### 3. Inspecionar Storage
```bash
# Ver dados persistidos
adb shell run-as com.ktar ls -la /data/data/com.ktar/files/

# Ver DataStore
adb shell run-as com.ktar cat /data/data/com.ktar/files/datastore/ssh_hosts.preferences_pb
```

### 4. Screenshot/Screenrecord
```bash
# Tirar screenshot
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Gravar tela (30s)
adb shell screenrecord /sdcard/demo.mp4 &
# ... usar o app ...
# Ctrl+C para parar
adb pull /sdcard/demo.mp4
```

---

## 🚨 Checklist de Debug

Quando encontrar um bug, colete:

- [ ] **Stack trace completo** (`adb logcat -d > full_log.txt`)
- [ ] **Passos para reproduzir**
- [ ] **Screenshot/Screenrecord**
- [ ] **Versão do Android** (`adb shell getprop ro.build.version.release`)
- [ ] **Modelo do dispositivo** (`adb shell getprop ro.product.model`)
- [ ] **Última ação antes do crash**

---

## 💡 Dicas Rápidas

### Reiniciar Debug Rapidamente
```bash
# Script rápido
adb uninstall com.ktar && \
./gradlew installDebug && \
adb logcat -c && \
adb shell am start -n com.ktar/.MainActivity && \
adb logcat | grep -E "androidsshterminal|FATAL"
```

### Limpar Tudo e Recomeçar
```bash
# Limpar build
./gradlew clean

# Limpar dados do app no dispositivo
adb shell pm clear com.ktar

# Reinstalar
./gradlew installDebug
```

---

## 📱 Info do Dispositivo

```bash
# Modelo
adb shell getprop ro.product.model

# Android Version
adb shell getprop ro.build.version.release

# API Level
adb shell getprop ro.build.version.sdk

# Arquitetura
adb shell getprop ro.product.cpu.abi

# Memória disponível
adb shell dumpsys meminfo com.ktar
```

---

## 🎯 Próximos Passos

1. **Execute o comando de monitoramento**:
   ```bash
   adb logcat -v time | grep -E "androidsshterminal|FATAL|ERROR"
   ```

2. **Abra o app no tablet**

3. **Reproduza o bug**

4. **Copie o stack trace completo**

5. **Me mostre o erro para corrigirmos juntos!**

---

**Pronto para debugar!** 🔍
