# 🧪 Guia de Testes - KTAR

## 📱 Como Testar o Aplicativo

### Opção 1: Emulador Android Studio

1. **Abra o Android Studio**
   ```bash
   # No diretório do projeto
   cd /home/felipe-macedo/projects/ktar
   ```

2. **Inicie um Emulador**
   - Android Studio → Device Manager
   - Crie um dispositivo virtual (recomendado: Pixel 5, API 34)
   - Inicie o emulador

3. **Execute o App**
   ```bash
   ./gradlew installDebug
   ```
   Ou use o botão "Run" no Android Studio

### Opção 2: Dispositivo Físico via ADB

1. **Habilite Depuração USB no Dispositivo**
   - Configurações → Sobre o telefone
   - Toque 7x em "Número da build"
   - Configurações → Opções do desenvolvedor
   - Habilite "Depuração USB"

2. **Conecte o Dispositivo via USB**
   ```bash
   # Verifique se o dispositivo foi detectado
   adb devices
   ```

3. **Instale o APK**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Opção 3: Instalar APK Diretamente

1. **Copie o APK para o Dispositivo**
   ```bash
   adb push app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/
   ```

2. **No Dispositivo**
   - Navegue até Downloads
   - Toque em `app-debug.apk`
   - Habilite "Instalar apps desconhecidas" se solicitado
   - Instale

---

## 🧪 Cenários de Teste

### Teste 1: Adicionar Nova Conexão
**Objetivo**: Verificar se consegue salvar um host SSH

**Passos**:
1. Abra o app
2. Toque no botão `+` (FAB)
3. Preencha os campos:
   - **Nome**: `Servidor de Teste`
   - **Host**: `192.168.1.100` ou `localhost`
   - **Porta**: `22`
   - **Usuário**: `seu_usuario`
   - **Senha**: `sua_senha`
4. Toque em "Salvar Sem Conectar"

**Resultado Esperado**: ✅ Host aparece na lista

---

### Teste 2: Conectar via Senha
**Objetivo**: Testar conexão SSH com autenticação por senha

**Pré-requisitos**: 
- Servidor SSH acessível (pode usar seu PC com OpenSSH)
- Credenciais válidas

**Passos**:
1. Toque no botão `+`
2. Preencha:
   - **Host**: IP do servidor SSH
   - **Porta**: `22`
   - **Usuário**: usuário válido
   - **Método**: Senha
   - **Senha**: senha válida
3. Toque em "Conectar"

**Resultado Esperado**: 
- ✅ Dialog "Conectando..."
- ✅ Navega para tela de terminal
- ✅ Mensagem "Conectado a..."

---

### Teste 3: Executar Comandos no Terminal
**Objetivo**: Verificar execução de comandos SSH

**Pré-requisitos**: Conexão SSH ativa (Teste 2)

**Passos**:
1. No terminal, digite: `ls -la`
2. Pressione Enter (ou botão de enviar)
3. Aguarde resposta
4. Digite: `pwd`
5. Digite: `whoami`
6. Digite: `uname -a`

**Resultado Esperado**:
- ✅ Comandos aparecem no terminal
- ✅ Saída dos comandos é exibida
- ✅ Sem crashes ou freezes

---

### Teste 4: Editar Conexão Salva
**Objetivo**: Verificar edição de hosts

**Passos**:
1. Na lista de hosts, toque no ícone de editar
2. Altere o nome para `Servidor Editado`
3. Altere a porta para `2222`
4. Salve

**Resultado Esperado**:
- ✅ Alterações são salvas
- ✅ Lista atualiza com novo nome

---

### Teste 5: Deletar Conexão
**Objetivo**: Verificar remoção de hosts

**Passos**:
1. Na lista, toque no ícone de deletar
2. Confirme a ação

**Resultado Esperado**:
- ✅ Dialog de confirmação aparece
- ✅ Host é removido da lista

---

### Teste 6: Desconectar Sessão
**Objetivo**: Verificar desconexão limpa

**Passos**:
1. Conecte a um servidor (Teste 2)
2. No terminal, digite: `exit`
3. Pressione Enter

**Resultado Esperado**:
- ✅ Mensagem "Desconectado de..."
- ✅ Terminal indica sessão encerrada

---

### Teste 7: Conectar com Chave Privada
**Objetivo**: Testar autenticação por chave SSH

**Pré-requisitos**: Chave privada SSH válida

**Passos**:
1. Toque no `+`
2. Selecione "Chave Pública"
3. Cole o conteúdo da chave privada (PEM)
4. Preencha host e usuário
5. Conecte

**Resultado Esperado**:
- ✅ Conexão estabelecida
- ✅ Terminal acessível

---

### Teste 8: Validação de Campos
**Objetivo**: Verificar validação de entrada

**Passos**:
1. Tente salvar host com:
   - Campo nome vazio
   - Host inválido (`abc@#$`)
   - Porta inválida (`99999`)
   - Senha vazia (método senha)

**Resultado Esperado**:
- ✅ Mensagens de erro apropriadas
- ✅ Botão salvar desabilitado ou erro exibido

---

### Teste 9: Modo Escuro
**Objetivo**: Verificar tema dark

**Passos**:
1. Nas configurações do dispositivo, habilite modo escuro
2. Abra o app

**Resultado Esperado**:
- ✅ Interface em tema escuro
- ✅ Cores apropriadas (contraste adequado)

---

### Teste 10: Rotação de Tela
**Objetivo**: Testar estado durante rotação

**Passos**:
1. Abra uma conexão
2. Gire o dispositivo (portrait ↔ landscape)
3. Digite comando e gire novamente

**Resultado Esperado**:
- ✅ UI se adapta
- ✅ Estado é preservado
- ✅ Conexão mantida

---

## 🐛 Como Reportar Bugs

### Informações Necessárias:
```
**Descrição**: 
[Descreva o problema]

**Passos para Reproduzir**:
1. [Passo 1]
2. [Passo 2]
3. [Passo 3]

**Resultado Esperado**:
[O que deveria acontecer]

**Resultado Atual**:
[O que aconteceu]

**Ambiente**:
- Dispositivo: [ex: Pixel 5]
- Android: [ex: Android 13]
- Versão do App: [1.0.0]

**Logs** (se disponível):
```
[Cole os logs do logcat]
```

**Screenshots** (se relevante):
[Anexe imagens]
```

### Como Coletar Logs:
```bash
# Logs do app
adb logcat -s SSHTerminal:* SSH_CONNECTION:* SSH_COMMAND:* SECURITY:*

# Limpar logs antigos e monitorar
adb logcat -c
adb logcat | grep -E "SSHTerminal|SSH_|SECURITY"
```

---

## 📊 Checklist de Teste Completo

### Funcionalidades Core
- [ ] Adicionar host
- [ ] Editar host
- [ ] Deletar host
- [ ] Listar hosts salvos
- [ ] Conectar via senha
- [ ] Conectar via chave privada
- [ ] Executar comandos
- [ ] Desconectar sessão
- [ ] Limpar terminal

### UI/UX
- [ ] Navegação entre telas
- [ ] Loading states
- [ ] Error messages
- [ ] Dialogs de confirmação
- [ ] Tema dark/light
- [ ] Rotação de tela
- [ ] Teclado no terminal
- [ ] Scroll no terminal
- [ ] FAB de adicionar

### Segurança
- [ ] Credenciais criptografadas
- [ ] Senhas não visíveis em logs
- [ ] Conexão SSL/TLS
- [ ] Host key verification (se implementado)

### Performance
- [ ] App inicia < 3s
- [ ] Conexão estabelece < 10s
- [ ] Comandos respondem < 1s
- [ ] Sem memory leaks
- [ ] Sem ANR (App Not Responding)

### Compatibilidade
- [ ] Android 8.0 (API 26)
- [ ] Android 9.0 (API 28)
- [ ] Android 10 (API 29)
- [ ] Android 11 (API 30)
- [ ] Android 12 (API 31)
- [ ] Android 13 (API 33)
- [ ] Android 14 (API 34)
- [ ] Android 15 (API 35)

---

## 🚀 Testes Avançados

### Teste de Stress
```bash
# Executar múltiplos comandos rapidamente
for i in {1..100}; do echo "ls -la"; done
```

### Teste de Longa Duração
```bash
# Manter sessão aberta por 1 hora
# Verificar se não há memory leaks ou crashes
```

### Teste de Concorrência
- Abrir múltiplas conexões (se suportado no futuro)
- Executar comandos simultâneos

---

## 📈 Métricas de Qualidade

### Critérios de Aprovação
- ✅ 0 crashes em testes básicos
- ✅ Todos os cenários principais funcionando
- ✅ UI responsiva (< 16ms por frame)
- ✅ Conexões confiáveis (>95% de sucesso)

---

## 🎯 Servidor SSH para Testes

### Opção 1: Usar seu PC Linux
```bash
# Instalar OpenSSH
sudo apt install openssh-server

# Iniciar serviço
sudo systemctl start ssh
sudo systemctl enable ssh

# Verificar status
sudo systemctl status ssh

# Ver IP
ip addr show
```

### Opção 2: Docker Container
```bash
# SSH Server em Docker
docker run -d -p 2222:22 \
  -e PUID=1000 -e PGID=1000 \
  -e USER_NAME=testuser \
  -e USER_PASSWORD=testpass \
  linuxserver/openssh-server
```

### Opção 3: Serviços Online
- **SSH.cloud** (grátis, temporário)
- **AWS EC2** (free tier)
- **DigitalOcean** (trial)

---

## ✅ Resultado dos Testes

Data: ___/___/______

| Teste | Status | Notas |
|-------|--------|-------|
| Teste 1 - Adicionar | ⬜ | |
| Teste 2 - Conectar | ⬜ | |
| Teste 3 - Comandos | ⬜ | |
| Teste 4 - Editar | ⬜ | |
| Teste 5 - Deletar | ⬜ | |
| Teste 6 - Desconectar | ⬜ | |
| Teste 7 - Chave SSH | ⬜ | |
| Teste 8 - Validação | ⬜ | |
| Teste 9 - Modo Escuro | ⬜ | |
| Teste 10 - Rotação | ⬜ | |

**Legenda**: ⬜ Não testado | ✅ Passou | ❌ Falhou | 🔄 Parcial

---

**Pronto para testar!** 🚀
