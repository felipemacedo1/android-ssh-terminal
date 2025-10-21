# Guia de Configuração do Ambiente de Desenvolvimento

Este documento fornece instruções detalhadas para configurar o ambiente de desenvolvimento do Android SSH Terminal.

## 📋 Pré-requisitos

### Software Necessário

1. **Android Studio Hedgehog (2023.1.1) ou superior**
   - Download: https://developer.android.com/studio
   - Instale os componentes recomendados durante o setup

2. **JDK 17**
   - Android Studio geralmente inclui o JDK
   - Verifique: `java -version`
   - Se necessário, baixe em: https://adoptium.net/

3. **Git**
   - Download: https://git-scm.com/downloads
   - Configure seu nome e email:
     ```bash
     git config --global user.name "Seu Nome"
     git config --global user.email "seu@email.com"
     ```

4. **SDK Android**
   - API Level 26 (Android 8.0) - Mínimo
   - API Level 35 (Android 15) - Target
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools

### Hardware Recomendado

- **RAM**: 8 GB mínimo, 16 GB recomendado
- **Armazenamento**: 10 GB de espaço livre
- **Processor**: Intel i5/AMD Ryzen 5 ou superior

## 🛠️ Configuração Inicial

### 1. Clonar o Repositório

```bash
git clone https://github.com/felipemacedo1/android-ssh-terminal.git
cd android-ssh-terminal
```

### 2. Abrir no Android Studio

1. Abra o Android Studio
2. File → Open → Selecione a pasta `android-ssh-terminal`
3. Aguarde a sincronização do Gradle (primeira vez pode demorar)

### 3. Configurar Android SDK

No Android Studio:

1. File → Settings (ou Android Studio → Preferences no Mac)
2. Appearance & Behavior → System Settings → Android SDK
3. Marque as seguintes SDKs:
   - Android 15.0 (API 35) - Target
   - Android 8.0 (API 26) - Minimum
4. Na aba "SDK Tools", marque:
   - Android SDK Build-Tools 34
   - Android SDK Platform-Tools
   - Android Emulator
   - Intel x86 Emulator Accelerator (HAXM) - se Intel
5. Clique em "Apply" e aguarde o download

### 4. Configurar Emulador (Opcional)

1. Tools → Device Manager
2. Create Device
3. Selecione um dispositivo (recomendado: Pixel 6)
4. Selecione uma imagem do sistema (API 35, x86_64)
5. Finalize a configuração

### 5. Configurar Dispositivo Físico (Opcional)

1. No dispositivo Android:
   - Settings → About Phone
   - Toque 7x em "Build Number" para habilitar Developer Options
   - Settings → Developer Options
   - Ative "USB Debugging"

2. Conecte o dispositivo via USB
3. Aceite a permissão de depuração no dispositivo

## 🔧 Verificação da Instalação

### Verificar Gradle

```bash
./gradlew --version
```

Deve exibir Gradle 8.2 ou superior.

### Sincronizar Projeto

No Android Studio:
- File → Sync Project with Gradle Files

Se houver erros, verifique:
- Conexão com a internet
- Configuração do proxy (se aplicável)
- Versões do JDK e SDK

### Build do Projeto

```bash
./gradlew clean build
```

Este comando deve:
1. Baixar todas as dependências
2. Compilar o código
3. Executar testes unitários
4. Gerar o APK debug

## 🏃 Executando o App

### Via Android Studio

1. Certifique-se de que um dispositivo/emulador está conectado
2. Clique no botão "Run" (▶️) ou pressione `Shift + F10`
3. Aguarde a instalação e inicialização

### Via Linha de Comando

```bash
# Instalar no dispositivo conectado
./gradlew installDebug

# Executar
adb shell am start -n com.felipemacedo.androidsshterminal/.MainActivity
```

## 🧪 Executando Testes

### Testes Unitários

```bash
./gradlew test
```

### Testes Instrumentados

```bash
./gradlew connectedAndroidTest
```

### Ver Relatórios de Testes

Os relatórios são gerados em:
- Unitários: `app/build/reports/tests/testDebugUnitTest/index.html`
- Instrumentados: `app/build/reports/androidTests/connected/index.html`

## 📦 Dependências do Projeto

### Principais Bibliotecas

| Biblioteca | Versão | Propósito |
|-----------|--------|-----------|
| Jetpack Compose | 2024.02.00 | UI moderna |
| Kotlin Coroutines | 1.7.3 | Programação assíncrona |
| Navigation Compose | 2.7.7 | Navegação entre telas |
| DataStore | 1.0.0 | Persistência de dados |
| SSHJ | 0.38.0 | Cliente SSH |
| BouncyCastle | 1.77 | Criptografia |

### Instalação de Dependências

As dependências são automaticamente baixadas pelo Gradle na primeira build. Se houver problemas:

```bash
# Limpar cache do Gradle
./gradlew clean

# Forçar download de dependências
./gradlew build --refresh-dependencies
```

## 🐛 Solução de Problemas Comuns

### Erro: "SDK location not found"

**Solução**: Crie o arquivo `local.properties` na raiz do projeto:

```properties
sdk.dir=/caminho/para/Android/Sdk
```

No Windows:
```properties
sdk.dir=C\:\\Users\\SeuUsuario\\AppData\\Local\\Android\\Sdk
```

### Erro: "Unsupported class file major version"

**Solução**: Verifique a versão do JDK:

```bash
java -version
```

Deve ser JDK 17. Configure no Android Studio:
- File → Project Structure → SDK Location → JDK location

### Erro: "Unable to resolve dependency"

**Solução**:
1. Verifique a conexão com internet
2. Limpe o cache do Gradle:
   ```bash
   ./gradlew clean
   rm -rf ~/.gradle/caches/
   ```
3. Sincronize novamente

### Erro: "Execution failed for task ':app:mergeDebugResources'"

**Solução**:
```bash
./gradlew clean
./gradlew --stop
./gradlew build
```

### Emulador lento ou travando

**Solução**:
1. Certifique-se de que a virtualização está habilitada na BIOS
2. Instale HAXM (Intel) ou use emulador ARM64
3. Aumente a RAM do emulador (mínimo 2 GB)
4. Use imagens do sistema sem Google Play (mais leves)

## 🔐 Configuração de Keystore (Release)

Para builds de produção, configure um keystore:

### 1. Gerar Keystore

```bash
keytool -genkey -v -keystore release-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias android-ssh-terminal
```

### 2. Configurar gradle.properties

Crie `gradle.properties` local (não commitado):

```properties
KEYSTORE_FILE=/caminho/completo/para/release-keystore.jks
KEYSTORE_PASSWORD=sua_senha_do_keystore
KEY_ALIAS=android-ssh-terminal
KEY_PASSWORD=sua_senha_da_chave
```

### 3. Build Release

```bash
./gradlew assembleRelease
```

APK gerado em: `app/build/outputs/apk/release/app-release.apk`

## 📱 Testando em Dispositivo Real

### Habilitar Modo Desenvolvedor

1. Configurações → Sobre o telefone
2. Toque 7x em "Número da versão"
3. Configurações → Opções do desenvolvedor
4. Ative "Depuração USB"

### Conectar via USB

```bash
# Listar dispositivos
adb devices

# Instalar app
./gradlew installDebug

# Ver logs em tempo real
adb logcat | grep -i "androidsshterminal"
```

### Conectar via WiFi (sem cabo)

```bash
# 1. Conecte via USB primeiro
adb tcpip 5555

# 2. Descubra o IP do dispositivo (Configurações → Sobre → Status)

# 3. Conecte via WiFi
adb connect <IP_DO_DISPOSITIVO>:5555

# 4. Desconecte o cabo USB
```

## 🔄 Workflow de Desenvolvimento

### 1. Criar Feature Branch

```bash
git checkout -b feature/minha-feature
```

### 2. Desenvolver e Testar

```bash
# Durante o desenvolvimento
./gradlew test

# Antes de commit
./gradlew build
```

### 3. Commit (Conventional Commits)

```bash
git add .
git commit -m "feat: adiciona suporte a SFTP"
```

### 4. Push e Pull Request

```bash
git push origin feature/minha-feature
```

## 📊 Análise de Código

### Lint

```bash
./gradlew lint
```

Relatório em: `app/build/reports/lint-results-debug.html`

### Detekt (Análise estática)

Adicione ao `build.gradle.kts` se desejar:

```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
}
```

## 🚀 Dicas de Produtividade

### Atalhos Úteis do Android Studio

- `Ctrl + Shift + A` - Find Action
- `Shift + Shift` - Search Everywhere
- `Ctrl + Alt + L` - Formatar código
- `Ctrl + Alt + O` - Organizar imports
- `Alt + Enter` - Quick fix
- `Ctrl + B` - Ir para definição

### Live Templates

Crie templates para código repetitivo:
- File → Settings → Editor → Live Templates

### Plugins Recomendados

- **Jetpack Compose Preview** - Preview de componentes
- **Rainbow Brackets** - Colorir chaves/parênteses
- **GitToolBox** - Ferramentas Git extras
- **Key Promoter X** - Aprender atalhos

## 📚 Recursos Adicionais

- [Documentação Android](https://developer.android.com/docs)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [SSHJ GitHub](https://github.com/hierynomus/sshj)
- [Material Design 3](https://m3.material.io/)

## 🆘 Precisa de Ajuda?

- Abra uma [issue no GitHub](https://github.com/felipemacedo1/android-ssh-terminal/issues)
- Consulte as [discussões do projeto](https://github.com/felipemacedo1/android-ssh-terminal/discussions)
- Email: felipe@example.com (substituir pelo email real)

---

**Última atualização**: Outubro 2025
