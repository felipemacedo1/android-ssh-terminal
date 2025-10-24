# 📂 SFTP Guide - KTAR v1.2.0

## 🎯 Visão Geral

O KTAR v1.2.0 inclui suporte completo a SFTP (SSH File Transfer Protocol), permitindo upload e download de arquivos através da mesma sessão SSH usada no terminal.

## 🚀 Recursos

- ✅ **Listar arquivos e diretórios remotos**
- ✅ **Upload de arquivos locais para o servidor**
- ✅ **Download de arquivos do servidor**
- ✅ **Navegação em diretórios**
- ✅ **Indicador de progresso em tempo real**
- ✅ **Reutilização da sessão SSH ativa**
- ✅ **Informações detalhadas (permissões, tamanho, data)**

## 📋 Pré-requisitos

### Permissões Android
- **Android 13+**: Usa Storage Access Framework (SAF) - sem permissões adicionais
- **Android ≤ 12**: Solicita `WRITE_EXTERNAL_STORAGE` automaticamente ao fazer download

### Servidor SSH
- Servidor SSH com suporte a SFTP habilitado
- Credenciais válidas (senha ou chave privada)
- Permissões adequadas nos diretórios remotos

## 🎮 Como Usar

### 1. Acessar o SFTP Manager

1. Conecte-se a um host SSH através da tela de conexão
2. No terminal ativo, toque no menu (⋮) no canto superior direito
3. Selecione **"SFTP Manager"**

### 2. Navegar pelos Arquivos

- **Abrir diretório**: Toque em uma pasta
- **Voltar**: Use o botão ⬆️ no topo
- **Atualizar**: Botão 🔄 no topo
- **Caminho atual**: Exibido abaixo do título

### 3. Upload de Arquivo

1. Toque no botão flutuante verde (📤) no canto inferior direito
2. Selecione o arquivo desejado do dispositivo
3. O arquivo será enviado para o diretório atual
4. Progresso exibido em tempo real
5. Mensagem de sucesso/erro ao finalizar

### 4. Download de Arquivo

1. Toque no arquivo que deseja baixar
2. Confirme o download no diálogo
3. Arquivo será salvo em `/Downloads`
4. Progresso exibido em tempo real
5. Mensagem de sucesso com nome do arquivo

## 🧩 Arquitetura

### Estrutura de Pacotes
```
app/src/main/java/com/ktar/
├── core/sftp/
│   ├── SFTPFile.kt           # Modelo de arquivo/diretório
│   ├── SFTPResult.kt          # Resultado de operações
│   ├── SFTPProgressListener.kt # Callback de progresso
│   └── SFTPManager.kt         # Lógica principal SFTP
└── ui/screens/sftp/
    ├── SFTPViewModel.kt       # ViewModel MVVM
    └── SFTPScreen.kt          # Interface Compose
```

### Fluxo de Dados
```
SFTPScreen → SFTPViewModel → SFTPManager → SSHJ Library → SSH Server
```

## 🐛 Debug e Logs

### Tags de Log

Filtre os logs SFTP usando:

```bash
# Todos os logs SFTP
adb logcat | grep -E "SFTP"

# Por operação
adb logcat | grep "SFTP_UPLOAD"
adb logcat | grep "SFTP_DOWNLOAD"
adb logcat | grep "SFTP_LIST"

# Combinado
adb logcat | grep -E "SFTP_UPLOAD|SFTP_DOWNLOAD|SFTP_LIST"
```

### Comandos de Debug

```bash
# Verificar permissões de arquivo
adb logcat | grep "SFTP" | grep -i "permission"

# Ver progresso de transferência
adb logcat | grep "Transfer"

# Erros apenas
adb logcat *:E | grep "SFTP"
```

## ❌ Possíveis Erros e Soluções

### 1. "SFTP client not connected"
**Causa**: Sessão SSH não está ativa  
**Solução**: Reconecte ao servidor e tente novamente

### 2. "Remote file not found"
**Causa**: Arquivo foi movido/deletado ou caminho incorreto  
**Solução**: Atualize a listagem (🔄) e verifique o caminho

### 3. "Failed to create local directory"
**Causa**: Sem permissão de escrita ou espaço insuficiente  
**Solução**: 
- Verifique permissões (Android ≤ 12)
- Libere espaço no dispositivo

### 4. "Cannot read local file"
**Causa**: Arquivo não existe ou sem permissão  
**Solução**: Verifique se o arquivo existe e está acessível

### 5. "Permission denied" (servidor)
**Causa**: Usuário SSH sem permissão no diretório remoto  
**Solução**: Verifique permissões no servidor:
```bash
ls -la /caminho/do/diretorio
chmod 755 /caminho/do/diretorio  # Se necessário
```

## 🔒 Segurança

### Boas Práticas

1. **Dados não são logados**: Senhas e conteúdo de arquivos nunca aparecem no logcat
2. **Mesma sessão SSH**: Reutiliza autenticação existente
3. **Arquivos temporários**: Limpos automaticamente após upload
4. **Permissões mínimas**: Solicita apenas o necessário

### Limitações

- Não suporta edição de arquivos remotos (somente download/upload)
- Upload de múltiplos arquivos não é simultâneo
- Tamanho máximo de arquivo limitado pela memória disponível

## 📊 Performance

### Otimizações
- Buffer de 32KB para transferências
- Operações assíncronas (não bloqueia UI)
- Listagem ordenada (diretórios primeiro)
- Cache de arquivos temporários

### Limites Recomendados
- **Upload/Download**: Até 500 MB por arquivo
- **Listagem**: Até 1000 arquivos por diretório

## 🧪 Testes

### Teste Manual

1. **Teste de Upload**:
   - Selecione arquivo pequeno (< 1MB)
   - Verifique progresso
   - Confirme no servidor: `ls -lh`

2. **Teste de Download**:
   - Baixe arquivo conhecido
   - Verifique em `/storage/emulated/0/Download`

3. **Teste de Navegação**:
   - Entre em vários diretórios
   - Use "voltar" múltiplas vezes
   - Atualize listagem

### Teste de Permissões

```bash
# Android ≤ 12
adb shell pm list permissions | grep STORAGE

# Verificar se foi concedida
adb shell dumpsys package com.ktar | grep STORAGE
```

## 🔧 Troubleshooting Avançado

### Verificar conexão SFTP
```bash
adb logcat | grep "SFTP client connected"
```

### Monitorar transferências
```bash
# Em tempo real
adb logcat -s SFTP_UPLOAD SFTP_DOWNLOAD

# Com timestamp
adb logcat -v time | grep SFTP
```

### Limpar cache de arquivos temporários
Os arquivos temporários são criados em:
- Cache: `/data/data/com.ktar/cache/upload_*`
- Download: `/storage/emulated/0/Download/`

## 📞 Suporte

Para problemas não listados aqui:

1. Verifique logs com as tags apropriadas
2. Confirme que SSH funciona via terminal
3. Teste com comandos SFTP nativos no servidor
4. Abra uma issue no repositório com:
   - Versão do Android
   - Logs relevantes
   - Passos para reproduzir

## 🎓 Referências

- [SSHJ Library](https://github.com/hierynomus/sshj)
- [SSH File Transfer Protocol (RFC 4251)](https://www.ietf.org/rfc/rfc4251.txt)
- [Android Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider)

---

**Versão**: 1.2.0  
**Data**: 2025-01-23  
**Autor**: KTAR Team
