# Quick Start: SonarCloud → GitHub Issues Sync

## 🚀 Setup em 3 Passos

### 1. Configure o Token do SonarCloud

1. Acesse: https://sonarcloud.io/account/security
2. Crie um novo token: `GitHub Issues Sync`
3. Copie o token gerado

### 2. Adicione o Token nos Secrets do GitHub

1. Vá para: https://github.com/felipemacedo1/ktar/settings/secrets/actions
2. Clique em **New repository secret**
3. Adicione:
   - **Name:** `SONAR_TOKEN`
   - **Value:** `<seu-token-copiado>`
4. Clique em **Add secret**

**Opcional:** Se sua organização/projeto tiver nomes diferentes:
- **SONAR_ORG:** `felipemacedo1`
- **SONAR_PROJECT:** `felipemacedo1_ktar`

### 3. Execute o Setup Inicial

```bash
# Cria labels e valida configuração
./scripts/setup_sonar_sync.sh
```

## ✅ Pronto!

A sincronização já está configurada e rodará automaticamente toda **segunda-feira às 12:00 UTC**.

## 🧪 Testar Agora

### Opção 1: Via GitHub Actions (Recomendado)

```bash
# Trigger manual do workflow
gh workflow run sonar-sync.yml

# Ver status
gh run list --workflow=sonar-sync.yml
```

### Opção 2: Localmente (Dry Run)

```bash
export SONAR_TOKEN="seu-token"
./scripts/sync_sonar_issues.sh --dry-run
```

## 📋 Ver Issues Criadas

Após a execução, veja as issues em:

🔗 https://github.com/felipemacedo1/ktar/issues?q=is:issue+is:open+label:sonarcloud

## 🎛️ Personalizar Filtros

Edite `.github/workflows/sonar-sync.yml` para mudar os padrões:

```yaml
env:
  SEVERITIES: 'BLOCKER,CRITICAL'        # Apenas críticas
  TYPES: 'BUG,VULNERABILITY'            # Apenas bugs e vulnerabilidades
```

Ou execute manualmente com filtros:

```bash
./scripts/sync_sonar_issues.sh \
  --severities=BLOCKER,CRITICAL \
  --types=BUG,VULNERABILITY
```

## 📚 Documentação Completa

Para mais detalhes, consulte: [docs/SONARCLOUD_SYNC.md](./docs/SONARCLOUD_SYNC.md)

## ❓ Problemas Comuns

### "Missing required dependencies"
```bash
sudo apt-get install -y curl jq
# Instale GitHub CLI: https://cli.github.com/
```

### "Invalid JSON response from SonarCloud"
- Verifique se o `SONAR_TOKEN` está correto
- Confirme que `SONAR_ORG` e `SONAR_PROJECT` estão corretos

### "Failed to create issue"
```bash
# Autentique o GitHub CLI
gh auth login

# Ou defina o token
export GH_TOKEN="ghp_..."
```

## 🆘 Suporte

Encontrou um problema? Abra uma issue: https://github.com/felipemacedo1/ktar/issues/new
