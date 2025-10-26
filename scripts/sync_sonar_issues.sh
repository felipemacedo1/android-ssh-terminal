#!/usr/bin/env bash
set -euo pipefail

################################################################################
# SonarCloud to GitHub Issues Synchronization Script
# 
# Resilient, colorized, and self-verified integration script
# Fetches issues from SonarCloud API and creates corresponding GitHub Issues
#
# Usage:
#   ./sync_sonar_issues.sh
#
# Required environment variables:
#   SONAR_TOKEN     SonarCloud API token
#   GH_TOKEN        GitHub token (or use gh auth)
#
# Optional environment variables:
#   DRY_RUN         Set to "true" to simulate without creating issues
################################################################################

# Configuration
ORG="felipemacedo1"
PROJECT="felipemacedo1_ktar"
REPO="felipemacedo1/ktar"
SEVERITIES="${SEVERITIES:-BLOCKER,CRITICAL,MAJOR}"
TYPES="${TYPES:-BUG,VULNERABILITY,CODE_SMELL}"
DRY_RUN="${DRY_RUN:-false}"

# Colors
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
RESET="\033[0m"

# Counters
TOTAL_SONAR_ISSUES=0
CREATED_ISSUES=0
SKIPPED_ISSUES=0

echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔄 Iniciando sincronização SonarCloud → GitHub Issues"
echo -e "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"

# 1️⃣ Verifica dependências
for cmd in curl jq gh; do
  if ! command -v $cmd &>/dev/null; then
    echo -e "${RED}❌ Dependência ausente: $cmd${RESET}"
    echo -e "${YELLOW}💡 Para instalar gh CLI:${RESET}"
    echo "  curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg"
    echo "  echo \"deb [arch=\$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main\" | sudo tee /etc/apt/sources.list.d/github-cli.list"
    echo "  sudo apt-get update && sudo apt-get install gh"
    exit 1
  fi
done

# 1.5️⃣ Verifica autenticação do gh
if ! gh auth status &>/dev/null; then
  echo -e "${RED}❌ GitHub CLI não autenticado${RESET}"
  echo -e "${YELLOW}💡 Execute: gh auth login${RESET}"
  echo -e "${YELLOW}💡 Ou defina GH_TOKEN: export GH_TOKEN=seu_token${RESET}"
  exit 1
fi

# 2️⃣ Verifica token do SonarCloud
if [ -z "${SONAR_TOKEN:-}" ]; then
  echo -e "${RED}❌ SONAR_TOKEN não definido${RESET}"
  echo -e "${YELLOW}💡 Exporte a variável: export SONAR_TOKEN=seu_token${RESET}"
  exit 1
fi

# 3️⃣ Busca issues do SonarCloud
echo -e "${YELLOW}📡 Consultando SonarCloud...${RESET}"
response=$(curl -s -u "$SONAR_TOKEN:" \
"https://sonarcloud.io/api/issues/search?componentKeys=${PROJECT}&severities=${SEVERITIES}&types=${TYPES}&resolved=false")

# 4️⃣ Valida JSON
if ! echo "$response" | jq empty >/dev/null 2>&1; then
  echo -e "${RED}❌ Resposta inválida do SonarCloud. Verifique o token ou o project key.${RESET}"
  echo -e "${YELLOW}Resposta recebida:${RESET}"
  echo "$response" | head -20
  exit 1
fi

count=$(echo "$response" | jq '.total')
if [ "$count" -eq 0 ]; then
  echo -e "${YELLOW}⚠️  Nenhuma issue encontrada.${RESET}"
  echo -e "${GREEN}✅ Sincronização concluída (nada a fazer).${RESET}"
  exit 0
fi

echo -e "${GREEN}✅ $count issues encontradas!${RESET}"
TOTAL_SONAR_ISSUES=$count

# 5️⃣ Processa cada issue
echo "$response" | jq -c '.issues[]' | while read -r issue; do
  key=$(echo "$issue" | jq -r '.key')
  severity=$(echo "$issue" | jq -r '.severity')
  message=$(echo "$issue" | jq -r '.message')
  file=$(echo "$issue" | jq -r '.component' | sed "s|$PROJECT:||")
  line=$(echo "$issue" | jq -r '.line // "?"')
  type=$(echo "$issue" | jq -r '.type')
  
  title="[$severity][$type] $message"
  
  # Trunca se muito longo
  if [ ${#title} -gt 200 ]; then
    title="${title:0:197}..."
  fi
  
  body="**Arquivo:** \`$file:$line\`

**SonarCloud Rule:** \`$key\`

🔗 [Ver no SonarCloud](https://sonarcloud.io/project/issues?id=$PROJECT&issues=$key&open=$key)

---
> 🤖 Issue criada automaticamente pelo workflow de sincronização
> 📅 $(date -u +"%Y-%m-%d %H:%M:%S UTC")"

  # 6️⃣ Checa se já existe
  if gh issue list --repo "$REPO" --label "sonarcloud" --state open --json title,number 2>/dev/null | jq -e --arg t "$title" '.[] | select(.title == $t)' >/dev/null 2>&1; then
    echo -e "${YELLOW}⚙️  Já existe: $key${RESET}"
    ((SKIPPED_ISSUES++))
  else
    if [ "$DRY_RUN" = "true" ]; then
      echo -e "${YELLOW}🧪 Simulação: criaria issue para $key${RESET}"
      ((CREATED_ISSUES++))
    else
      echo -e "${GREEN}🆕 Criando issue: $key${RESET}"
      
      # Determina labels
      labels="sonarcloud"
      case "$type" in
        BUG) labels="$labels,bug" ;;
        VULNERABILITY) labels="$labels,security,vulnerability" ;;
        CODE_SMELL) labels="$labels,code-quality" ;;
      esac
      
      case "$severity" in
        BLOCKER|CRITICAL) labels="$labels,priority:high" ;;
        MAJOR) labels="$labels,priority:medium" ;;
        MINOR|INFO) labels="$labels,priority:low" ;;
      esac
      
      if gh issue create \
        --repo "$REPO" \
        --title "$title" \
        --body "$body" \
        --label "$labels" >/dev/null 2>&1; then
        ((CREATED_ISSUES++))
      else
        echo -e "${RED}❌ Falha ao criar issue para $key${RESET}"
      fi
    fi
  fi
done

# 7️⃣ Sumário final
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Resumo da Sincronização"
echo -e "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo ""
echo "  Issues encontradas no SonarCloud:  $TOTAL_SONAR_ISSUES"
echo "  Issues criadas no GitHub:          $CREATED_ISSUES"
echo "  Issues já existentes (puladas):    $SKIPPED_ISSUES"
echo ""

if [ "$DRY_RUN" = "true" ]; then
  echo -e "${YELLOW}⚠️  MODO SIMULAÇÃO - Nenhuma issue foi criada de fato${RESET}"
  echo ""
fi

echo -e "${GREEN}✅ Sincronização concluída com sucesso.${RESET}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"