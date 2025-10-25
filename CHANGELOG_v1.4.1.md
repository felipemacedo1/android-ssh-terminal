# 📋 Changelog - KTAR v1.4.1

**Data de Release**: 2025-10-25  
**Tipo de Release**: Feature + UI Improvements  
**Compatibilidade**: Android 8.0+ (API 26+)

---

## 🎉 Resumo da Versão

A versão **1.4.1** traz **melhorias massivas de UX/UI** que tornam o terminal SSH do KTAR verdadeiramente **profissional e informativo**, expondo todas as funcionalidades implementadas na v1.4.0 de forma clara e intuitiva.

---

## ✨ Novidades

### 🎨 Terminal Status Bar (NOVO)

Barra de status em tempo real mostrando:
- **Modo atual**: Shell Persistente ou Modo Exec
- **Buffer usage**: Linhas atuais/máximo com código de cores
  - 🟢 Verde (< 50%): Tudo bem
  - 🟡 Amarelo (50-80%): Atenção
  - 🔴 Vermelho (> 80%): Limite próximo
- **Polling interval**: Velocidade atual do polling adaptativo (50-500ms)

### 📱 TopBar Contextual

- **Hostname** exibido como subtítulo
- **Ícone verde** de status quando conectado
- **Informação útil** sempre visível

### 📋 Menu Reorganizado

- Mostra **estado atual** ao invés de toggle confuso
- Info clara: "✓ Shell Persistente - PTY sempre ativo"
- Toggle **desabilitado** quando não aplicável
- Organização com **Divider**

### 🚀 Input Melhorado

- **Botão Send** visível ao lado do input
- Cor **adaptativa** (ativo/inativo)
- Habilitado apenas quando há comando
- **Enter** continua funcionando

### 📊 UI State Expandido

Novos campos de estado:
- `hostName`: Nome do host conectado
- `connectionTime`: Timestamp de conexão
- `currentPollInterval`: Intervalo de polling atual
- `bufferUsage`: Número de linhas no buffer

---

## 🔧 Melhorias Técnicas

### TerminalViewModel.kt

- ✅ `setSession()` captura hostname e connectionTime
- ✅ `startOutputPolling()` expõe intervalo no state
- ✅ `addOutputLine()` atualiza bufferUsage
- ✅ `disconnect()` reseta todos os campos

### TerminalScreen.kt

- ✅ Novo componente `TerminalStatusBar`
- ✅ TopAppBar com subtítulo e ícone de status
- ✅ Menu com info clara de modo
- ✅ Input com IconButton de Send

---

## 📈 Comparação com v1.4.0

| Feature | v1.4.0 | v1.4.1 |
|---------|--------|--------|
| **Status do Modo** | ❌ Invisível | ✅ Status bar |
| **Info de Performance** | ❌ Nenhuma | ✅ Polling + Buffer |
| **TopBar** | ⚪ Genérica | ✅ Contextual |
| **Menu PTY** | ⚠️ Confuso | ✅ Claro |
| **Input** | ⚪ Básico | ✅ Com botão |
| **Feedback Visual** | ❌ Mínimo | ✅ Completo |

---

## 🐛 Correções

- Nenhuma - esta é uma release de features puras

---

## 💡 Melhorias de UX

1. **Feature invisível não existe**: shellMode agora óbvio ao usuário
2. **Feedback é essencial**: polling adaptativo visível em tempo real
3. **Contexto importa**: TopBar mostra onde está conectado
4. **Clareza > Complexidade**: Menu sem ambiguidade

---

## 📊 Estatísticas

- **+211 linhas** de código
- **1 componente novo** (TerminalStatusBar)
- **0 bugs** introduzidos
- **100%** retrocompatível

---

## 🔄 Dependências

Nenhuma mudança em dependências.

---

## 📝 Notas de Upgrade

### De v1.4.0 para v1.4.1

✅ **Upgrade automático** - sem breaking changes  
✅ **Sem migração necessária**  
✅ **Totalmente retrocompatível**

---

## 🚀 Próximas Versões

### v1.5.0 (Planejado)
- Teclas especiais (setas, backspace, Ctrl+C)
- Histórico de comandos
- Tab completion

### v1.6.0 (Planejado)
- Parser ANSI (cores, formatação)
- Clear screen suportado

### v2.0.0 (Visão)
- Editores full-screen (vi, vim, nano)
- Múltiplas abas/sessões
- Snippet manager

---

## 👥 Créditos

- **Desenvolvimento**: Equipe KTAR
- **Análise de UX**: Feedback de usuários
- **Testes**: Validação manual completa

---

## 📄 Links

- [Código fonte](https://github.com/felipemacedo1/ktar)
- [Documentação completa](docs/)
- [Relatório de implementação](docs/PTY_FINAL_REPORT.md)

---

**Changelog anterior**: [v1.4.0](CHANGELOG_v1.4.0.md)  
**Próximo release**: v1.5.0 (a definir)

---

_KTAR - Terminal SSH profissional para Android_
