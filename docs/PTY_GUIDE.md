# 📟 PTY (Pseudo-Terminal) Guide - KTAR v1.3.0

## 🎯 Visão Geral

O KTAR v1.3.0 introduz **suporte opcional a PTY (Pseudo-Terminal)**, permitindo a execução de comandos interativos que antes não funcionavam corretamente.

## ✨ O que é PTY?

**PTY (Pseudo-Terminal)** é uma interface que emula um terminal real, permitindo:
- Comandos interativos (`vi`, `vim`, `nano`, `top`, `htop`)
- Controle de TTY (Terminal Teletype)
- Suporte a escape sequences e caracteres especiais
- Aplicações que esperam input interativo

### 🔄 Modos de Operação

#### 1. **Modo Padrão (PTY Desabilitado)**
- Execução não interativa (`session.exec()`)
- Ideal para comandos simples: `ls`, `pwd`, `cat`, `grep`
- Mais rápido e com menos overhead
- **Padrão atual** (retrocompatibilidade)

#### 2. **Modo Interativo (PTY Habilitado)**  
- Execução com pseudo-terminal (`session.allocateDefaultPTY()`)
- Necessário para: `vi`, `vim`, `nano`, `top`, `htop`, `less`, `more`
- Emula terminal real
- **Opcional** (ativado pelo usuário)

---

## 🚀 Como Usar

### Ativar Modo PTY

1. Conecte a um servidor SSH
2. No terminal, toque no menu (⋮) no canto superior direito
3. Selecione **"Modo Interativo (PTY)"**
4. Um ✓ aparecerá ao lado quando ativado
5. Execute comandos interativos normalmente

### Desativar Modo PTY

1. Abra o menu (⋮) novamente
2. Toque em **"Modo Interativo (PTY)"** para desativar
3. O ✓ desaparecerá
4. Modo padrão restaurado

---

## 🧩 Comandos Suportados

### ✅ Funcionam **SEM** PTY (Modo Padrão)
```bash
ls -la
pwd
cat arquivo.txt
grep "texto" arquivo.txt
echo "Hello World"
whoami
uname -a
df -h
ps aux
curl https://example.com
```

### ⚙️ Requerem PTY (Modo Interativo)
```bash
vi arquivo.txt        # Editor Vi
vim arquivo.txt       # Editor Vim
nano arquivo.txt      # Editor Nano
top                   # Monitor de processos
htop                  # Monitor interativo
less arquivo.txt      # Paginador
more arquivo.txt      # Paginador simples
man comando           # Manual pages
ssh outro-servidor    # SSH aninhado
tmux                  # Terminal multiplexer
screen                # Terminal multiplexer
```

---

## 🔍 Detecção Automática

O KTAR detecta automaticamente se um comando requer PTY e **avisa o usuário**:

```
$ vi arquivo.txt
⚠️ O comando 'vi' pode requerer modo interativo (PTY)
   Ative o modo interativo no menu se o comando não funcionar corretamente
```

### Lista de Comandos Detectados
- Editores: `vi`, `vim`, `nano`, `emacs`
- Monitores: `top`, `htop`
- Paginadores: `less`, `more`, `man`
- Multiplexers: `screen`, `tmux`
- Outros: `ssh`, `telnet`

---

## 🐛 Debug e Logs

### Tags de Log

O PTY usa tags específicas para facilitar o debug:

```bash
# Ver execuções com PTY
adb logcat | grep "SSH_PTY"

# Ver execuções padrão
adb logcat | grep "SSH_EXEC"

# Combinado
adb logcat | grep -E "SSH_PTY|SSH_EXEC"
```

### Exemplos de Logs

```
D/SSH_PTY: PTY allocated for command: vi teste.txt
D/SSH_EXEC: Executing command: ls -la
```

---

## ⚠️ Limitações Atuais (v1.3.0)

### 🚧 Comportamento Conhecido

1. **Input/Output Não Simultâneo**
   - PTY v1.3 ainda usa `exec()` com PTY alocado
   - Não há stream contínuo de I/O
   - Comandos devem terminar para mostrar output

2. **Sem Suporte a Teclas Especiais**
   - Setas, Backspace, Ctrl+C não funcionam ainda
   - Apenas input de texto simples

3. **Sem Buffer Persistente**
   - Output não é capturado em tempo real
   - Comandos longos (como `top`) podem não funcionar perfeitamente

### 💡 Workarounds

**Para comandos que não terminam (`top`, `tail -f`):**
- Use versões com timeout ou limite:
  ```bash
  # Ao invés de: top
  top -b -n 1  # Batch mode, uma iteração
  
  # Ao invés de: tail -f arquivo.log
  tail -n 50 arquivo.log  # Últimas 50 linhas
  ```

---

## 🚀 Roadmap - PTY Completo (v2.0)

### 🎯 Visão de Médio Prazo

O **objetivo final** é transformar o KTAR em um **terminal SSH real**, onde:

- ✅ **PTY sempre habilitado** (não opcional)
- ✅ **Stream I/O contínuo** (`Flow<String>`)
- ✅ **Suporte a teclas especiais** (setas, Ctrl+C, Esc, etc.)
- ✅ **Buffer persistente** com histórico
- ✅ **Scroll infinito** com gestão de memória
- ✅ **Comandos interativos funcionam 100%**
- ✅ **Terminal ANSI completo** (cores, escape sequences)

### 📅 Evolução Planejada

```
v1.3.0 → PTY opcional (atual)
        ↓
v1.4.0 → PTY com stream I/O inicial
        ↓
v1.5.0 → Suporte a teclas especiais
        ↓
v2.0.0 → Terminal SSH real (PTY padrão)
```

---

## 🧪 Testes

### Teste Básico PTY

1. **Sem PTY (deve falhar ou dar output estranho):**
   ```bash
   # Desative PTY
   $ vi teste.txt
   # Output confuso ou erro
   ```

2. **Com PTY (deve funcionar):**
   ```bash
   # Ative PTY
   $ vi teste.txt
   # (vi pode não funcionar 100% ainda, mas PTY está alocado)
   ```

### Teste de Detecção

```bash
# O app deve avisar automaticamente
$ top
⚠️ O comando 'top' pode requerer modo interativo (PTY)
```

---

## 📊 Comparação de Modos

| Feature                  | Modo Padrão | Modo PTY |
|--------------------------|-------------|----------|
| Comandos simples         | ✅          | ✅       |
| Velocidade               | ⚡ Rápida   | 🐢 Normal|
| Editores (`vi`, `nano`)  | ❌          | ✅       |
| Monitores (`top`)        | ❌          | ⚙️ Parcial|
| SSH aninhado             | ❌          | ✅       |
| Overhead                 | Baixo       | Médio    |
| Ideal para               | Scripts     | Interação|

---

## 🔐 Segurança

- PTY **não expõe** dados sensíveis
- Logs **não contêm** senhas ou chaves
- Mesma segurança do modo padrão
- Autenticação **não é afetada**

---

## 💡 Dicas de Uso

### ✅ Quando usar PTY

- Precisa editar arquivos remotos (`vi`, `nano`)
- Quer monitorar processos em tempo real
- Vai usar comandos que esperam input
- Precisa de SSH aninhado

### ❌ Quando NÃO usar PTY

- Executando scripts simples
- Comandos rápidos (`ls`, `pwd`, `cat`)
- Performance é crítica
- Não há necessidade de interação

---

## 📞 Troubleshooting

### Problema: Comando interativo não funciona

**Solução**: Ative o modo PTY no menu

### Problema: Output aparece truncado

**Causa**: Limitação atual do PTY v1.3  
**Solução**: Use comandos com output finito (evite loops infinitos)

### Problema: Teclas especiais não funcionam

**Causa**: Feature planejada para v2.0  
**Solução**: Use apenas input de texto por enquanto

---

## 🎓 Referências Técnicas

- [RFC 4254 - SSH Protocol](https://www.rfc-editor.org/rfc/rfc4254.html)
- [PTY Wikipedia](https://en.wikipedia.org/wiki/Pseudoterminal)
- [SSHJ Documentation](https://github.com/hierynomus/sshj)
- [SSH Channel Types](https://www.ssh.com/academy/ssh/channel)

---

**Versão**: 1.3.0  
**Status**: PTY Opcional (Transição)  
**Próximo**: PTY Padrão (Terminal Real v2.0)  
**Data**: 2024-10-24
