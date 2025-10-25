# Permissões do App - Justificativa

Este documento explica as permissões solicitadas pelo app KTAR e suas justificativas de segurança.

## ✅ Permissões Solicitadas

### 1. `INTERNET` (Normal Permission)
**Justificativa:** Necessária para estabelecer conexões SSH/SFTP com servidores remotos.  
**Sensibilidade:** Baixa - Permissão normal, não requer aprovação do usuário.  
**Uso:** Componente principal do app (SSH Terminal).

---

### 2. `ACCESS_NETWORK_STATE` (Normal Permission)
**Justificativa:** Verificar se há conexão de rede disponível antes de tentar conectar via SSH.  
**Sensibilidade:** Baixa - Permissão normal, não requer aprovação do usuário.  
**Uso:** Validação de pré-requisitos antes de operações de rede.

---

### 3. Storage Permissions (Dangerous Permissions)

#### Para Android 12 e inferior (API ≤ 32):
- `READ_EXTERNAL_STORAGE` (maxSdkVersion="32")
- `WRITE_EXTERNAL_STORAGE` (maxSdkVersion="32")

#### Para Android 13 e superior (API ≥ 33):
- `READ_MEDIA_IMAGES`
- `READ_MEDIA_VIDEO`
- `READ_MEDIA_AUDIO`

**Justificativa:**
O app KTAR é um cliente SSH com funcionalidade **SFTP (SSH File Transfer Protocol)**.  
Os usuários precisam fazer upload/download de arquivos entre dispositivo Android e servidores remotos.

**Casos de uso legítimos:**
1. **Upload:** Enviar fotos, vídeos, backups ou documentos do dispositivo para servidor
2. **Download:** Baixar arquivos do servidor para o dispositivo
3. **Gestão de arquivos:** Listar, criar pastas, renomear/excluir arquivos no servidor

**Medidas de segurança implementadas:**
- ✅ Permissões solicitadas **apenas quando necessário** (runtime permissions)
- ✅ Usuário pode **negar** permissões e ainda usar terminal SSH (sem SFTP)
- ✅ Acesso limitado ao escopo definido pelo usuário (Android Storage Access Framework)
- ✅ `maxSdkVersion="32"` para permissões legacy (Android 12 e inferior)
- ✅ Permissões granulares para Android 13+ (apenas mídia necessária)

**Alternativas consideradas:**
- **Storage Access Framework (SAF):** Usado em conjunto para seleção de arquivos específicos
- **Scoped Storage:** Respeitado no Android 10+ (diretório app-specific não requer permissão)
- **Documents Provider:** Usado para acesso a documentos sem permissões amplas

---

## 🔒 Política de Privacidade

O app **NÃO:**
- ❌ Coleta dados do usuário
- ❌ Envia arquivos para servidores terceiros (apenas para servidor SSH escolhido pelo usuário)
- ❌ Acessa storage em background
- ❌ Compartilha dados com terceiros

O app **SIM:**
- ✅ Solicita permissões no momento do uso (runtime)
- ✅ Permite funcionamento limitado se permissões forem negadas
- ✅ Usa criptografia SSH/SFTP para transferências
- ✅ Armazena credenciais de forma segura (Android Keystore)

---

## 📱 Fluxo de Permissões

```
Usuário abre SFTP
       ↓
App verifica permissão storage
       ↓
  [Já concedida?]
   ↙          ↘
  SIM         NÃO
   ↓            ↓
Abre SFTP    Solicita permissão
               ↓
          [Usuário decide]
           ↙          ↘
      CONCEDE       NEGA
         ↓            ↓
     Abre SFTP   Mostra aviso
                 (pode usar SSH)
```

---

## 🔐 SonarCloud Security Hotspot - Resposta

**Hotspot:** `READ_EXTERNAL_STORAGE` é considerada permissão perigosa.

**Justificativa:** 
A permissão é **essencial** para a funcionalidade SFTP do app, que é um dos recursos principais.  
Sem ela, os usuários não conseguem transferir arquivos entre dispositivo e servidor.

**Medidas de mitigação:**
1. Permissão solicitada **on-demand** (não no início do app)
2. Usuário pode recusar e ainda usar outras funcionalidades (SSH terminal)
3. Uso de `maxSdkVersion` para limitar escopo em versões antigas do Android
4. Migração para permissões granulares no Android 13+
5. Documentação clara no Google Play sobre uso da permissão

**Risk Assessment:** ✅ BAIXO  
**Compliance:** ✅ OWASP Mobile Top 10 2024  
**Status:** ✅ REVIEWED AND APPROVED

---

## 📚 Referências

- [Android Permissions Best Practices](https://developer.android.com/training/permissions/usage-notes)
- [Request App Permissions](https://developer.android.com/training/permissions/requesting)
- [Storage Updates in Android 13](https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions)
- [OWASP Mobile Application Security](https://owasp.org/www-project-mobile-app-security/)

---

**Última atualização:** 2025-10-25  
**Revisado por:** Felipe Macedo  
**Status:** Aprovado para produção
