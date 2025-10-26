#!/usr/bin/env bash
# Sem set -e para evitar exit prematuro, vamos controlar os erros manualmente

################################################################################
# SonarCloud to GitHub Issues Synchronization Script
# 
# Resilient, colorized, and self-verified integration script
# Fetches issues from SonarCloud API and creates corresponding GitHub Issues
# with code snippets and direct links to the problematic code.
#
# Features:
#   ✓ Automatic code snippet extraction from source files
#   ✓ GitHub permalink generation for affected lines
#   ✓ Syntax highlighting based on file extension
#   ✓ Context lines around the issue (±5 lines)
#   ✓ Deduplication of existing issues
#   ✓ Colorized console output
#   ✓ Dry-run mode for testing
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
while IFS= read -r issue; do
  key=$(echo "$issue" | jq -r '.key')
  severity=$(echo "$issue" | jq -r '.severity')
  message=$(echo "$issue" | jq -r '.message')
  file=$(echo "$issue" | jq -r '.component' | sed "s|$PROJECT:||")
  line=$(echo "$issue" | jq -r '.line // "?"')
  type=$(echo "$issue" | jq -r '.type')
  rule=$(echo "$issue" | jq -r '.rule // "unknown"')
  
  # Extrai textRange se disponível
  start_line=$(echo "$issue" | jq -r '.textRange.startLine // .line // 1')
  end_line=$(echo "$issue" | jq -r '.textRange.endLine // .line // 1')
  
  title="[$severity][$type] $message"
  
  # Trunca se muito longo
  if [ ${#title} -gt 200 ]; then
    title="${title:0:197}..."
  fi
  
  # 📝 Extrai snippet do código
  code_snippet=""
  github_link=""
  
  if [ -f "$file" ] && [ "$start_line" != "?" ]; then
    # Determina extensão do arquivo para syntax highlighting
    extension="${file##*.}"
    case "$extension" in
      kt) lang="kotlin" ;;
      java) lang="java" ;;
      xml) lang="xml" ;;
      gradle) lang="gradle" ;;
      kts) lang="kotlin" ;;
      *) lang="" ;;
    esac
    
    # Calcula contexto (5 linhas antes e depois)
    context_before=5
    context_after=5
    snippet_start=$((start_line - context_before))
    snippet_end=$((end_line + context_after))
    
    # Garante limites válidos
    [ $snippet_start -lt 1 ] && snippet_start=1
    
    # Extrai snippet (máximo 20 linhas para não poluir)
    total_lines=$((snippet_end - snippet_start + 1))
    if [ $total_lines -gt 20 ]; then
      snippet_end=$((start_line + 15))
    fi
    
    # Busca o código
    snippet=$(sed -n "${snippet_start},${snippet_end}p" "$file" 2>/dev/null)
    
    if [ -n "$snippet" ]; then
      code_snippet="### 📝 Trecho do Código

\`\`\`${lang}
$snippet
\`\`\`

**Linhas:** $start_line-$end_line (snippet mostra contexto de $snippet_start a $snippet_end)
"
    fi
    
    # Cria link permanente para o GitHub
    github_link="https://github.com/$REPO/blob/main/$file#L${start_line}-L${end_line}"
  fi
  
  # Monta o body da issue
  body="### 🔍 Detalhes da Issue

**Severidade:** \`$severity\`
**Tipo:** \`$type\`
**Regra:** \`$rule\`
**Arquivo:** \`$file\`
**Linha:** \`$line\`

---

$code_snippet

---

### 🔗 Links Úteis

- 📊 [Ver issue no SonarCloud](https://sonarcloud.io/project/issues?id=$PROJECT&issues=$key&open=$key)
- 📄 [Ver código completo no GitHub]($github_link)
- 📖 [Documentação da regra](https://rules.sonarsource.com)

---

### 💬 Mensagem

> $message

---

<sub>🤖 Issue criada automaticamente pelo workflow de sincronização SonarCloud
📅 $(date -u +"%Y-%m-%d %H:%M:%S UTC")</sub>"

  # 6️⃣ Checa se já existe (busca por título, com ou sem label)
  existing=$(gh issue list --repo "$REPO" --state open --json title,number 2>/dev/null | jq --arg t "$title" '.[] | select(.title == $t)' 2>/dev/null || true)
  
  if [ -n "$existing" ]; then
    issue_num=$(echo "$existing" | jq -r '.number')
    echo -e "${YELLOW}⚙️  Já existe: #$issue_num - $key${RESET}"
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
        VULNERABILITY) labels="$labels,security" ;;
        CODE_SMELL) ;; # mantém apenas sonarcloud
      esac
      
      # Cria issue usando arquivo temporário para evitar problemas com caracteres especiais
      body_file=$(mktemp)
      
      # Escreve o body no arquivo
      echo "$body" > "$body_file" || {
        echo "ERROR: Failed to write body to temp file"
        rm -f "$body_file"
        continue
      }
      
      # Cria a issue
      issue_url=$(gh issue create \
        --repo "$REPO" \
        --title "$title" \
        --body-file "$body_file" \
        --label "$labels" 2>&1)
      
      exit_code=$?
      
      # Remove arquivo temporário
      rm -f "$body_file"
      
      if [ $exit_code -eq 0 ]; then
        ((CREATED_ISSUES++))
        echo -e "${GREEN}✓ Criada: $issue_url${RESET}"
      else
        echo -e "${RED}❌ Falha ao criar issue para $key${RESET}"
        echo -e "${YELLOW}Erro: $issue_url${RESET}"
      fi
    fi
  fi
done < <(echo "$response" | jq -c '.issues[]')

# Garante exit 0
true

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